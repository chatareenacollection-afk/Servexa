package com.example.ui.screens.provider

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.BookingEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.ServexaViewModel

@Composable
fun ProviderDashboardScreen(
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val bookings by viewModel.providerBookings.collectAsState()
    val wallet by viewModel.customerWallet.collectAsState()
    val store by viewModel.currentProviderStore.collectAsState()

    var isOnline by remember { mutableStateOf(true) }
    var isEmergencyReady by remember { mutableStateOf(true) }

    val pendingRequests = remember(bookings) {
        bookings.filter { it.status == "REQUESTED" }
    }
    val activeJobs = remember(bookings) {
        bookings.filter { it.status in listOf("ACCEPTED", "PROVIDER_ON_THE_WAY", "ARRIVED", "IN_PROGRESS") }
    }
    val completedJobs = remember(bookings) {
        bookings.filter { it.status == "COMPLETED" }
    }

    Scaffold(
        topBar = {
            ServexaTopBar(
                title = "Provider Console",
                subtitle = "Master Professional Dashboard",
                actions = {
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("provider_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Log Out",
                            tint = ServexaRose
                        )
                    }
                }
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
            // 1. Provider Status Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .background(
                                            Brush.linearGradient(listOf(ServexaIndigo, ServexaTeal)),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentUser?.name?.take(2)?.uppercase() ?: "PR",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Column {
                                    Text(
                                        text = currentUser?.name ?: "Provider Pro",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    VerificationBadge(isVerified = true)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isOnline) "ONLINE" else "OFFLINE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOnline) ServexaGreen else Color.Gray
                                )
                                Switch(
                                    checked = isOnline,
                                    onCheckedChange = { isOnline = it }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = ServexaAmber)
                                Text("24/7 Emergency Dispatch Mode", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                            Switch(
                                checked = isEmergencyReady,
                                onCheckedChange = { isEmergencyReady = it }
                            )
                        }
                    }
                }
            }

            // 2. Metrics 4-Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Wallet Balance",
                        value = "$${"%.2f".format(wallet?.availableBalance ?: 0.0)}",
                        subtitle = "Net Earnings (94%)",
                        icon = Icons.Default.AccountBalanceWallet,
                        color = ServexaTeal,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Active Jobs",
                        value = "${activeJobs.size}",
                        subtitle = "${pendingRequests.size} New Requests",
                        icon = Icons.Default.Engineering,
                        color = ServexaIndigo,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Completed",
                        value = "${completedJobs.size}",
                        subtitle = "100% On-time Rate",
                        icon = Icons.Default.CheckCircle,
                        color = ServexaGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Customer Rating",
                        value = "4.94 ★",
                        subtitle = "142 Total Reviews",
                        icon = Icons.Default.Star,
                        color = ServexaAmber,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Store Subdomain $5/mo Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(ServexaRoyalBlueDark, ServexaRoyalBlue, ServexaRoyalBlueVibrant)
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Language, contentDescription = null, tint = Color.White)
                                    Text(
                                        "Store Web Subdomain",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (store != null && store?.isActive == true) ServexaGreen else Color.White.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        if (store != null && store?.isActive == true) "LIVE ONLINE" else "$5.00/MONTH",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Text(
                                if (store != null && store?.isActive == true)
                                    "Your official web store is active at https://${store?.subdomain}.servexa.com. Clients can book directly from the web!"
                                else
                                    "Launch your custom store sub domain (e.g. your-name.servexa.com). Accept direct client bookings online for only $5/month.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.navigateTo(ScreenDestination.ProviderStoreSubdomain) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("manage_subdomain_button")
                                ) {
                                    Icon(Icons.Default.Dns, contentDescription = null, tint = ServexaRoyalBlue, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Manage Subdomain", fontWeight = FontWeight.Bold, color = ServexaRoyalBlue)
                                }

                                if (store != null && store?.isActive == true) {
                                    FilledTonalButton(
                                        onClick = { viewModel.openWebStorefront(store!!.subdomain) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = Color.White.copy(alpha = 0.2f),
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.testTag("preview_subdomain_store_button")
                                    ) {
                                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Preview")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Provider Management Hub
            item {
                Text(
                    text = "Professional Tools & Catalog",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column {
                        ProfileOptionItem(
                            icon = Icons.Default.Language,
                            title = "Store Web Subdomain ($5/mo)",
                            subtitle = if (store != null) "https://${store?.subdomain}.servexa.com" else "Setup custom web address & store page",
                            onClick = { viewModel.navigateTo(ScreenDestination.ProviderStoreSubdomain) }
                        )
                        Divider(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        ProfileOptionItem(
                            icon = Icons.Default.PendingActions,
                            title = "Pending Service Requests (${pendingRequests.size})",
                            subtitle = "Review customer details and accept/decline",
                            onClick = { viewModel.navigateTo(ScreenDestination.ProviderRequests) }
                        )
                        Divider(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        ProfileOptionItem(
                            icon = Icons.Default.DirectionsCar,
                            title = "Active Jobs & Live Tracking (${activeJobs.size})",
                            subtitle = "Update travel, arrive at job, complete work",
                            onClick = { viewModel.navigateTo(ScreenDestination.ProviderActiveJobs) }
                        )
                        Divider(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        ProfileOptionItem(
                            icon = Icons.Default.Build,
                            title = "My Services & Packages",
                            subtitle = "Manage trade catalog, prices and durations",
                            onClick = { viewModel.navigateTo(ScreenDestination.ProviderServices) }
                        )
                        Divider(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        ProfileOptionItem(
                            icon = Icons.Default.VideoCameraBack,
                            title = "Work Portfolio & Videos",
                            subtitle = "Upload project videos to attract customers",
                            onClick = { viewModel.navigateTo(ScreenDestination.ProviderPortfolio) }
                        )
                        Divider(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        ProfileOptionItem(
                            icon = Icons.Default.Payments,
                            title = "Earnings & 6% Commission Payouts",
                            subtitle = "Transparent earnings statement and bank payouts",
                            onClick = { viewModel.navigateTo(ScreenDestination.ProviderEarnings) }
                        )
                        Divider(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        ProfileOptionItem(
                            icon = Icons.Default.Logout,
                            title = "Sign Out",
                            subtitle = "Log out from your business provider account",
                            onClick = { viewModel.logout() }
                        )
                    }
                }
            }

            // 4. Pending Requests Preview
            if (pendingRequests.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Incoming Requests",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(pendingRequests, key = { it.id }) { req ->
                    ProviderRequestCard(
                        booking = req,
                        onAccept = { viewModel.updateBookingStatus(req.id, "ACCEPTED") },
                        onReject = { viewModel.updateBookingStatus(req.id, "REJECTED", "Provider unavailable at requested time") }
                    )
                }
            }
        }
    }
}

@Composable
fun ProviderRequestCard(
    booking: BookingEntity,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = booking.serviceTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$${"%.2f".format(booking.price)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ServexaIndigo
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("Location: ${booking.address}", style = MaterialTheme.typography.bodySmall)
            Text("Problem: ${booking.problemDescription}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(6.dp))

            // Commission info
            Text(
                text = "Net Payout: $${"%.2f".format(booking.providerNetAmount)} (6% Platform Commission: $${"%.2f".format(booking.platformCommission)})",
                style = MaterialTheme.typography.labelSmall,
                color = ServexaTeal
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ServexaRose)
                ) {
                    Text("Decline")
                }

                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ServexaIndigo)
                ) {
                    Text("Accept Job")
                }
            }
        }
    }
}
