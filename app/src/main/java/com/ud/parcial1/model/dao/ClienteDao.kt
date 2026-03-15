package com.ud.parcial1.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ud.parcial1.model.data.Cliente

@Dao
interface ClienteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cliente: Cliente)

    @Update
    suspend fun update(cliente: Cliente)

    @Delete
    suspend fun delete(cliente: Cliente)

    @Query("SELECT * FROM cliente WHERE id = :id")
    suspend fun getClienteById(id: Int): Cliente?

    @Query("SELECT * FROM cliente ORDER BY nombre ASC")
    suspend fun getAllClientes(): List<Cliente>
}
