package com.example.inventory.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Database access object to access the Task table in the database
 */
@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTask(id: Int): Flow<Task?>

    @Query("SELECT * FROM tasks WHERE estado = :estado ORDER BY fechaHoraVencimiento")
    fun getTasksByEstado(estado: Boolean): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE videoUri IS NOT NULL")
    fun getTasksWithVideo(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE fotoUri IS NOT NULL")
    fun getTasksWithPhoto(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE audioUri IS NOT NULL")
    fun getTasksWithAudio(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)
}
