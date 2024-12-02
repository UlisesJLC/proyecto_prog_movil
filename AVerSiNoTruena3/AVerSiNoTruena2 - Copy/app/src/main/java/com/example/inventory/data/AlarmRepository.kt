package com.example.inventory.data

import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    fun getAlarmsByTaskId(taskId: Int): Flow<List<Alarm>>
    fun getAlarmStream(id: Int): Flow<Alarm?>
    suspend fun insertAlarm(alarm: Alarm):Long
    suspend fun updateAlarm(alarm: Alarm)
    suspend fun deleteAlarm(alarm: Alarm)
    suspend fun deleteAlarmsByTaskId(taskId: Int)
}

