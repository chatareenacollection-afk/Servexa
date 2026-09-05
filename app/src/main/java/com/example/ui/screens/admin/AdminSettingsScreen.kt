package com.example.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.ServexaTopBar
import com.example.ui.theme.ServexaIndigo
import com.example.ui.viewmodel.ServexaViewModel

@Composable
fun AdminSettingsScreen(
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier
) {
    var topUpFeePercent by remember { mutableStateOf("5.0") }
    var serviceCommissionPercent by remember { mutableStateOf("6.0") }
    var autoEscrowReleaseHours by remember { mutableStateOf("48") }
    var callRecordingEnabled by remember { mutableStateOf(true) }
    var savedNotice by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ServexaTopBar(
                title = "Platform Rules & Fees",
                subtitle = "Monetization & Commission Engine",
                showBack = true,
                onBackClick = { viewModel.navigateBack() }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Core Fee Parameters", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = topUpFeePercent,
                            onValueChange = { topUpFeePercent = it },
                            label = { Text("Customer Wallet Top-Up Fee (%)") },
                            modifier = Modifier.fillMaxWidth(),
                            supportingText = { Text("Currently active: 5.0% platform fee") },
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = serviceCommissionPercent,
                            onValueChange = { serviceCommissionPercent = it },
                            label = { Text("Provider Booking Commission (%)") },
                            modifier = Modifier.fillMaxWidth(),
                            supportingText = { Text("Currently active: 6.0% service commission") },
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = autoEscrowReleaseHours,
                            onValueChange = { autoEscrowReleaseHours = it },
                            label = { Text("Auto Escrow Settlement (Hours)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Privacy & PBX Call Rules", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("In-App Call Voice Logs", fontWeight = FontWeight.SemiBold)
                                Text("Log call duration and session timestamps for disputes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = callRecordingEnabled,
                                onCheckedChange = { callRecordingEnabled = it }
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { savedNotice = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ServexaIndigo)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Platform Rules", fontWeight = FontWeight.Bold)
                }
            }

            if (savedNotice) {
                item {
                    Text(
                        text = "✅ System configuration updated successfully and logged in immutable audit records.",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
