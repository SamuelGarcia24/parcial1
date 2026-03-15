package com.ud.parcial1.model.data

import androidx.room.Embedded
import androidx.room.Relation

data class ReservaWithDetails(
    @Embedded val reserva: Reserva,
    
    @Relation(
        parentColumn = "id_cliente",
        entityColumn = "id"
    )
    val cliente: Cliente,

    @Relation(
        parentColumn = "id_estado",
        entityColumn = "id"
    )
    val estado: Estado
)
