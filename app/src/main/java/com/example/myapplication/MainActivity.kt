package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var isLoggedIn by remember { mutableStateOf(false) }
                var loggedInName by remember { mutableStateOf<String?>(null) }
                var loggedInEmail by remember { mutableStateOf<String?>(null) }
                var showQueryScreen by remember { mutableStateOf(false) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when {
                        !isLoggedIn -> {
                            AuthScreen(
                                modifier = Modifier.padding(innerPadding),
                                onLoginSuccess = { name, email ->
                                    loggedInName = name
                                    loggedInEmail = email
                                    isLoggedIn = true
                                }
                            )
                        }
                        showQueryScreen -> {
                            QueryScreen(
                                userEmail = loggedInEmail,
                                modifier = Modifier.padding(innerPadding),
                                onBack = { showQueryScreen = false }
                            )
                        }
                        else -> {
                            HomeScreen(
                                name = loggedInName,
                                userEmail = loggedInEmail,
                                modifier = Modifier.padding(innerPadding),
                                onLogout = {
                                    isLoggedIn = false
                                    loggedInName = null
                                    loggedInEmail = null
                                },
                                onNavigateToQuery = {
                                    showQueryScreen = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}