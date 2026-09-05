package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import com.example.ui.components.LocationButtonVariant
import com.example.ui.components.ServexaTopBar
import com.example.ui.components.UseCurrentLocationButton
import com.example.ui.components.WalletTopUpDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.ServexaViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingWorkflowScreen(
    providerId: String,
    initialServiceId: String?,
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier
) {
    val allProviders by viewModel.searchResults.collectAsState()
    val providerItem = remember(allProviders, providerId) {
        allProviders.find { it.user.id == providerId }
    }

    val customerWallet by viewModel.customerWallet.collectAsState()
    val paymentMethods by viewModel.paymentMethods.collectAsState()

    var selectedServiceId by remember {
        mutableStateOf(initialServiceId ?: providerItem?.services?.firstOrNull()?.id ?: "")
    }

    val selectedService = remember(providerItem, selectedServiceId) {
        providerItem?.services?.find { it.id == selectedServiceId }
            ?: providerItem?.services?.firstOrNull()
    }

    var serviceAddress by remember { mutableStateOf(viewModel.userLocationName.value.ifBlank { "742 Evergreen Terrace, Apt 4B, San Francisco, CA" }) }
    var problemDescription by remember { mutableStateOf("Need immediate diagnosis and repair for sparking switch and trip.") }
    var specialInstructions by remember { mutableStateOf("Please call when at the gate code #4190.") }
    var selectedDateText by remember { mutableStateOf("Tomorrow, 10:00 AM") }

    var showTopUpDialog by remember { mutableStateOf(false) }

    val servicePrice = selectedService?.price ?: 0.0
    val walletBalance = customerWallet?.availableBalance ?: 0.0
    val hasEnoughBalance = walletBalance >= servicePrice

    // 6% Platform Commission Rule Calculation
    val commissionAmount = (servicePrice * 0.06 * 100.0).roundToInt() / 100.0
    val providerNetEarning = servicePrice - commissionAmount

    Scaffold(
        topBar = {
            ServexaTopBar(
                title = "Book Service",
                subtitle = "Provider: ${providerItem?.user?.name ?: "Expert"}",
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
            // 1. Service Selection
            item {
                Text(
                    text = "1. Select Service Package",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (providerItem != null) {
                items(providerItem.services, key = { it.id }) { srv ->
                    val isSelected = (srv.id == selectedServiceId)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedServiceId = srv.id }
                            .testTag("service_item_${srv.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = srv.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${srv.durationMinutes} min • ${srv.description}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "$${"%.2f".format(srv.price)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = ServexaIndigo
                            )
                        }
                    }
                }
            }

            // 2. Schedule & Address
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "2. Appointment & Location",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    UseCurrentLocationButton(
                        viewModel = viewModel,
                        buttonText = "Use Current Location",
                        variant = LocationButtonVariant.TONAL,
                        onLocationCaptured = { loc ->
                            serviceAddress = loc.address
                        }
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = selectedDateText,
                    onValueChange = { selectedDateText = it },
                    label = { Text("Appointment Date & Time") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = serviceAddress,
                    onValueChange = { serviceAddress = it },
                    label = { Text("Service Location Address") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("booking_address_input"),
                    singleLine = true
                )
            }

            // 3. Problem description & instructions
            item {
                Text(
                    text = "3. Problem Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                OutlinedTextField(
                    value = problemDescription,
                    onValueChange = { problemDescription = it },
                    label = { Text("Describe the issue or task") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("booking_problem_desc_input"),
                    minLines = 2
                )
            }

            item {
                OutlinedTextField(
                    value = specialInstructions,
                    onValueChange = { specialInstructions = it },
                    label = { Text("Special Access / Gate Instructions (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // 4. Financial Breakdown Card (With 6% Commission Platform Transparency)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Payment Summary & Platform Escrow",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Service Package Price:", style = MaterialTheme.typography.bodyMedium)
                            Text("$${"%.2f".format(servicePrice)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Platform Commission (6%):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$${"%.2f".format(commissionAmount)} (Included in price)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Your Wallet Balance:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "$${"%.2f".format(walletBalance)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (hasEnoughBalance) ServexaGreen else ServexaRose
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Due Now:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$${"%.2f".format(servicePrice)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = ServexaIndigo
                            )
                        }
                    }
                }
            }

            // 5. Submit / Top Up Button
            item {
                if (!hasEnoughBalance) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Insufficient wallet funds. Please top up your wallet to reserve this service.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ServexaRose
                        )
                        Button(
                            onClick = { showTopUpDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("topup_from_booking_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ServexaTeal)
                        ) {
                            Icon(Icons.Default.AddCard, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Top Up Wallet (5% Fee Applies)", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            if (selectedService != null && serviceAddress.isNotBlank()) {
                                viewModel.createBooking(
                                    providerId = providerId,
                                    serviceId = selectedService.id,
                                    address = serviceAddress,
                                    scheduledAt = System.currentTimeMillis() + 86400000L,
                                    problemDescription = problemDescription,
                                    specialInstructions = specialInstructions,
                                    onSuccess = { bookingId -> }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("confirm_booking_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ServexaIndigo)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Confirm & Reserve Service ($${"%.2f".format(servicePrice)})",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }

    if (showTopUpDialog) {
        WalletTopUpDialog(
            currentBalance = walletBalance,
            availablePaymentMethods = paymentMethods,
            onDismiss = { showTopUpDialog = false },
            onTopUp = { amount, method, ref -> viewModel.topUpWallet(amount, method, ref) }
        )
    }
}
