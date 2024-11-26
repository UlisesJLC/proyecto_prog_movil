package com.example.inventory.ui.task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.TasksRepository
import com.example.inventory.data.Task
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
    val estado: Boolean = false
)

fun Task.toTaskDetailsInfo(): TaskDetailsInfo = TaskDetailsInfo(
    id = this.id,
    titulo = this.titulo,
    descripcion = this.descripcion,
    fechaHoraVencimiento = this.fechaHoraVencimiento,
    estado = this.estado
)

fun TaskDetailsInfo.toTask(): Task = Task(
    id = this.id,
    titulo = this.titulo,
    descripcion = this.descripcion,
    fechaHoraVencimiento = this.fechaHoraVencimiento ?: "",
    estado = this.estado
)
