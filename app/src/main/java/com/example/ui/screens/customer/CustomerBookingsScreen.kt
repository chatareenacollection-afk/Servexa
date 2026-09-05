package com.example.ui.screens.customer

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.BookingEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ServexaTopBar
import com.example.ui.components.StatusPill
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.ServexaViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CustomerBookingsScreen(
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier
) {
    val bookings by viewModel.customerBookings.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredBookings = remember(bookings, selectedFilter) {
        when (selectedFilter) {
            "ACTIVE" -> bookings.filter { it.status in listOf("REQUESTED", "ACCEPTED", "PROVIDER_ON_THE_WAY", "ARRIVED", "IN_PROGRESS") }
            "COMPLETED" -> bookings.filter { it.status == "COMPLETED" }
            "OTHER" -> bookings.filter { it.status in listOf("CANCELLED", "REJECTED", "DISPUTED", "REFUNDED") }
            else -> bookings
        }
    }

    val canNavigateBack by viewModel.canNavigateBack.collectAsState()

    Scaffold(
        topBar = {
            ServexaTopBar(
                title = "My Bookings",
                subtitle = "${bookings.size} total service orders",
                showBack = canNavigateBack,
                onBackClick = { viewModel.navigateBack() }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ALL" to "All",
                    "ACTIVE" to "Active",
                    "COMPLETED" to "Completed",
                    "OTHER" to "Other"
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { selectedFilter = key },
                        label = { Text(label) }
                    )
                }
            }

            if (filteredBookings.isEmpty()) {
                EmptyStateView(
                    title = "No bookings found",
                    subtitle = "When you book a professional service, your appointments will appear here.",
                    icon = Icons.Default.CalendarMonth,
                    actionButtonText = "Explore Services",
                    onActionClick = { viewModel.navigateTo(ScreenDestination.Home) }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredBookings, key = { it.id }) { booking ->
                        BookingItemCard(
                            booking = booking,
                            onClick = {
                                viewModel.navigateTo(ScreenDestination.ActiveBookingTracking(booking.id))
                            },
                            onCall = {
                                viewModel.initiateCall(
                                    bookingId = booking.id,
                                    receiverId = booking.providerId,
                                    receiverName = "Assigned Provider",
                                    receiverRole = "PROVIDER"
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookingItemCard(
    booking: BookingEntity,
    onClick: () -> Unit,
    onCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateStr = remember(booking.createdAt) {
        SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(booking.createdAt))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("booking_card_${booking.id}"),
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
                    text = "#${booking.id}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                StatusPill(status = booking.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = booking.serviceTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = dateStr,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Location: ${booking.address}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$${"%.2f".format(booking.price)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ServexaIndigo
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onCall,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = ServexaTeal)
                    }

                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ServexaIndigo)
                    ) {
                        Text("Track Order")
                    }
                }
            }
        }
    }
}
