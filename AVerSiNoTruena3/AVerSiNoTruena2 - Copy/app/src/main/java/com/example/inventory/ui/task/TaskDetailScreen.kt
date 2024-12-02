package com.example.inventory.ui.task

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.inventory.data.Task
import com.example.inventory.ui.AppViewModelProvider
import com.example.inventory.ui.item.ReproducirAudioScreen
import com.example.inventory.ui.navigation.NavigationDestination
import com.example.inventory.ui.theme.InventoryTheme
import kotlinx.coroutines.launch
import java.io.File
//import kotlin.coroutines.jvm.internal.CompletedContinuation.context

object TaskDetailsDestination : NavigationDestination {
    override val route = "task_details"
    override val titleRes = R.string.task_detail_title
    const val taskIdArg = "taskId"
    val routeWithArgs = "$route/{$taskIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsScreen(
    navigateToEditTask: (Int) -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            InventoryTopAppBar(
                title = stringResource(TaskDetailsDestination.titleRes),
                canNavigateBack = true,
                navigateUp = navigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navigateToEditTask(uiState.value.taskDetails.id) },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .padding(
                        end = WindowInsets.safeDrawing.asPaddingValues()
                            .calculateEndPadding(LocalLayoutDirection.current)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit_task_title),
                )
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        TaskDetailsBody(
            taskDetailsUiState = uiState.value,
            onCompleteTask = {
                coroutineScope.launch {
                    viewModel.completeTask()
                }
            },
            onDelete = {
                coroutineScope.launch {
                    viewModel.deleteTask()
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
            viewModel
        )
    }
}

@Composable
private fun TaskDetailsBody(
    taskDetailsUiState: TaskDetailsUiState,
    onCompleteTask: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskDetailsViewModel
) {
    val photoUris = viewModel.getPhotoUris()
    val videoUris = viewModel.getVideoUris()
    val audioUris = viewModel.getAudioUris()

    // Logs para verificar las listas
    Log.d("TaskDetailsBody", "Photo URIs: $photoUris")
    Log.d("TaskDetailsBody", "Video URIs: $videoUris")
    Log.d("TaskDetailsBody", "Audio URIs: $audioUris")



    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ) {
        var deleteConfirmationRequired by rememberSaveable { mutableStateOf(false) }

        TaskDetails(
            task = taskDetailsUiState.taskDetails.toTask(),
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
                onRemovePhoto = { uri -> viewModel.removePhotoUri(uri) },
                onRemoveVideo = { uri -> viewModel.removeVideoUri(uri) },
                onRemoveAudio = { uri -> viewModel.removeAudioUri(uri) },
                showRemoveButtons = false
            )
        }

        if (!taskDetailsUiState.isCompleted) {
            Button(
                onClick = onCompleteTask,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) {
                Text(stringResource(R.string.complete_task))
            }
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
}


@Composable
fun MultimediaViewer(
    photoUris: List<String>,
    videoUris: List<String>,
    audioUris: List<String>,
    onRemovePhoto: (String) -> Unit = {},
    onRemoveVideo: (String) -> Unit = {},
    onRemoveAudio: (String) -> Unit = {},
    showRemoveButtons: Boolean = false,
    modifier: Modifier = Modifier
) {
    var fullscreenImageUri by remember { mutableStateOf<String?>(null) }
    var fullscreenVideoUri by remember { mutableStateOf<String?>(null) }

    LazyRow(
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_small)),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mostrar imágenes
        items(photoUris.size) { index ->
            val uri = photoUris[index]
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    .clickable { fullscreenImageUri = uri } // Abre el diálogo de imagen
            ) {
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
                if (showRemoveButtons) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.delete),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .clickable { onRemovePhoto(uri) }
                            .size(24.dp),
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
                    .size(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { fullscreenVideoUri = uri } // Abre el diálogo de video
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = stringResource(R.string.video),
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (showRemoveButtons) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.delete),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .clickable { onRemoveVideo(uri) }
                            .size(24.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Mostrar audios
        items(audioUris.size) { index ->
            val uriString = audioUris[index]
            val uri = Uri.parse(uriString)
            val realPath = uri.getRealPath(LocalContext.current)
            val audioFile = File(realPath)
            ReproducirAudioScreen(audioFile)
        }
    }

    // Diálogo de imagen en pantalla completa
    fullscreenImageUri?.let { uri ->
        FullscreenImageDialog(uri = uri, onDismiss = { fullscreenImageUri = null })
    }

    // Diálogo de video en pantalla completa
    fullscreenVideoUri?.let { uri ->
        FullscreenVideoDialog(uri = uri, onDismiss = { fullscreenVideoUri = null })
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
fun AudioCard(
    uri: String,
    onRemoveAudio: (String) -> Unit = {},
    showRemoveButtons: Boolean = false
) {
    val context = LocalContext.current
    val player = remember { AndroidAudioPlayer(context) }
    var isPlaying by remember { mutableStateOf(false) }

    // Convertir URI a path utilizando la función `getRealPath`
    val filePath = Uri.parse(uri).getRealPath(context)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(id = R.dimen.padding_small)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = {
            if (isPlaying) {
                player.stop()
                isPlaying = false
            } else {
                if (filePath != null) {
                    try {
                        player.start(File(filePath))
                        isPlaying = true
                    } catch (e: Exception) {
                        Log.e("AudioCard", "Error starting playback: ${e.message}")
                    }
                } else {
                    Log.e("AudioCard", "Invalid file path for URI: $uri")
                }
            }
        }) {
            Text(if (isPlaying) "Stop" else "Play")
        }
        Text(
            text = uri.split("/").lastOrNull() ?: "Audio File",
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )

        if (showRemoveButtons) {
            IconButton(onClick = { onRemoveAudio(uri) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}





@Composable
fun TaskDetails(
    task: Task, modifier: Modifier = Modifier
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
            TaskDetailsRow(
                labelResID = R.string.task_title,
                taskDetail = task.titulo,
                modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_medium))
            )
            TaskDetailsRow(
                labelResID = R.string.task_description,
                taskDetail = task.descripcion,
                modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_medium))
            )
            // Si task tiene fecha de vencimiento, mostrar la fila
            if (task.fechaHoraVencimiento != null) {
                TaskDetailsRow(
                    labelResID = R.string.task_due_time,
                    taskDetail = task.fechaHoraVencimiento.toString(),
                    modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_medium))
                )
            }
            TaskDetailsRow(
                labelResID = R.string.task_status,
                taskDetail = if (task.estado) "Cumplida" else "Pendiente",
                modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_medium))
            )
        }
    }
}


@Composable
private fun TaskDetailsRow(
    @StringRes labelResID: Int, taskDetail: String, modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Text(text = stringResource(labelResID))
        Spacer(modifier = Modifier.weight(1f))
        Text(text = taskDetail, fontWeight = FontWeight.Bold)
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

@Preview(showBackground = true)
@Composable
fun TaskDetailsScreenPreview() {
    InventoryTheme {
        TaskDetailsBody(
            taskDetailsUiState = TaskDetailsUiState(
                isCompleted = false,
                taskDetails = TaskDetailsInfo(
                    id = 1,
                    titulo = "Ejemplo Titulo",
                    descripcion = "Ejemplo Descripción",
                    fechaHoraVencimiento = null,
                    estado = false
                )
            ),
            onCompleteTask = {},
            onDelete = {},
            viewModel = viewModel()
        )
    }
}
