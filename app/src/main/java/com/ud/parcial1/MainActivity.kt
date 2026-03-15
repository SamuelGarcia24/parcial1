package com.ud.parcial1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.ud.parcial1.model.data.AppDatabase
import com.ud.parcial1.ui.ReservaScreen
import com.ud.parcial1.ui.ReservaViewModel
import com.ud.parcial1.ui.theme.Parcial1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            Parcial1Theme {
                val scope = rememberCoroutineScope()
                val db = AppDatabase.getDatabase(applicationContext, scope)
                
                // Inicializamos el ViewModel con su Factory
                val viewModel: ReservaViewModel by viewModels {
                    ReservaViewModel.Factory(db.reservaDao(), db.clienteDao())
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ReservaScreen(viewModel = viewModel)
                }
            }
        }
    }
}
