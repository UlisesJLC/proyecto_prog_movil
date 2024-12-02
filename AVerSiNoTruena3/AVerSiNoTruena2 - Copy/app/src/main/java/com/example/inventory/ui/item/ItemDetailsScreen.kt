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

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.VideoView
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.inventory.InventoryTopAppBar
import com.example.inventory.R
import com.example.inventory.data.Item
import com.example.inventory.ui.AppViewModelProvider
import com.example.inventory.ui.navigation.NavigationDestination
import com.example.inventory.ui.theme.InventoryTheme
import kotlinx.coroutines.launch
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File


object ItemDetailsDestination : NavigationDestination {
    override val route = "item_details"
    override val titleRes = R.string.item_detail_title
    const val itemIdArg = "itemId"
    val routeWithArgs = "$route/{$itemIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsScreen(
    navigateToEditItem: (Int) -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ItemDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            InventoryTopAppBar(
                title = stringResource(ItemDetailsDestination.titleRes),
                canNavigateBack = true,
                navigateUp = navigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navigateToEditItem(uiState.value.itemDetails.id) },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .padding(
                        end = WindowInsets.safeDrawing.asPaddingValues()
                            .calculateEndPadding(LocalLayoutDirection.current)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit_item_title),
                )
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        ItemDetailsBody(
            itemDetailsUiState = uiState.value,
            onCompleteItem = {
                coroutineScope.launch {
                    viewModel.completeItem()
                }
            },
            onDelete = {
                coroutineScope.launch {
                    viewModel.deleteItem()
                    navigateBack()
                }
            },
            modifier = Modifier
                .padding(
                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                )
                .verticalScroll(rememberScrollState()),
            viewModel = viewModel
        )
    }
}

@Composable
private fun ItemDetailsBody(
    itemDetailsUiState: ItemDetailsUiState,
    onDelete: () -> Unit,
    onCompleteItem: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ItemDetailsViewModel
) {
    val photoUris = viewModel.getPhotoUris()
    val videoUris = viewModel.getVideoUris()
    val audioUris = viewModel.getAudioUris()

    // Estado para el contenido multimedia seleccionado
    var selectedMediaUri by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedMediaType by rememberSaveable { mutableStateOf<MediaType?>(null) }

    // Logs para verificar las listas
    Log.d("ItemDetailsBody", "Photo URIs: $photoUris")
    Log.d("ItemDetailsBody", "Video URIs: $videoUris")
    Log.d("ItemDetailsBody", "Audio URIs: $audioUris")

    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ) {
        var deleteConfirmationRequired by rememberSaveable { mutableStateOf(false) }

        ItemDetails(
            item = itemDetailsUiState.itemDetails.toItem(),
            modifier = Modifier.fillMaxWidth()
        )

        if (photoUris.isNotEmpty() || videoUris.isNotEmpty() || audioUris.isNotEmpty()) {
            Text(
                text = stringResource(R.string.multimedia),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            MultimediaViewer(
                photoUris = photoUris,
                videoUris = videoUris,
                audioUris = audioUris,
                onMediaClick = { uri, type ->
                    selectedMediaUri = uri
                    selectedMediaType = type
                }
            )
        }

        OutlinedButton(
            onClick = { deleteConfirmationRequired = true },
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.delete))
        }

        if (deleteConfirmationRequired) {
            DeleteConfirmationDialog(
                onDeleteConfirm = {
                    deleteConfirmationRequired = false
                    onDelete()
                },
                onDeleteCancel = { deleteConfirmationRequired = false },
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))
            )
        }
    }

    // Mostrar el contenido multimedia seleccionado en un Dialog
    selectedMediaUri?.let { uri ->
        when (selectedMediaType) {
            MediaType.IMAGE -> FullscreenImageDialog(uri = uri, onDismiss = { selectedMediaUri = null })
            MediaType.VIDEO -> FullscreenVideoDialog(uri = uri, onDismiss = { selectedMediaUri = null })
            MediaType.AUDIO -> FullscreenAudioDialog(uri = uri, onDismiss = { selectedMediaUri = null })
            else -> {}
        }
    }
}

enum class MediaType {
    IMAGE, VIDEO, AUDIO
}

@Composable
fun MultimediaViewer(
    photoUris: List<String>,
    videoUris: List<String>,
    audioUris: List<String>,
    onMediaClick: (String, MediaType) -> Unit,
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
                        .clickable { onMediaClick(uri, MediaType.IMAGE) }
                )
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
                    .clickable { onMediaClick(uri, MediaType.VIDEO) }
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = stringResource(R.string.video),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        // Mostrar audios
        items(audioUris.size) { index ->
            val uriString = audioUris[index]
            val uri = Uri.parse(uriString)
            val realPath = uri.getRealPath(context)
            val audioFile = File(realPath)
            ReproducirAudioScreen(audioFile)
        }
    }
}

@Composable
fun FullscreenImageDialog(uri: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        modifier = Modifier.padding(16.dp),
        text = {
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
            )
        }
    )
}

@Composable
fun FullscreenVideoDialog(uri: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        modifier = Modifier.padding(16.dp),
        text = {
            AndroidView(
                factory = { context ->
                    VideoView(context).apply {
                        setVideoURI(android.net.Uri.parse(uri))
                        setOnPreparedListener { it.start() } // Inicia el video automáticamente
                        setOnCompletionListener { onDismiss() } // Cierra el dialog al terminar
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
            )
        }
    )
}


@Composable
fun FullscreenAudioDialog(uri: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        modifier = Modifier.padding(16.dp),
        text = {
            Text("Audio playback not implemented. URI: $uri")
        }
    )
}



@Composable
fun ItemDetails(
    item: Item, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
        ) {
            ItemDetailsRow(
                labelResID = R.string.item_title,
                itemDetail = item.titulo,
                modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_medium))
            )
            ItemDetailsRow(
                labelResID = R.string.item_description,
                itemDetail = item.descripcion,
                modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_medium))
            )

            ItemDetailsRow(
                labelResID = R.string.item_status,
                itemDetail = if (item.estado) "Cumplida" else "Pendiente",
                modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_medium))
            )

        }
    }
}


@Composable
private fun ItemDetailsRow(
    @StringRes labelResID: Int, itemDetail: String, modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Text(text = stringResource(labelResID))
        Spacer(modifier = Modifier.weight(1f))
        Text(text = itemDetail, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onDeleteConfirm: () -> Unit, onDeleteCancel: () -> Unit, modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { /* Do nothing */ },
        title = { Text(stringResource(R.string.attention)) },
        text = { Text(stringResource(R.string.delete_question)) },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDeleteCancel) {
                Text(text = stringResource(R.string.no))
            }
        },
        confirmButton = {
            TextButton(onClick = onDeleteConfirm) {
                Text(text = stringResource(R.string.yes))
            }
        }
    )
}

