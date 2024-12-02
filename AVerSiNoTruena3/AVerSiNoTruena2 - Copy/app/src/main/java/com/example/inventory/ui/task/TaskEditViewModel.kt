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
import com.example.inventory.data.TasksRepository
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
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
    private val workManager: WorkManager
) : ViewModel() {

    var taskUiState by mutableStateOf(TaskUiState())
        private set

    var alarmsUiState by mutableStateOf<List<Alarm>>(emptyList())
        private set

    // Variables temporales para multimedia
    var tempImageUris by mutableStateOf(listOf<String>())
        private set
    var tempVideoUris by mutableStateOf(listOf<String>())
        private set

    var tempAudioUris by mutableStateOf(listOf<String>())
        private set

    private val taskId: Int = checkNotNull(savedStateHandle[TaskEditDestination.taskIdArg])

    init {
        viewModelScope.launch {
            // Cargar datos de la tarea
            val task = tasksRepository.getTaskStream(taskId)
                .filterNotNull()
                .first()

            taskUiState = task.toTaskUiState()

            // Inicializar URIs temporales con los valores actuales de la tarea
            tempImageUris = Gson().fromJson(
                task.fotoUri ?: "[]",
                object : TypeToken<MutableList<String>>() {}.type
            )
            tempVideoUris = Gson().fromJson(
                task.videoUri ?: "[]",
                object : TypeToken<MutableList<String>>() {}.type
            )

            // Cargar alarmas asociadas a la tarea
            alarmsRepository.getAlarmsByTaskId(taskId).collect { alarms ->
                alarmsUiState = alarms
            }
        }
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


    fun addTempImageUri(uri: String) {
        tempImageUris = tempImageUris + uri
    }

    fun addTempVideoUri(uri: String) {
        tempVideoUris = tempVideoUris + uri
    }

    fun removeTempImageUri(uri: String) {
        tempImageUris = tempImageUris - uri
    }

    fun removeTempVideoUri(uri: String) {
        tempVideoUris = tempVideoUris - uri
    }



    suspend fun updateTask() {
        if (validateInput(taskUiState.taskDetails)) {
            // Actualiza las listas persistentes con las listas temporales al guardar
            val updatedTaskDetails = taskUiState.taskDetails.copy(
                fotoUri = Gson().toJson(tempImageUris),
                videoUri = Gson().toJson(tempVideoUris)
            )
            tasksRepository.updateTask(updatedTaskDetails.toTask())
        }
    }

    fun updateUiState(taskDetails: TaskDetails) {
        taskUiState = TaskUiState(
            taskDetails = taskDetails,
            isEntryValid = validateInput(taskDetails)
        )
    }

    private fun validateInput(uiState: TaskDetails = taskUiState.taskDetails): Boolean {
        return with(uiState) {
            titulo.isNotBlank() && descripcion.isNotBlank() &&
                    fechaHoraVencimiento!!.isNotBlank()
        }
    }
}
