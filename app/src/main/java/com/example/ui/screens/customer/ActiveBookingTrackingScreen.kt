package com.example.ui.screens.customer

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LiveLocationMapCanvas
import com.example.ui.components.RatingBadge
import com.example.ui.components.ServexaTopBar
import com.example.ui.components.StatusPill
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.ServexaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveBookingTrackingScreen(
    bookingId: String,
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier
) {
    val bookings by viewModel.customerBookings.collectAsState()
    val booking = remember(bookings, bookingId) {
        bookings.find { it.id == bookingId }
    }

    val allProviders by viewModel.searchResults.collectAsState()
    val providerItem = remember(allProviders, booking?.providerId) {
        allProviders.find { it.user.id == booking?.providerId }
    }

    val liveLocation by viewModel.observeBookingLiveLocation(bookingId).collectAsState(initial = null)

    var showReviewDialog by remember { mutableStateOf(false) }
    var reviewRating by remember { mutableStateOf(5.0) }
    var reviewComment by remember { mutableStateOf("") }

    var showDisputeDialog by remember { mutableStateOf(false) }
    var disputeReason by remember { mutableStateOf("Service Quality / Incomplete Work") }
    var disputeDescription by remember { mutableStateOf("") }

    val isCompleted = booking?.status == "COMPLETED"
    val isDisputed = booking?.status == "DISPUTED"

    Scaffold(
        topBar = {
            ServexaTopBar(
                title = "Live GPS Service Tracker",
                subtitle = "Booking #${booking?.id ?: bookingId}",
                showBack = true,
                onBackClick = { viewModel.navigateBack() },
                actions = {
                    if (providerItem != null) {
                        IconButton(
                            onClick = {
                                viewModel.navigateTo(
                                    ScreenDestination.Chat(
                                        recipientId = providerItem.user.id,
                                        recipientName = providerItem.user.name,
                                        bookingId = booking?.id ?: bookingId
                                    )
                                )
                            }
                        ) {
                            Icon(imageVector = Icons.Default.ChatBubbleOutline, contentDescription = "Chat", tint = MaterialTheme.colorScheme.primary)
                        }

                        IconButton(
                            onClick = {
                                viewModel.initiateCall(
                                    bookingId = booking?.id ?: bookingId,
                                    receiverId = providerItem.user.id,
                                    receiverName = providerItem.user.name,
                                    receiverRole = "PROVIDER"
                                )
                            },
                            modifier = Modifier.testTag("booking_tracker_call_button")
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = "Call Provider", tint = ServexaTeal)
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (booking == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading service tracking details...")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Status Header Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                    text = booking.serviceTitle,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                StatusPill(status = booking.status)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Location: ${booking.address}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "Problem: ${booking.problemDescription}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Provider Live Tracking Map
                item {
                    LiveLocationMapCanvas(
                        providerName = providerItem?.user?.name ?: "Service Professional",
                        status = booking.status,
                        distanceKm = liveLocation?.distanceKm ?: when (booking.status) {
                            "PROVIDER_ON_THE_WAY" -> 1.4
                            "ARRIVED", "IN_PROGRESS" -> 0.05
                            else -> 3.2
                        },
                        etaMinutes = liveLocation?.etaMinutes ?: when (booking.status) {
                            "PROVIDER_ON_THE_WAY" -> 4
                            "ARRIVED" -> 0
                            else -> 12
                        },
                        customerName = booking.address.take(28),
                        streetName = liveLocation?.streetName ?: "Market Street & 4th Ave",
                        speedKmh = liveLocation?.speedKmh ?: 34.0,
                        isProviderView = false,
                        onRefreshGps = {
                            viewModel.broadcastCurrentLocationToBooking(booking.id)
                        }
                    )
                }

                // Share GPS & Sync live location actions
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.broadcastCurrentLocationToBooking(booking.id)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp), tint = ServexaTeal)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync My GPS Beacon", fontSize = 12.sp)
                        }

                        if (providerItem != null) {
                            Button(
                                onClick = {
                                    viewModel.sendChatLocation(
                                        recipientId = providerItem.user.id,
                                        recipientName = providerItem.user.name,
                                        lat = 37.7749,
                                        lng = -122.4194,
                                        address = booking.address,
                                        bookingId = booking.id
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ServexaIndigo)
                            ) {
                                Icon(Icons.Default.ShareLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Send Pin to Chat", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Assigned Professional Card
                if (providerItem != null) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(ServexaIndigo, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = providerItem.user.name.take(2).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = providerItem.user.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = providerItem.profile.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    RatingBadge(rating = providerItem.profile.rating, reviewCount = providerItem.profile.reviewCount)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.navigateTo(
                                                ScreenDestination.Chat(
                                                    recipientId = providerItem.user.id,
                                                    recipientName = providerItem.user.name,
                                                    bookingId = booking.id
                                                )
                                            )
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Chat", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Chat", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.initiateCall(
                                                bookingId = booking.id,
                                                receiverId = providerItem.user.id,
                                                receiverName = providerItem.user.name,
                                                receiverRole = "PROVIDER"
                                            )
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ServexaTeal),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Call", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Lifecycle Simulation Controls (Allows user to simulate pro progress for testing)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Service Progression Simulator",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Advance status to test real-time escrow, commission release, and review flow:",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                when (booking.status) {
                                    "REQUESTED" -> {
                                        Button(
                                            onClick = { viewModel.updateBookingStatus(booking.id, "ACCEPTED") },
                                            modifier = Modifier.weight(1f)
                                        ) { Text("Accept") }
                                    }
                                    "ACCEPTED" -> {
                                        Button(
                                            onClick = { viewModel.updateBookingStatus(booking.id, "PROVIDER_ON_THE_WAY") },
                                            modifier = Modifier.weight(1f)
                                        ) { Text("On The Way") }
                                    }
                                    "PROVIDER_ON_THE_WAY" -> {
                                        Button(
                                            onClick = { viewModel.updateBookingStatus(booking.id, "ARRIVED") },
                                            modifier = Modifier.weight(1f)
                                        ) { Text("Mark Arrived") }
                                    }
                                    "ARRIVED" -> {
                                        Button(
                                            onClick = { viewModel.updateBookingStatus(booking.id, "IN_PROGRESS") },
                                            modifier = Modifier.weight(1f)
                                        ) { Text("Start Work") }
                                    }
                                    "IN_PROGRESS" -> {
                                        Button(
                                            onClick = { viewModel.updateBookingStatus(booking.id, "COMPLETED") },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = ServexaGreen)
                                        ) { Text("Complete Job (Release 94% Funds)") }
                                    }
                                    "COMPLETED" -> {
                                        Text(
                                            text = "✅ Service completed & 6% platform commission successfully settled.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = ServexaGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Completion Review Action
                if (isCompleted) {
                    item {
                        Button(
                            onClick = { showReviewDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("leave_review_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ServexaAmber),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Rate & Review Provider", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }

                // Dispute / Support Button
                if (!isCompleted && !isDisputed) {
                    item {
                        OutlinedButton(
                            onClick = { showDisputeDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("open_dispute_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ServexaRose),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.ReportProblem, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Report Issue / Open Dispute")
                        }
                    }
                }
            }
        }
    }

    // Leave Review Dialog
    if (showReviewDialog && booking != null) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Rate & Review Service", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("How was your experience with ${providerItem?.user?.name ?: "the provider"}?")
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { reviewRating = star.toDouble() }) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "$star Stars",
                                    tint = if (star <= reviewRating) ServexaAmber else Color.Gray,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = reviewComment,
                        onValueChange = { reviewComment = it },
                        label = { Text("Write your review (Optional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("review_comment_input"),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitReview(booking.id, reviewRating, reviewComment) {
                            showReviewDialog = false
                        }
                    },
                    modifier = Modifier.testTag("submit_review_button")
                ) {
                    Text("Submit Review")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Open Dispute Dialog
    if (showDisputeDialog && booking != null) {
        AlertDialog(
            onDismissRequest = { showDisputeDialog = false },
            title = { Text("Open Dispute", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Please explain why this service requires administrative review:")

                    OutlinedTextField(
                        value = disputeReason,
                        onValueChange = { disputeReason = it },
                        label = { Text("Dispute Reason") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = disputeDescription,
                        onValueChange = { disputeDescription = it },
                        label = { Text("Provide details") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.openDispute(booking.id, disputeReason, disputeDescription) {
                            showDisputeDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ServexaRose)
                ) {
                    Text("Submit to Admin")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisputeDialog = false }) { Text("Cancel") }
            }
        )
    }
}
