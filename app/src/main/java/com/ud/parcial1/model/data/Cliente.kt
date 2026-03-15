package com.ud.parcial1.model.data
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cliente")
data class Cliente(
    val nombre: String,
    val telefono: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)
