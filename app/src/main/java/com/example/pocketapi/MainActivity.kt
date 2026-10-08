package com.example.pocketapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pocketapi.ui.screens.MainScreen
import com.example.pocketapi.ui.theme.PocketApiTheme
import com.example.pocketapi.ui.viewmodel.PocketApiViewModel
import com.example.pocketapi.ui.viewmodel.PocketApiViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as PocketApiApp
        val repository = app.repository

        setContent {
            PocketApiTheme {
                val viewModel: PocketApiViewModel = viewModel(
                    factory = PocketApiViewModelFactory(repository)
                )
                MainScreen(viewModel = viewModel)
            }
        }
    }
}