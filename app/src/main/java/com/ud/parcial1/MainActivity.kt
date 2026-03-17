package com.ud.parcial1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ud.parcial1.model.data.AppDatabase
import com.ud.parcial1.model.data.ReservaWithDetails
import com.ud.parcial1.ui.ReservaScreen
import com.ud.parcial1.ui.ReservaViewModel
import com.ud.parcial1.ui.screens.DetalleReservaScreen
import com.ud.parcial1.ui.screens.ReservaFormScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppPrincipal()
        }
    }
}

// esta es la pantalla principal que decide que mostrar
@Composable
fun AppPrincipal() {
    val context = LocalContext.current
    // necesitamos un scope para las operaciones largas de base de datos
    val scope = remember { CoroutineScope(Dispatchers.IO + SupervisorJob()) }

    // aqui creamos la base de datos (solo se hace una vez en toda la app)
    val database = remember {
        AppDatabase.getDatabase(context, scope)
    }

    // creamos el viewModel con su fabrica para pasarle los daos
    val viewModel: ReservaViewModel = viewModel(
        factory = ReservaViewModel.Factory(
            reservaDao = database.reservaDao(),
            clienteDao = database.clienteDao()
        )
    )

    // controlamos en que pantalla estamos: listado o formulario
    var pantallaActual by remember { mutableStateOf(Pantalla.LISTADO) }
    // guardamos la reserva que vamos a editar (si es null, es nueva)
    var reservaParaEditar by remember { mutableStateOf<ReservaWithDetails?>(null) }

    // mostramos una pantalla u otra segun el estado
    when (pantallaActual) {
        Pantalla.LISTADO -> {
            ReservaScreen(
                viewModel = viewModel,
                onNuevaReserva = {
                    // cuando el usuario hace clic en el boton +, vamos al formulario vacio
                    reservaParaEditar = null
                    pantallaActual = Pantalla.FORMULARIO
                },
                onVerDetalle = { reserva ->
                    // cuando el usuario hace clic en ver detalle, vamos a la pantalla de detalle
                    viewModel.seleccionarReserva(reserva)
                    pantallaActual = Pantalla.DETALLE
                },
                onEditarReserva = { reserva ->
                    // cuando el usuario hace clic en editar, vamos al formulario con datos
                    reservaParaEditar = reserva
                    pantallaActual = Pantalla.FORMULARIO
                }
            )
        }
        Pantalla.FORMULARIO -> {
            ReservaFormScreen(
                viewModel = viewModel,
                reservaParaEditar = reservaParaEditar,
                onGuardarCompleto = {
                    // cuando guarda exitosamente, volvemos al listado
                    pantallaActual = Pantalla.LISTADO
                    reservaParaEditar = null
                },
                onCancelar = {
                    // si cancela, volvemos al listado sin guardar
                    pantallaActual = Pantalla.LISTADO
                    reservaParaEditar = null
                }
            )
        }
        Pantalla.DETALLE -> {
            val reservaSeleccionada by viewModel.reservaSeleccionada.collectAsState()

            if (reservaSeleccionada != null) {
                DetalleReservaScreen(
                    reserva = reservaSeleccionada!!,
                    onEditar = {
                        // cuando hace clic en editar desde detalle
                        reservaParaEditar = reservaSeleccionada
                        pantallaActual = Pantalla.FORMULARIO
                    },
                    onVolver = {
                        // cuando vuelve de detalle
                        viewModel.limpiarSeleccion()
                        pantallaActual = Pantalla.LISTADO
                    }
                )
            } else {
                // si no hay reserva seleccionada, volvemos al listado
                LaunchedEffect(Unit) {
                    pantallaActual = Pantalla.LISTADO
                }
            }
        }
    }
}

// para saber en que pantalla estamos
enum class Pantalla {
    LISTADO,
    FORMULARIO,
    DETALLE
}