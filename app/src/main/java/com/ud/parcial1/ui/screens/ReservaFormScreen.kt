package com.ud.parcial1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ud.parcial1.model.data.Cliente
import com.ud.parcial1.model.data.ReservaWithDetails
import com.ud.parcial1.ui.ReservaViewModel

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

    // estado del formulario
    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var numeroPista by remember { mutableStateOf("1") }
    var cantidadJugadores by remember { mutableStateOf("4") }
    var estadoSeleccionado by remember { mutableStateOf("Activa") }

    // para los dropdowns
    var dropdownClienteExpandido by remember { mutableStateOf(false) }
    var dropdownEstadoExpandido by remember { mutableStateOf(false) }

    // errores de validacion
    var errores by remember { mutableStateOf(mapOf<String, String>()) }

    // mensaje de disponibilidad (regla #1)
    var mensajeDisponibilidad by remember { mutableStateOf<String?>(null) }

    // estado para el diálogo de nuevo cliente
    var mostrarDialogoNuevoCliente by remember { mutableStateOf(false) }
    var nuevoClienteNombre by remember { mutableStateOf("") }
    var nuevoClienteTelefono by remember { mutableStateOf("") }
    var errorNuevoCliente by remember { mutableStateOf<String?>(null) }

    // lista de estados disponibles
    val estados = listOf("Activa", "Cancelada", "Finalizada")

    // si estamos editando, cargamos los datos
    LaunchedEffect(reservaParaEditar) {
        if (reservaParaEditar != null) {
            clienteSeleccionado = reservaParaEditar.cliente
            fecha = reservaParaEditar.reserva.fecha
            hora = reservaParaEditar.reserva.hora
            numeroPista = reservaParaEditar.reserva.numeroPista.toString()
            cantidadJugadores = reservaParaEditar.reserva.cantidadJugadores.toString()
            estadoSeleccionado = reservaParaEditar.estado.descripcion
        }
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

                            // 🆕 BOTÓN DE AÑADIR CLIENTE (separado del dropdown)
                            TextButton(
                                onClick = {
                                    mostrarDialogoNuevoCliente = true
                                    dropdownClienteExpandido = false // cerramos el dropdown si estaba abierto
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

                    // campo fecha
                    Column {
                        Text(
                            text = "Fecha *",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = fecha,
                            onValueChange = {
                                fecha = it
                                mensajeDisponibilidad = null
                            },
                            placeholder = { Text("DD/MM/AAAA") },
                            isError = errores.containsKey("fecha"),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            supportingText = {
                                if (errores.containsKey("fecha")) {
                                    Text(errores["fecha"]!!)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }

                    // campo hora
                    Column {
                        Text(
                            text = "Hora *",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = hora,
                            onValueChange = {
                                hora = it
                                mensajeDisponibilidad = null
                            },
                            placeholder = { Text("Ej: 4:00 PM") },
                            isError = errores.containsKey("hora"),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            supportingText = {
                                if (errores.containsKey("hora")) {
                                    Text(errores["hora"]!!)
                                }
                            }
                        )
                    }

                    // campo número de pista
                    Column {
                        Text(
                            text = "Número de Pista *",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = numeroPista,
                            onValueChange = {
                                numeroPista = it
                                mensajeDisponibilidad = null
                            },
                            placeholder = { Text("1") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = errores.containsKey("pista"),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            supportingText = {
                                if (errores.containsKey("pista")) {
                                    Text(errores["pista"]!!)
                                }
                            }
                        )
                    }

                    // campo cantidad de jugadores
                    Column {
                        Text(
                            text = "Cantidad de Jugadores *",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = cantidadJugadores,
                            onValueChange = { cantidadJugadores = it },
                            placeholder = { Text("4") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = errores.containsKey("jugadores"),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            supportingText = {
                                if (errores.containsKey("jugadores")) {
                                    Text(errores["jugadores"]!!)
                                }
                            }
                        )
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
                    if (fecha.isBlank()) {
                        nuevosErrores["fecha"] = "La fecha es obligatoria"
                    }
                    if (hora.isBlank()) {
                        nuevosErrores["hora"] = "La hora es obligatoria"
                    }
                    val pistaNum = numeroPista.toIntOrNull()
                    if (pistaNum == null || pistaNum <= 0) {
                        nuevosErrores["pista"] = "Número de pista inválido"
                    }
                    val jugadoresNum = cantidadJugadores.toIntOrNull()
                    if (jugadoresNum == null || jugadoresNum <= 0) {
                        nuevosErrores["jugadores"] = "Cantidad de jugadores inválida"
                    }

                    if (nuevosErrores.isEmpty()) {
                        if (reservaParaEditar == null) {
                            // para nueva reserva, verificamos disponibilidad
                            viewModel.crearReserva(
                                idCliente = clienteSeleccionado!!.id,
                                fecha = fecha,
                                hora = hora,
                                numeroPista = pistaNum!!,
                                cantidadJugadores = jugadoresNum!!
                            )
                            onGuardarCompleto()
                        } else {
                            // actualizar reserva existente con el nuevo estado
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
                                fecha = fecha,
                                hora = hora,
                                numeroPista = pistaNum!!,
                                cantidadJugadores = jugadoresNum!!
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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

                        // Crear cliente temporal para selección inmediata
                        val tempId = (clientes.maxOfOrNull { it.id } ?: 0) + 1
                        val clienteTemp = Cliente(
                            id = tempId,
                            nombre = nuevoClienteNombre,
                            telefono = nuevoClienteTelefono
                        )
                        clienteSeleccionado = clienteTemp

                        // Guardar en BD
                        viewModel.crearCliente(nuevoClienteNombre, nuevoClienteTelefono)

                        // Cerrar diálogo
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