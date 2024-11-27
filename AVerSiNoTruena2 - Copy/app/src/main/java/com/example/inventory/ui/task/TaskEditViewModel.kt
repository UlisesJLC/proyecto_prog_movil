package com.example.inventory.ui.task

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.example.inventory.data.Alarm
import com.example.inventory.data.AlarmRepository
import com.example.inventory.data.OfflineAlarmsRepository
import com.example.inventory.data.TasksRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
//import kotlin.coroutines.jvm.internal.CompletedContinuation.context

/**
 * ViewModel to retrieve and update a task from the [TasksRepository]'s data source.
 */
class TaskEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val tasksRepository: TasksRepository,
    private val alarmsRepository: AlarmRepository,
    private val workManager: WorkManager // Agregar WorkManager aquí
) : ViewModel() {

    var taskUiState by mutableStateOf(TaskUiState())
        private set

    var alarmsUiState by mutableStateOf<List<Alarm>>(emptyList())
        private set

    private val taskId: Int = checkNotNull(savedStateHandle[TaskEditDestination.taskIdArg])

    init {
        viewModelScope.launch {
            // Cargar datos de la tarea
            taskUiState = tasksRepository.getTaskStream(taskId)
                .filterNotNull()
                .first()
                .toTaskUiState()

            // Cargar alarmas asociadas a la tarea
            alarmsRepository.getAlarmsByTaskId(taskId).collect { alarms ->
                Log.d("TaskEditViewModel", "Loaded alarms for taskId $taskId: $alarms")
                alarmsUiState = alarms
            }
        }
    }

    suspend fun updateTask() {
        if (validateInput(taskUiState.taskDetails)) {
            tasksRepository.updateTask(taskUiState.taskDetails.toTask())
        }
    }

    fun updateUiState(taskDetails: TaskDetails) {
        taskUiState = TaskUiState(
            taskDetails = taskDetails,
            isEntryValid = validateInput(taskDetails)
        )
    }

    fun addAlarm(alarm: Alarm) {
        viewModelScope.launch {
            alarmsRepository.insertAlarm(alarm)
        }
    }

    fun updateAlarm(alarm: Alarm) {
        viewModelScope.launch {
            alarmsRepository.updateAlarm(alarm)
        }
    }

    fun deleteAlarm(context: Context, alarm: Alarm) {
        viewModelScope.launch {
            try {
                if (alarm.workManagerId.isNotBlank()) {
                    // Usa el contexto proporcionado para obtener el WorkManager
                    val workManager = WorkManager.getInstance(context)
                    workManager.cancelWorkById(UUID.fromString(alarm.workManagerId))
                }
                alarmsRepository.deleteAlarm(alarm)
            } catch (e: IllegalArgumentException) {
                Log.e("TaskEditViewModel", "Error deleting alarm: Invalid UUID string: ${alarm.workManagerId}", e)
            }
        }
    }



    private fun validateInput(uiState: TaskDetails = taskUiState.taskDetails): Boolean {
        return with(uiState) {
            titulo.isNotBlank() && descripcion.isNotBlank() &&
                    fechaHoraVencimiento!!.isNotBlank()
        }
    }
}
