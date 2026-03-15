package com.ud.parcial1.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ud.parcial1.model.data.Estado

@Dao
interface EstadoDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(estados: List<Estado>)

    @Query("SELECT * FROM estado")
    suspend fun getAllEstados(): List<Estado>
}
