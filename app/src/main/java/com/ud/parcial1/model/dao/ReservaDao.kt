package com.ud.parcial1.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ud.parcial1.model.data.Reserva
import com.ud.parcial1.model.data.ReservaWithDetails

@Dao
interface ReservaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reserva: Reserva)

    @Update
    suspend fun update(reserva: Reserva)

    @Delete
    suspend fun delete(reserva: Reserva)

    // Regla 1: Validar si la pista está ocupada (Estado 3 = Activa)
    @Query("""
        SELECT COUNT(*) FROM reserva 
        WHERE numeroPista = :pista 
        AND fecha = :fecha 
        AND hora = :hora 
        AND id_estado = 3
    """)
    suspend fun isPistaOcupada(pista: Int, fecha: String, hora: String): Int

    // Regla 2: Buscar reservas por nombre del cliente
    @Transaction
    @Query("""
        SELECT reserva.* FROM reserva 
        INNER JOIN cliente ON reserva.id_cliente = cliente.id 
        WHERE cliente.nombre LIKE '%' || :nombre || '%'
    """)
    suspend fun getReservasByClienteNombre(nombre: String): List<ReservaWithDetails>

    // Regla 3: Listado completo con detalles (Nombre de cliente y Estado)
    @Transaction
    @Query("SELECT * FROM reserva ORDER BY fecha ASC, hora ASC")
    suspend fun getAllReservasWithDetails(): List<ReservaWithDetails>
}
