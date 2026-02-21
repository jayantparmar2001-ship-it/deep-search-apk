package com.example.myapplication

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.api.CreateServiceRequest
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.api.ServiceTypeItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabourHomeScreen(
    userEmail: String?,
    token: String?,
    modifier: Modifier = Modifier,
    onOpenServiceManager: (Int) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var serviceName by remember { mutableStateOf("") }
    var serviceDescription by remember { mutableStateOf("") }
    var serviceDuration by remember { mutableStateOf("") }
    var servicePrice by remember { mutableStateOf("") }
    var serviceMainImageUrl by remember { mutableStateOf("") }
    var serviceGalleryPhotos by remember { mutableStateOf("") }
    var typeName by remember { mutableStateOf("") }
    var typeDescription by remember { mutableStateOf("") }
    var subscriptionPlan by remember { mutableStateOf("WEEKLY") }
    var typePrice by remember { mutableStateOf("") }
    var typePhotos by remember { mutableStateOf("") }
    val serviceTypes = remember { mutableStateListOf<ServiceTypeItem>() }
    var isCreating by remember { mutableStateOf(false) }

    var selectedMainImageUri by remember { mutableStateOf<Uri?>(null) }

    val mainImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            selectedMainImageUri = it
            serviceMainImageUrl = it.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Labour Home") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Welcome Labour Provider",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (userEmail.isNullOrBlank()) "Manage your services and mappings" else "Logged in as $userEmail",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Create Service You Provide",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = serviceName,
                        onValueChange = { serviceName = it },
                        label = { Text("Service Name (e.g. Car Wash)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = serviceDescription,
                        onValueChange = { serviceDescription = it },
                        label = { Text("Service Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = serviceDuration,
                        onValueChange = { serviceDuration = it },
                        label = { Text("Duration (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = servicePrice,
                        onValueChange = { servicePrice = it },
                        label = { Text("Default Price (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = serviceMainImageUrl,
                        onValueChange = { serviceMainImageUrl = it },
                        label = { Text("Main Service Image URL (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(
                        onClick = {
                            mainImagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Text("Pick Main Image from Gallery")
                    }
                    OutlinedTextField(
                        value = serviceGalleryPhotos,
                        onValueChange = { serviceGalleryPhotos = it },
                        label = { Text("Gallery Image URLs (comma separated, optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Add Type / Plan",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = typeName,
                        onValueChange = { typeName = it },
                        label = { Text("Type (Interior / Exterior / Full)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = subscriptionPlan,
                        onValueChange = { subscriptionPlan = it.uppercase() },
                        label = { Text("Subscription Plan (WEEKLY/MONTHLY/YEARLY)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = typeDescription,
                        onValueChange = { typeDescription = it },
                        label = { Text("Type Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = typePrice,
                        onValueChange = { typePrice = it },
                        label = { Text("Type Price") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = typePhotos,
                        onValueChange = { typePhotos = it },
                        label = { Text("Photo URLs (comma separated, optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            val parsedTypePrice = typePrice.toDoubleOrNull()
                            if (typeName.isBlank() || parsedTypePrice == null || parsedTypePrice <= 0.0) {
                                Toast.makeText(context, "Enter valid type and type price", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val photos = typePhotos.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            serviceTypes.add(
                                ServiceTypeItem(
                                    typeName = typeName.trim(),
                                    typeDescription = typeDescription.trim().ifBlank { null },
                                    subscriptionPlan = subscriptionPlan.trim().ifBlank { null },
                                    typePrice = parsedTypePrice,
                                    photoUrls = photos
                                )
                            )
                            typeName = ""
                            typeDescription = ""
                            subscriptionPlan = "WEEKLY"
                            typePrice = ""
                            typePhotos = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Type To Service")
                    }

                    if (serviceTypes.isNotEmpty()) {
                        Text(
                            text = "Types: " + serviceTypes.joinToString { "${it.typeName} - ${it.subscriptionPlan ?: "PLAN"} - ${it.typePrice}" },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(
                        onClick = {
                            if (token.isNullOrBlank()) {
                                Toast.makeText(context, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val parsedPrice = servicePrice.toDoubleOrNull()
                            val hasDefaultPrice = parsedPrice != null && parsedPrice > 0.0
                            val galleryList = serviceGalleryPhotos.split(",")
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                            if (serviceName.isBlank() || (!hasDefaultPrice && serviceTypes.isEmpty())) {
                                Toast.makeText(
                                    context,
                                    "Enter service name and either default price or at least one type",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                            isCreating = true
                            coroutineScope.launch {
                                try {
                                    val response = RetrofitClient.labourApi.createService(
                                        CreateServiceRequest(
                                            serviceName = serviceName.trim(),
                                            description = serviceDescription.trim().ifBlank { null },
                                            price = if (hasDefaultPrice) parsedPrice else null,
                                            duration = serviceDuration.trim().ifBlank { null },
                                            serviceTypes = serviceTypes.toList(),
                                            mainImageUrl = serviceMainImageUrl.trim().ifBlank { null },
                                            galleryPhotoUrls = galleryList
                                        )
                                    )
                                    val body = response.body()
                                    if (response.isSuccessful && body != null) {
                                        Toast.makeText(context, "Service created successfully", Toast.LENGTH_SHORT).show()
                                        serviceName = ""
                                        serviceDescription = ""
                                        serviceDuration = ""
                                        servicePrice = ""
                                        serviceTypes.clear()
                                        onOpenServiceManager(0)
                                    } else {
                                        Toast.makeText(context, "Failed to create service", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isCreating = false
                                }
                            }
                        },
                        enabled = !isCreating,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (isCreating) "Creating..." else "Create Service")
                    }
                }
            }

            ActionCard(
                title = "Add / Map Service",
                subtitle = "Create a new service or map yourself to existing services",
                actionText = "Open Service Manager",
                onClick = { onOpenServiceManager(0) }
            )

            ActionCard(
                title = "My Mapped Services",
                subtitle = "View and search services you already mapped",
                actionText = "View My Services",
                onClick = { onOpenServiceManager(1) }
            )
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    actionText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = onClick) {
                    Text(actionText)
                }
            }
        }
    }
}

