package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.local.db.ServexaDatabase
import com.example.data.repository.ServexaRepository
import com.example.ui.ServexaApp
import com.example.ui.theme.ServexaTheme
import com.example.ui.viewmodel.ServexaViewModel
import com.example.ui.viewmodel.ServexaViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel: ServexaViewModel by viewModels {
        val database = ServexaDatabase.getInstance(applicationContext)
        val repository = ServexaRepository(database)
        ServexaViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val themeColor by viewModel.themeColor.collectAsState()

            ServexaTheme(
                themeMode = themeMode,
                themeColor = themeColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ServexaApp(viewModel = viewModel)
                }
            }
        }
    }
}


