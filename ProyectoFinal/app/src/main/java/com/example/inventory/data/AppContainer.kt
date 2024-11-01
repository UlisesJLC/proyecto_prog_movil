package com.example.inventory.data

import android.content.Context

/**
 * Dependency container at the application level.
 */
interface AppContainer {
    val itemsRepository: ItemsRepository
    val tasksRepository: TasksRepository
}

/**
 * [AppContainer] implementation that provides instance of [ItemsRepository] and [TaskRepository]
 */
class AppDataContainer(private val context: Context) : AppContainer {

    private val database: InventoryDatabase by lazy {
        InventoryDatabase.getDatabase(context)
    }

    override val itemsRepository: ItemsRepository by lazy {
        OfflineItemsRepository(database.itemDao())
    }

    override val tasksRepository: TasksRepository by lazy {
        OfflineTasksRepository(database.taskDao())
    }
}
