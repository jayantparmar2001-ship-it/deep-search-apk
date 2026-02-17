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
                var loggedInToken by remember { mutableStateOf<String?>(null) }
                var loggedInRole by remember { mutableStateOf<String?>(null) }
                var selectedUserType by remember { mutableStateOf<String?>(null) }
                var showLabourScreen by remember { mutableStateOf(false) }
                var selectedCustomerServiceId by remember { mutableStateOf<Int?>(null) }
                var labourInitialTab by remember { mutableStateOf(0) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when {
                        !isLoggedIn && selectedUserType == null -> {
                            UserTypeSelectionScreen(
                                modifier = Modifier.padding(innerPadding),
                                onSelectCustomer = { selectedUserType = "CUSTOMER" },
                                onSelectLabour = { selectedUserType = "LABOUR" }
                            )
                        }
                        !isLoggedIn -> {
                            AuthScreen(
                                modifier = Modifier.padding(innerPadding),
                                preferredRole = selectedUserType,
                                onBackToRoleSelection = { selectedUserType = null },
                                onLoginSuccess = { name, email, token, role ->
                                    loggedInName = name
                                    loggedInEmail = email
                                    loggedInToken = token
                                    loggedInRole = role
                                    isLoggedIn = true
                                }
                            )
                        }
                        "LABOUR".equals(loggedInRole, ignoreCase = true) -> {
                            if (!showLabourScreen) {
                                LabourHomeScreen(
                                    userEmail = loggedInEmail,
                                    token = loggedInToken,
                                    modifier = Modifier.padding(innerPadding),
                                    onOpenServiceManager = { tab ->
                                        labourInitialTab = tab
                                        showLabourScreen = true
                                    },
                                    onLogout = {
                                        isLoggedIn = false
                                        loggedInName = null
                                        loggedInEmail = null
                                        loggedInToken = null
                                        loggedInRole = null
                                        selectedUserType = null
                                    }
                                )
                            } else {
                                LabourProviderScreen(
                                    userEmail = loggedInEmail,
                                    token = loggedInToken,
                                    initialTab = labourInitialTab,
                                    modifier = Modifier.padding(innerPadding),
                                    onBack = { showLabourScreen = false },
                                    onLogout = {
                                        isLoggedIn = false
                                        loggedInName = null
                                        loggedInEmail = null
                                        loggedInToken = null
                                        loggedInRole = null
                                        selectedUserType = null
                                        showLabourScreen = false
                                    }
                                )
                            }
                        }
                        showLabourScreen -> {
                            LabourScreen(
                                userEmail = loggedInEmail,
                                token = loggedInToken,
                                initialServiceId = selectedCustomerServiceId,
                                modifier = Modifier.padding(innerPadding),
                                onBack = {
                                    showLabourScreen = false
                                    selectedCustomerServiceId = null
                                }
                            )
                        }
                        else -> {
                            HomeScreen(
                                name = loggedInName,
                                userEmail = loggedInEmail,
                                token = loggedInToken,
                                modifier = Modifier.padding(innerPadding),
                                onLogout = {
                                    isLoggedIn = false
                                    loggedInName = null
                                    loggedInEmail = null
                                    loggedInToken = null
                                    loggedInRole = null
                                    selectedUserType = null
                                },
                                onNavigateToLabour = { serviceId ->
                                    selectedCustomerServiceId = serviceId
                                    showLabourScreen = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}