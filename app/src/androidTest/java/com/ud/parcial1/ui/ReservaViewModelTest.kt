package com.ud.parcial1.ui

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ud.parcial1.model.dao.ClienteDao
import com.ud.parcial1.model.dao.EstadoDao
import com.ud.parcial1.model.dao.ReservaDao
import com.ud.parcial1.model.data.AppDatabase
import com.ud.parcial1.model.data.Cliente
import com.ud.parcial1.model.data.Estado
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ReservaViewModelTest {

    private lateinit var db: AppDatabase
    private lateinit var reservaDao: ReservaDao
    private lateinit var clienteDao: ClienteDao
    private lateinit var estadoDao: EstadoDao
    private lateinit var viewModel: ReservaViewModel

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        reservaDao = db.reservaDao()
        clienteDao = db.clienteDao()
        estadoDao = db.estadoDao()
        viewModel = ReservaViewModel(reservaDao, clienteDao)
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
    }

    @Test
    fun testCrearReservaExitosa() = runBlocking {
        estadoDao.insertAll(listOf(Estado(id = 3, descripcion = "Activa")))
        clienteDao.insert(Cliente(id = 1, nombre = "Test", telefono = "123"))

        viewModel.crearReserva(1, "2023-10-10", "10:00 AM", 1, 2)
        
        // ESPERAR a que la corrutina termine 
        delay(500) 

        val lista = viewModel.reservas.value // .value para ver el estado actual
        assertEquals("La reserva debería haberse creado", 1, lista.size)
        assertEquals("El nombre del cliente debe coincidir", "Test", lista[0].cliente.nombre)
        assertNotNull("Debería haber un mensaje de éxito", viewModel.mensajeExito.value)
    }

    @Test
    fun testCrearReservaFallidaPistaOcupada() = runBlocking {
        estadoDao.insertAll(listOf(Estado(id = 3, descripcion = "Activa")))
        clienteDao.insert(Cliente(id = 1, nombre = "C1", telefono = "1"))
        
        viewModel.crearReserva(1, "2023-10-10", "10:00 AM", 1, 2)
        delay(500)
        
        viewModel.crearReserva(1, "2023-10-10", "10:00 AM", 1, 2)
        delay(500)

        assertNotNull("Debería haber un mensaje de error", viewModel.mensajeError.value)
        assertEquals("Solo debería existir una reserva", 1, viewModel.reservas.value.size)
    }

    @Test
    fun testEditarReserva() = runBlocking {
        estadoDao.insertAll(listOf(
            Estado(id = 3, descripcion = "Activa"),
            Estado(id = 2, descripcion = "Finalizada")
        ))
        clienteDao.insert(Cliente(id = 1, nombre = "Usuario Original", telefono = "000"))
        
        viewModel.crearReserva(1, "20/10/2023", "08:00 AM", 1, 2)
        delay(500)
        
        val reservaOriginal = viewModel.reservas.value[0].reserva
        
        // pista de 1 a 2 y estado a finalizada
        viewModel.actualizarReserva(
            id = reservaOriginal.id,
            idCliente = 1,
            idEstado = 2,
            fecha = "20/10/2023",
            hora = "08:00 AM",
            numeroPista = 2,
            cantidadJugadores = 4
        )
        delay(500)
        
        val listaEditada = viewModel.reservas.value
        assertEquals("Debe seguir habiendo 1 reserva", 1, listaEditada.size)
        assertEquals("La pista debe haberse actualizado a 2", 2, listaEditada[0].reserva.numeroPista)
        assertEquals("El estado debe ser Finalizada (ID 2)", 2, listaEditada[0].reserva.idEstado)
        assertEquals("Los jugadores deben ser 4", 4, listaEditada[0].reserva.cantidadJugadores)
        assertNotNull("Debería haber un mensaje de éxito al editar", viewModel.mensajeExito.value)
    }

    @Test
    fun testEliminarReserva() = runBlocking {
        estadoDao.insertAll(listOf(Estado(id = 3, descripcion = "Activa")))
        clienteDao.insert(Cliente(id = 1, nombre = "C1", telefono = "1"))
        
        viewModel.crearReserva(1, "2023-11-11", "11:00 AM", 2, 2)
        delay(500)
        
        val reservaGuardada = viewModel.reservas.value[0].reserva
        
        viewModel.eliminarReserva(reservaGuardada)
        delay(500)
        
        assertEquals("La lista debería estar vacía", 0, viewModel.reservas.value.size)
    }
}
