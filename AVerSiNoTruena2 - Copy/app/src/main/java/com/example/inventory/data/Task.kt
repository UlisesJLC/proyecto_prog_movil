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
    val esTask: Boolean? = null, // Guarda la fecha y hora de la tarea en milisegundos, si es tarea
    val horaCumplimiento: Long? = null, // Guarda la fecha y hora de la tarea en milisegundos, si es tarea


    // Almacenar múltiples URIs como listas en JSON
    val videoUris: List<String> = emptyList(),
    val fotoUris: List<String> = emptyList(),
    val audioUris: List<String> = emptyList()
)

