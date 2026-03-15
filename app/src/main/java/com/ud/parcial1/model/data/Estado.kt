package com.ud.parcial1.model.data
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "estado")
data class Estado(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val descripcion: String
)
