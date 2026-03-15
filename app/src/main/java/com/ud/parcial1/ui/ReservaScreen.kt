package com.ud.parcial1.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ud.parcial1.model.data.ReservaWithDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservaScreen(viewModel: ReservaViewModel) {
    val reservas by viewModel.reservas.collectAsState()
    var searchText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Reservas Bowling") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Abrir formulario */ }) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Reserva")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Barra de búsqueda (Regla 2)
            OutlinedTextField(
                value = searchText,
                onValueChange = { 
                    searchText = it
                    viewModel.buscarPorNombre(it)
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Buscar por nombre de cliente...") },
                leadingIcon = { Icon(Icons.Default.Search, null) }
            )

            // Listado de Reservas (Regla 3 y 4)
            LazyColumn {
                items(reservas) { item ->
                    ReservaItem(
                        item = item,
                        onDelete = { viewModel.eliminarReserva(item.reserva) },
                        onEdit = { /* TODO: Implementar edición */ }
                    )
                }
            }
        }
    }
}

@Composable
fun ReservaItem(
    item: ReservaWithDetails,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Cliente: ${item.cliente.nombre}", style = MaterialTheme.typography.titleMedium)
                // Visualización del estado (Regla 3)
                StatusBadge(status = item.estado.descripcion)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Pista: ${item.reserva.numeroPista} | Fecha: ${item.reserva.fecha}")
            Text(text = "Hora: ${item.reserva.hora}")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Blue)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "Activa" -> Color(0xFF4CAF50)
        "Cancelada" -> Color(0xFFF44336)
        "Finalizada" -> Color(0xFF2196F3)
        else -> Color.Gray
    }
    Surface(
        color = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
