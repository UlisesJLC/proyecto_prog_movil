package com.example.inventory.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.flow.Flow

class OfflineTasksRepository(
    private val taskDao: TaskDao,
    private val context: Context // Aquí agregas el contexto
) : TasksRepository {


    override fun getAllTasksStream(): Flow<List<Task>> = taskDao.getAllTasks()

    override fun getTaskStream(id: Int): Flow<Task?> = taskDao.getTask(id)

    override fun getTasksByEstado(estado: Boolean): Flow<List<Task>> = taskDao.getTasksByEstado(estado)

    override fun getTasksWithVideo(): Flow<List<Task>> = taskDao.getTasksWithVideo()

    override fun getTasksWithPhoto(): Flow<List<Task>> = taskDao.getTasksWithPhoto()

    override fun getTasksWithAudio(): Flow<List<Task>> = taskDao.getTasksWithAudio()

    override suspend fun insertTask(task: Task): Long = taskDao.insert(task) // Devuelve el ID generado

    override suspend fun updateTask(task: Task) = taskDao.update(task)

    override suspend fun deleteTask(task: Task) = taskDao.delete(task)




}
