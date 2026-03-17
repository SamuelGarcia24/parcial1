package com.ud.parcial1.model.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ud.parcial1.model.data.AppDatabase
import com.ud.parcial1.model.data.Cliente
import com.ud.parcial1.model.data.Estado
import com.ud.parcial1.model.data.Reserva
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ReservaDaoTest {

    private lateinit var reservaDao: ReservaDao
    private lateinit var clienteDao: ClienteDao
    private lateinit var estadoDao: EstadoDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // bd en memoria para pruebas
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        reservaDao = db.reservaDao()
        clienteDao = db.clienteDao()
        estadoDao = db.estadoDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun testPistaOcupadaRule() = runBlocking {
        val cliente = Cliente(id = 1, nombre = "Carlos", telefono = "555")
        clienteDao.insert(cliente)

        val estadoActivo = Estado(id = 3, descripcion = "Activa")
        estadoDao.insertAll(listOf(estadoActivo))

        // reserva activa para la pista 5
        val fecha = "20/10/2023"
        val hora = "10:00 AM - 12:00 PM"
        val reserva = Reserva(
            idCliente = 1,
            idEstado = 3,
            fecha = fecha,
            hora = hora,
            numeroPista = 5,
            cantidadJugadores = 2
        )
        reservaDao.insert(reserva)

        // validar regla: misma pista fecha y hora -> OCUPADA
        val resultadoOcupado = reservaDao.isPistaOcupada(5, fecha, hora)
        assertEquals("La pista debería estar marcada como ocupada", 1, resultadoOcupado)

        // validar regla: misma pista, diferente hora ->  LIBRE
        val resultadoLibreHora = reservaDao.isPistaOcupada(5, fecha, "02:00 PM - 04:00 PM")
        assertEquals("La pista debería estar libre en otro horario", 0, resultadoLibreHora)

        // validar regla: diferente pista, misma hora -> LIBRE
        val resultadoLibrePista = reservaDao.isPistaOcupada(1, fecha, hora)
        assertEquals("Otra pista debería estar libre a la misma hora", 0, resultadoLibrePista)
    }

    @Test
    fun testBuscarPorNombreCliente() = runBlocking {
        val cliente1 = Cliente(id = 1, nombre = "Andres Lopez", telefono = "111")
        val cliente2 = Cliente(id = 2, nombre = "Beatriz Sosa", telefono = "222")
        clienteDao.insert(cliente1)
        clienteDao.insert(cliente2)

        val estado = Estado(id = 3, descripcion = "Activa")
        estadoDao.insertAll(listOf(estado))

        reservaDao.insert(Reserva(idCliente = 1, idEstado = 3, fecha = "01/01", hora = "10", numeroPista = 1, cantidadJugadores = 1))
        reservaDao.insert(Reserva(idCliente = 2, idEstado = 3, fecha = "01/01", hora = "12", numeroPista = 2, cantidadJugadores = 1))

        // Buscar por el primer nombre  "Andres"
        val resultados = reservaDao.getReservasByClienteNombre("Andres")
        
        assertEquals(1, resultados.size)
        assertEquals("Andres Lopez", resultados[0].cliente.nombre)
    }
}
