package com.example.myapplication

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.mutableStateListOf
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
import com.example.myapplication.api.LabourServiceMapRequest
import com.example.myapplication.api.MappedServiceItem
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.api.ServiceItem
import com.example.myapplication.api.CreateServiceRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabourProviderScreen(
    userEmail: String?,
    token: String?,
    initialTab: Int = 0,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(initialTab.coerceIn(0, 1)) }
    var services by remember { mutableStateOf<List<ServiceItem>>(emptyList()) }
    var selectedService by remember { mutableStateOf<ServiceItem?>(null) }
    var serviceDropdownExpanded by remember { mutableStateOf(false) }
    var experienceYearsText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var newServiceName by remember { mutableStateOf("") }
    var newServiceDescription by remember { mutableStateOf("") }
    var newServicePrice by remember { mutableStateOf("") }
    var newServiceDuration by remember { mutableStateOf("") }
    var newTypeName by remember { mutableStateOf("") }
    var newTypeDescription by remember { mutableStateOf("") }
    var newTypePrice by remember { mutableStateOf("") }
    var newTypePhotos by remember { mutableStateOf("") }
    val serviceTypes = remember { mutableStateListOf<com.example.myapplication.api.ServiceTypeItem>() }
    var searchText by remember { mutableStateOf("") }
    var mappedServices by remember { mutableStateOf<List<MappedServiceItem>>(emptyList()) }

    var isServicesLoading by remember { mutableStateOf(false) }
    var isMapLoading by remember { mutableStateOf(false) }
    var isCreateServiceLoading by remember { mutableStateOf(false) }
    var isMappedLoading by remember { mutableStateOf(false) }

    fun loadAllServices() {
        isServicesLoading = true
        coroutineScope.launch {
            try {
                val response = RetrofitClient.labourApi.getServices()
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    services = body
                    if (selectedService == null) {
                        selectedService = body.firstOrNull()
                    }
                } else {
                    Toast.makeText(context, "Failed to load services", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                isServicesLoading = false
            }
        }
    }

    fun loadMappedServices(search: String = searchText) {
        if (token.isNullOrBlank()) {
            Toast.makeText(context, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }

        isMappedLoading = true
        coroutineScope.launch {
            try {
                val response = RetrofitClient.labourApi.getMappedServices(
                    token = token,
                    search = search.ifBlank { null }
                )
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    mappedServices = body.services
                } else {
                    mappedServices = emptyList()
                    Toast.makeText(context, body?.message ?: "Failed to fetch mapped services", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                isMappedLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadAllServices()
        loadMappedServices()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Labour Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    onBack?.let { backAction ->
                        IconButton(onClick = backAction) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout")
                    }
                }
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
                    text = { Text("Attach Service") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        loadMappedServices()
                    },
                    text = { Text("My Services") }
                )
            }

            if (selectedTab == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (userEmail.isNullOrBlank()) "Labour user" else "Logged in as $userEmail",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isServicesLoading) {
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
                                label = { Text("Service") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceDropdownExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
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
                        value = experienceYearsText,
                        onValueChange = { experienceYearsText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Experience Years (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (token.isNullOrBlank()) {
                                Toast.makeText(context, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val service = selectedService
                            if (service == null) {
                                Toast.makeText(context, "Please select a service", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isMapLoading = true
                            coroutineScope.launch {
                                try {
                                    val response = RetrofitClient.labourApi.mapService(
                                        LabourServiceMapRequest(
                                            token = token,
                                            serviceId = service.serviceId,
                                            experienceYears = experienceYearsText.toIntOrNull(),
                                            notes = notes.ifBlank { null }
                                        )
                                    )
                                    val body = response.body()
                                    if (response.isSuccessful && body?.success == true) {
                                        Toast.makeText(context, "Service attached successfully", Toast.LENGTH_SHORT).show()
                                        notes = ""
                                        experienceYearsText = ""
                                        loadMappedServices()
                                    } else {
                                        Toast.makeText(context, body?.message ?: "Failed to attach service", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isMapLoading = false
                                }
                            }
                        },
                        enabled = !isMapLoading && !isServicesLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isMapLoading) CircularProgressIndicator() else Text("Attach Service")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Or create a new service",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = newServiceName,
                        onValueChange = { newServiceName = it },
                        label = { Text("Service Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newServiceDescription,
                        onValueChange = { newServiceDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newServicePrice,
                        onValueChange = { newServicePrice = it },
                        label = { Text("Default Price (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newServiceDuration,
                        onValueChange = { newServiceDuration = it },
                        label = { Text("Duration (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Add Service Type",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = newTypeName,
                        onValueChange = { newTypeName = it },
                        label = { Text("Type Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTypeDescription,
                        onValueChange = { newTypeDescription = it },
                        label = { Text("Type Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTypePrice,
                        onValueChange = { newTypePrice = it },
                        label = { Text("Type Price") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTypePhotos,
                        onValueChange = { newTypePhotos = it },
                        label = { Text("Type Photos URLs (comma separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            val parsedTypePrice = newTypePrice.toDoubleOrNull()
                            if (newTypeName.isBlank() || parsedTypePrice == null || parsedTypePrice <= 0.0) {
                                Toast.makeText(context, "Enter valid type name and type price", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val photos = newTypePhotos.split(",")
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                            serviceTypes.add(
                                com.example.myapplication.api.ServiceTypeItem(
                                    typeName = newTypeName.trim(),
                                    typeDescription = newTypeDescription.trim().ifBlank { null },
                                    typePrice = parsedTypePrice,
                                    photoUrls = photos
                                )
                            )
                            newTypeName = ""
                            newTypeDescription = ""
                            newTypePrice = ""
                            newTypePhotos = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Type")
                    }
                    if (serviceTypes.isNotEmpty()) {
                        Text(
                            text = "Added types: ${serviceTypes.joinToString { "${it.typeName} (${it.typePrice})" }}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Button(
                        onClick = {
                            val parsedPrice = newServicePrice.toDoubleOrNull()
                            val hasValidDefaultPrice = parsedPrice != null && parsedPrice > 0.0
                            val hasTypes = serviceTypes.isNotEmpty()
                            if (newServiceName.isBlank() || (!hasValidDefaultPrice && !hasTypes)) {
                                Toast.makeText(
                                    context,
                                    "Enter service name and either default price or at least one service type",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                            isCreateServiceLoading = true
                            coroutineScope.launch {
                                try {
                                    val response = RetrofitClient.labourApi.createService(
                                        CreateServiceRequest(
                                            serviceName = newServiceName.trim(),
                                            description = newServiceDescription.trim().ifBlank { null },
                                            price = if (hasValidDefaultPrice) parsedPrice else null,
                                            duration = newServiceDuration.trim().ifBlank { null },
                                            serviceTypes = serviceTypes.toList()
                                        )
                                    )
                                    val body = response.body()
                                    if (response.isSuccessful && body != null) {
                                        Toast.makeText(context, "Service created successfully", Toast.LENGTH_SHORT).show()
                                        newServiceName = ""
                                        newServiceDescription = ""
                                        newServicePrice = ""
                                        newServiceDuration = ""
                                        serviceTypes.clear()
                                        loadAllServices()
                                        selectedService = body
                                    } else {
                                        Toast.makeText(context, "Failed to create service", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isCreateServiceLoading = false
                                }
                            }
                        },
                        enabled = !isCreateServiceLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isCreateServiceLoading) CircularProgressIndicator() else Text("Create Service")
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            label = { Text("Search my services") },
                            modifier = Modifier.weight(1f)
                        )
                        Button(onClick = { loadMappedServices(searchText) }, enabled = !isMappedLoading) {
                            Text("Search")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isMappedLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (mappedServices.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No mapped services found")
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(mappedServices) { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = item.serviceName ?: "Service",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (!item.description.isNullOrBlank()) {
                                            Text(item.description, style = MaterialTheme.typography.bodySmall)
                                        }
                                        Text(
                                            text = "Experience: ${item.experienceYears ?: 0} years",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        if (!item.notes.isNullOrBlank()) {
                                            Text("Notes: ${item.notes}", style = MaterialTheme.typography.bodySmall)
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

