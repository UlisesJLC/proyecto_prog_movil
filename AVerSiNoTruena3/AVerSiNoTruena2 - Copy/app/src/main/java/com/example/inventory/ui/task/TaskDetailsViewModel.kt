package com.example.inventory.ui.task

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.Task
import com.example.inventory.data.TasksRepository
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val tasksRepository: TasksRepository,
) : ViewModel() {

    private val taskId: Int = checkNotNull(savedStateHandle[TaskDetailsDestination.taskIdArg])
    // Listas temporales de URIs
    private val imageUris = mutableListOf<String>()
    private val videoUris = mutableListOf<String>()

    val uiState: StateFlow<TaskDetailsUiState> =
        tasksRepository.getTaskStream(taskId)
            .filterNotNull()
            .map {
                TaskDetailsUiState(
                    isCompleted = it.estado,
                    taskDetails = it.toTaskDetailsInfo()
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = TaskDetailsUiState()
            )

    fun completeTask() {
        viewModelScope.launch {
            val currentTask = uiState.value.taskDetails.toTask()
            if (!currentTask.estado) {
                tasksRepository.updateTask(currentTask.copy(estado = true))
            }
        }
    }

    fun deleteTask() {
        viewModelScope.launch {
            tasksRepository.deleteTask(uiState.value.taskDetails.toTask())
        }
    }

    fun getPhotoUris(): List<String> {
        val fotoUri = uiState.value.taskDetails.fotoUri
        return if (!fotoUri.isNullOrBlank()) {
            Gson().fromJson(fotoUri, object : TypeToken<List<String>>() {}.type)
        } else {
            emptyList()
        }
    }

    fun getVideoUris(): List<String> {
        val videoUri = uiState.value.taskDetails.videoUri
        return if (!videoUri.isNullOrBlank()) {
            Gson().fromJson(videoUri, object : TypeToken<List<String>>() {}.type)
        } else {
            emptyList()
        }
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
    private fun validateInput(uiState: TaskDetails = taskUiState.taskDetails): Boolean {
        return with(uiState) {
            titulo.isNotBlank() && descripcion.isNotBlank()
        }
    }









    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class TaskDetailsUiState(
    val isCompleted: Boolean = false,
    val taskDetails: TaskDetailsInfo = TaskDetailsInfo()
)

data class TaskDetailsInfo(
    val id: Int = 0,
    val titulo: String = "",
    val descripcion: String = "",
    val fechaHoraVencimiento: String? = null,
    val estado: Boolean = false,
    val fotoUri: String? = null, // Serializado como JSON
    val videoUri: String? = null // Serializado como JSON
)

fun Task.toTaskDetailsInfo(): TaskDetailsInfo = TaskDetailsInfo(
    id = this.id,
    titulo = this.titulo,
    descripcion = this.descripcion,
    fechaHoraVencimiento = this.fechaHoraVencimiento,
    estado = this.estado,
    fotoUri = this.fotoUri, // Asegúrate de asignar este campo
    videoUri = this.videoUri // Asegúrate de asignar este campo
)


fun TaskDetailsInfo.toTask(): Task = Task(
    id = this.id,
    titulo = this.titulo,
    descripcion = this.descripcion,
    fechaHoraVencimiento = this.fechaHoraVencimiento ?: "",
    estado = this.estado
)
