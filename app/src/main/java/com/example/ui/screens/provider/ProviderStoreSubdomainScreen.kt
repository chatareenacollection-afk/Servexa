package com.example.ui.screens.provider

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.ServexaViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderStoreSubdomainScreen(
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val store by viewModel.currentProviderStore.collectAsState()
    val wallet by viewModel.customerWallet.collectAsState()

    var subdomainInput by remember(store) {
        mutableStateOf(store?.subdomain ?: (currentUser?.name?.lowercase()?.replace(" ", "-")?.replace(Regex("[^a-z0-9-]"), "") ?: "my-store"))
    }
    var storeTitleInput by remember(store) {
        mutableStateOf(store?.storeTitle ?: (currentUser?.name?.let { "$it Official Services" } ?: "My Professional Store"))
    }
    var taglineInput by remember(store) {
        mutableStateOf(store?.tagline ?: "Certified & Insured Professional Services")
    }
    var aboutBioInput by remember(store) {
        mutableStateOf(store?.aboutBio ?: "Welcome to our official digital storefront! We deliver top-tier, certified service backed by 100% satisfaction guarantee.")
    }
    var categoryInput by remember(store) {
        mutableStateOf(store?.category ?: "Home & Commercial Services")
    }
    var phoneInput by remember(store) {
        mutableStateOf(store?.contactPhone ?: (currentUser?.phone ?: "+1 (555) 234-5678"))
    }
    var emailInput by remember(store) {
        mutableStateOf(store?.contactEmail ?: (currentUser?.email ?: "service@servexa.com"))
    }
    var whatsappInput by remember(store) {
        mutableStateOf(store?.whatsappNumber ?: (currentUser?.phone ?: "+1 (555) 234-5678"))
    }
    var addressInput by remember(store) {
        mutableStateOf(store?.businessAddress ?: "Downtown Business District, Suite 400")
    }
    var hoursInput by remember(store) {
        mutableStateOf(store?.operatingHours ?: "Mon - Sat: 8:00 AM - 7:00 PM")
    }
    var announcementInput by remember(store) {
        mutableStateOf(store?.announcement ?: "🎉 15% discount on all bookings made through our official web store this month!")
    }

    val cleanSubdomainSlug = subdomainInput.trim().lowercase().replace(Regex("[^a-z0-9-]"), "").trim('-')
    val fullWebUrl = "https://${if (cleanSubdomainSlug.isNotBlank()) cleanSubdomainSlug else "your-store"}.servexa.com"

    val isAlreadyActive = store != null && store?.subscriptionStatus == "ACTIVE"
    val walletBalance = wallet?.availableBalance ?: 0.0
    val hasEnoughBalance = walletBalance >= 5.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Store Web Subdomain",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Custom Web Storefront • $5.00/month",
                            style = MaterialTheme.typography.bodySmall,
                            color = ServexaRoyalBlueLight
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isAlreadyActive) {
                        IconButton(
                            onClick = {
                                viewModel.openWebStorefront(store!!.subdomain)
                            },
                            modifier = Modifier.testTag("preview_web_store_action")
                        ) {
                            Icon(Icons.Default.Language, contentDescription = "Open Web Storefront", tint = ServexaRoyalBlue)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Hero Royal Blue Subdomain Card
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
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Public, contentDescription = null, tint = Color.White)
                                    }
                                    Column {
                                        Text(
                                            "Web Store Subdomain",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            "Your custom public web address",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isAlreadyActive) ServexaGreen.copy(alpha = 0.9f) else ServexaAmber
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            if (isAlreadyActive) Icons.Default.CheckCircle else Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            if (isAlreadyActive) "ACTIVE" else "READY TO LAUNCH",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Live URL Badge Box
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.25f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = ServexaGreen, modifier = Modifier.size(16.dp))
                                        Text(
                                            fullWebUrl,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Subdomain URL", fullWebUrl)
                                            clipboard.setPrimaryClip(clip)
                                            viewModel.showMessage("📋 Store link copied to clipboard!")
                                        },
                                        modifier = Modifier.size(32.dp).testTag("copy_subdomain_url_button")
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy URL", tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Plan: $5.00/month (auto-renews from wallet)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                if (isAlreadyActive) {
                                    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(store!!.nextBillingDate))
                                    Text(
                                        "Renews: $dateStr",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.75f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Actions Bar (if active)
            if (isAlreadyActive) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.openWebStorefront(store!!.subdomain)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_web_browser_preview_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ServexaRoyalBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Web Storefront", fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.toggleProviderStoreActive(!store!!.isActive)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("toggle_store_active_button")
                        ) {
                            Icon(
                                if (store!!.isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (store!!.isActive) "Pause" else "Publish")
                        }
                    }
                }

                // Traffic & Performance Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Storefront Web Analytics",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "${store!!.totalVisitors}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ServexaRoyalBlue
                                    )
                                    Text("Web Visitors", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "${store!!.totalOrdersFromSubdomain}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ServexaGreen
                                    )
                                    Text("Online Bookings", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "100%",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ServexaAmber
                                    )
                                    Text("Uptime (SSL)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // Billing & Wallet Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = ServexaRoyalBlue)
                                Text("Provider Wallet Balance", fontWeight = FontWeight.SemiBold)
                            }
                            Text(
                                "$${"%.2f".format(walletBalance)}",
                                fontWeight = FontWeight.Bold,
                                color = if (hasEnoughBalance) ServexaGreen else ServexaRose
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (!hasEnoughBalance && !isAlreadyActive) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ServexaRose.copy(alpha = 0.1f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = ServexaRose, modifier = Modifier.size(18.dp))
                                    Text(
                                        "Balance below $5.00 setup fee. Top up your wallet to activate your store subdomain.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ServexaRose
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    viewModel.navigateTo(ScreenDestination.CustomerWallet)
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Top-Up Wallet in Payments Hub")
                            }
                        } else {
                            Text(
                                "Monthly charge of $5.00 will be auto-debited each month from your wallet balance to maintain your dedicated DNS subdomain and SSL certificate.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Subdomain Configuration Form
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            "Subdomain & Storefront Settings",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ServexaRoyalBlue
                        )

                        // Subdomain Slug Input
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Desired Subdomain Slug *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            OutlinedTextField(
                                value = subdomainInput,
                                onValueChange = { subdomainInput = it.lowercase().replace(" ", "-") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("subdomain_slug_input"),
                                placeholder = { Text("e.g. alex-plumbing") },
                                leadingIcon = { Icon(Icons.Default.Dns, contentDescription = null, tint = ServexaRoyalBlue) },
                                trailingIcon = {
                                    Text(
                                        ".servexa.com",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ServexaRoyalBlue,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            Text(
                                "Full address: $fullWebUrl",
                                style = MaterialTheme.typography.labelSmall,
                                color = ServexaRoyalBlue
                            )
                        }

                        // Store Title
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Storefront Display Name *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            OutlinedTextField(
                                value = storeTitleInput,
                                onValueChange = { storeTitleInput = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("store_title_input"),
                                placeholder = { Text("e.g. Apex Electrical Master Store") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        // Tagline
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Store Tagline / Slogan", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            OutlinedTextField(
                                value = taglineInput,
                                onValueChange = { taglineInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("e.g. 24/7 Emergency Repairs & Installations") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        // Category
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Primary Service Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            OutlinedTextField(
                                value = categoryInput,
                                onValueChange = { categoryInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        // About Bio
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("About Store / Business Bio", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            OutlinedTextField(
                                value = aboutBioInput,
                                onValueChange = { aboutBioInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 5,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        // Special Announcement Banner
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Storefront Top Alert / Promotion", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            OutlinedTextField(
                                value = announcementInput,
                                onValueChange = { announcementInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("e.g. Free estimates & 15% discount this month!") },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Divider()

                        Text("Contact & Business Location", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                        // Phone & WhatsApp
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it },
                                label = { Text("Business Phone") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = whatsappInput,
                                onValueChange = { whatsappInput = it },
                                label = { Text("WhatsApp Number") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        // Email
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Public Contact Email") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Hours & Address
                        OutlinedTextField(
                            value = hoursInput,
                            onValueChange = { hoursInput = it },
                            label = { Text("Operating Hours") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = addressInput,
                            onValueChange = { addressInput = it },
                            label = { Text("Business Address") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                if (cleanSubdomainSlug.length < 3) {
                                    viewModel.showMessage("Subdomain must be at least 3 characters.")
                                    return@Button
                                }
                                viewModel.createOrRenewProviderSubdomain(
                                    subdomain = cleanSubdomainSlug,
                                    storeTitle = storeTitleInput.trim(),
                                    tagline = taglineInput.trim(),
                                    aboutBio = aboutBioInput.trim(),
                                    category = categoryInput.trim(),
                                    contactPhone = phoneInput.trim(),
                                    contactEmail = emailInput.trim(),
                                    whatsappNumber = whatsappInput.trim(),
                                    businessAddress = addressInput.trim(),
                                    operatingHours = hoursInput.trim(),
                                    announcement = announcementInput.trim(),
                                    onSuccess = {
                                        viewModel.openWebStorefront(it.subdomain)
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_and_activate_subdomain_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ServexaRoyalBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.RocketLaunch, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isAlreadyActive) "Update Storefront Settings" else "Activate Subdomain ($5.00/month)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        if (isAlreadyActive) {
                            TextButton(
                                onClick = {
                                    viewModel.cancelProviderSubdomain()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cancel_subdomain_subscription_button")
                            ) {
                                Text("Cancel Monthly Subscription", color = ServexaRose)
                            }
                        }
                    }
                }
            }
        }
    }
}
