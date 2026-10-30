package com.example.miniproyecto03

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.miniproyecto03.presentation.AddScreen
import com.example.miniproyecto03.presentation.ListScreen
import com.example.miniproyecto03.presentation.PublicacionViewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.miniproyecto03.ui.theme.MiniProyecto03Theme

class MainActivity : ComponentActivity() {
    private val viewModel: PublicacionViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "list") {
                composable("list") {
                    ListScreen(viewModel = viewModel) {
                        navController.navigate("add")
                    }
                }
                composable("add") {
                    AddScreen(viewModel = viewModel) {
                        navController.popBackStack()
                    }
                }
            }
        }
    }
}