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

    fun eliminarReserva(reserva: Reserva) {
        viewModelScope.launch {
            reservaDao.delete(reserva)
            cargarReservas()
        }
    }

    // Esta cosa sirve para crear el viewmodel con los dao, sin esto no funciona
    class Factory(
        private val reservaDao: ReservaDao,
        private val clienteDao: ClienteDao
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReservaViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST") // esto calla las advertencias del compilador
                return ReservaViewModel(reservaDao, clienteDao) as T
            }
            throw IllegalArgumentException("Viewmodel desconocido")
        }
    }
}
