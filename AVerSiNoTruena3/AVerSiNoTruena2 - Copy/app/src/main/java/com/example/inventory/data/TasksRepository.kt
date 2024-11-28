package com.example.inventory.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides insert, update, delete, and retrieve of [Task] from a given data source.
 */
interface TasksRepository {
    fun getAllTasksStream(): Flow<List<Task>>
    fun getTaskStream(id: Int): Flow<Task?>
    fun getTasksByEstado(estado: Boolean): Flow<List<Task>>
    fun getTasksWithVideo(): Flow<List<Task>>
    fun getTasksWithPhoto(): Flow<List<Task>>
    fun getTasksWithAudio(): Flow<List<Task>>

    suspend fun insertTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)

}

