package com.example.inventory.ui.task

import android.Manifest
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.inventory.ComposeFileProvider
import com.example.inventory.InventoryTopAppBar
import com.example.inventory.R
import com.example.inventory.data.Alarm
import com.example.inventory.ui.AppViewModelProvider
import com.example.inventory.ui.navigation.NavigationDestination
import com.example.inventory.worker.NotificationWorker
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
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
        TaskEditBody(
            taskUiState = viewModel.taskUiState,
            alarms = alarms,
            onTaskValueChange = viewModel::updateUiState,
            onSaveClick = {
                coroutineScope.launch {
                    viewModel.updateTask()
                    val taskId = viewModel.taskUiState.taskDetails.id
                    viewModel.processTempAlarms(taskId)
                    navigateBack()
                }
            },
            onAddAlarmClick = {
                coroutineScope.launch {
                    showDatePicker(context) { selectedDate ->
                        showTimePicker(context) { selectedTime ->
                            val newAlarmTime = "$selectedDate $selectedTime"
                            Log.d("TaskEntry", "Adding temporary alarm at $newAlarmTime")
                            viewModel.addTempAlarm(newAlarmTime)
                        }
                    }
                }
            }

            ,
            onDeleteAlarmClick = { alarm ->
                coroutineScope.launch {
                    viewModel.deleteAlarm(context, alarm)
                }
            },
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
            viewModel = viewModel
        )
    }
}


@Composable
fun TaskEditBody(
    taskUiState: TaskUiState,
    alarms: List<Alarm>,
    onTaskValueChange: (TaskDetails) -> Unit,
    onSaveClick: () -> Unit,
    onAddAlarmClick: () -> Unit,
    onDeleteAlarmClick: (Alarm) -> Unit,
    viewModel: TaskEditViewModel,
    modifier: Modifier = Modifier
) {
    val tempPhotoUris = viewModel.tempImageUris
    val tempVideoUris = viewModel.tempVideoUris
    val tempAudioUris = viewModel.tempAudioUris
    val tempAlarms = viewModel.tempAlarms

    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
    ) {
        TaskInputForm(
            taskDetails = taskUiState.taskDetails,
            onValueChange = onTaskValueChange,
            enabled = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onSaveClick,
            enabled = true,
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

        // Mostrar alarmas temporales
        if (tempAlarms.isNotEmpty()) {
            Text(
                text = "Alarmas Temporales:",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            tempAlarms.forEachIndexed { index, alarm ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Fecha y Hora: ${alarm.first}")
                        Text(text = "Tipo: ${alarm.second}")
                    }
                    Button(
                        onClick = { viewModel.removeTempAlarm(index) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(text = "Eliminar")
                    }
                }
                Divider(color = MaterialTheme.colorScheme.onSurfaceVariant, thickness = 1.dp)
            }
        }



        buttonTakePhoto(onPhotoCaptured = { uri -> viewModel.addTempImageUri(uri) })
        buttonTakeVideo(onVideoCaptured = { uri -> viewModel.addTempVideoUri(uri) })

        if (tempPhotoUris.isNotEmpty() || tempVideoUris.isNotEmpty()) {
            Text(
                text = stringResource(R.string.multimedia),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            MultimediaViewer(
                photoUris = tempPhotoUris,
                videoUris = tempVideoUris,
                audioUris = tempAudioUris,
                onRemovePhoto = { uri -> viewModel.removeTempImageUri(uri) },
                onRemoveVideo = { uri -> viewModel.removeTempVideoUri(uri) },
                showRemoveButtons = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (alarms.isEmpty()) {
            Text(
                text = "No hay alarmas asociadas a esta tarea.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            alarms.forEachIndexed { index, alarm ->
                AlarmDisplay(alarm, index, onDelete = onDeleteAlarmClick)
            }
        }
    }
}




@Composable
fun AlarmDisplay(alarm: Alarm, index: Int, onDelete: (Alarm) -> Unit) {
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


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun buttonTakePhoto(onPhotoCaptured: (String) -> Unit) {
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val context = LocalContext.current
    var uri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            Log.d("buttonTakePhoto", "Camera result: $success, URI: $uri")
            if (success && uri != null) {
                onPhotoCaptured(uri.toString())
                Log.d("buttonTakePhoto", "Photo URI captured: $uri")
            }
        }
    )

    Button(
        onClick = {
            if (cameraPermissionState.status.isGranted) {
                uri = ComposeFileProvider.getImageUri(context)
                Log.d("buttonTakePhoto", "Generated URI: $uri")
                cameraLauncher.launch(uri)
            } else {
                cameraPermissionState.launchPermissionRequest()
            }
        }
    ) {
        Text("Tomar foto")
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun buttonTakeVideo(onVideoCaptured: (String) -> Unit) {
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val context = LocalContext.current
    var uri by remember { mutableStateOf<Uri?>(null) }

    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo(),
        onResult = { success ->
            Log.d("buttonTakeVideo", "Video result: $success, URI: $uri")
            if (success && uri != null) {
                onVideoCaptured(uri.toString())
                Log.d("buttonTakeVideo", "Video URI captured: $uri")
            }
        }
    )

    Button(
        onClick = {
            if (cameraPermissionState.status.isGranted) {
                uri = ComposeFileProvider.getVideoUri(context)
                Log.d("buttonTakeVideo", "Generated URI: $uri")
                videoLauncher.launch(uri)
            } else {
                cameraPermissionState.launchPermissionRequest()
            }
        }
    ) {
        Text("Tomar video")
    }
}

