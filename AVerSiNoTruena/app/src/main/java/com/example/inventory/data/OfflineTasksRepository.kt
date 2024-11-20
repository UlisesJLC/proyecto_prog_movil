package com.example.inventory.data

import kotlinx.coroutines.flow.Flow

class OfflineTasksRepository(private val taskDao: TaskDao) : TasksRepository {
    override fun getAllTasksStream(): Flow<List<Task>> = taskDao.getAllTasks()

    override fun getTaskStream(id: Int): Flow<Task?> = taskDao.getTask(id)

    override fun getTasksByEstado(estado: Boolean): Flow<List<Task>> = taskDao.getTasksByEstado(estado)

    override fun getTasksWithVideo(): Flow<List<Task>> = taskDao.getTasksWithVideo()

    override fun getTasksWithPhoto(): Flow<List<Task>> = taskDao.getTasksWithPhoto()

    override fun getTasksWithAudio(): Flow<List<Task>> = taskDao.getTasksWithAudio()

    override suspend fun insertTask(task: Task) = taskDao.insert(task)

    override suspend fun updateTask(task: Task) = taskDao.update(task)

    override suspend fun deleteTask(task: Task) = taskDao.delete(task)
}
