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


// com.example.inventory.work.scheduleAlarmNotification
//import com.example.inventory.ui.task.GrabarAudioScreen // Si quieres usar GrabarAudioScreen
import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
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
import com.example.inventory.data.Task
import com.example.inventory.ui.AppViewModelProvider
import com.example.inventory.ui.navigation.NavigationDestination
import com.example.inventory.worker.NotificationWorker
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit


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
                    val taskId = viewModel.saveTask()



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


                    if (taskId != null) {
                        viewModel.processTempAlarms(taskId)
                    } else {
                        Log.e("TaskEntryScreen", "Failed to save task")
                    }

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
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
            ,
            viewModel = viewModel
        )
    }
}

@Composable
fun TaskEntryBody(
    taskUiState: TaskUiState,
    onTaskValueChange: (TaskDetails) -> Unit,
    onSaveClick: () -> Unit,
    onAddAlarmClick: () -> Unit,
    viewModel: TaskEntryViewModel, // Agregar este parámetro,
    modifier: Modifier = Modifier
) {
    val photoUris = viewModel.getPhotoUris()
    val videoUris = viewModel.getVideoUris()
    val audioUris = viewModel.getAudioUris()
    val tempAlarms = viewModel.tempAlarms
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



        takeAudio(viewModel = viewModel)

        buttonTakePhoto(viewModel = viewModel)
        buttonTakeVideo(viewModel = viewModel)
        // Vista previa de fotos y videos
        if (photoUris.isNotEmpty() || videoUris.isNotEmpty()) {
            Text(
                text = stringResource(R.string.multimedia),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            MultimediaViewer(
                photoUris = photoUris,
                videoUris = videoUris,
                audioUris = audioUris,
                onRemovePhoto = { uri -> viewModel.removePhotoUri(uri) },
                onRemoveVideo = { uri -> viewModel.removeVideoUri(uri) },
                showRemoveButtons = true)
        }


    }
}

@Composable
fun TaskInputForm(
    taskDetails: TaskDetails,
    onValueChange: (TaskDetails) -> Unit,
    modifier: Modifier = Modifier,
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun buttonTakePhoto(viewModel: TaskEntryViewModel) {
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val context = LocalContext.current
    var uri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            Log.d("buttonTakePhoto", "Camera result: $success, URI: $uri")
            if (success && uri != null) {
                viewModel.addImageUri(uri!!)
                Log.d("buttonTakePhoto", "Added URI to ViewModel: $uri")
            }
        }
    )

    Button(
        onClick = {
            if (cameraPermissionState.status.isGranted) {
                uri = ComposeFileProvider.getImageUri(context)
                Log.d("buttonTakePhoto", "Generated URI: $uri")
                cameraLauncher.launch(uri!!)
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
fun buttonTakeVideo(viewModel: TaskEntryViewModel) {
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val context = LocalContext.current
    var uri by remember { mutableStateOf<Uri?>(null) }

    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo(),
        onResult = { success ->
            Log.d("buttonTakeVideo", "Video result: $success, URI: $uri")
            if (success && uri != null) {
                viewModel.addVideoUri(uri!!)
                Log.d("buttonTakeVideo", "Added URI to ViewModel: $uri")
            }
        }
    )
    Button(
        onClick = {
            if (cameraPermissionState.status.isGranted) {
                uri = ComposeFileProvider.getVideoUri(context)
                Log.d("buttonTakeVideo", "Generated URI: $uri")
                videoLauncher.launch(uri!!)
            } else {
                cameraPermissionState.launchPermissionRequest()
            }
        }
    ) {
        Text("Tomar video")
    }

}

@Composable
fun takeAudio(viewModel: TaskEntryViewModel) {
    val context = LocalContext.current
    val recorder by lazy { AndroidAudioRecorder(context) }
    val player by lazy { AndroidAudioPlayer(context) }
    var audioFile: File? = null
    var audioFile2: File? = null
    var audioUri by remember { mutableStateOf<Uri?>(null) }

    GrabarAudioScreen(
        onClickStGra = {
            val audioFileName = "audio_${System.currentTimeMillis()}.mp3"
            audioFile = File(context.filesDir, audioFileName)
            audioUri = ComposeFileProvider.getAudioUri(context, audioFile!!)
            audioFile?.let {
                recorder.start(it)
            }
        },
        onClickSpGra = {
            recorder.stop()
            if (audioUri != null) {
                viewModel.addAudioUri(audioUri!!)
                Log.d("takeAudio", "Added Audio URI to ViewModel: $audioUri")
            }
        },
        onClickStRe = {
            audioUri?.let { uri ->
                val realPath = uri.getRealPath(context)
                if (realPath != null) {
                    audioFile2 = File(realPath) // Crea el objeto File con la ruta
                    // ... usa audioFile aquí ...
                } else {
                    // Maneja el caso en que no se pudo obtener la ruta
                    Log.e("takeAudio", "Error getting real path from URI")
                }
            }
            audioFile2?.let { player.start(it)}
        },
        onClickSpRe = { player.stop() }

    )
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
fun Uri.getRealPath(context: Context): String? {
    if (scheme == "content") {
        // Intenta obtener la ruta del archivo directamente de la URI
        val pathSegments = pathSegments
        if (pathSegments.size > 1) {
            val fileName = pathSegments.last()
            val directory = context.filesDir // O la ubicación donde se guarda el archivo
            return File(directory, fileName).absolutePath
        }
    }
    return path
}