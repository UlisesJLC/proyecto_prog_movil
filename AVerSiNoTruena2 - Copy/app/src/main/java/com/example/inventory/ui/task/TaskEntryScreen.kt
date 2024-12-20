/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.inventory.ui.task

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventory.InventoryTopAppBar
import com.example.inventory.R
import com.example.inventory.data.Task
import com.example.inventory.ui.AppViewModelProvider
import com.example.inventory.ui.navigation.NavigationDestination
import com.example.inventory.ui.theme.InventoryTheme
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.work.*
import com.example.inventory.worker.NotificationWorker

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Data
// com.example.inventory.work.scheduleAlarmNotification
import java.util.concurrent.TimeUnit

import java.text.SimpleDateFormat
import java.util.*


object TaskEntryDestination : NavigationDestination {
    override val route = "task_entry"
    override val titleRes = R.string.task_entry_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEntryScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    canNavigateBack: Boolean = true,
    viewModel: TaskEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            InventoryTopAppBar(
                title = stringResource(TaskEntryDestination.titleRes),
                canNavigateBack = canNavigateBack,
                navigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
        TaskEntryBody(
            taskUiState = viewModel.taskUiState,
            onTaskValueChange = viewModel::updateUiState,
            onSaveClick = {
                val task = Task(
                    titulo = viewModel.taskUiState.taskDetails.titulo,
                    descripcion = viewModel.taskUiState.taskDetails.descripcion,
                    fechaHoraVencimiento = viewModel.taskUiState.taskDetails.fechaHoraVencimiento ?: "",
                    estado = false // Configuración predeterminada
                )

                coroutineScope.launch {
                    viewModel.saveTask()



                    // Programar notificación
                    val notificationWork = OneTimeWorkRequestBuilder<NotificationWorker>()
                        .setInitialDelay(calculateDelay(task.fechaHoraVencimiento), TimeUnit.MILLISECONDS)
                        .setInputData(
                            Data.Builder()
                                .putString("task_title", task.titulo)
                                .putString("task_description", task.descripcion)
                                .build()
                        )
                        .build()

                    WorkManager.getInstance(context).enqueue(notificationWork)

                    navigateBack()
                }
            },
            onAddAlarmClick = {
                coroutineScope.launch {
                    val taskId = viewModel.taskUiState.taskDetails.id
                    val newAlarmTime = "2024-11-30 14:00" // Reemplaza con tu lógica de obtención
                    Log.d("TaskEntry", "Adding alarm for taskId: $taskId at $newAlarmTime")

                    viewModel.addAlarm(taskId, newAlarmTime)

                    // Verifica el delay calculado
                    val delay = calculateDelay(newAlarmTime)
                    Log.d("TaskEntry", "Calculated delay: $delay ms")

                    if (delay > 0) {
                        // Programar la alarma en WorkManager
                        val alarmId = UUID.randomUUID().toString()
                        val alarmRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                            .setInputData(
                                Data.Builder()
                                    .putString("task_title", viewModel.taskUiState.taskDetails.titulo)
                                    .putString("task_description", viewModel.taskUiState.taskDetails.descripcion)
                                    .putString("workManagerId", alarmId)
                                    .build()
                            )
                            .build()

                        WorkManager.getInstance(context).enqueue(alarmRequest)
                        Log.d("TaskEntry", "Alarm enqueued with WorkManager ID: $alarmId")
                    } else {
                        Log.e("TaskEntry", "Failed to add alarm: Invalid delay.")
                    }
                }
            }
            ,
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
        )
    }
}

@Composable
fun TaskEntryBody(
    taskUiState: TaskUiState,
    onTaskValueChange: (TaskDetails) -> Unit,
    onSaveClick: () -> Unit,
    onAddAlarmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
    ) {
        TaskInputForm(
            taskDetails = taskUiState.taskDetails,
            onValueChange = onTaskValueChange,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onSaveClick,
            enabled = taskUiState.isEntryValid,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.save_action))
        }

        Button(
            onClick = onAddAlarmClick,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.add_alarm))
        }
    }
}

@Composable
fun TaskInputForm(
    taskDetails: TaskDetails,
    modifier: Modifier = Modifier,
    onValueChange: (TaskDetails) -> Unit = {},
    enabled: Boolean = true
) {
    var selectedDate by remember { mutableStateOf(taskDetails.fechaHoraVencimiento ?: "") }
    val context = LocalContext.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ) {
        OutlinedTextField(
            value = taskDetails.titulo,
            onValueChange = { onValueChange(taskDetails.copy(titulo = it)) },
            label = { Text(stringResource(R.string.task_title_req)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true
        )
        OutlinedTextField(
            value = taskDetails.descripcion,
            onValueChange = { onValueChange(taskDetails.copy(descripcion = it)) },
            label = { Text(stringResource(R.string.task_description_req)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true
        )

        // Selector de fecha y hora
        OutlinedTextField(
            value = selectedDate,
            onValueChange = { /* Evitar edición manual */ },
            label = { Text(stringResource(R.string.task_due_time)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) {
                    // Mostrar diálogo de fecha
                    showDatePicker(
                        context = context,
                        onDateSelected = { date ->
                            showTimePicker(context) { time ->
                                val dateTime = "$date $time"
                                selectedDate = dateTime
                                onValueChange(taskDetails.copy(fechaHoraVencimiento = dateTime))
                            }
                        }
                    )
                },
            enabled = false,
            singleLine = true
        )
    }
}


fun calculateDelay(dateTime: String): Long {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return try {
        val taskTime = formatter.parse(dateTime)?.time ?: 0L
        val currentTime = System.currentTimeMillis()
        taskTime - currentTime
    } catch (e: Exception) {
        Log.e("TaskEntry", "Error parsing date: $e")
        0L
    }
}


fun showDatePicker(context: Context, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            onDateSelected(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

fun showTimePicker(context: Context, onTimeSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
            onTimeSelected(formattedTime)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true // Modo de 24 horas
    ).show()
}



/*
@Preview(showBackground = true)
@Composable
private fun TaskEntryScreenPreview() {
    InventoryTheme {
        TaskEntryBody(
            taskUiState = TaskUiState(
                taskDetails = TaskDetails(
                    titulo = "Título de ejemplo",
                    descripcion = "Descripción de ejemplo",
                    fechaHoraVencimiento = "2024-10-31 15:00"
                )
            ),
            onTaskValueChange = {},
            onSaveClick = {}
        )
    }
}
*/