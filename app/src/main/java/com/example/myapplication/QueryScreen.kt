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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.api.CustomerQueryRequest
import com.example.myapplication.api.UserQueryItem
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueryScreen(
    userEmail: String? = null,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf(userEmail ?: "") }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isSubmitLoading by remember { mutableStateOf(false) }
    var isFetchLoading by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var queries by remember { mutableStateOf<List<UserQueryItem>>(emptyList()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Queries") },
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
                    text = { Text("Submit Query") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        if (queries.isEmpty() && email.isNotBlank()) {
                            isFetchLoading = true
                            coroutineScope.launch {
                                try {
                                    val response = RetrofitClient.queryApi.getUserQueries(email)
                                    val body = response.body()
                                    if (response.isSuccessful && body?.success == true) {
                                        queries = body.queries
                                    } else {
                                        val errorMsg = body?.message ?: "Failed to fetch queries"
                                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Network error: ${e.localizedMessage}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } finally {
                                    isFetchLoading = false
                                }
                            }
                        }
                    },
                    text = { Text("My Queries") }
                )
            }

            if (selectedTab == 0) {
                SubmitQueryTab(
                    email = email,
                    subject = subject,
                    message = message,
                    isLoading = isSubmitLoading,
                    onEmailChange = { email = it },
                    onSubjectChange = { subject = it },
                    onMessageChange = { message = it },
                    onSubmit = {
                        if (email.isBlank() || subject.isBlank() || message.isBlank()) {
                            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                        } else {
                            isSubmitLoading = true
                            coroutineScope.launch {
                                try {
                                    val response = RetrofitClient.queryApi.submitQuery(
                                        CustomerQueryRequest(
                                            email = email,
                                            subject = subject,
                                            message = message
                                        )
                                    )
                                    val body = response.body()
                                    if (response.isSuccessful && body?.success == true) {
                                        Toast.makeText(
                                            context,
                                            "Query submitted successfully! Query ID: ${body.queryId}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        subject = ""
                                        message = ""
                                    } else {
                                        val errorMsg = body?.message ?: "Failed to submit query"
                                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
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
                        }
                    }
                )
            } else {
                MyQueriesTab(
                    email = email,
                    queries = queries,
                    isLoading = isFetchLoading,
                    onRefresh = {
                        if (email.isBlank()) {
                            Toast.makeText(context, "Email is required", Toast.LENGTH_SHORT).show()
                            return@MyQueriesTab
                        }
                        isFetchLoading = true
                        coroutineScope.launch {
                            try {
                                val response = RetrofitClient.queryApi.getUserQueries(email)
                                val body = response.body()
                                if (response.isSuccessful && body?.success == true) {
                                    queries = body.queries
                                } else {
                                    queries = emptyList()
                                    val errorMsg = body?.message ?: "Failed to fetch queries"
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Network error: ${e.localizedMessage}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } finally {
                                isFetchLoading = false
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SubmitQueryTab(
    email: String,
    subject: String,
    message: String,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onSubjectChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Customer Support",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
            text = "Have a question or need help? Submit your query below.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = email,
            onValueChange = onEmailChange,
                label = { Text("Your Email") },
                singleLine = true,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = subject,
            onValueChange = onSubjectChange,
                label = { Text("Subject") },
                placeholder = { Text("Brief description of your query") },
                singleLine = true,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = message,
            onValueChange = onMessageChange,
                label = { Text("Your Message") },
                placeholder = { Text("Describe your query in detail...") },
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                minLines = 5,
                maxLines = 10,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Button(
            onClick = onSubmit,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Submit Query",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                }
            }
        }
    }
}

@Composable
private fun MyQueriesTab(
    email: String,
    queries: List<UserQueryItem>,
    isLoading: Boolean,
    onRefresh: () -> Unit
            ) {
                Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                text = "My Requests",
                style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
            TextButton(onClick = onRefresh, enabled = !isLoading) {
                Text("Refresh")
            }
        }

        Text(
            text = if (email.isBlank()) "No user email found." else "Showing queries for $email",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (queries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No queries found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(queries) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = item.subject ?: "No subject",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.message ?: "",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Status: ${item.status ?: "UNKNOWN"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
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

@Preview(showBackground = true)
@Composable
private fun QueryScreenPreview() {
    MyApplicationTheme {
        QueryScreen(userEmail = "user@example.com")
    }
}
