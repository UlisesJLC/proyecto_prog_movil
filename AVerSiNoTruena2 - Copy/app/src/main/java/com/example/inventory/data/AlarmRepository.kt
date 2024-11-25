package com.example.inventory.data

import kotlinx.coroutines.flow.Flow

interface AlarmsRepository {
    fun getAlarmsByTaskId(taskId: Int): Flow<List<Alarm>>
    fun getAlarmStream(id: Int): Flow<Alarm?>
    suspend fun insertAlarm(alarm: Alarm)
    suspend fun updateAlarm(alarm: Alarm)
    suspend fun deleteAlarm(alarm: Alarm)
    suspend fun deleteAlarmsByTaskId(taskId: Int)
}

class OfflineAlarmsRepository(
    private val alarmDao: AlarmDao
) : AlarmsRepository {
    override fun getAlarmsByTaskId(taskId: Int): Flow<List<Alarm>> = alarmDao.getAlarmsByTaskId(taskId)
    override fun getAlarmStream(id: Int): Flow<Alarm?> = alarmDao.getAlarm(id)
    override suspend fun insertAlarm(alarm: Alarm) = alarmDao.insert(alarm)
    override suspend fun updateAlarm(alarm: Alarm) = alarmDao.update(alarm)
    override suspend fun deleteAlarm(alarm: Alarm) = alarmDao.delete(alarm)
    override suspend fun deleteAlarmsByTaskId(taskId: Int) = alarmDao.deleteAlarmsByTaskId(taskId)
}
