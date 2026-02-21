package com.example.myapplication

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.api.LabourRequestSubmitRequest
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.api.ServiceItem
import com.example.myapplication.api.UserLabourRequestItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabourScreen(
    userEmail: String?,
    token: String?,
    initialServiceId: Int? = null,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    var services by remember { mutableStateOf<List<ServiceItem>>(emptyList()) }
    var selectedService by remember { mutableStateOf<ServiceItem?>(null) }
    var serviceDropdownExpanded by remember { mutableStateOf(false) }
    var address by remember { mutableStateOf("") }
    var preferredDate by remember { mutableStateOf("") } // yyyy-MM-dd
    var notes by remember { mutableStateOf("") }

    var isServiceLoading by remember { mutableStateOf(false) }
    var isSubmitLoading by remember { mutableStateOf(false) }
    var isRequestsLoading by remember { mutableStateOf(false) }
    var myRequests by remember { mutableStateOf<List<UserLabourRequestItem>>(emptyList()) }

    fun loadServices() {
        isServiceLoading = true
        coroutineScope.launch {
            try {
                val response = RetrofitClient.labourApi.getServices()
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    services = body
                    selectedService = body.firstOrNull { it.serviceId == initialServiceId } ?: body.firstOrNull()
                } else {
                    Toast.makeText(context, "Failed to load services", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                isServiceLoading = false
            }
        }
    }

    fun loadMyRequests() {
        if (token.isNullOrBlank()) {
            Toast.makeText(context, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }
        isRequestsLoading = true
        coroutineScope.launch {
            try {
                val response = RetrofitClient.labourApi.getMyLabourRequests(token)
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    myRequests = body.requests
                } else {
                    myRequests = emptyList()
                    Toast.makeText(context, body?.message ?: "Failed to fetch requests", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                isRequestsLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadServices() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Labour Services") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Submit Request") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        loadMyRequests()
                    },
                    text = { Text("My Requests") }
                )
            }

            if (selectedTab == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (userEmail.isNullOrBlank()) "Logged in user" else "Logged in as $userEmail",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isServiceLoading) {
                        CircularProgressIndicator()
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = serviceDropdownExpanded,
                            onExpandedChange = { serviceDropdownExpanded = !serviceDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedService?.serviceName ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Service") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            DropdownMenu(
                                expanded = serviceDropdownExpanded,
                                onDismissRequest = { serviceDropdownExpanded = false }
                            ) {
                                services.forEach { service ->
                                    DropdownMenuItem(
                                        text = { Text(service.serviceName) },
                                        onClick = {
                                            selectedService = service
                                            serviceDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = preferredDate,
                        onValueChange = { preferredDate = it },
                        label = { Text("Preferred Date (yyyy-MM-dd)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (token.isNullOrBlank()) {
                                Toast.makeText(context, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (selectedService == null || address.isBlank()) {
                                Toast.makeText(context, "Please select service and address", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isSubmitLoading = true
                            coroutineScope.launch {
                                try {
                                    val request = LabourRequestSubmitRequest(
                                        token = token,
                                        serviceId = selectedService!!.serviceId,
                                        address = address.trim(),
                                        preferredDate = preferredDate.trim().ifBlank { null },
                                        notes = notes.trim().ifBlank { null }
                                    )
                                    val response = RetrofitClient.labourApi.submitLabourRequest(request)
                                    val body = response.body()
                                    if (response.isSuccessful && body?.success == true) {
                                        Toast.makeText(
                                            context,
                                            "Request submitted successfully (ID: ${body.requestId})",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        address = ""
                                        preferredDate = ""
                                        notes = ""
                                    } else {
                                        Toast.makeText(
                                            context,
                                            body?.message ?: "Failed to submit request",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Network error: ${e.localizedMessage}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } finally {
                                    isSubmitLoading = false
                                }
                            }
                        },
                        enabled = !isSubmitLoading && !isServiceLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isSubmitLoading) {
                            CircularProgressIndicator()
                        } else {
                            Text("Submit Labour Request")
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("My Labour Requests", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { loadMyRequests() }, enabled = !isRequestsLoading) {
                            Text("Refresh")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isRequestsLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (myRequests.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No requests found")
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(myRequests) { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(item.serviceName ?: "Service", fontWeight = FontWeight.SemiBold)
                                        Text("Status: ${item.status ?: "NEW"}")
                                        Text("Address: ${item.address ?: "-"}")
                                        if (!item.preferredDate.isNullOrBlank()) {
                                            Text("Preferred Date: ${item.preferredDate}")
                                        }
                                        if (!item.createdAt.isNullOrBlank()) {
                                            Text(
                                                text = "Created: ${item.createdAt}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


