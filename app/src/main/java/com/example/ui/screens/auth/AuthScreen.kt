package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ServexaTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.ServexaViewModel

@Composable
fun AuthScreen(
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier
) {
    val canNavigateBack by viewModel.canNavigateBack.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Login, 1: Register

    var loginMode by remember { mutableStateOf(0) } // 0: Email/Password, 1: Mobile Phone + OTP
    var loginEmailOrUsername by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    var loginPhone by remember { mutableStateOf("") }
    var loginOtpCode by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }

    var registerName by remember { mutableStateOf("") }
    var registerEmail by remember { mutableStateOf("") }
    var registerPhone by remember { mutableStateOf("") }
    var registerPassword by remember { mutableStateOf("") }
    var registerPasswordVisible by remember { mutableStateOf(false) }
    var registerRole by remember { mutableStateOf("CUSTOMER") }
    var registerTitle by remember { mutableStateOf("") }
    var registerBio by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            ServexaTopBar(
                title = if (selectedTab == 0) "Sign In" else "Create Account",
                subtitle = "Servexa On-Demand Services",
                showBack = canNavigateBack,
                onBackClick = { viewModel.navigateBack() }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            // Brand Logo & Header
            item {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(
                            Brush.linearGradient(listOf(ServexaIndigo, ServexaTeal)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Handyman,
                        contentDescription = "Servexa",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Servexa",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = ServexaIndigo
                )

                Text(
                    text = "On-Demand Service Marketplace Platform",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Continue as Guest Option
            item {
                OutlinedButton(
                    onClick = {
                        viewModel.navigateTo(ScreenDestination.Home)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Explore Marketplace as Guest", fontWeight = FontWeight.SemiBold)
                }
            }

            // Fast Google 1-Tap Sign-In
            item {
                OutlinedButton(
                    onClick = {
                        viewModel.loginWithGoogle()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("google_direct_login_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = "Google Sign In",
                        tint = ServexaIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Continue with Google (Instant Sign In)", fontWeight = FontWeight.SemiBold)
                }
            }

            // Auth Tabs
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Sign In", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Create Account", fontWeight = FontWeight.Bold) }
                    )
                }
            }

            if (selectedTab == 0) {
                // Log In Options: Standard or Phone OTP
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = loginMode == 0,
                            onClick = { loginMode = 0 },
                            label = { Text("Email / Account") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = loginMode == 1,
                            onClick = { loginMode = 1 },
                            label = { Text("Phone with OTP") },
                            leadingIcon = { Icon(Icons.Default.PhoneIphone, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (loginMode == 0) {
                    item {
                        OutlinedTextField(
                            value = loginEmailOrUsername,
                            onValueChange = { loginEmailOrUsername = it },
                            label = { Text("Email Address or Username") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_email_input"),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                    Icon(
                                        imageVector = if (loginPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Password"
                                    )
                                }
                            },
                            visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input"),
                            singleLine = true
                        )
                    }

                    item {
                        Button(
                            onClick = {
                                if (loginEmailOrUsername.isNotBlank() && loginPassword.isNotBlank()) {
                                    viewModel.login(loginEmailOrUsername, loginPassword)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("login_submit_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ServexaIndigo)
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign In", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Phone Number with OTP verification
                    item {
                        OutlinedTextField(
                            value = loginPhone,
                            onValueChange = { loginPhone = it },
                            label = { Text("Mobile Phone Number") },
                            placeholder = { Text("+1 (555) 000-0000") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            trailingIcon = {
                                TextButton(
                                    onClick = {
                                        if (loginPhone.isNotBlank()) {
                                            otpSent = true
                                            loginOtpCode = "8899"
                                            viewModel.showMessage("OTP code sent to $loginPhone: 8899")
                                        }
                                    }
                                ) {
                                    Text(if (otpSent) "Resend" else "Get Code", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_phone_input"),
                            singleLine = true
                        )
                    }

                    if (otpSent) {
                        item {
                            OutlinedTextField(
                                value = loginOtpCode,
                                onValueChange = { loginOtpCode = it },
                                label = { Text("Verification OTP Code") },
                                placeholder = { Text("Enter 4-digit code") },
                                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_otp_input"),
                                singleLine = true
                            )
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                if (loginPhone.isNotBlank()) {
                                    viewModel.loginWithPhoneOtp(loginPhone, loginOtpCode.ifBlank { "8899" })
                                }
                            },
                            enabled = loginPhone.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("login_otp_submit_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ServexaTeal)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verify & Sign In", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Register Form
                item {
                    Text(
                        text = "Select Account Type:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = registerRole == "CUSTOMER",
                            onClick = { registerRole = "CUSTOMER" },
                            label = { Text("Hire Services (Customer)") },
                            leadingIcon = {
                                if (registerRole == "CUSTOMER") {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = registerRole == "PROVIDER",
                            onClick = { registerRole = "PROVIDER" },
                            label = { Text("Offer Services (Pro)") },
                            leadingIcon = {
                                if (registerRole == "PROVIDER") {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (registerRole == "PROVIDER") {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "All new business & service provider signups are reviewed and verified before receiving live customer requests.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = registerName,
                        onValueChange = { registerName = it },
                        label = { Text("Full Name or Business Name") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_name_input"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = registerEmail,
                        onValueChange = { registerEmail = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_email_input"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = registerPhone,
                        onValueChange = { registerPhone = it },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_phone_input"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = registerPassword,
                        onValueChange = { registerPassword = it },
                        label = { Text("Create Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { registerPasswordVisible = !registerPasswordVisible }) {
                                Icon(
                                    imageVector = if (registerPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Password"
                                )
                            }
                        },
                        visualTransformation = if (registerPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_password_input"),
                        singleLine = true
                    )
                }

                if (registerRole == "PROVIDER") {
                    item {
                        OutlinedTextField(
                            value = registerTitle,
                            onValueChange = { registerTitle = it },
                            label = { Text("Professional Title (e.g. Master Electrician)") },
                            leadingIcon = { Icon(Icons.Default.Engineering, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = registerBio,
                            onValueChange = { registerBio = it },
                            label = { Text("Business Experience & Bio") },
                            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                    }
                }

                item {
                    Button(
                        onClick = {
                            if (registerName.isNotBlank() && registerEmail.isNotBlank() && registerPassword.isNotBlank()) {
                                viewModel.register(
                                    name = registerName,
                                    email = registerEmail,
                                    phone = registerPhone,
                                    passwordRaw = registerPassword,
                                    role = registerRole,
                                    title = if (registerRole == "PROVIDER") (registerTitle.ifBlank { "Professional Specialist" }) else null,
                                    bio = registerBio
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("register_submit_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ServexaTeal)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (registerRole == "PROVIDER") "Submit Provider Application" else "Create Customer Account", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
