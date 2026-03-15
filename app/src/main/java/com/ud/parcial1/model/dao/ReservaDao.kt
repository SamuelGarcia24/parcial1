package com.ud.parcial1.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ud.parcial1.model.data.Reserva

@Dao
interface ReservaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reserva: Reserva)

    @Update
    suspend fun update(reserva: Reserva)

    @Delete
    suspend fun delete(reserva: Reserva)

    @Query("SELECT * FROM reserva WHERE id = :id")
    suspend fun getReservaById(id: Int): Reserva?

    @Query("SELECT * FROM reserva ORDER BY fecha ASC, hora ASC")
    suspend fun getAllReservas(): List<Reserva>

    @Query("SELECT * FROM reserva WHERE id_cliente = :idCliente")
    suspend fun getReservasByCliente(idCliente: Int): List<Reserva>
}
