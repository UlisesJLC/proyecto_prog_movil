package com.example.inventory.ui.task

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import com.example.inventory.data.Task
import com.example.inventory.data.TasksRepository
import androidx.work.WorkManager
import com.example.inventory.worker.NotificationWorker
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class TaskEntryViewModel(private val tasksRepository: TasksRepository) : ViewModel() {

    var taskUiState by mutableStateOf(TaskUiState())
        private set

    fun updateUiState(taskDetails: TaskDetails) {
        taskUiState = TaskUiState(
            taskDetails = taskDetails,
            isEntryValid = validateInput(taskDetails)
        )
    }

    suspend fun saveTask(context: Context) {
        if (validateInput()) {
            val task = taskUiState.taskDetails.toTask()
            // Si es una actualización, cancelar alarmas previas
            if (task.id != 0) {
                cancelAlarmsForTask(context, task.id)
            }
            // Insertar/Actualizar la tarea en la base de datos
            tasksRepository.insertTask(task)
            // Programar nuevas alarmas
            scheduleAlarmsForTask(context, task)
        }
    }

    private fun validateInput(uiState: TaskDetails = taskUiState.taskDetails): Boolean {
        return with(uiState) {
            titulo.isNotBlank() && descripcion.isNotBlank()
        }
    }

    private fun cancelAlarmsForTask(context: Context, taskId: Int) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag("task_$taskId")
    }

    private fun scheduleAlarmsForTask(context: Context, task: Task) {
        val workManager = WorkManager.getInstance(context)
        val alarmTimes = calculateAlarmTimes(task.fechaHoraVencimiento)

        alarmTimes.forEach { delay ->
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
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val alarmTimes = mutableListOf<Long>()
        try {
            val taskTime = formatter.parse(dueDateTime ?: return emptyList())?.time ?: return emptyList()
            val currentTime = System.currentTimeMillis()
            if (taskTime - currentTime > 5 * 60 * 1000) alarmTimes.add(taskTime - currentTime - 5 * 60 * 1000) // 5 min antes
            if (taskTime - currentTime > 30 * 60 * 1000) alarmTimes.add(taskTime - currentTime - 30 * 60 * 1000) // 30 min antes
            if (taskTime - currentTime > 60 * 60 * 1000) alarmTimes.add(taskTime - currentTime - 60 * 60 * 1000) // 1 hora antes
            if (taskTime - currentTime > 12 * 60 * 60 * 1000) alarmTimes.add(taskTime - currentTime - 12 * 60 * 60 * 1000) // 12 horas antes
            if (taskTime - currentTime > 24 * 60 * 60 * 1000) alarmTimes.add(taskTime - currentTime - 24 * 60 * 60 * 1000) // 1 día antes
        } catch (e: Exception) {
            Log.e("TaskEntryViewModel", "Error calculating alarm times: $e")
        }
        return alarmTimes
    }
}


data class TaskUiState(
    val taskDetails: TaskDetails = TaskDetails(),
    val isEntryValid: Boolean = false
)

data class TaskDetails(
    val id: Int = 0,
    val titulo: String = "",
    val descripcion: String = "",
    val fechaHoraVencimiento: String? = null,
    val estado: Boolean = false
)

fun TaskDetails.toTask(): Task = Task(
    id = id,
    titulo = titulo,
    descripcion = descripcion,
    fechaHoraVencimiento = fechaHoraVencimiento ?: "", // Valor predeterminado si es nulo
    estado = estado
)


fun Task.toTaskUiState(isEntryValid: Boolean = false): TaskUiState = TaskUiState(
    taskDetails = this.toTaskDetails(),
    isEntryValid = isEntryValid
)

fun Task.toTaskDetails(): TaskDetails = TaskDetails(
    id = id,
    titulo = titulo,
    descripcion = descripcion,
    fechaHoraVencimiento = fechaHoraVencimiento,
    estado = estado
)
