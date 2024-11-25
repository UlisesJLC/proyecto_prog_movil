package com.example.inventory.ui.task

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.TasksRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.inventory.data.Task
import com.example.inventory.worker.NotificationWorker
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * ViewModel to retrieve and update a task from the [TasksRepository]'s data source.
 */
class TaskEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val tasksRepository: TasksRepository
) : ViewModel() {

    /**
     * Holds current task UI state
     */
    var taskUiState by mutableStateOf(TaskUiState())
        private set

    private val taskId: Int = checkNotNull(savedStateHandle[TaskEditDestination.taskIdArg])

    init {
        viewModelScope.launch {
            taskUiState = tasksRepository.getTaskStream(taskId)
                .filterNotNull()
                .first()
                .toTaskUiState(true)
        }
    }

    /**
     * Update the task in the [TasksRepository]'s data source
     */
    suspend fun updateTask(context: Context) {
        if (validateInput(taskUiState.taskDetails)) {
            val updatedTask = taskUiState.taskDetails.toTask()

            // Cancelar alarmas existentes para esta tarea
            cancelAlarmsForTask(context, taskId)

            // Actualizar la tarea en la base de datos
            tasksRepository.updateTask(updatedTask)

            // Programar nuevas alarmas
            scheduleAlarmsForTask(context, updatedTask)
        }
    }

    /**
     * Cancela todas las alarmas asociadas a esta tarea.
     */
    private fun cancelAlarmsForTask(context: Context, taskId: Int) {
        val workManager = WorkManager.getInstance(context)
        workManager.getWorkInfosByTag("task_$taskId").get().forEach { workInfo ->
            Log.d("TaskEditViewModel", "Cancelando alarma: ${workInfo.id}")
            workManager.cancelWorkById(workInfo.id)
        }
    }


    /**
     * Programa nuevas alarmas para esta tarea.
     */
    private fun scheduleAlarmsForTask(context: Context, task: Task) {
        val workManager = WorkManager.getInstance(context)
        val alarmTimes = calculateAlarmTimes(task.fechaHoraVencimiento)

        alarmTimes.forEach { delay ->
            Log.d("TaskEditViewModel", "Programando alarma para task_${task.id} en $delay ms")
            val notificationWork = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(
                    Data.Builder()
                        .putString("task_title", task.titulo)
                        .putString("task_description", task.descripcion)
                        .build()
                )
                .addTag("task_${task.id}") // Etiqueta única para esta tarea
                .build()

            workManager.enqueue(notificationWork)
        }
    }

    private fun calculateAlarmTimes(dueDateTime: String?): List<Long> {
        if (dueDateTime.isNullOrBlank()) {
            Log.e("TaskEditViewModel", "fechaHoraVencimiento está vacío o nulo")
            return emptyList()
        }

        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val alarmTimes = mutableListOf<Long>()
        try {
            val taskTime = formatter.parse(dueDateTime)?.time ?: return emptyList()
            val currentTime = System.currentTimeMillis()
            if (taskTime > currentTime) {
                alarmTimes.add(taskTime - currentTime)
            } else {
                Log.e("TaskEditViewModel", "La fecha de vencimiento está en el pasado")
            }
        } catch (e: Exception) {
            Log.e("TaskEditViewModel", "Error al calcular tiempos de alarma: ${e.message}")
        }
        return alarmTimes
    }


    /**
     * Updates the [taskUiState] with the value provided in the argument. This method also triggers
     * a validation for input values.
     */
    fun updateUiState(taskDetails: TaskDetails) {
        taskUiState = TaskUiState(
            taskDetails = taskDetails,
            isEntryValid = validateInput(taskDetails)
        )
    }

    private fun validateInput(uiState: TaskDetails = taskUiState.taskDetails): Boolean {
        return with(uiState) {
            titulo.isNotBlank() && descripcion.isNotBlank() &&
                    (fechaHoraVencimiento != null)
        }
    }
}
