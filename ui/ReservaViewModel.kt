package com.ud.parcial1.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ud.parcial1.model.dao.ClienteDao
import com.ud.parcial1.model.dao.ReservaDao
import com.ud.parcial1.model.data.Cliente
import com.ud.parcial1.model.data.Reserva
import com.ud.parcial1.model.data.ReservaWithDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReservaViewModel(
    private val reservaDao: ReservaDao,
    private val clienteDao: ClienteDao
) : ViewModel() {

    private val _reservas = MutableStateFlow<List<ReservaWithDetails>>(emptyList())
    val reservas: StateFlow<List<ReservaWithDetails>> = _reservas

    init {
        cargarReservas()
    }

    fun cargarReservas() {
        viewModelScope.launch {
            _reservas.value = reservaDao.getAllReservasWithDetails()
        }
    }

    fun buscarPorNombre(nombre: String) {
        viewModelScope.launch {
            if (nombre.isBlank()) {
                cargarReservas()
            } else {
                _reservas.value = reservaDao.getReservasByClienteNombre(nombre)
            }
        }
    }

    suspend fun crearReserva(
        nombreCliente: String,
        telefono: String,
        pista: Int,
        fecha: String,
        hora: String
    ): String {
        // 1. Validar si la pista está ocupada
        val ocupada = reservaDao.isPistaOcupada(pista, fecha, hora)
        if (ocupada > 0) return "Error: La pista ya está reservada en ese horario."

        // 2. Crear o buscar el cliente
        // (Para simplificar, creamos uno nuevo, pero en una app real buscarías si ya existe)
        val nuevoCliente = Cliente(nombre = nombreCliente, telefono = telefono)
        clienteDao.insert(nuevoCliente)
        
        // Obtenemos el cliente recién creado (o el último con ese nombre)
        val clienteGuardado = clienteDao.getAllClientes().first { it.nombre == nombreCliente }

        // 3. Insertar la reserva (ID_ESTADO 3 = Activa)
        val reserva = Reserva(
            idCliente = clienteGuardado.id,
            idEstado = 3, 
            fecha = fecha,
            hora = hora,
            numeroPista = pista,
            cantidadJugadores = 2 // Valor por defecto
        )
        reservaDao.insert(reserva)
        cargarReservas()
        return "Reserva creada con éxito"
    }

    fun eliminarReserva(reserva: Reserva) {
        viewModelScope.launch {
            reservaDao.delete(reserva)
            cargarReservas()
        }
    }
}
