package com.example.inventory.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventory.InventoryTopAppBar
import com.example.inventory.R
import com.example.inventory.data.Item
import com.example.inventory.data.Task
import com.example.inventory.ui.AppViewModelProvider
import com.example.inventory.ui.navigation.NavigationDestination
import com.example.inventory.ui.theme.InventoryTheme

object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.app_name
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToItemEntry: () -> Unit,
    navigateToTaskEntry: () -> Unit,
    navigateToItemUpdate: (Int) -> Unit,
    navigateToTaskUpdate: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val homeUiState by viewModel.homeUiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            InventoryTopAppBar(
                title = stringResource(HomeDestination.titleRes),
                canNavigateBack = false,
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            Column {
                FloatingActionButton(
                    onClick = navigateToItemEntry,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.item_entry_title)
                    )
                }
                FloatingActionButton(
                    onClick = navigateToTaskEntry,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.task_entry_title)
                    )
                }
            }
        }
    ) { innerPadding ->
        HomeBody(
            itemList = homeUiState.itemList,
            taskList = homeUiState.taskList,
            onItemClick = navigateToItemUpdate,
            onTaskClick = navigateToTaskUpdate,
            contentPadding = innerPadding,
            modifier = modifier.fillMaxSize()
        )
    }
}

@Composable
private fun HomeBody(
    itemList: List<Item>,
    taskList: List<Task>,
    onItemClick: (Int) -> Unit,
    onTaskClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        if (itemList.isEmpty() && taskList.isEmpty()) {
            Text(
                text = stringResource(R.string.no_item_description),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(contentPadding)
            )
        } else {
            Text("Items", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp))
            InventoryList(
                itemList = itemList,
                onItemClick = onItemClick,
                contentPadding = contentPadding,
                modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_small))
            )

            Text("Tasks", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp))
            TaskList(
                taskList = taskList,
                onTaskClick = onTaskClick,
                contentPadding = contentPadding,
                modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_small))
            )
        }
    }
}

@Composable
private fun InventoryList(
    itemList: List<Item>,
    onItemClick: (Int) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        items(items = itemList, key = { it.id }) { item ->
            InventoryItem(
                item = item,
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.padding_small))
                    .clickable { onItemClick(item.id) }
            )
        }
    }
}

@Composable
private fun TaskList(
    taskList: List<Task>,
    onTaskClick: (Int) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        items(items = taskList, key = { it.id }) { task ->
            TaskItem(
                task = task,
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.padding_small))
                    .clickable { onTaskClick(task.id) }
            )
        }
    }
}

@Composable
private fun InventoryItem(
    item: Item, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = item.titulo, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                Text(
                    text = if (item.estado) "Cumplida" else "Pendiente",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(text = item.descripcion, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun TaskItem(
    task: Task, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = task.titulo, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                Text(
                    text = if (task.estado) "Cumplida" else "Pendiente",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(text = task.descripcion, style = MaterialTheme.typography.bodyMedium)
            Text(text = "Fecha de vencimiento: ${task.fechaHoraVencimiento}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

/*
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val sampleItems = listOf(
        Item(id = 1, titulo = "Item 1", descripcion = "Descripción del item 1", estado = false, clasificacion = "si"),
        Item(id = 2, titulo = "Item 2", descripcion = "Descripción del item 2", estado = true, clasificacion = "si")
    )

    val sampleTasks = listOf(
        Task(id = 1, titulo = "Task 1", descripcion = "Descripción de la tarea 1", fechaHoraVencimiento = "2024-12-01 14:00", estado = false),
        Task(id = 2, titulo = "Task 2", descripcion = "Descripción de la tarea 2", fechaHoraVencimiento = "2024-12-05 09:00", estado = true)
    )

    InventoryTheme {
        HomeBody(
            itemList = sampleItems,
            taskList = sampleTasks,
            onItemClick = {},
            onTaskClick = {},
            contentPadding = PaddingValues(16.dp)
        )
    }
}
*/