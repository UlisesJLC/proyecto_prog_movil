package com.example.inventory.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity data class represents a single row in the "tasks" table in the database.
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val descripcion: String,
    val fechaHoraVencimiento: String, // Guarda la fecha y hora de vencimiento de la tarea como String
    val estado: Boolean = false, // true si la tarea está cumplida, false si no


    //Estos son los agregados recientemente
    val horaCumplimiento: Long? = null, // Guarda la fecha y hora de la tarea en milisegundos, si es tarea
    val videoUri: String? = null, // Para imagen o video
    val fotoUri: String? = null, // Para imagen o video
    val audioUri: String? = null  // Para audio
)

@Entity
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val noteId: Int, // Relación con una nota
    val alarmTime: Long, // Hora de la alarma en timestamp
    val title: String, // Título o descripción
    val isActive: Boolean = true // Estado de la alarma
)
