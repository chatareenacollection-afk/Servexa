package com.example.ui.screens.provider

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BookingEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.LiveLocationMapCanvas
import com.example.ui.components.ServexaTopBar
import com.example.ui.components.StatusPill
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.ServexaViewModel

@Composable
fun ProviderActiveJobsScreen(
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier
) {
    val bookings by viewModel.providerBookings.collectAsState()
    val activeJobs = remember(bookings) {
        bookings.filter { it.status in listOf("ACCEPTED", "PROVIDER_ON_THE_WAY", "ARRIVED", "IN_PROGRESS") }
    }

    Scaffold(
        topBar = {
            ServexaTopBar(
                title = "Active Jobs & Live GPS",
                subtitle = "${activeJobs.size} jobs currently in progress",
                showBack = true,
                onBackClick = { viewModel.navigateBack() }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (activeJobs.isEmpty()) {
            EmptyStateView(
                title = "No Active Jobs",
                subtitle = "Accepted customer appointments and jobs on the way will appear here with live GPS navigation.",
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(activeJobs, key = { it.id }) { job ->
                    ActiveJobActionCard(
                        job = job,
                        viewModel = viewModel,
                        onUpdateStatus = { nextStatus ->
                            viewModel.updateBookingStatus(job.id, nextStatus)
                            if (nextStatus == "PROVIDER_ON_THE_WAY") {
                                viewModel.broadcastCurrentLocationToBooking(job.id)
                            }
                        },
                        onCallCustomer = {
                            viewModel.initiateCall(
                                bookingId = job.id,
                                receiverId = job.customerId,
                                receiverName = "Customer",
                                receiverRole = "CUSTOMER"
                            )
                        },
                        onChatCustomer = {
                            viewModel.navigateTo(
                                ScreenDestination.Chat(
                                    recipientId = job.customerId,
                                    recipientName = "Customer (${job.serviceTitle})",
                                    bookingId = job.id
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveJobActionCard(
    job: BookingEntity,
    viewModel: ServexaViewModel,
    onUpdateStatus: (String) -> Unit,
    onCallCustomer: () -> Unit,
    onChatCustomer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val liveLocation by viewModel.observeBookingLiveLocation(job.id).collectAsState(initial = null)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${job.id}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                StatusPill(status = job.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = job.serviceTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text("Customer Address: ${job.address}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Text("Issue: ${job.problemDescription}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (job.specialInstructions.isNotEmpty()) {
                Text("Note: ${job.specialInstructions}", style = MaterialTheme.typography.bodySmall, color = ServexaAmber)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Embedded Live Navigation Map for Provider
            LiveLocationMapCanvas(
                providerName = "You (Technician)",
                customerName = job.address.take(24),
                status = job.status,
                distanceKm = liveLocation?.distanceKm ?: when (job.status) {
                    "PROVIDER_ON_THE_WAY" -> 1.4
                    "ARRIVED", "IN_PROGRESS" -> 0.05
                    else -> 2.8
                },
                etaMinutes = liveLocation?.etaMinutes ?: when (job.status) {
                    "PROVIDER_ON_THE_WAY" -> 4
                    "ARRIVED" -> 0
                    else -> 9
                },
                streetName = liveLocation?.streetName ?: "Market Street & 4th Ave",
                speedKmh = liveLocation?.speedKmh ?: 36.0,
                isProviderView = true,
                onRefreshGps = {
                    viewModel.broadcastCurrentLocationToBooking(job.id)
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing & Commission Breakdown
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Job Value: $${"%.2f".format(job.price)}", style = MaterialTheme.typography.labelMedium)
                    Text("Your Net (94%): $${"%.2f".format(job.providerNetAmount)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ServexaGreen)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Call, Chat & Status Advancement Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onChatCustomer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(imageVector = Icons.Default.ChatBubbleOutline, contentDescription = "Chat Customer", tint = ServexaIndigo)
                }

                IconButton(
                    onClick = onCallCustomer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = "Call Customer", tint = ServexaTeal)
                }

                IconButton(
                    onClick = { viewModel.broadcastCurrentLocationToBooking(job.id) },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(imageVector = Icons.Default.MyLocation, contentDescription = "Ping Live GPS", tint = ServexaAmber)
                }

                when (job.status) {
                    "ACCEPTED" -> {
                        Button(
                            onClick = { onUpdateStatus("PROVIDER_ON_THE_WAY") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ServexaIndigo)
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Travel to Job", fontSize = 13.sp)
                        }
                    }
                    "PROVIDER_ON_THE_WAY" -> {
                        Button(
                            onClick = { onUpdateStatus("ARRIVED") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ServexaTeal)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("I Have Arrived", fontSize = 13.sp)
                        }
                    }
                    "ARRIVED" -> {
                        Button(
                            onClick = { onUpdateStatus("IN_PROGRESS") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ServexaIndigoLight)
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Start Work", fontSize = 13.sp)
                        }
                    }
                    "IN_PROGRESS" -> {
                        Button(
                            onClick = { onUpdateStatus("COMPLETED") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ServexaGreen)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Complete & Claim", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
