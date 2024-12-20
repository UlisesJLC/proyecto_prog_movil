package com.example.inventory.ui.task

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.inventory.data.Alarm
import com.example.inventory.data.AlarmDao
import com.example.inventory.data.AlarmRepository
import com.example.inventory.data.Task
import com.example.inventory.data.TasksRepository
import java.util.UUID


class TaskEntryViewModel(
    private val tasksRepository: TasksRepository,
    private val alarmsRepository: AlarmRepository
) : ViewModel() {

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
            tasksRepository.insertTask(taskUiState.taskDetails.toTask())

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
