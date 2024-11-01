package com.example.inventory.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Database access object to access the Task table in the database
 */
@Dao
interface TaskDao {

    @Query("SELECT * from tasks")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * from tasks WHERE id = :id")
    fun getTask(id: Int): Flow<Task>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)
}
