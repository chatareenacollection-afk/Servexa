package com.example.ui.screens.customer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.ServexaViewModel

@Composable
fun HomeScreen(
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val bookings by viewModel.customerBookings.collectAsState()
    val isCapturingLocation by viewModel.isCapturingLocation.collectAsState()
    var showLocationPicker by remember { mutableStateOf(false) }

    val activeBooking = remember(bookings) {
        bookings.firstOrNull { it.status in listOf("REQUESTED", "ACCEPTED", "PROVIDER_ON_THE_WAY", "ARRIVED", "IN_PROGRESS") }
    }

    if (showLocationPicker) {
        AddLocationDialog(
            viewModel = viewModel,
            initialAddress = viewModel.userLocationName.value,
            onDismiss = { showLocationPicker = false },
            onLocationSet = { _, _, _ -> }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. Header & Hero Area
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                ServexaIndigoDark,
                                ServexaIndigo,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SERVEXA",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = ServexaTealLight,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = if (currentUser != null) "Hello, ${currentUser?.name}" else "Find Trusted Services",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.navigateTo(ScreenDestination.WebPortalMode) },
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                    .testTag("home_web_portal_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "Web Portal & Subdomains",
                                    tint = Color.White
                                )
                            }
                            IconButton(
                                onClick = { viewModel.navigateTo(ScreenDestination.Notifications) },
                                modifier = Modifier.background(Color.White.copy(alpha = 0.15f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live Location GPS Status Bar & Quick Switcher
                    val userLocationName by viewModel.userLocationName.collectAsState()
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.18f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showLocationPicker = true }
                            .testTag("home_live_location_bar")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isCapturingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = ServexaTealLight
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "Current Location",
                                    tint = ServexaTealLight,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Text(
                                text = "Live: $userLocationName",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Icon(
                                imageVector = Icons.Default.EditLocationAlt,
                                contentDescription = "Change Location",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Clean Search Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.navigateTo(ScreenDestination.Search)
                            }
                            .testTag("home_search_bar_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Search services (e.g. Electrician, AC Repair...)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Search Tag Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Restaurants", "Hotels", "Taxis", "Bike Rides", "IT Specialists", "Doctors", "Veterinary", "Buy & Sell", "Electrician", "AC Repair", "Plumber", "House Cleaning").forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White.copy(alpha = 0.18f),
                                modifier = Modifier.clickable {
                                    if (tag.equals("Buy & Sell", ignoreCase = true)) {
                                        viewModel.navigateTo(ScreenDestination.ProductMarketplace)
                                    } else {
                                        viewModel.searchQuery.value = tag
                                        viewModel.navigateTo(ScreenDestination.Search)
                                    }
                                }
                            ) {
                                Text(
                                    text = tag,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Active Booking Alert Banner (if any active job)
        if (activeBooking != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clickable {
                            viewModel.navigateTo(ScreenDestination.ActiveBookingTracking(activeBooking.id))
                        }
                        .testTag("active_booking_alert_banner"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Active: ${activeBooking.serviceTitle}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                StatusPill(status = activeBooking.status)
                                Text(
                                    text = "Tap to track live",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Track",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // 2. Featured App Hero Visual Picture
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable {
                        viewModel.navigateTo(ScreenDestination.Search)
                    }
                    .testTag("home_hero_showcase_banner"),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner),
                        contentDescription = "Servexa On-Demand Services",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Gradient Scrim for crystal clear typography
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.15f),
                                        Color.Black.copy(alpha = 0.75f)
                                    )
                                )
                            )
                    )

                    // Overlay Content
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ServexaTeal
                            ) {
                                Text(
                                    text = "ALL-IN-ONE PLATFORM",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Trusted Pros at Your Doorstep",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Book rides, repairs, cleaning & verified experts in minutes",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Explore",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Service Categories (Auto-Moving Carousel from Left to Right)
        item {
            AutoMovingCategoriesSection(
                categories = categories,
                onCategoryClick = { category ->
                    if (category.id == "cat_buysell") {
                        viewModel.navigateTo(ScreenDestination.ProductMarketplace)
                    } else {
                        viewModel.selectedCategoryId.value = category.id
                        viewModel.searchQuery.value = category.name
                        viewModel.navigateTo(ScreenDestination.Search)
                    }
                },
                onSeeAllClick = { viewModel.navigateTo(ScreenDestination.Categories) }
            )
        }

        // 3.5 Featured Web Store Subdomains ($5/mo)
        item {
            val publicStores by viewModel.allPublicStores.collectAsState()
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = ServexaRoyalBlue, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Provider Web Stores",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TextButton(
                        onClick = { viewModel.navigateTo(ScreenDestination.WebPortalMode) },
                        modifier = Modifier.testTag("see_all_web_stores_button")
                    ) {
                        Text("Web Directory", color = ServexaRoyalBlue, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (publicStores.isNotEmpty()) {
                    androidx.compose.foundation.lazy.LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        items(publicStores) { st ->
                            Card(
                                modifier = Modifier
                                    .width(220.dp)
                                    .clickable { viewModel.openWebStorefront(st.subdomain) }
                                    .testTag("home_store_card_${st.subdomain}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(st.storeTitle, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = ServexaAmber, modifier = Modifier.size(14.dp))
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "${st.subdomain}.servexa.com",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ServexaRoyalBlue,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(st.tagline, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                            .clickable {
                                if (currentUser?.role == "PROVIDER") {
                                    viewModel.navigateTo(ScreenDestination.ProviderStoreSubdomain)
                                } else {
                                    viewModel.navigateTo(ScreenDestination.WebPortalMode)
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = ServexaRoyalBlueIce),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ServexaRoyalBlueBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Launch Your Store Subdomain", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = ServexaRoyalBlueDark)
                                Text("Providers get their own public web address (e.g. name.servexa.com) for only $5/month!", style = MaterialTheme.typography.bodySmall, color = ServexaRoyalBlueDark.copy(alpha = 0.8f))
                            }
                            Button(
                                onClick = {
                                    if (currentUser?.role == "PROVIDER") {
                                        viewModel.navigateTo(ScreenDestination.ProviderStoreSubdomain)
                                    } else {
                                        viewModel.navigateTo(ScreenDestination.WebPortalMode)
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ServexaRoyalBlue)
                            ) {
                                Text("$5/mo", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 4. Verified Service Professionals
        item {
            Column(modifier = Modifier.padding(top = 20.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available Professionals",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { viewModel.navigateTo(ScreenDestination.Search) }) {
                        Text("View All")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (searchResults.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Engineering,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Providers Listed Yet",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Registered and approved service providers will appear here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        searchResults.take(6).forEach { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.navigateTo(ScreenDestination.ProviderProfile(item.user.id))
                                    },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(CircleShape)
                                                .background(ServexaIndigo),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = item.user.name.take(1).uppercase(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    text = item.user.name,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (item.profile.verificationStatus == "VERIFIED") {
                                                    Icon(
                                                        imageVector = Icons.Default.Verified,
                                                        contentDescription = "Verified",
                                                        tint = ServexaTeal,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = item.profile.title.ifBlank { item.categoryName },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.NearMe,
                                                    contentDescription = null,
                                                    tint = ServexaTeal,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = "${"%.1f".format(item.estimatedDistanceKm)} km away • Nearest",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = ServexaTeal,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Icon(Icons.Default.Star, contentDescription = null, tint = ServexaAmber, modifier = Modifier.size(14.dp))
                                                Text(
                                                    text = if (item.profile.rating > 0) "%.1f".format(item.profile.rating) else "New",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = "From $${"%.0f".format(item.startingPrice)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Direct Action Buttons: Chat, Call, Book
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Chat Button
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.navigateTo(ScreenDestination.Chat(item.user.id, item.user.name))
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Chat", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Chat", fontSize = 12.sp)
                                        }

                                        // Call Button
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.initiateCall(
                                                    bookingId = "DIRECT-CALL",
                                                    receiverId = item.user.id,
                                                    receiverName = item.user.name,
                                                    receiverRole = "PROVIDER"
                                                )
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(16.dp), tint = ServexaTeal)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Call", fontSize = 12.sp, color = ServexaTeal)
                                        }

                                        // Book Button
                                        Button(
                                            onClick = {
                                                viewModel.navigateTo(ScreenDestination.BookingWorkflow(item.user.id))
                                            },
                                            modifier = Modifier.weight(1.2f),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = ServexaIndigo)
                                        ) {
                                            Text("Book", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Trust & Platform Security Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = ServexaTeal, modifier = Modifier.size(28.dp))
                    Column {
                        Text(
                            text = "Servexa Protection & Direct Calling",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Verified background checks, encrypted in-app voice calling, and escrow protection with 48h settlement.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Animated moving categories strip that glides continuously across the screen
 * with touch-pause, manual drag support, and instant navigation.
 */
@Composable
fun AutoMovingCategoriesSection(
    categories: List<CategoryEntity>,
    onCategoryClick: (CategoryEntity) -> Unit,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (categories.isEmpty()) return

    // Multiplied list for seamless continuous infinite motion
    val repeatedList = remember(categories) {
        List(60) { categories }.flatten()
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = 20)
    var isPausedByUser by remember { mutableStateOf(false) }

    // Auto-scroll loop
    LaunchedEffect(repeatedList, isPausedByUser) {
        if (repeatedList.isNotEmpty() && !isPausedByUser) {
            while (isActive) {
                if (!listState.isScrollInProgress) {
                    listState.scrollBy(1.25f)
                }
                delay(16) // ~60 FPS smooth motion
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
            .testTag("auto_moving_categories_section")
    ) {
        // Section Header with Motion Indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Animated Live Flow Indicator
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ServexaTeal.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isPausedByUser) Icons.Default.Pause else Icons.Default.MotionPhotosAuto,
                            contentDescription = null,
                            tint = ServexaTeal,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (isPausedByUser) "PAUSED" else "MOVING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = ServexaTeal,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { isPausedByUser = !isPausedByUser },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isPausedByUser) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = "Toggle Auto Movement",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                TextButton(onClick = onSeeAllClick) {
                    Text("See All", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Moving Horizontal List
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("categories_moving_lazy_row")
        ) {
            items(
                count = repeatedList.size,
                key = { index -> "${repeatedList[index].id}_$index" }
            ) { index ->
                val category = repeatedList[index]

                Card(
                    modifier = Modifier
                        .width(102.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onCategoryClick(category) }
                        .testTag("category_card_${category.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                            ServexaTeal.copy(alpha = 0.12f)
                                        )
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            CategoryIcon(
                                name = category.iconName,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
