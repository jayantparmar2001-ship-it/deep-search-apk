package com.example.myapplication

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.api.LoginRequest
import com.example.myapplication.api.RegisterRequest
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    preferredRole: String? = null,
    onBackToRoleSelection: () -> Unit = {},
    onLoginSuccess: (String?, String?, String?, String?) -> Unit = { _, _, _, _ -> }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isLoginMode by remember { mutableStateOf(true) }
    var usePhoneLogin by remember { mutableStateOf(false) } // Toggle between email/password and phone/OTP
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) } // Track if OTP has been sent
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isSendingOtp by remember { mutableStateOf(false) }
    val normalizedPreferredRole = preferredRole?.uppercase()
    val isRoleLocked = normalizedPreferredRole == "CUSTOMER" || normalizedPreferredRole == "LABOUR"
    var selectedRole by remember(normalizedPreferredRole) {
        mutableStateOf(
            normalizedPreferredRole ?: "CUSTOMER"
        )
    }

    var selectedProfileImageUri by remember { mutableStateOf<Uri?>(null) }

    val profileImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            selectedProfileImageUri = it
            profileImageUrl = it.toString()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // App Icon
        Icon(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "App Icon",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = if (isLoginMode) {
                if (isRoleLocked && normalizedPreferredRole == "LABOUR") "Welcome Labour Provider!" else "Welcome Back!"
            } else {
                if (isRoleLocked && normalizedPreferredRole == "LABOUR") "Create Labour Account" else "Create Account"
            },
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = if (isRoleLocked) {
                if (normalizedPreferredRole == "LABOUR") "Continue as Labour Provider" else "Continue as Customer"
            } else {
                if (isLoginMode) "Sign in to continue" else "Sign up to get started"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Login method toggle (Login mode only)
        AnimatedVisibility(visible = isLoginMode) {
            Column {
                Text(
                    text = "Login Method",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { usePhoneLogin = false },
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!usePhoneLogin)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text("Email")
                    }
                    Button(
                        onClick = { usePhoneLogin = true },
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (usePhoneLogin)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text("Phone")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Phone/OTP Login Fields
        AnimatedVisibility(visible = isLoginMode && usePhoneLogin) {
            Column {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    placeholder = { Text("+1234567890") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading && !otpSent,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (otpSent) {
                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { if (it.length <= 6) otpCode = it },
                        label = { Text("Enter OTP") },
                        placeholder = { Text("123456") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            otpSent = false
                            otpCode = ""
                        },
                        enabled = !isLoading
                    ) {
                        Text("Change Phone Number")
                    }
                } else {
                    Button(
                        onClick = {
                            if (phoneNumber.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Please enter your phone number",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                isSendingOtp = true
                                coroutineScope.launch {
                                    try {
                                        val response = RetrofitClient.authApi.sendOtp(
                                            com.example.myapplication.api.SendOtpRequest(phoneNumber = phoneNumber)
                                        )
                                        val body = response.body()
                                        if (response.isSuccessful && body?.success == true) {
                                            otpSent = true
                                            Toast.makeText(
                                                context,
                                                body.message ?: "OTP sent successfully",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            val errorMsg = body?.message ?: "Failed to send OTP"
                                            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "Network error: ${e.localizedMessage}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } finally {
                                        isSendingOtp = false
                                    }
                                }
                            }
                        },
                        enabled = !isLoading && !isSendingOtp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isSendingOtp) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Send OTP")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Name field (Sign Up only)
        AnimatedVisibility(visible = !isLoginMode) {
            Column {
                if (!isRoleLocked) {
                    Text(
                        text = "Account Type",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { selectedRole = "CUSTOMER" },
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedRole == "CUSTOMER")
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text("Customer")
                        }
                        Button(
                            onClick = { selectedRole = "LABOUR" },
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedRole == "LABOUR")
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text("Labour")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Text(
                        text = "Account Type: ${if (normalizedPreferredRole == "LABOUR") "Labour" else "Customer"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = profileImageUrl,
                    onValueChange = { profileImageUrl = it },
                    label = { Text("Profile Image URL (optional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        profileImagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Pick Profile Image from Gallery")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Email field (Email login or Sign Up)
        AnimatedVisibility(visible = !isLoginMode || (isLoginMode && !usePhoneLogin)) {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Password field (Email login or Sign Up)
        AnimatedVisibility(visible = !isLoginMode || (isLoginMode && !usePhoneLogin)) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = if (isLoginMode) ImeAction.Done else ImeAction.Next
                ),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(
                            text = if (passwordVisible) "Hide" else "Show",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

                // Confirm Password field (Sign Up only)
                AnimatedVisibility(visible = !isLoginMode) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading,
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            trailingIcon = {
                                IconButton(onClick = {
                                    confirmPasswordVisible = !confirmPasswordVisible
                                }) {
                                    Text(
                                        text = if (confirmPasswordVisible) "Hide" else "Show",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Forgot password (Email login only)
                AnimatedVisibility(visible = isLoginMode && !usePhoneLogin) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            Toast.makeText(context, "Forgot password clicked", Toast.LENGTH_SHORT)
                                .show()
                        }) {
                            Text("Forgot Password?")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isRoleLocked) {
                    TextButton(
                        onClick = onBackToRoleSelection,
                        enabled = !isLoading
                    ) {
                        Text("Change user type")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Login / Sign Up button
                Button(
                    onClick = {
                        if (isLoginMode) {
                            if (usePhoneLogin) {
                                // --- PHONE/OTP LOGIN: verify OTP ---
                                if (!otpSent) {
                                    Toast.makeText(
                                        context,
                                        "Please send OTP first",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else if (otpCode.isBlank() || otpCode.length != 6) {
                                    Toast.makeText(
                                        context,
                                        "Please enter a valid 6-digit OTP",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    isLoading = true
                                    coroutineScope.launch {
                                        try {
                                            val response = RetrofitClient.authApi.verifyOtp(
                                                com.example.myapplication.api.VerifyOtpRequest(
                                                    phoneNumber = phoneNumber,
                                                    otpCode = otpCode
                                                )
                                            )
                                            val body = response.body()
                                            if (response.isSuccessful && body?.success == true) {
                                                Toast.makeText(
                                                    context,
                                                    "Welcome ${body.name}!",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                onLoginSuccess(
                                                    body.name,
                                                    body.email,
                                                    body.token,
                                                    body.role
                                                )
                                                // Reset OTP fields
                                                otpSent = false
                                                otpCode = ""
                                                phoneNumber = ""
                                            } else {
                                                val errorMsg =
                                                    body?.message ?: "OTP verification failed"
                                                Toast.makeText(
                                                    context,
                                                    errorMsg,
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
                                            isLoading = false
                                        }
                                    }
                                }
                            } else {
                                // --- EMAIL/PASSWORD LOGIN: validate and call deep-search API ---
                                if (email.isBlank() || password.isBlank()) {
                                    Toast.makeText(
                                        context,
                                        "Please fill in all fields",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    isLoading = true
                                    coroutineScope.launch {
                                        try {
                                            val response = RetrofitClient.authApi.login(
                                                LoginRequest(email = email, password = password)
                                            )
                                            val body = response.body()
                                            if (response.isSuccessful && body?.success == true) {
                                                Toast.makeText(
                                                    context,
                                                    "Welcome ${body.name}! Token: ${body.token}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                onLoginSuccess(
                                                    body.name,
                                                    body.email,
                                                    body.token,
                                                    body.role
                                                )
                                            } else {
                                                val errorMsg = body?.message ?: "Login failed"
                                                Toast.makeText(
                                                    context,
                                                    errorMsg,
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
                                            isLoading = false
                                        }
                                    }
                                }
                            }
                        } else {
                            // --- SIGN UP: validate and call deep-search register API ---
                            if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Please fill in all fields",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else if (password != confirmPassword) {
                                Toast.makeText(
                                    context,
                                    "Passwords do not match",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                isLoading = true
                                coroutineScope.launch {
                                    try {
                                        val response = RetrofitClient.authApi.register(
                                            RegisterRequest(
                                                name = name,
                                                email = email,
                                                password = password,
                                                role = selectedRole,
                                                profileImageUrl = profileImageUrl.ifBlank { null }
                                            )
                                        )
                                        val body = response.body()
                                        if (response.isSuccessful && body?.success == true) {
                                            Toast.makeText(
                                                context,
                                                "Account created! Welcome ${body.name}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            // Switch to login mode after successful registration
                                            isLoginMode = true
                                            name = ""
                                            email = ""
                                            password = ""
                                            confirmPassword = ""
                                        } else {
                                            val errorMsg = body?.message ?: "Registration failed"
                                            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "Network error: ${e.localizedMessage}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
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
                        Text(
                            text = when {
                                isLoginMode && usePhoneLogin && otpSent -> "Verify OTP"
                                isLoginMode && usePhoneLogin -> "Send OTP"
                                isLoginMode -> "Login"
                                else -> "Sign Up"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Toggle between Login and Sign Up
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isLoginMode) "Don't have an account?" else "Already have an account?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = {
                            isLoginMode = !isLoginMode
                            // Reset fields when switching modes
                            name = ""
                            email = ""
                            password = ""
                            confirmPassword = ""
                            profileImageUrl = ""
                            phoneNumber = ""
                            otpCode = ""
                            otpSent = false
                            passwordVisible = false
                            confirmPasswordVisible = false
                            selectedRole = normalizedPreferredRole ?: "CUSTOMER"
                            usePhoneLogin = false
                        },
                        enabled = !isLoading
                    ) {
                        Text(
                            text = if (isLoginMode) "Sign Up" else "Login",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }

        @Preview(showBackground = true)
        @Composable
        fun AuthScreenLoginPreview() {
            MyApplicationTheme {
                AuthScreen()
            }
        }


