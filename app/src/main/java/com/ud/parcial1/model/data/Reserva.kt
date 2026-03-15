package com.ud.parcial1.model.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reserva",
    foreignKeys = [
        ForeignKey(
            entity = Cliente::class,
            parentColumns = ["id"],
            childColumns = ["id_cliente"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Estado::class,
            parentColumns = ["id"],
            childColumns = ["id_estado"],
            onDelete = ForeignKey.RESTRICT // No permite borrar un estado si hay reservas asociadas
        )
    ],
    // llave compuesta
    indices = [
        Index(value = ["id", "id_cliente"], unique = true),
        Index(value = ["id_estado"])
    ]
)
data class Reserva(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "id_cliente")
    val idCliente: Int,

    @ColumnInfo(name = "id_estado")
    val idEstado: Int,

    val fecha: String,
    val hora: String,
    val numeroPista: Int,
    val cantidadJugadores: Int
)
