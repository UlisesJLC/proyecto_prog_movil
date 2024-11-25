package com.example.inventory.ui.task

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
    suspend fun updateTask() {
        if (validateInput(taskUiState.taskDetails)) {
            tasksRepository.updateTask(taskUiState.taskDetails.toTask())
        }
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
            // Validamos solo los campos requeridos en función de la clasificación
            titulo.isNotBlank() && descripcion.isNotBlank()  &&
                    ( (fechaHoraVencimiento != null && estado != null))
        }
    }
}
