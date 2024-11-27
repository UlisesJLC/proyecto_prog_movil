package com.example.inventory.ui.task

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.inventory.InventoryTopAppBar
import com.example.inventory.R
import com.example.inventory.data.Alarm
import com.example.inventory.ui.AppViewModelProvider
import com.example.inventory.ui.navigation.NavigationDestination
import com.example.inventory.ui.theme.InventoryTheme
import com.example.inventory.worker.NotificationWorker
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit

object TaskEditDestination : NavigationDestination {
    override val route = "task_edit"
    override val titleRes = R.string.edit_task_title
    const val taskIdArg = "taskId"
    val routeWithArgs = "$route/{$taskIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskEditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    val alarms = viewModel.alarmsUiState // Lista de Alarmas
    val context = LocalContext.current

    Scaffold(
        topBar = {
            InventoryTopAppBar(
                title = stringResource(TaskEditDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Cuerpo principal con botón "Add Alarm"
            TaskEntryBody(
                taskUiState = viewModel.taskUiState,
                onTaskValueChange = viewModel::updateUiState,
                onSaveClick = {
                    coroutineScope.launch {
                        viewModel.updateTask()
                        navigateBack()
                    }
                },
                modifier = Modifier.padding(16.dp),
                onAddAlarmClick = {
                    coroutineScope.launch {
                        val taskId = viewModel.taskUiState.taskDetails.id
                        var newAlarmTime: String? = null

                        // Mostrar DatePicker y TimePicker
                        showDatePicker(context) { selectedDate ->
                            showTimePicker(context) { selectedTime ->
                                newAlarmTime = "$selectedDate $selectedTime"

                                newAlarmTime?.let {
                                    val alarm = Alarm(
                                        taskId = taskId,
                                        fechaHora = it,
                                        tipo = "Notification",
                                        estado = true,
                                        workManagerId = ""
                                    )
                                    viewModel.addAlarm(alarm)

                                    val delay = calculateDelay(it)
                                    if (delay > 0) {
                                        val alarmId = UUID.randomUUID().toString()
                                        val alarmRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                                            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                                            .setInputData(
                                                Data.Builder()
                                                    .putString("task_title", viewModel.taskUiState.taskDetails.titulo)
                                                    .putString("task_description", viewModel.taskUiState.taskDetails.descripcion)
                                                    .putString("workManagerId", alarmId) // Enviar el UUID al WorkManager
                                                    .build()
                                            )
                                            .build()

                                        WorkManager.getInstance(context).enqueue(alarmRequest)

                                        // Actualizar el workManagerId en la alarma
                                        val updatedAlarm = alarm.copy(workManagerId = alarmRequest.id.toString())
                                        viewModel.addAlarm(updatedAlarm) // Actualiza la alarma con el ID válido

                                    }
                                }
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mostrar alarmas con una lógica flexible
            if (alarms.isEmpty()) {
                Text(
                    text = "No hay alarmas asociadas a esta tarea.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                // Lógica de la pantalla principal
                alarms.forEachIndexed { index, alarm ->
                    AlarmDisplay(alarm, index) { alarmToDelete ->
                        coroutineScope.launch {
                            viewModel.deleteAlarm(context, alarmToDelete)
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun AlarmDisplay(alarm: Alarm, index: Int, onDelete: (Alarm) -> Unit) {
    if(alarm.workManagerId!="") {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Alarma",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Fecha y Hora: ${alarm.fechaHora}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Tipo: ${alarm.tipo}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = if (alarm.estado) "Estado: Activa" else "Estado: Inactiva",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botón para eliminar
            Button(
                onClick = { onDelete(alarm) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = "Eliminar")
            }

            Divider(color = MaterialTheme.colorScheme.onSurfaceVariant, thickness = 1.dp)


        }
    }
}

