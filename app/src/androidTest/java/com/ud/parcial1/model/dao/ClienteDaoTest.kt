package com.ud.parcial1.model.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ud.parcial1.model.data.AppDatabase
import com.ud.parcial1.model.data.Cliente
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ClienteDaoTest {

    private lateinit var clienteDao: ClienteDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        clienteDao = db.clienteDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetCliente() = runBlocking {
        val cliente = Cliente(nombre = "Juan Perez", telefono = "123456789")
        clienteDao.insert(cliente)
        
        val todosLosClientes = clienteDao.getAllClientes()
        
        assertNotNull(todosLosClientes)
        assertEquals(1, todosLosClientes.size)
        assertEquals("Juan Perez", todosLosClientes[0].nombre)
    }

    @Test
    fun deleteCliente() = runBlocking {
        val cliente = Cliente(id = 1, nombre = "Maria", telefono = "987")
        clienteDao.insert(cliente)
        clienteDao.delete(cliente)
        
        val lista = clienteDao.getAllClientes()
        assertEquals(0, lista.size)
    }
}
