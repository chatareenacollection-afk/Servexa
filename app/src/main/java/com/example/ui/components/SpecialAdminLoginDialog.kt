package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ServexaIndigo
import com.example.ui.theme.ServexaRose
import com.example.ui.theme.ServexaTeal
import com.example.ui.viewmodel.ServexaViewModel

@Composable
fun SpecialAdminLoginDialog(
    viewModel: ServexaViewModel,
    onDismiss: () -> Unit
) {
    var authMode by remember { mutableStateOf(0) } // 0: Security Passkey / Master PIN, 1: Admin Credentials
    var adminKeyInput by remember { mutableStateOf("") }
    var adminEmailInput by remember { mutableStateOf("admin@servexa.com") }
    var adminPasswordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isAuthenticating by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(ServexaRose.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin Gate",
                        tint = ServexaRose,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "Special Admin Portal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Restricted Enterprise Gateway",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ServexaRose.copy(alpha = 0.10f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ServexaRose.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = ServexaRose,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Access is restricted to authorized platform administrators only. All login attempts are cryptographically audited.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                item {
                    TabRow(
                        selectedTabIndex = authMode,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = authMode == 0,
                            onClick = { authMode = 0 },
                            text = { Text("Master Passkey", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = authMode == 1,
                            onClick = { authMode = 1 },
                            text = { Text("Admin Account", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                if (authMode == 0) {
                    item {
                        OutlinedTextField(
                            value = adminKeyInput,
                            onValueChange = { adminKeyInput = it },
                            label = { Text("Admin Passkey or Master PIN") },
                            placeholder = { Text("e.g. ADMIN-2026 or 778899") },
                            leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = ServexaRose) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("special_admin_key_input")
                        )
                    }

                    item {
                        Text(
                            text = "Quick Demo Master Key:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { adminKeyInput = "ADMIN-2026" },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("ADMIN-2026", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { adminKeyInput = "778899" },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("PIN 778899", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    item {
                        OutlinedTextField(
                            value = adminEmailInput,
                            onValueChange = { adminEmailInput = it },
                            label = { Text("Admin Email") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("special_admin_email_input")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = adminPasswordInput,
                            onValueChange = { adminPasswordInput = it },
                            label = { Text("Admin Master Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("special_admin_password_input")
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = {
                                adminEmailInput = "admin@servexa.com"
                                adminPasswordInput = "admin123"
                            }) {
                                Text("Fill Default Admin Credentials", fontSize = 11.sp, color = ServexaRose)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isAuthenticating = true
                    if (authMode == 0) {
                        viewModel.loginSpecialAdmin(adminKeyInput) { success ->
                            isAuthenticating = false
                            if (success) {
                                onDismiss()
                            }
                        }
                    } else {
                        viewModel.loginSpecialAdmin(
                            passcodeOrKey = adminPasswordInput,
                            adminEmail = adminEmailInput
                        ) { success ->
                            isAuthenticating = false
                            if (success) {
                                onDismiss()
                            }
                        }
                    }
                },
                enabled = if (authMode == 0) adminKeyInput.isNotBlank() else (adminEmailInput.isNotBlank() && adminPasswordInput.isNotBlank()),
                colors = ButtonDefaults.buttonColors(containerColor = ServexaRose),
                modifier = Modifier.testTag("special_admin_submit_btn")
            ) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Authorize & Enter Admin")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
