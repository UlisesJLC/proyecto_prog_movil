package com.example.inventory.ui.task

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.inventory.data.Alarm
import com.example.inventory.data.AlarmRepository
import com.example.inventory.data.TasksRepository
import com.example.inventory.worker.NotificationWorker
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit

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

    private val _tempAlarms = mutableStateListOf<Pair<String, String>>()
    val tempAlarms: List<Pair<String, String>> get() = _tempAlarms


    private val taskId: Int = checkNotNull(savedStateHandle[TaskEditDestination.taskIdArg])


    fun addTempAlarm(fechaHora: String, tipo: String = "Manual") {
        // Evitar duplicados
        if (_tempAlarms.none { it.first == fechaHora && it.second == tipo }) {
            _tempAlarms.add(fechaHora to tipo)
            Log.d("TaskEditViewModel", "Temporary alarm added: $fechaHora, $tipo")
        } else {
            Log.w("TaskEditViewModel", "Temporary alarm already exists: $fechaHora, $tipo")
        }
    }

    fun removeTempAlarm(index: Int) {
        if (index in _tempAlarms.indices) {
            val removedAlarm = _tempAlarms[index]
            _tempAlarms.removeAt(index)
            Log.d("TaskEditViewModel", "Temporary alarm removed: $removedAlarm")
        } else {
            Log.w("TaskEditViewModel", "Index out of bounds for removal: $index")
        }
    }




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
            launch {
                alarmsRepository.getAlarmsByTaskId(taskId).collect { alarms ->
                    alarmsUiState = alarms
                    Log.d("TaskEditViewModel", "Loaded alarms: $alarms")
                }
            }
        }
    }


    suspend fun processTempAlarms(taskId: Int) {
        tempAlarms.forEach { (fechaHora, tipo) ->
            val alarm = Alarm(
                taskId = taskId,
                fechaHora = fechaHora,
                estado = true,
                tipo = tipo,
                workManagerId = "" // Inicialmente vacío
            )

            // Inserta la alarma y captura el ID generado
            val alarmId = alarmsRepository.insertAlarm(alarm).toInt()
            Log.d("processTempAlarms", "Inserted alarm with ID: $alarmId")

            if (alarmId > 0) {
                // Configura la alarma en WorkManager
                val delay = calculateDelay(fechaHora)
                if (delay > 0) {
                    val alarmRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(
                            Data.Builder()
                                .putString("task_title", taskUiState.taskDetails.titulo)
                                .putString("task_description", taskUiState.taskDetails.descripcion)
                                .build()
                        )
                        .build()

                    WorkManager.getInstance().enqueue(alarmRequest)

                    // Actualiza la alarma con el WorkManager ID
                    val updatedAlarm = alarm.copy(
                        id = alarmId,
                        workManagerId = alarmRequest.id.toString()
                    )
                    alarmsRepository.updateAlarm(updatedAlarm)
                    Log.d("processTempAlarms", "Updated alarm with WorkManager ID: ${alarmRequest.id}")
                } else {
                    Log.w("processTempAlarms", "Skipped creating alarm due to invalid delay: $delay")
                }
            } else {
                Log.e("processTempAlarms", "Failed to insert alarm into the database.")
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
                    Log.d("TaskEditViewModel", "Attempting to cancel WorkManager job with ID: ${alarm.workManagerId}")
                    val workManager = WorkManager.getInstance(context)
                    workManager.cancelWorkById(UUID.fromString(alarm.workManagerId))
                    Log.d("TaskEditViewModel", "WorkManager job cancelled successfully.")
                } else {
                    Log.w("TaskEditViewModel", "WorkManager ID is blank; skipping cancellation.")
                }

                // Eliminar la alarma de la base de datos
                alarmsRepository.deleteAlarm(alarm)
                Log.d("TaskEditViewModel", "Alarm deleted successfully from the repository.")
            } catch (e: IllegalArgumentException) {
                Log.e("TaskEditViewModel", "Error deleting alarm: Invalid UUID string: ${alarm.workManagerId}", e)
            } catch (e: Exception) {
                Log.e("TaskEditViewModel", "Unexpected error while deleting alarm: ${e.message}", e)
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
