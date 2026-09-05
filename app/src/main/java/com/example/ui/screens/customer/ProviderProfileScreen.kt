package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ReviewEntity
import com.example.data.local.entity.ServiceEntity
import com.example.data.local.entity.WorkVideoEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.ServexaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderProfileScreen(
    providerId: String,
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier
) {
    val allProviders by viewModel.searchResults.collectAsState()
    val providerItem = remember(allProviders, providerId) {
        allProviders.find { it.user.id == providerId }
    }

    val allVideos by viewModel.workVideos.collectAsState()
    val providerVideos = remember(allVideos, providerId) {
        allVideos.filter { it.providerId == providerId }
    }

    val providerProducts by viewModel.getProductsForSeller(providerId).collectAsState(initial = emptyList())

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Services", "Products & Parts", "About & Docs", "Work Videos", "Reviews")

    var showComplaintDialog by remember { mutableStateOf(false) }
    var complaintReason by remember { mutableStateOf("") }
    var complaintDetails by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            ServexaTopBar(
                title = providerItem?.user?.name ?: "Provider Profile",
                subtitle = providerItem?.profile?.title,
                showBack = true,
                onBackClick = { viewModel.navigateBack() },
                actions = {
                    if (providerItem != null) {
                        IconButton(
                            onClick = {
                                viewModel.navigateTo(ScreenDestination.Chat(providerItem.user.id, providerItem.user.name))
                            }
                        ) {
                            Icon(imageVector = Icons.Default.ChatBubbleOutline, contentDescription = "Chat", tint = MaterialTheme.colorScheme.primary)
                        }

                        IconButton(
                            onClick = {
                                viewModel.initiateCall(
                                    bookingId = "PROFILE-CALL",
                                    receiverId = providerItem.user.id,
                                    receiverName = providerItem.user.name,
                                    receiverRole = "PROVIDER"
                                )
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = "Secure Call", tint = ServexaTeal)
                        }

                        IconButton(
                            onClick = { showComplaintDialog = true }
                        ) {
                            Icon(imageVector = Icons.Default.ReportProblem, contentDescription = "Report / Complain", tint = ServexaRose)
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (providerItem != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.navigateTo(ScreenDestination.Chat(providerItem.user.id, providerItem.user.name))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Chat", fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.initiateCall(
                                    bookingId = "PROFILE-CALL",
                                    receiverId = providerItem.user.id,
                                    receiverName = providerItem.user.name,
                                    receiverRole = "PROVIDER"
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ServexaTeal)
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call", fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.navigateTo(ScreenDestination.BookingWorkflow(providerId))
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("profile_book_now_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Book", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (providerItem == null) {
            EmptyStateView(
                title = "Provider Not Found",
                subtitle = "The requested service professional profile could not be loaded.",
                icon = Icons.Default.PersonOff,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Provider Header Banner
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .background(
                                    Brush.linearGradient(listOf(ServexaIndigo, ServexaTeal)),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = providerItem.user.name.take(2).uppercase(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = providerItem.user.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = providerItem.profile.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            VerificationBadge(isVerified = providerItem.profile.verificationStatus == "VERIFIED")
                            RatingBadge(rating = providerItem.profile.rating, reviewCount = providerItem.profile.reviewCount)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = ServexaTeal, modifier = Modifier.size(16.dp))
                            Text(
                                text = "${providerItem.profile.locationName} • ${"%.1f".format(providerItem.estimatedDistanceKm)} km away",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Tab Selector
                item {
                    ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 16.dp) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title, fontWeight = FontWeight.SemiBold) }
                            )
                        }
                    }
                }

                // Tab Contents
                when (selectedTab) {
                    0 -> { // Services Catalog
                        if (providerItem.services.isEmpty()) {
                            item {
                                EmptyStateView(
                                    title = "No services listed",
                                    subtitle = "This provider hasn't published service packages yet.",
                                    icon = Icons.Default.BuildCircle
                                )
                            }
                        } else {
                            items(providerItem.services, key = { it.id }) { srv ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = srv.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = "$${"%.2f".format(srv.price)}",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = srv.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                                Text("Est. ${srv.durationMinutes} min", style = MaterialTheme.typography.labelMedium)
                                            }

                                            Button(
                                                onClick = {
                                                    viewModel.navigateTo(ScreenDestination.BookingWorkflow(providerId, srv.id))
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                            ) {
                                                Text("Select & Book")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> { // Products & Parts Catalog
                        if (providerProducts.isEmpty()) {
                            item {
                                EmptyStateView(
                                    title = "No Products Listed",
                                    subtitle = "This service provider hasn't added retail parts or hardware materials yet.",
                                    icon = Icons.Default.ShoppingBag
                                )
                            }
                        } else {
                            items(providerProducts, key = { it.id }) { prod ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = prod.name,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = ServexaTeal.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = "Stock: ${prod.inventory} in stock",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = ServexaTeal,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "$${"%.2f".format(prod.price)}",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = prod.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = ServexaAmber, modifier = Modifier.size(16.dp))
                                                Text("${prod.rating} ★ (${prod.reviewCount})", style = MaterialTheme.typography.labelMedium)
                                            }

                                            Button(
                                                onClick = {
                                                    viewModel.addToCart(prod.id)
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = ServexaIndigo)
                                            ) {
                                                Icon(imageVector = Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Add to Cart")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    2 -> { // About & Docs
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Professional Bio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(providerItem.profile.bio, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }

                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("Operational Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Text(providerItem.profile.workingHours, style = MaterialTheme.typography.bodyMedium)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.Map, contentDescription = null, tint = ServexaTeal)
                                            Text("Coverage: ${providerItem.profile.serviceArea}", style = MaterialTheme.typography.bodyMedium)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = ServexaGreen)
                                            Text(providerItem.profile.verificationDocuments, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    3 -> { // Work Videos
                        if (providerVideos.isEmpty()) {
                            item {
                                EmptyStateView(
                                    title = "No work videos uploaded",
                                    subtitle = "Portfolio videos will appear here once uploaded.",
                                    icon = Icons.Default.VideoLibrary
                                )
                            }
                        } else {
                            items(providerVideos, key = { it.id }) { video ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(video.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(video.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                            Text("❤️ ${video.likesCount} likes", style = MaterialTheme.typography.labelMedium)
                                            Text("💬 ${video.commentsCount} comments", style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    4 -> { // Reviews
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "${"%.1f".format(providerItem.profile.rating)} ★",
                                                style = MaterialTheme.typography.headlineLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = ServexaAmber
                                            )
                                            Text("Based on ${providerItem.profile.reviewCount} customer reviews", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Icon(Icons.Default.Stars, contentDescription = null, tint = ServexaAmber, modifier = Modifier.size(48.dp))
                                    }
                                }

                                Text(
                                    text = "Verified Customer Reviews",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                // Static preview reviews
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Alex Morgan", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                            RatingBadge(rating = 5.0)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Punctual, thorough, and very courteous. Explained all the steps clearly and fixed our issue on the first visit!",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                        ) {
                                            Text(
                                                text = "Pro Reply: Thank you! Always here to provide reliable service.",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(8.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Direct Complain Dialog
    if (showComplaintDialog) {
        AlertDialog(
            onDismissRequest = { showComplaintDialog = false },
            title = { Text("Lodge a Formal Complaint") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Your complaint regarding ${providerItem?.user?.name ?: "provider"} will be submitted directly to the Admin Support Desk.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = complaintReason,
                        onValueChange = { complaintReason = it },
                        label = { Text("Complaint Subject / Category") },
                        placeholder = { Text("e.g. Unprofessional behavior, overcharging, delay") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = complaintDetails,
                        onValueChange = { complaintDetails = it },
                        label = { Text("Detailed Description") },
                        placeholder = { Text("Describe what happened in detail...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitComplaint(
                            reason = complaintReason,
                            description = complaintDetails,
                            bookingId = "PROVIDER-PROFILE-$providerId"
                        ) {
                            showComplaintDialog = false
                            complaintReason = ""
                            complaintDetails = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ServexaRose)
                ) {
                    Text("Submit to Admin")
                }
            },
            dismissButton = {
                TextButton(onClick = { showComplaintDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
