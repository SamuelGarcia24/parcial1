package com.ud.parcial1.model.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ud.parcial1.model.dao.ClienteDao
import com.ud.parcial1.model.dao.EstadoDao
import com.ud.parcial1.model.dao.ReservaDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Cliente::class, Reserva::class, Estado::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun clienteDao(): ClienteDao
    abstract fun reservaDao(): ReservaDao
    abstract fun estadoDao(): EstadoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "parcial1_database"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.estadoDao())
                }
            }
        }

        suspend fun populateDatabase(estadoDao: EstadoDao) {
            val estados = listOf(
                Estado(descripcion = "Cancelada"),
                Estado(descripcion = "Finalizada"),
                Estado(descripcion = "Activa")
            )
            estadoDao.insertAll(estados)
        }
    }
}
