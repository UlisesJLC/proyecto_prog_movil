package com.example.inventory.ui.task

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.inventory.data.Alarm
import com.example.inventory.data.AlarmRepository
import com.example.inventory.data.Task
import com.example.inventory.data.TasksRepository
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.util.UUID


class TaskEntryViewModel(
    private val tasksRepository: TasksRepository,
    private val alarmsRepository: AlarmRepository
) : ViewModel() {

    // Listas temporales de URIs
    private val imageUris = mutableListOf<String>()
    private val videoUris = mutableListOf<String>()

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



    var taskUiState by mutableStateOf(TaskUiState())
        private set

    fun updateUiState(taskDetails: TaskDetails) {
        taskUiState = TaskUiState(
            taskDetails = taskDetails,
            isEntryValid = validateInput(taskDetails)
        )
    }

    suspend fun saveTask() {
        if (validateInput()) {
            val serializedImageUris = Gson().toJson(imageUris)
            val serializedVideoUris = Gson().toJson(videoUris)

            Log.d("TaskEntryViewModel", "Saving Task with Photo URIs: $serializedImageUris")
            Log.d("TaskEntryViewModel", "Saving Task with Video URIs: $serializedVideoUris")

            val task = taskUiState.taskDetails.copy(
                fotoUri = serializedImageUris,
                videoUri = serializedVideoUris
            ).toTask()

            tasksRepository.insertTask(task)
            Log.d("TaskEntryViewModel", "Task saved: $task")
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
    val videoUri: String? = null // Serializado como JSON
)

fun TaskDetails.toTask(): Task = Task(
    id = id,
    titulo = titulo,
    descripcion = descripcion,
    fechaHoraVencimiento = fechaHoraVencimiento ?: "", // Valor predeterminado si es nulo
    estado = estado,
    fotoUri = fotoUri, // Agregar esta línea
    videoUri = videoUri // Agregar esta línea
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
