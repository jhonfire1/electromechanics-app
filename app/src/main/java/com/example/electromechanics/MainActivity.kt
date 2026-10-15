package com.example.electromechanics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.electromechanics.data.local.AppDatabase
import com.example.electromechanics.data.local.DatabaseSeeder
import com.example.electromechanics.ui.navigation.SetupNavGraph
import com.example.electromechanics.ui.theme.ElectromechanicsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ElectromechanicsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Poblar base de datos inicial en segundo plano
                    LaunchedEffect(Unit) {
                        val db = AppDatabase.getDatabase(applicationContext)
                        DatabaseSeeder.insertInitialData(db)
                    }

                    SetupNavGraph(navController = navController)
                }
            }
        }
    }
}