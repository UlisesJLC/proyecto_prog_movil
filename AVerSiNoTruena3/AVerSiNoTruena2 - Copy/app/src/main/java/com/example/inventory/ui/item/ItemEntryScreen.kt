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

package com.example.inventory.ui.item

import android.Manifest
import android.R.attr.value
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.inventory.ComposeFileProvider
import com.example.inventory.InventoryTopAppBar
import com.example.inventory.R
import com.example.inventory.ui.AppViewModelProvider
import com.example.inventory.ui.task.getRealPath
import com.example.inventory.ui.navigation.NavigationDestination
import com.example.inventory.ui.task.AndroidAudioPlayer
import com.example.inventory.ui.task.AndroidAudioRecorder
import com.example.inventory.ui.task.GrabarAudioScreen
import com.example.inventory.ui.task.TaskEntryViewModel
import com.example.inventory.ui.task.getRealPath
import com.example.inventory.ui.theme.InventoryTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Currency
import java.util.Date
import java.util.Locale

object ItemEntryDestination : NavigationDestination {
    override val route = "item_entry"
    override val titleRes = R.string.item_entry_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEntryScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    canNavigateBack: Boolean = true,
    viewModel: ItemEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            InventoryTopAppBar(
                title = stringResource(ItemEntryDestination.titleRes),
                canNavigateBack = canNavigateBack,
                navigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
        ItemEntryBody(
            itemUiState = viewModel.itemUiState,
            onItemValueChange = viewModel::updateUiState,
            onSaveClick = {
                coroutineScope.launch {
                    viewModel.saveItem()
                    navigateBack()
                }
            },
            onRemovePhoto = { uri -> viewModel.removePhoto(uri) },
            onRemoveVideo = { uri -> viewModel.removeVideo(uri) },
            onRemoveAudio = { uri -> viewModel.removeAudio(uri) },
            modifier = Modifier
                .padding(
                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                )
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
            viewModel = viewModel
        )
    }
}


@Composable
fun ItemEntryBody(
    itemUiState: ItemUiState,
    onItemValueChange: (ItemDetails) -> Unit,
    onSaveClick: () -> Unit,
    onRemovePhoto: (String) -> Unit,
    onRemoveVideo: (String) -> Unit,
    onRemoveAudio: (String) -> Unit,
    viewModel: ItemEntryViewModel,
    modifier: Modifier = Modifier
) {
    val tempPhotoUris = viewModel.tempPhotoUris
    val tempVideoUris = viewModel.tempVideoUris
    val tempAudioUris = viewModel.tempAudioUris
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            // TODO
            // 3
            Log.d("TXT", uri.toString())
            //hasImage = uri != null
            //imageUri = uri
        }
    )
    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
    ) {
        // Formulario de entrada de datos
        ItemInputForm(
            itemDetails = itemUiState.itemDetails,
            onValueChange = onItemValueChange,
            modifier = Modifier.fillMaxWidth()
        )

        // Captura de multimedia: fotos y videos
        buttonTakePhoto(onPhotoCaptured = { uri -> viewModel.addTempImageUri(uri) })
        buttonTakeVideo(onVideoCaptured = { uri -> viewModel.addTempVideoUri(uri) })
        takeAudio(viewModel = viewModel) // Llama a la función takeAudio
        Button(
            onClick = { imagePicker.launch("image/*") },
        ) {
            Text(
                text = "Select Image"
            )
        }

        // Mostrar multimedia con opciones para eliminar
        if (tempPhotoUris.isNotEmpty() || tempVideoUris.isNotEmpty()) {
            Text(
                text = stringResource(R.string.multimedia),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            MultimediaViewers(
                photoUris = tempPhotoUris,
                videoUris = tempVideoUris,
                audioUris = tempAudioUris,
                onRemovePhoto = onRemovePhoto,
                onRemoveVideo = onRemoveVideo,
                onRemoveAudio  = onRemoveAudio,
                showRemoveButtons = true
            )
        }

        Button(
            onClick = onSaveClick,
            enabled = itemUiState.isEntryValid,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.save_action))
        }
    }
}

@Composable
fun ItemInputForm(
    itemDetails: ItemDetails,
    modifier: Modifier = Modifier,
    onValueChange: (ItemDetails) -> Unit = {},
    enabled: Boolean = true
) {

    var mostrarDatePicker by remember { mutableStateOf(false) }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ) {
        OutlinedTextField(
            value = itemDetails.titulo,
            onValueChange = { onValueChange(itemDetails.copy(titulo = it)) },
            label = { Text("Titulo") },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true
        )
        OutlinedTextField(
            value = itemDetails.descripcion,
            onValueChange = { onValueChange(itemDetails.copy(descripcion = it)) },
            label = { Text("Descripcion") },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true
        )


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
            if (success && uri != null) {
                onPhotoCaptured(uri.toString())
            }
        }
    )

    Button(
        onClick = {
            if (cameraPermissionState.status.isGranted) {
                uri = ComposeFileProvider.getImageUri(context)
                cameraLauncher.launch(uri!!)
            } else {
                cameraPermissionState.launchPermissionRequest()
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Capturar Foto")
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
            if (success && uri != null) {
                onVideoCaptured(uri.toString())
            }
        }
    )

    Button(
        onClick = {
            if (cameraPermissionState.status.isGranted) {
                uri = ComposeFileProvider.getVideoUri(context)
                videoLauncher.launch(uri!!)
            } else {
                cameraPermissionState.launchPermissionRequest()
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Grabar Video")
    }
}

@Composable
fun buttonPickAudio(onAudioPicked: (String) -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                onAudioPicked(uri.toString())
            }
        }
    )

    Button(
        onClick = { launcher.launch("audio/*") },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Seleccionar Audio")
    }
}




@Composable
fun MultimediaViewers(
    photoUris: List<String>,
    videoUris: List<String>,
    audioUris: List<String>,
    onRemovePhoto: (String) -> Unit = {},
    onRemoveVideo: (String) -> Unit = {},
    onRemoveAudio: (String) -> Unit = {},
    showRemoveButtons: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
    ) {
        // Mostrar imágenes
        items(photoUris.size) { index ->
            val uri = photoUris[index]
            Box(modifier = Modifier.size(100.dp)) {
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                )
                if (showRemoveButtons) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clickable { onRemovePhoto(uri) },
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Mostrar videos
        items(videoUris.size) { index ->
            val uri = videoUris[index]
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = stringResource(R.string.video),
                    modifier = Modifier.align(Alignment.Center)
                )
                if (showRemoveButtons) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clickable { onRemoveVideo(uri) },
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Mostrar audios
        items(audioUris.size) { index ->
            val uriString = audioUris[index]
            val uri = Uri.parse(uriString)
            val realPath = uri.getRealPath(context)
            val audioFile = File(realPath)
            Column {
                ReproducirAudioScreen(audioFile)
                /*if (showRemoveButtons) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }*/
            }
        }
    }
}

@Composable
fun takeAudio(viewModel: ItemEntryViewModel) {
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
                viewModel.addAudioUri((audioUri!!).toString())
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
            audioFile2?.let { player.start(it) }
        },
        onClickSpRe = { player.stop() }

    )
}


