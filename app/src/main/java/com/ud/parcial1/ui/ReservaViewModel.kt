package com.ud.parcial1.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

    // listas para la UI
    private val _reservas = MutableStateFlow<List<ReservaWithDetails>>(emptyList())
    val reservas: StateFlow<List<ReservaWithDetails>> = _reservas

    private val _clientes = MutableStateFlow<List<Cliente>>(emptyList())
    val clientes: StateFlow<List<Cliente>> = _clientes

    // estados de carga y mensajes
    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError

    private val _mensajeExito = MutableStateFlow<String?>(null)
    val mensajeExito: StateFlow<String?> = _mensajeExito

    // seleccion para el detalle (opcional)
    private val _reservaSeleccionada = MutableStateFlow<ReservaWithDetails?>(null)
    val reservaSeleccionada: StateFlow<ReservaWithDetails?> = _reservaSeleccionada

    init {
        cargarDatosIniciales()
    }

    // ==================== FUNCIONES DE CARGA ====================

    fun cargarDatosIniciales() {
        viewModelScope.launch {
            _cargando.value = true
            try {
                _clientes.value = clienteDao.getAllClientes()
                cargarReservas()
            } catch (e: Exception) {
                _mensajeError.value = "error al cargar datos: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun cargarReservas() {
        viewModelScope.launch {
            try {
                _reservas.value = reservaDao.getAllReservasWithDetails()
            } catch (e: Exception) {
                _mensajeError.value = "error al cargar reservas: ${e.message}"
            }
        }
    }

    // ==================== FUNCIONES DE BUSQUEDA ====================

    fun buscarPorNombre(nombre: String) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                _reservas.value = if (nombre.isBlank()) {
                    reservaDao.getAllReservasWithDetails()
                } else {
                    reservaDao.getReservasByClienteNombre(nombre)
                }
            } catch (e: Exception) {
                _mensajeError.value = "error en la busqueda: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    // ==================== FUNCIONES PARA DETALLE ====================

    fun seleccionarReserva(reserva: ReservaWithDetails?) {
        _reservaSeleccionada.value = reserva
    }

    fun limpiarSeleccion() {
        _reservaSeleccionada.value = null
    }

    // ==================== FUNCIONES PARA CREAR ====================

    fun crearReserva(
        idCliente: Int,
        fecha: String,
        hora: String,
        numeroPista: Int,
        cantidadJugadores: Int
    ) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                // validamos disponibilidad (regla de negocio #1)
                val pistaOcupada = reservaDao.isPistaOcupada(
                    numeroPista, fecha, hora
                ) > 0

                if (pistaOcupada) {
                    _mensajeError.value = "la pista $numeroPista ya esta reservada para esa fecha y hora"
                    return@launch
                }

                // creamos con estado activo (id 3 segun tu precarga)
                val nuevaReserva = Reserva(
                    idCliente = idCliente,
                    idEstado = 3, // activa
                    fecha = fecha,
                    hora = hora,
                    numeroPista = numeroPista,
                    cantidadJugadores = cantidadJugadores
                )

                reservaDao.insert(nuevaReserva)
                _mensajeExito.value = "reserva creada exitosamente"
                cargarReservas() // recargamos la lista

            } catch (e: Exception) {
                _mensajeError.value = "error al crear reserva: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    // ==================== FUNCIONES PARA EDITAR ====================

    fun actualizarReserva(
        id: Int,
        idCliente: Int,
        idEstado: Int,
        fecha: String,
        hora: String,
        numeroPista: Int,
        cantidadJugadores: Int
    ) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val reservaActualizada = Reserva(
                    id = id,
                    idCliente = idCliente,
                    idEstado = idEstado,
                    fecha = fecha,
                    hora = hora,
                    numeroPista = numeroPista,
                    cantidadJugadores = cantidadJugadores
                )

                reservaDao.update(reservaActualizada)
                _mensajeExito.value = "reserva actualizada exitosamente"
                cargarReservas() // recargamos la lista

                // si la reserva que editamos era la seleccionada, actualizamos el detalle
                if (_reservaSeleccionada.value?.reserva?.id == id) {
                    // recargamos la reserva seleccionada
                    _reservaSeleccionada.value = reservas.value.find { it.reserva.id == id }
                }

            } catch (e: Exception) {
                _mensajeError.value = "error al actualizar reserva: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    // ==================== FUNCIONES PARA ELIMINAR ====================

    fun eliminarReserva(reserva: Reserva) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                reservaDao.delete(reserva)
                _mensajeExito.value = "reserva eliminada exitosamente"

                // si la reserva eliminada era la seleccionada, limpiamos seleccion
                if (_reservaSeleccionada.value?.reserva?.id == reserva.id) {
                    _reservaSeleccionada.value = null
                }

                cargarReservas() // recargamos la lista

            } catch (e: Exception) {
                _mensajeError.value = "error al eliminar reserva: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    // ==================== FUNCIONES DE UTILIDAD ====================

    fun limpiarMensajes() {
        _mensajeError.value = null
        _mensajeExito.value = null
    }

    // para obtener un cliente por su id (util en formularios)
    fun getClienteById(id: Int): Cliente? {
        return _clientes.value.find { it.id == id }
    }

    //Funcion para agregar cliente

    fun crearCliente(nombre: String, telefono: String) {
        viewModelScope.launch {
            try {
                val nuevoCliente = Cliente(
                    nombre = nombre,
                    telefono = telefono
                )
                clienteDao.insert(nuevoCliente)
                // recargar la lista de clientes
                _clientes.value = clienteDao.getAllClientes()
            } catch (e: Exception) {
                _mensajeError.value = "error al crear cliente: ${e.message}"
            }
        }
    }

    // ==================== FACTORY ====================

    class Factory(
        private val reservaDao: ReservaDao,
        private val clienteDao: ClienteDao
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReservaViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ReservaViewModel(reservaDao, clienteDao) as T
            }
            throw IllegalArgumentException("viewmodel desconocido")
        }
    }
}

