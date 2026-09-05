package com.example.ui.screens.web

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ProviderStoreEntity
import com.example.data.local.entity.ServiceEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.ServexaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderWebStorefrontScreen(
    subdomain: String,
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentProviderStore by viewModel.currentProviderStore.collectAsState()
    val allStores by viewModel.allPublicStores.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Find store by subdomain, or fallback to current provider store, or generate fallback store
    val store = remember(subdomain, allStores, currentProviderStore) {
        allStores.find { it.subdomain.equals(subdomain, ignoreCase = true) }
            ?: if (currentProviderStore?.subdomain.equals(subdomain, ignoreCase = true)) currentProviderStore
            else null
    } ?: ProviderStoreEntity(
        id = "store_preview",
        providerId = currentUser?.id ?: "prov_marcus_1",
        providerName = currentUser?.name ?: "Certified Master Pro",
        subdomain = subdomain.ifBlank { "certified-pro" },
        storeTitle = "${currentUser?.name ?: "Master Service"} Official Web Store",
        tagline = "Professional Certified & Insured Solutions",
        aboutBio = "Providing industry-grade workmanship, upfront transparent pricing, and 100% verified customer satisfaction. Contact us or book online instantly.",
        category = "Home & Commercial Services",
        themeColorHex = "#1D4ED8",
        contactPhone = "+1 (555) 789-0123",
        contactEmail = "support@servexa.com",
        whatsappNumber = "+1 (555) 789-0123",
        businessAddress = "742 Evergreen Terrace, Suite 100",
        operatingHours = "Mon - Sat: 8:00 AM - 7:00 PM",
        announcement = "🎉 Web Special: Book directly through our verified subdomain for priority scheduling & escrow guarantee!"
    )

    var isDesktopLayout by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("SERVICES") } // "SERVICES", "ABOUT", "REVIEWS", "CONTACT"
    var showShareDialog by remember { mutableStateOf(false) }

    val fullUrl = "https://${store.subdomain}.servexa.com"

    // Mock services offered by this store
    val services = remember {
        listOf(
            ServiceEntity(
                id = "srv_sub_1",
                providerId = store.providerId,
                categoryId = "cat_plumbing",
                title = "Standard Inspection & Diagnostic",
                description = "Comprehensive on-site assessment, troubleshooting report, and immediate quote.",
                price = 65.0,
                durationMinutes = 45
            ),
            ServiceEntity(
                id = "srv_sub_2",
                providerId = store.providerId,
                categoryId = "cat_plumbing",
                title = "Full System Maintenance & Repair",
                description = "Standard repair work including certified parts, leak detection, testing, and 90-day warranty.",
                price = 140.0,
                durationMinutes = 90
            ),
            ServiceEntity(
                id = "srv_sub_3",
                providerId = store.providerId,
                categoryId = "cat_plumbing",
                title = "Emergency Priority Dispatch",
                description = "Priority 24/7 same-day dispatch with certified master technician on-site within 60 minutes.",
                price = 195.0,
                durationMinutes = 120
            ),
            ServiceEntity(
                id = "srv_sub_4",
                providerId = store.providerId,
                categoryId = "cat_plumbing",
                title = "Complete Upgrade & Installation",
                description = "Turnkey equipment installation, calibration, safety compliance check, and old unit haul-away.",
                price = 320.0,
                durationMinutes = 180
            )
        )
    }

    Scaffold(
        topBar = {
            // Authentic Web Browser Chrome Frame
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A)) // Sleek dark slate browser top
            ) {
                // Browser Window Controls & Tab Title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Window Dots (Mac/Browser style)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(11.dp).clip(CircleShape).background(Color(0xFFFF5F56)))
                        Box(modifier = Modifier.size(11.dp).clip(CircleShape).background(Color(0xFFFFBD2E)))
                        Box(modifier = Modifier.size(11.dp).clip(CircleShape).background(Color(0xFF27C93F)))
                    }

                    // Active Tab Chip
                    Surface(
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                        color = Color(0xFF1E293B),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = ServexaRoyalBlueLight, modifier = Modifier.size(14.dp))
                            Text(
                                "${store.subdomain}.servexa.com",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    // Viewport Switcher (Desktop vs Mobile simulation)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isDesktopLayout = !isDesktopLayout },
                            modifier = Modifier.size(28.dp).testTag("toggle_desktop_layout_button")
                        ) {
                            Icon(
                                if (isDesktopLayout) Icons.Default.Smartphone else Icons.Default.DesktopWindows,
                                contentDescription = "Toggle Viewport",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.navigateBack() },
                            modifier = Modifier.size(28.dp).testTag("close_browser_button")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Browser Navigation Bar & Omnibox Address Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = { viewModel.showMessage("Refreshed web storefront") },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                    }

                    // Omnibox URL Bar
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = "SSL Secure", tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                                Text(
                                    fullUrl,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFE2E8F0),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Store URL", fullUrl))
                                        viewModel.showMessage("📋 Link copied: $fullUrl")
                                    },
                                    modifier = Modifier.size(24.dp).testTag("copy_url_button")
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Link", tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                                }
                                IconButton(
                                    onClick = { showShareDialog = true },
                                    modifier = Modifier.size(24.dp).testTag("share_url_button")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(if (isDesktopLayout) Color(0xFF0F172A) else MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            // Viewport container: either desktop frame (max 640dp) or full screen
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .then(
                        if (isDesktopLayout) Modifier
                            .widthIn(max = 680.dp)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                        else Modifier.fillMaxWidth()
                    )
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // Store Announcement Bar
                    if (store.announcement.isNotBlank()) {
                        item {
                            Surface(
                                color = ServexaRoyalBlueDark,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Campaign, contentDescription = null, tint = ServexaAmber, modifier = Modifier.size(18.dp))
                                    Text(
                                        store.announcement,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // Web Header & Navbar
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(ServexaRoyalBlue, ServexaRoyalBlueVibrant)
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.White)
                                    }
                                    Column {
                                        Text(
                                            store.storeTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            "${store.subdomain}.servexa.com",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ServexaRoyalBlue,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        viewModel.navigateTo(ScreenDestination.BookingWorkflow(store.providerId))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ServexaRoyalBlue),
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    modifier = Modifier.testTag("book_online_cta_button")
                                ) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Book Online", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    // Royal Blue Web Hero Banner
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            ServexaRoyalBlueDark,
                                            ServexaRoyalBlue,
                                            Color(0xFF1E3A8A)
                                        )
                                    )
                                )
                                .padding(horizontal = 20.dp, vertical = 24.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.White.copy(alpha = 0.2f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.Verified, contentDescription = null, tint = ServexaAmber, modifier = Modifier.size(14.dp))
                                            Text(
                                                "VERIFIED SERVICE PROVIDER",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = ServexaGreen.copy(alpha = 0.9f)
                                    ) {
                                        Text(
                                            "ONLINE NOW",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    store.storeTitle,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    store.tagline,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.9f)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Rating & Trust Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = ServexaAmber, modifier = Modifier.size(18.dp))
                                        Text(
                                            "4.95",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            "(128 reviews)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.75f)
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Shield, contentDescription = null, tint = ServexaGreen, modifier = Modifier.size(16.dp))
                                        Text(
                                            "$50K Escrow Guarantee",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Fast Contact Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    FilledTonalButton(
                                        onClick = {
                                            viewModel.showMessage("Dialing store: ${store.contactPhone}")
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = Color.White.copy(alpha = 0.15f),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Call Store", fontWeight = FontWeight.SemiBold)
                                    }

                                    FilledTonalButton(
                                        onClick = {
                                            viewModel.showMessage("Opening WhatsApp chat with store: ${store.whatsappNumber}")
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = Color(0xFF25D366).copy(alpha = 0.9f),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("WhatsApp", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }

                    // Navigation Tabs (Services, About, Reviews, Location)
                    item {
                        TabRow(
                            selectedTabIndex = when (selectedTab) {
                                "SERVICES" -> 0
                                "ABOUT" -> 1
                                "REVIEWS" -> 2
                                else -> 3
                            },
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = ServexaRoyalBlue
                        ) {
                            Tab(
                                selected = selectedTab == "SERVICES",
                                onClick = { selectedTab = "SERVICES" },
                                text = { Text("Services Catalog", fontWeight = FontWeight.SemiBold) }
                            )
                            Tab(
                                selected = selectedTab == "ABOUT",
                                onClick = { selectedTab = "ABOUT" },
                                text = { Text("About & Trust", fontWeight = FontWeight.SemiBold) }
                            )
                            Tab(
                                selected = selectedTab == "REVIEWS",
                                onClick = { selectedTab = "REVIEWS" },
                                text = { Text("Reviews", fontWeight = FontWeight.SemiBold) }
                            )
                            Tab(
                                selected = selectedTab == "CONTACT",
                                onClick = { selectedTab = "CONTACT" },
                                text = { Text("Location", fontWeight = FontWeight.SemiBold) }
                            )
                        }
                    }

                    // Tab Content
                    when (selectedTab) {
                        "SERVICES" -> {
                            item {
                                PaddingValues(16.dp)
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "Available Services & Pricing",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Select any service to book directly online with secure escrow protection.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            items(services) { srv ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                srv.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                srv.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Text(
                                                    "$${"%.2f".format(srv.price)}",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = ServexaRoyalBlue
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant
                                                ) {
                                                    Text(
                                                        "⏱ ${srv.durationMinutes} mins",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Button(
                                            onClick = {
                                                viewModel.navigateTo(ScreenDestination.BookingWorkflow(store.providerId, srv.id))
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = ServexaRoyalBlue),
                                            modifier = Modifier.testTag("book_service_${srv.id}_button")
                                        ) {
                                            Text("Book Now", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        "ABOUT" -> {
                            item {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text("About Our Business", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                store.aboutBio.ifBlank { "We are dedicated to delivering top-grade professional work with upfront pricing and satisfaction guarantee." },
                                                style = MaterialTheme.typography.bodyMedium,
                                                lineHeight = 22.sp
                                            )
                                        }
                                    }

                                    // Trust & Safety Cards
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = ServexaRoyalBlueIce),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, ServexaRoyalBlueBorder)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text(
                                                "Servexa Web Verified Guarantee",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = ServexaRoyalBlueDark
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ServexaGreen, modifier = Modifier.size(18.dp))
                                                Text("Background Checked & Certified Identity", style = MaterialTheme.typography.bodySmall)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ServexaGreen, modifier = Modifier.size(18.dp))
                                                Text("Secure Escrow Payment Protection (6% commission)", style = MaterialTheme.typography.bodySmall)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ServexaGreen, modifier = Modifier.size(18.dp))
                                                Text("Dedicated Subdomain with SSL Encryption", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "REVIEWS" -> {
                            item {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("Customer Reviews (4.9 ★)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                                    val mockReviews = listOf(
                                        Triple("Jessica Taylor", "5.0", "Booked right from their web subdomain! Arrived within 30 minutes, solved the issue completely, and payment through escrow was super smooth."),
                                        Triple("David Miller", "5.0", "Outstanding master craftsmanship! Fair pricing and very communicative. Highly recommended."),
                                        Triple("Samantha Lee", "4.8", "Fast booking process on their website. Excellent technician who explained everything thoroughly.")
                                    )

                                    mockReviews.forEach { (author, rating, text) ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(author, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Star, contentDescription = null, tint = ServexaAmber, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(rating, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "CONTACT" -> {
                            item {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text("Store Location & Hours", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = ServexaRoyalBlue)
                                                Column {
                                                    Text("Address", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Text(store.businessAddress, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                                }
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Icon(Icons.Default.Schedule, contentDescription = null, tint = ServexaRoyalBlue)
                                                Column {
                                                    Text("Operating Hours", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Text(store.operatingHours, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                                }
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Icon(Icons.Default.Phone, contentDescription = null, tint = ServexaRoyalBlue)
                                                Column {
                                                    Text("Direct Phone", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Text(store.contactPhone, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                                }
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Icon(Icons.Default.Email, contentDescription = null, tint = ServexaRoyalBlue)
                                                Column {
                                                    Text("Inquiry Email", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Text(store.contactEmail, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Web Footer Section
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A))
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "© 2026 ${store.storeTitle}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Official Subdomain: ${store.subdomain}.servexa.com",
                                style = MaterialTheme.typography.bodySmall,
                                color = ServexaRoyalBlueSoft
                            )
                            Text(
                                "Powered by Servexa Global Services Marketplace • 🔒 256-Bit SSL Escrow",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("Share Store Subdomain") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Share this direct storefront link with your clients on social media, business cards, or WhatsApp:")
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            fullUrl,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = ServexaRoyalBlue
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Store URL", fullUrl))
                        showShareDialog = false
                        viewModel.showMessage("📋 Link copied to clipboard!")
                    }
                ) {
                    Text("Copy Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
