package com.example.inventory.data
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "alarms",
    foreignKeys = [ForeignKey(
        entity = Task::class,
        parentColumns = ["id"],
        childColumns = ["taskId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val taskId: Int, // Relación con la tarea
    val fechaHora: String, // Fecha y hora de la alarma como String
    val tipo: String, // Opcional: especifica el propósito de la alarma (recordatorio, vencimiento, etc.)
    val estado: Boolean = false // Indica si la alarma está activa
)
