package com.example.inventory.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow



@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms WHERE taskId = :taskId")
    fun getAlarmsByTaskId(taskId: Int): Flow<List<Alarm>>

    @Query("SELECT * FROM alarms WHERE id = :id")
    fun getAlarm(id: Int): Flow<Alarm?>

    @Update
    suspend fun update(alarm: Alarm)


    @Query("DELETE FROM alarms WHERE taskId = :taskId")
    suspend fun deleteAlarmsByTaskId(taskId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarm: Alarm): Long

    @Delete
    suspend fun delete(alarm: Alarm)

    @Query("SELECT * FROM alarms WHERE id = :id")
    fun getAlarmById(id: Int): Flow<Alarm>

}


