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

// este viewModel conecta la interfaz con la base de datos
class ReservaViewModel(
    private val reservaDao: ReservaDao,   // para hablar con la tabla reservas
    private val clienteDao: ClienteDao    // para hablar con la tabla clientes
) : ViewModel() {

    // lista de reservas que se muestra en pantalla
    private val _reservas = MutableStateFlow<List<ReservaWithDetails>>(emptyList())
    val reservas: StateFlow<List<ReservaWithDetails>> = _reservas

    // lista de clientes para llenar el combo box del formulario
    private val _clientes = MutableStateFlow<List<Cliente>>(emptyList())
    val clientes: StateFlow<List<Cliente>> = _clientes

    // bandera para mostrar circulito de carga
    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    // mensajes para el usuario
    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError

    private val _mensajeExito = MutableStateFlow<String?>(null)
    val mensajeExito: StateFlow<String?> = _mensajeExito

    // cuando se crea el viewModel, cargamos los datos iniciales
    init {
        cargarDatosIniciales()
    }

    // trae los clientes y las reservas
    fun cargarDatosIniciales() {
        viewModelScope.launch {
            _cargando.value = true
            try {
                // primero traemos los clientes para el formulario
                _clientes.value = clienteDao.getAllClientes()
                // luego traemos las reservas para la lista
                cargarReservas()
            } catch (e: Exception) {
                _mensajeError.value = "error al cargar datos: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    // trae todas las reservas con su cliente y estado
    fun cargarReservas() {
        viewModelScope.launch {
            _cargando.value = true
            try {
                _reservas.value = reservaDao.getAllReservasWithDetails()
            } catch (e: Exception) {
                _mensajeError.value = "error al cargar reservas: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    // busca reservas por el nombre del cliente (regla de negocio #2)
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

    // crea una reserva nueva (regla de negocio #1: validar disponibilidad)
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
                // validamos que la pista no este ocupada (idEstado = 3 es "activa")
                val pistaOcupada = reservaDao.isPistaOcupada(
                    numeroPista, fecha, hora
                ) > 0

                if (pistaOcupada) {
                    _mensajeError.value = "la pista $numeroPista ya esta reservada para esa fecha y hora"
                    return@launch
                }

                // si esta disponible, creamos la reserva con estado activo (id 3)
                val nuevaReserva = Reserva(
                    idCliente = idCliente,
                    idEstado = 3, // activa (segun los datos precargados)
                    fecha = fecha,
                    hora = hora,
                    numeroPista = numeroPista,
                    cantidadJugadores = cantidadJugadores
                )

                reservaDao.insert(nuevaReserva)
                _mensajeExito.value = "reserva creada exitosamente"
                cargarReservas() // recargamos para que aparezca la nueva

            } catch (e: Exception) {
                _mensajeError.value = "error al crear reserva: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    // actualiza una reserva existente
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
                cargarReservas()

            } catch (e: Exception) {
                _mensajeError.value = "error al actualizar reserva: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    // elimina una reserva (regla de negocio #4)
    fun eliminarReserva(reserva: Reserva) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                reservaDao.delete(reserva)
                _mensajeExito.value = "reserva eliminada exitosamente"
                cargarReservas()
            } catch (e: Exception) {
                _mensajeError.value = "error al eliminar reserva: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    // limpia los mensajes para que desaparezcan de la pantalla
    fun limpiarMensajes() {
        _mensajeError.value = null
        _mensajeExito.value = null
    }

    // fabrica para crear el viewModel con los daos (esto es necesario)
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