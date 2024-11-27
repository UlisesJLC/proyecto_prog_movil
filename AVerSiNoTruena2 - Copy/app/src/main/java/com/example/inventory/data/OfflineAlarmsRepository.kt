package com.example.inventory.data

import kotlinx.coroutines.flow.Flow

class OfflineAlarmsRepository(
    private val alarmDao: AlarmDao
) : AlarmRepository {
    override fun getAlarmsByTaskId(taskId: Int): Flow<List<Alarm>> = alarmDao.getAlarmsByTaskId(taskId)
    override fun getAlarmStream(id: Int): Flow<Alarm?> = alarmDao.getAlarm(id)
    override suspend fun insertAlarm(alarm: Alarm) = alarmDao.insert(alarm)
    override suspend fun updateAlarm(alarm: Alarm) = alarmDao.update(alarm)
    override suspend fun deleteAlarm(alarm: Alarm) = alarmDao.delete(alarm)
    override suspend fun deleteAlarmsByTaskId(taskId: Int) = alarmDao.deleteAlarmsByTaskId(taskId)
}