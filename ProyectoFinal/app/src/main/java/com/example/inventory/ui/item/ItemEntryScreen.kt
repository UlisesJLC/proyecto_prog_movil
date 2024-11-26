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
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import coil.compose.AsyncImage
import com.example.inventory.ComposeFileProvider
import com.example.inventory.InventoryTopAppBar
import com.example.inventory.R
import com.example.inventory.VideoPlayer
import com.example.inventory.ui.AppViewModelProvider
import com.example.inventory.ui.navigation.NavigationDestination
import com.example.inventory.ui.theme.InventoryTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Currency
import java.util.Date
import java.util.Locale
import kotlin.toString

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
            modifier = Modifier
                .padding(
                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                )
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
        )
    }
}

@Composable
fun ItemEntryBody(
    itemUiState: ItemUiState,
    onItemValueChange: (ItemDetails) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
    ) {
        ItemInputForm(
            itemDetails = itemUiState.itemDetails,
            onValueChange = onItemValueChange,
            modifier = Modifier.fillMaxWidth()
        )
        buttonTakePhoto()
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun buttonTakePhoto(){
    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )
    var uri : Uri? = null
    // 1
    var hasImage by remember {
        mutableStateOf(false)
    }
    var hasVideo by remember {
        mutableStateOf(false)
    }
    // 2
    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            Log.d("IMG", hasImage.toString())
            Log.d("URI", imageUri.toString())
            if(success) imageUri = uri
            hasImage = success
        }
    )


    val context = LocalContext.current
    if ((hasImage or hasVideo) && imageUri != null) {
        // 5
        if(hasImage){
            AsyncImage(
                model = imageUri,
                modifier = Modifier.size(100.dp),
                contentDescription = "Selected image",
            )
        }
        if(hasVideo) {VideoPlayer(videoUri = imageUri!!)}
    }
    Button(
        modifier = Modifier.padding(top = 16.dp),
        onClick = {
            if ((cameraPermissionState.status.isGranted)) {
                uri = ComposeFileProvider.getImageUri(context) // uri guarda la direccion de la imagen
                //imageUri = uri
                cameraLauncher.launch(uri!!)

            } else {
                // El permiso no está concedido, solicítalo
                cameraPermissionState.launchPermissionRequest()
                try {
                    cameraLauncher.launch(uri!!)
                }catch (e: Exception){}
            }
        },
    ) {
        Text(
            text = "Take photo"
        )
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
        OutlinedTextField(
            value = itemDetails.clasificacion,
            onValueChange = { onValueChange(itemDetails.copy(clasificacion = it)) },
            label = { Text("Nota") },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true
        )

        /*OutlinedTextField(
            value = itemDetails.horaCumplimiento?.toString() ?: "",
            onValueChange = { onValueChange(itemDetails.copy(horaCumplimiento = it.toLongOrNull())) },
            label = { Text(stringResource(R.string.item_due_time)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true
        )*/
        DatePickerModal({},itemDetails=itemDetails,onValueChange=onValueChange)
    }
}

@Preview(showBackground = true)
@Composable
private fun ItemEntryScreenPreview() {
    InventoryTheme {
        ItemEntryBody(
            itemUiState = ItemUiState(
                itemDetails = ItemDetails(
                    titulo = "Título de ejemplo",
                    descripcion = "Descripción de ejemplo",
                    clasificacion = "nota",
                    horaCumplimiento = System.currentTimeMillis()
                )
            ),
            onItemValueChange = {},
            onSaveClick = {}
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    itemDetails: ItemDetails,
    onValueChange: (ItemDetails) -> Unit = {}
    //onValueChange:()->Unit
) {

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val selectedDate = datePickerState.selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: ""
    OutlinedTextField(
        value = itemDetails.horaCumplimiento?.toString() ?: selectedDate,
        onValueChange = { onValueChange(itemDetails.copy(horaCumplimiento = datePickerState.selectedDateMillis)) },
        label = { Text(itemDetails.horaCumplimiento?.toString() ?: selectedDate) },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDatePicker = !showDatePicker }) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select date"
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    )
    if(showDatePicker){
        DatePickerDialog(
            onDismissRequest = { showDatePicker=false },
            confirmButton = {
                TextButton(onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                    showDatePicker=false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {showDatePicker=false}) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

}

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}