package com.example.inventory.ui.task

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.inventory.data.Alarm
import com.example.inventory.data.AlarmRepository
import com.example.inventory.data.Task
import com.example.inventory.data.TasksRepository
import com.example.inventory.worker.NotificationWorker
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.util.UUID
import java.util.concurrent.TimeUnit


class TaskEntryViewModel(
    private val tasksRepository: TasksRepository,
    private val alarmsRepository: AlarmRepository
) : ViewModel() {

    // Listas temporales de URIs
    private val imageUris = mutableListOf<String>()
    private val videoUris = mutableListOf<String>()
    private val audioUris = mutableListOf<String>()
    // Cambia tempAlarms a un SnapshotStateList
    private val _tempAlarms = mutableStateListOf<Pair<String, String>>()
    val tempAlarms: List<Pair<String, String>> get() = _tempAlarms

    fun addTempAlarm(fechaHora: String, tipo: String = "Manual") {
        _tempAlarms.add(fechaHora to tipo)
        Log.d("TaskEntryViewModel", "Temporary alarm added: $fechaHora, $tipo")
    }

    fun removeTempAlarm(index: Int) {
        if (index in _tempAlarms.indices) {
            val removedAlarm = _tempAlarms.removeAt(index)
            Log.d("TaskEntryViewModel", "Temporary alarm removed: $removedAlarm")
        } else {
            Log.w("TaskEntryViewModel", "Invalid index for removing temporary alarm: $index")
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








    fun addImageUri(uri: Uri) {
        imageUris.add(uri.toString())
        Log.d("TaskEntryViewModel", "Added Image URI: $uri")
        updateUiState(
            taskUiState.taskDetails.copy(
                fotoUri = Gson().toJson(imageUris) // Serializar los URI actualizados
            )
        )
    }

    fun addVideoUri(uri: Uri) {
        videoUris.add(uri.toString())
        Log.d("TaskEntryViewModel", "Added Video URI: $uri")
        updateUiState(
            taskUiState.taskDetails.copy(
                videoUri = Gson().toJson(videoUris) // Serializar los URI actualizados
            )
        )
    }

    fun addAudioUri(uri: Uri) {
        audioUris.add(uri.toString())
        Log.d("TaskEntryViewModel", "Added Audio URI: $uri")
        updateUiState(
            taskUiState.taskDetails.copy(
                audioUri = Gson().toJson(audioUris) // Serializar los URI actualizados correctamente
            )
        )
    }


    private fun updatePhotoUrisInUiState() {
        val serializedUris = Gson().toJson(imageUris)
        Log.d("TaskEntryViewModel", "Serialized Photo URIs: $serializedUris")
        updateUiState(
            taskUiState.taskDetails.copy(fotoUri = serializedUris)
        )
    }

    private fun updateVideoUrisInUiState() {
        val serializedUris = Gson().toJson(videoUris)
        Log.d("TaskEntryViewModel", "Serialized Video URIs: $serializedUris")
        updateUiState(
            taskUiState.taskDetails.copy(videoUri = serializedUris)
        )
    }



    fun removePhotoUri(uri: String) {
        imageUris.remove(uri)
        updateUiState(
            taskUiState.taskDetails.copy(
                fotoUri = Gson().toJson(imageUris) // Actualiza las URIs serializadas
            )
        )
        Log.d("TaskEntryViewModel", "Removed Photo URI: $uri")
    }

    fun removeVideoUri(uri: String) {
        videoUris.remove(uri)
        updateUiState(
            taskUiState.taskDetails.copy(
                videoUri = Gson().toJson(videoUris) // Actualiza las URIs serializadas
            )
        )
        Log.d("TaskEntryViewModel", "Removed Video URI: $uri")
    }
    fun removeAudioUri(uri: String) {
        audioUris.remove(uri)
        updateUiState(
            taskUiState.taskDetails.copy(
                audioUri = Gson().toJson(videoUris) // Actualiza las URIs serializadas
            )
        )
        Log.d("TaskEntryViewModel", "Removed Audio URI: $uri")
    }



    var taskUiState by mutableStateOf(TaskUiState())
        private set

    fun updateUiState(taskDetails: TaskDetails) {
        taskUiState = TaskUiState(
            taskDetails = taskDetails,
            isEntryValid = validateInput(taskDetails)
        )
    }

    suspend fun saveTask(): Int? {
        return if (validateInput()) {
            val serializedImageUris = Gson().toJson(imageUris)
            val serializedVideoUris = Gson().toJson(videoUris)
            val serializedAudioUris = Gson().toJson(audioUris)

            Log.d("TaskEntryViewModel", "Saving Task with multimedia URIs")
            val task = taskUiState.taskDetails.copy(
                fotoUri = serializedImageUris,
                videoUri = serializedVideoUris,
                audioUri = serializedAudioUris
            ).toTask()

            val generatedId = tasksRepository.insertTask(task).toInt()
            Log.d("TaskEntryViewModel", "Task saved with ID: $generatedId")

            // Actualiza el estado con el ID generado
            updateUiState(taskUiState.taskDetails.copy(id = generatedId))
            return generatedId
        } else {
            null
        }
    }



    suspend fun addAlarm(taskId: Int, fechaHora: String) {
        val alarm = Alarm(
            taskId = taskId,
            fechaHora = fechaHora,
            estado = true,
            tipo = "Manual",
            workManagerId = UUID.randomUUID().toString()
        )
        alarmsRepository.insertAlarm(alarm)
    }




    private fun validateInput(uiState: TaskDetails = taskUiState.taskDetails): Boolean {
        return with(uiState) {
            titulo.isNotBlank() && descripcion.isNotBlank()
        }
    }



    fun getPhotoUris(): List<String> {
        val fotoUri = taskUiState.taskDetails.fotoUri
        return if (!fotoUri.isNullOrBlank()) {
            Gson().fromJson(fotoUri, object : TypeToken<List<String>>() {}.type)
        } else {
            emptyList()
        }
    }

    fun getVideoUris(): List<String> {
        val videoUri = taskUiState.taskDetails.videoUri
        return if (!videoUri.isNullOrBlank()) {
            Gson().fromJson(videoUri, object : TypeToken<List<String>>() {}.type)
        } else {
            emptyList()
        }
    }
    fun getAudioUris(): List<String> {
        val audioUri = taskUiState.taskDetails.audioUri
        return if (!audioUri.isNullOrBlank()) {
            Gson().fromJson(audioUri, object : TypeToken<List<String>>() {}.type)
        } else {
            emptyList()
        }
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
    val estado: Boolean = false,
    val fotoUri: String? = null, // Serializado como JSON
    val videoUri: String? = null, // Serializado como JSON
    val audioUri: String? = null // Serializado como JSON

)

fun TaskDetails.toTask(): Task = Task(
    id = id,
    titulo = titulo,
    descripcion = descripcion,
    fechaHoraVencimiento = fechaHoraVencimiento ?: "", // Valor predeterminado si es nulo
    estado = estado,
    fotoUri = fotoUri, // Agregar esta línea
    videoUri = videoUri, // Agregar esta línea
    audioUri = audioUri // Agregar esta línea
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
