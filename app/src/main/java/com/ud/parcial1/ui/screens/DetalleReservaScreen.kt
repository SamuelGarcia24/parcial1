package com.ud.parcial1.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ud.parcial1.model.data.ReservaWithDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleReservaScreen(
    reserva: ReservaWithDetails,
    onEditar: () -> Unit,
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("detalle de la reserva") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "volver")
                    }
                },
                actions = {
                    IconButton(onClick = onEditar) {
                        Icon(Icons.Default.Edit, contentDescription = "editar")
                    }
                }
            )
        }
    ) { paddingValues ->
        // contenedor con scroll por si hay muchos datos
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // tarjeta principal con toda la info
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // encabezado con estado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "reserva #${reserva.reserva.id}",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        BadgeDetalle(estado = reserva.estado.descripcion)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // informacion del cliente
                    SeccionInfo(titulo = "informacion del cliente")

                    FilaInfo(
                        label = "nombre",
                        valor = reserva.cliente.nombre
                    )
                    FilaInfo(
                        label = "telefono",
                        valor = reserva.cliente.telefono
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // informacion de la reserva
                    SeccionInfo(titulo = "detalles de la reserva")

                    FilaInfo(
                        label = "fecha",
                        valor = reserva.reserva.fecha
                    )
                    FilaInfo(
                        label = "hora",
                        valor = reserva.reserva.hora
                    )
                    FilaInfo(
                        label = "numero de pista",
                        valor = reserva.reserva.numeroPista.toString()
                    )
                    FilaInfo(
                        label = "cantidad de jugadores",
                        valor = reserva.reserva.cantidadJugadores.toString()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // informacion adicional (reglas de negocio)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "informacion importante",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• la reserva debe cancelarse con 24 horas de anticipacion",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• el estado actual es: ${reserva.estado.descripcion}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (reserva.estado.descripcion == "Activa") {
                        Text(
                            text = "• la pista ${reserva.reserva.numeroPista} esta confirmada",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

// componente para mostrar una fila de informacion
@Composable
fun FilaInfo(
    label: String,
    valor: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label:",
            modifier = Modifier.width(120.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}

// componente para titulos de seccion
@Composable
fun SeccionInfo(titulo: String) {
    Text(
        text = titulo,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

// badge para el estado (version detallada)
@Composable
fun BadgeDetalle(estado: String) {
    val (colorFondo, colorTexto) = when (estado) {
        "Activa" -> Pair(Color(0xFF4CAF50).copy(alpha = 0.2f), Color(0xFF4CAF50))
        "Cancelada" -> Pair(Color(0xFFF44336).copy(alpha = 0.2f), Color(0xFFF44336))
        "Finalizada" -> Pair(Color(0xFF2196F3).copy(alpha = 0.2f), Color(0xFF2196F3))
        else -> Pair(Color.Gray.copy(alpha = 0.2f), Color.Gray)
    }

    Surface(
        color = colorFondo,
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = estado,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = colorTexto,
            style = MaterialTheme.typography.labelLarge
        )
    }
}