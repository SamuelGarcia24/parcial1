package com.ud.parcial1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ud.parcial1.model.data.Cliente
import com.ud.parcial1.model.data.ReservaWithDetails
import com.ud.parcial1.ui.ReservaViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservaFormScreen(
    viewModel: ReservaViewModel,
    reservaParaEditar: ReservaWithDetails?, // null = nueva reserva
    onGuardarCompleto: () -> Unit,
    onCancelar: () -> Unit
) {
    // observamos los datos
    val clientes by viewModel.clientes.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val reservas by viewModel.reservas.collectAsState()

    // estado del formulario
    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }
    var fechaSeleccionada by remember { mutableStateOf("") }
    var rangoHorasSeleccionado by remember { mutableStateOf("8:00 AM - 10:00 AM") }
    var pistaSeleccionada by remember { mutableStateOf(1) }
    var jugadoresSeleccionados by remember { mutableStateOf(2) }
    var estadoSeleccionado by remember { mutableStateOf("Activa") }

    // para los dropdowns
    var dropdownClienteExpandido by remember { mutableStateOf(false) }
    var dropdownRangoHorasExpandido by remember { mutableStateOf(false) }
    var dropdownPistaExpandido by remember { mutableStateOf(false) }
    var dropdownJugadoresExpandido by remember { mutableStateOf(false) }
    var dropdownEstadoExpandido by remember { mutableStateOf(false) }

    // para el calendario
    var mostrarDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // errores de validacion
    var errores by remember { mutableStateOf(mapOf<String, String>()) }

    // mensaje de disponibilidad
    var mensajeDisponibilidad by remember { mutableStateOf<String?>(null) }

    // estado para el diálogo de nuevo cliente
    var mostrarDialogoNuevoCliente by remember { mutableStateOf(false) }
    var nuevoClienteNombre by remember { mutableStateOf("") }
    var nuevoClienteTelefono by remember { mutableStateOf("") }
    var errorNuevoCliente by remember { mutableStateOf<String?>(null) }

    // lista de estados disponibles
    val estados = listOf("Activa", "Cancelada", "Finalizada")

    // 🆕 RANGOS DE HORAS (de 2 horas cada uno)
    val rangosHoras = listOf(
        "8:00 AM - 10:00 AM",
        "10:00 AM - 12:00 PM",
        "12:00 PM - 2:00 PM",
        "2:00 PM - 4:00 PM",
        "4:00 PM - 6:00 PM",
        "6:00 PM - 8:00 PM",
        "8:00 PM - 10:00 PM"
    )

    val pistasDisponibles = (1..8).toList()
    val jugadoresPorPista = (1..4).toList()

    // si estamos editando, cargamos los datos
    LaunchedEffect(reservaParaEditar) {
        if (reservaParaEditar != null) {
            clienteSeleccionado = reservaParaEditar.cliente
            fechaSeleccionada = reservaParaEditar.reserva.fecha
            rangoHorasSeleccionado = reservaParaEditar.reserva.hora
            pistaSeleccionada = reservaParaEditar.reserva.numeroPista
            jugadoresSeleccionados = reservaParaEditar.reserva.cantidadJugadores
            estadoSeleccionado = reservaParaEditar.estado.descripcion
        }
    }

    // 🆕 Función para convertir timestamp a fecha string
    fun timestampToDateString(timestamp: Long): String {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = timestamp
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")
        return format.format(calendar.time)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (reservaParaEditar == null) "Nueva Reserva" else "Editar Reserva",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancelar) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (cargando) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // mensaje informativo
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Los campos marcados con * son obligatorios",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // formulario
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // selector de cliente
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cliente *",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            TextButton(
                                onClick = {
                                    mostrarDialogoNuevoCliente = true
                                    dropdownClienteExpandido = false
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    Icons.Default.PersonAdd,
                                    contentDescription = "Añadir cliente",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Añadir nuevo")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = clienteSeleccionado?.nombre ?: "",
                                onValueChange = {},
                                readOnly = true,
                                placeholder = { Text("Seleccione un cliente") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = errores.containsKey("cliente"),
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownClienteExpandido)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    errorBorderColor = MaterialTheme.colorScheme.error
                                )
                            )

                            DropdownMenu(
                                expanded = dropdownClienteExpandido,
                                onDismissRequest = { dropdownClienteExpandido = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                            ) {
                                clientes.forEach { cliente ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    cliente.nombre,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = "Tel: ${cliente.telefono}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                            }
                                        },
                                        onClick = {
                                            clienteSeleccionado = cliente
                                            dropdownClienteExpandido = false
                                        }
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable {
                                        if (!dropdownClienteExpandido) {
                                            dropdownClienteExpandido = true
                                        }
                                    }
                            )
                        }

                        if (errores.containsKey("cliente")) {
                            Text(
                                text = errores["cliente"]!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                            )
                        }
                    }

                    // 🆕 CAMPO FECHA CON CALENDARIO
                    Column {
                        Text(
                            text = "Fecha *",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = fechaSeleccionada,
                                onValueChange = {},
                                readOnly = true,
                                placeholder = { Text("Seleccione fecha") },
                                modifier = Modifier.weight(1f),
                                isError = errores.containsKey("fecha"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            Button(
                                onClick = { mostrarDatePicker = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(56.dp)
                            ) {
                                Text("📅")
                            }
                        }

                        if (errores.containsKey("fecha")) {
                            Text(
                                text = errores["fecha"]!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                            )
                        }
                    }

                    // 🆕 SELECTOR DE RANGO DE HORAS
                    Column {
                        Text(
                            text = "Rango de Horas *",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = rangoHorasSeleccionado,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownRangoHorasExpandido)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            DropdownMenu(
                                expanded = dropdownRangoHorasExpandido,
                                onDismissRequest = { dropdownRangoHorasExpandido = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp)
                                    .padding(horizontal = 8.dp)
                            ) {
                                rangosHoras.forEach { rango ->
                                    DropdownMenuItem(
                                        text = { Text(rango) },
                                        onClick = {
                                            rangoHorasSeleccionado = rango
                                            dropdownRangoHorasExpandido = false
                                            mensajeDisponibilidad = null
                                        }
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { dropdownRangoHorasExpandido = true }
                            )
                        }
                    }

                    // SELECTOR DE PISTA
                    Column {
                        Text(
                            text = "Número de Pista * (1-8)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = pistaSeleccionada.toString(),
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownPistaExpandido)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            DropdownMenu(
                                expanded = dropdownPistaExpandido,
                                onDismissRequest = { dropdownPistaExpandido = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                            ) {
                                pistasDisponibles.forEach { pista ->
                                    DropdownMenuItem(
                                        text = { Text("Pista $pista") },
                                        onClick = {
                                            pistaSeleccionada = pista
                                            dropdownPistaExpandido = false
                                            mensajeDisponibilidad = null
                                        }
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { dropdownPistaExpandido = true }
                            )
                        }
                    }

                    // SELECTOR DE JUGADORES
                    Column {
                        Text(
                            text = "Cantidad de Jugadores * (Máx 4)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = "$jugadoresSeleccionados ${if (jugadoresSeleccionados == 1) "Jugador" else "Jugadores"}",
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownJugadoresExpandido)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            DropdownMenu(
                                expanded = dropdownJugadoresExpandido,
                                onDismissRequest = { dropdownJugadoresExpandido = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                            ) {
                                jugadoresPorPista.forEach { cantidad ->
                                    DropdownMenuItem(
                                        text = {
                                            Text("$cantidad ${if (cantidad == 1) "Jugador" else "Jugadores"}")
                                        },
                                        onClick = {
                                            jugadoresSeleccionados = cantidad
                                            dropdownJugadoresExpandido = false
                                        }
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { dropdownJugadoresExpandido = true }
                            )
                        }
                    }

                    // SELECTOR DE ESTADO (solo visible en edición)
                    if (reservaParaEditar != null) {
                        Column {
                            Text(
                                text = "Estado *",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = estadoSeleccionado,
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownEstadoExpandido)
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )

                                DropdownMenu(
                                    expanded = dropdownEstadoExpandido,
                                    onDismissRequest = { dropdownEstadoExpandido = false },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp)
                                ) {
                                    estados.forEach { estado ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(12.dp)
                                                            .padding(end = 8.dp)
                                                            .background(
                                                                color = when (estado) {
                                                                    "Activa" -> Color(0xFF4CAF50)
                                                                    "Cancelada" -> Color(0xFFF44336)
                                                                    "Finalizada" -> Color(0xFF2196F3)
                                                                    else -> Color.Gray
                                                                },
                                                                shape = CircleShape
                                                            )
                                                    )
                                                    Text(estado)
                                                }
                                            },
                                            onClick = {
                                                estadoSeleccionado = estado
                                                dropdownEstadoExpandido = false
                                            }
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { dropdownEstadoExpandido = true }
                                )
                            }
                        }
                    }

                    // mensaje de disponibilidad si existe
                    if (mensajeDisponibilidad != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = mensajeDisponibilidad!!,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // botón guardar
            Button(
                onClick = {
                    // validamos los campos
                    val nuevosErrores = mutableMapOf<String, String>()

                    if (clienteSeleccionado == null) {
                        nuevosErrores["cliente"] = "Debe seleccionar un cliente"
                    }
                    if (fechaSeleccionada.isBlank()) {
                        nuevosErrores["fecha"] = "La fecha es obligatoria"
                    }

                    if (nuevosErrores.isEmpty()) {
                        // 🆕 VALIDACIÓN DE DISPONIBILIDAD POR RANGO DE HORAS
                        val pistaOcupada = reservas.any { reserva ->
                            reserva.reserva.numeroPista == pistaSeleccionada &&
                                    reserva.reserva.fecha == fechaSeleccionada &&
                                    reserva.reserva.hora == rangoHorasSeleccionado && // Mismo rango
                                    reserva.reserva.idEstado == 3 && // Solo activas
                                    (reservaParaEditar == null || reserva.reserva.id != reservaParaEditar.reserva.id)
                        }

                        if (pistaOcupada) {
                            mensajeDisponibilidad = "La pista $pistaSeleccionada ya está reservada para el $fechaSeleccionada en el horario $rangoHorasSeleccionado"
                            return@Button
                        }

                        if (reservaParaEditar == null) {
                            // crear nueva reserva
                            viewModel.crearReserva(
                                idCliente = clienteSeleccionado!!.id,
                                fecha = fechaSeleccionada,
                                hora = rangoHorasSeleccionado,
                                numeroPista = pistaSeleccionada,
                                cantidadJugadores = jugadoresSeleccionados
                            )
                            onGuardarCompleto()
                        } else {
                            // actualizar reserva existente
                            val idEstado = when (estadoSeleccionado) {
                                "Activa" -> 3
                                "Cancelada" -> 1
                                "Finalizada" -> 2
                                else -> 3
                            }
                            viewModel.actualizarReserva(
                                id = reservaParaEditar.reserva.id,
                                idCliente = clienteSeleccionado!!.id,
                                idEstado = idEstado,
                                fecha = fechaSeleccionada,
                                hora = rangoHorasSeleccionado,
                                numeroPista = pistaSeleccionada,
                                cantidadJugadores = jugadoresSeleccionados
                            )
                            onGuardarCompleto()
                        }
                    } else {
                        errores = nuevosErrores
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !cargando,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (reservaParaEditar == null) "Guardar Reserva" else "Actualizar Reserva",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // 🆕 DatePicker Dialog
    if (mostrarDatePicker) {
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { timestamp ->
                            fechaSeleccionada = timestampToDateString(timestamp)
                            mensajeDisponibilidad = null
                        }
                        mostrarDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Diálogo para añadir nuevo cliente
    if (mostrarDialogoNuevoCliente) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoNuevoCliente = false
                nuevoClienteNombre = ""
                nuevoClienteTelefono = ""
                errorNuevoCliente = null
            },
            title = {
                Text(
                    text = "Nuevo Cliente",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = nuevoClienteNombre,
                        onValueChange = { nuevoClienteNombre = it },
                        label = { Text("Nombre *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = nuevoClienteTelefono,
                        onValueChange = { nuevoClienteTelefono = it },
                        label = { Text("Teléfono *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (errorNuevoCliente != null) {
                        Text(
                            text = errorNuevoCliente!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nuevoClienteNombre.isBlank() || nuevoClienteTelefono.isBlank()) {
                            errorNuevoCliente = "Todos los campos son obligatorios"
                            return@Button
                        }

                        val tempId = (clientes.maxOfOrNull { it.id } ?: 0) + 1
                        val clienteTemp = Cliente(
                            id = tempId,
                            nombre = nuevoClienteNombre,
                            telefono = nuevoClienteTelefono
                        )
                        clienteSeleccionado = clienteTemp

                        viewModel.crearCliente(nuevoClienteNombre, nuevoClienteTelefono)

                        mostrarDialogoNuevoCliente = false
                        nuevoClienteNombre = ""
                        nuevoClienteTelefono = ""
                        errorNuevoCliente = null
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Guardar", fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoNuevoCliente = false
                        nuevoClienteNombre = ""
                        nuevoClienteTelefono = ""
                        errorNuevoCliente = null
                    }
                ) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.error)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}