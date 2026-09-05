package com.example.ui.screens.customer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.AddLocationDialog
import com.example.ui.components.LiveLocationAccountHeaderCard
import com.example.ui.components.ServexaTopBar
import com.example.ui.components.UseCurrentLocationButton
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.ServexaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProfileScreen(
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val wallet by viewModel.customerWallet.collectAsState()
    val kycDoc by viewModel.userKycDocument.collectAsState()
    val currentThemeMode by viewModel.themeMode.collectAsState()
    val currentThemeColor by viewModel.themeColor.collectAsState()
    val userLat by viewModel.userLatitude.collectAsState()
    val userLng by viewModel.userLongitude.collectAsState()
    val userLocationName by viewModel.userLocationName.collectAsState()

    var showComplaintDialog by remember { mutableStateOf(false) }
    var complaintReason by remember { mutableStateOf("") }
    var complaintDetails by remember { mutableStateOf("") }

    var showLoginDialog by remember { mutableStateOf(false) }
    var showAddLocationDialog by remember { mutableStateOf(false) }
    var showKycUploadDialog by remember { mutableStateOf(false) }
    val canNavigateBack by viewModel.canNavigateBack.collectAsState()

    Scaffold(
        topBar = {
            ServexaTopBar(
                title = "Account & Profile",
                subtitle = if (currentUser != null) "${currentUser?.email} (${currentUser?.role})" else "Customer Profile & Verification",
                showBack = canNavigateBack,
                onBackClick = { viewModel.navigateBack() },
                actions = {
                    IconButton(
                        onClick = { showLoginDialog = true },
                        modifier = Modifier.testTag("topbar_switch_account_btn")
                    ) {
                        Icon(Icons.Default.SwitchAccount, contentDescription = "Switch Account", tint = MaterialTheme.colorScheme.primary)
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
            // 0. TOP OF USER ACCOUNT: Live Exact GPS Location Card with "Use Current Location"
            item {
                LiveLocationAccountHeaderCard(
                    viewModel = viewModel,
                    modifier = Modifier.testTag("account_top_live_location"),
                    onOpenAddLocationDialog = { showAddLocationDialog = true }
                )
            }

            // 1. LOGGED IN USER PROFILE HEADER CARD
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_user_profile_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(
                                        Brush.linearGradient(listOf(ServexaIndigo, ServexaTeal)),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser?.name?.take(2)?.uppercase() ?: "AJ",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = currentUser?.name ?: "Alex Johnson",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = if (kycDoc?.verificationStatus == "VERIFIED") ServexaTeal else ServexaAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = currentUser?.email ?: "alex@customer.com",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (!currentUser?.phone.isNullOrBlank()) {
                                    Text(
                                        text = "Phone: ${currentUser?.phone}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (currentUser?.role) {
                                        "ADMIN" -> ServexaRose.copy(alpha = 0.15f)
                                        "PROVIDER" -> ServexaAmber.copy(alpha = 0.15f)
                                        else -> ServexaTeal.copy(alpha = 0.15f)
                                    }
                                ) {
                                    Text(
                                        text = "ACTIVE ROLE: ${currentUser?.role ?: "CUSTOMER"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when (currentUser?.role) {
                                            "ADMIN" -> ServexaRose
                                            "PROVIDER" -> ServexaAmber
                                            else -> ServexaTeal
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { showLoginDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("account_switch_user_btn")
                            ) {
                                Icon(Icons.Default.SwitchAccount, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Switch Account / Role", fontSize = 12.sp)
                            }

                            if (currentUser?.role == "PROVIDER") {
                                FilledTonalButton(
                                    onClick = { viewModel.navigateTo(ScreenDestination.ProviderDashboard) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Provider Console", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else if (currentUser?.role == "ADMIN") {
                                FilledTonalButton(
                                    onClick = { viewModel.navigateTo(ScreenDestination.AdminDashboard) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Admin Dashboard", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 2. MANDATORY IDENTITY & KYC DOCUMENT VERIFICATION CARD
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_kyc_verification_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                        .background(
                                            if (kycDoc?.verificationStatus == "VERIFIED") ServexaTeal.copy(alpha = 0.15f) else ServexaIndigo.copy(alpha = 0.15f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Badge,
                                        contentDescription = null,
                                        tint = if (kycDoc?.verificationStatus == "VERIFIED") ServexaTeal else ServexaIndigo
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Identity & Wallet Verification",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Driving License, ID Card, or Passport",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Status Tag
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = when (kycDoc?.verificationStatus) {
                                    "VERIFIED" -> ServexaTeal.copy(alpha = 0.15f)
                                    "PENDING" -> ServexaAmber.copy(alpha = 0.15f)
                                    "REJECTED" -> ServexaRose.copy(alpha = 0.15f)
                                    else -> MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
                                }
                            ) {
                                Text(
                                    text = when (kycDoc?.verificationStatus) {
                                        "VERIFIED" -> "VERIFIED"
                                        "PENDING" -> "UNDER REVIEW"
                                        "REJECTED" -> "REJECTED"
                                        else -> "REQUIRED"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when (kycDoc?.verificationStatus) {
                                        "VERIFIED" -> ServexaTeal
                                        "PENDING" -> ServexaAmber
                                        "REJECTED" -> ServexaRose
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (kycDoc != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.background,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Document Type:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(kycDoc?.documentType?.replace("_", " ") ?: "", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Document No:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(kycDoc?.documentNumber ?: "", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Issuing Country:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${kycDoc?.issuingCountry} (${kycDoc?.issuingStateOrProvince})", style = MaterialTheme.typography.labelMedium)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Expiry Date:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(kycDoc?.expiryDate ?: "", style = MaterialTheme.typography.labelMedium)
                                    }
                                    if (kycDoc?.adminNotes?.isNotBlank() == true) {
                                        Text("Admin Notes: ${kycDoc?.adminNotes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "To unlock full wallet capabilities (Top-ups, POS credits, withdrawals, and merchant payments), upload your Driving License, National ID, or Passport for admin review.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { showKycUploadDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("upload_kyc_document_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (kycDoc?.verificationStatus == "VERIFIED") MaterialTheme.colorScheme.secondaryContainer else ServexaIndigo
                            )
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (kycDoc != null) "Update Identity Documents" else "Upload Identity Document (DL / ID / Passport)",
                                fontWeight = FontWeight.Bold,
                                color = if (kycDoc?.verificationStatus == "VERIFIED") MaterialTheme.colorScheme.onSecondaryContainer else Color.White
                            )
                        }
                    }
                }
            }

            // 2. Theme & Appearance Customization (Light theme / color palette)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Theme & Visual Style", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Text("Display Mode", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = currentThemeMode == AppThemeMode.LIGHT,
                                onClick = { viewModel.setThemeMode(AppThemeMode.LIGHT) },
                                label = { Text("Light") },
                                leadingIcon = { Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = currentThemeMode == AppThemeMode.DARK,
                                onClick = { viewModel.setThemeMode(AppThemeMode.DARK) },
                                label = { Text("Dark") },
                                leadingIcon = { Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = currentThemeMode == AppThemeMode.SYSTEM,
                                onClick = { viewModel.setThemeMode(AppThemeMode.SYSTEM) },
                                label = { Text("System") },
                                leadingIcon = { Icon(Icons.Default.BrightnessAuto, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Text("Accent Color Palette", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val colors = listOf(
                                Pair(AppThemeColor.INDIGO, ServexaIndigo),
                                Pair(AppThemeColor.TEAL, ServexaTeal),
                                Pair(AppThemeColor.EMERALD, ThemeEmerald),
                                Pair(AppThemeColor.OCEAN, ThemeOcean),
                                Pair(AppThemeColor.CORAL, ThemeAmberCoral),
                                Pair(AppThemeColor.VIOLET, ThemeViolet)
                            )
                            items(colors) { (themeCol, color) ->
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable { viewModel.setThemeColor(themeCol) }
                                        .border(
                                            width = if (currentThemeColor == themeCol) 3.dp else 1.dp,
                                            color = if (currentThemeColor == themeCol) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (currentThemeColor == themeCol) {
                                        Icon(Icons.Default.Check, contentDescription = "Selected", tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Saved Addresses & Custom Delivery Hubs
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Place, contentDescription = null, tint = ServexaTeal)
                                Text("Saved Address Book", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }

                            TextButton(
                                onClick = { showAddLocationDialog = true },
                                modifier = Modifier.testTag("account_add_address_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Add New", fontWeight = FontWeight.Bold, color = ServexaTeal)
                            }
                        }

                        // Current selected active address item
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        Icons.Default.HomeWork,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("Default Service Address", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = ServexaTeal.copy(alpha = 0.15f)
                                            ) {
                                                Text("ACTIVE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ServexaTeal, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                            }
                                        }
                                        Text(userLocationName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                IconButton(onClick = { showAddLocationDialog = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Address", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            // 4. Navigation Settings Options & Complain Helpdesk
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column {
                        ProfileOptionItem(
                            icon = Icons.Default.AccountBalanceWallet,
                            title = "Servexa Pay Wallet",
                            subtitle = if (currentUser != null) "Balance: $${"%.2f".format(wallet?.availableBalance ?: 0.0)}" else "Sign in to access digital wallet",
                            onClick = { viewModel.navigateTo(ScreenDestination.CustomerWallet) }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))

                        ProfileOptionItem(
                            icon = Icons.Default.CalendarMonth,
                            title = "My Service Bookings",
                            subtitle = "View and manage active appointments",
                            onClick = { viewModel.navigateTo(ScreenDestination.CustomerBookings) }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))

                        ProfileOptionItem(
                            icon = Icons.Default.Call,
                            title = "Secure Voice Call History",
                            subtitle = "View PBX call logs and durations",
                            onClick = { viewModel.navigateTo(ScreenDestination.CallHistory) }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))

                        ProfileOptionItem(
                            icon = Icons.Default.ShoppingCart,
                            title = "Marketplace Purchases",
                            subtitle = "Tools and supplies orders",
                            onClick = { viewModel.navigateTo(ScreenDestination.ProductMarketplace) }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))

                        ProfileOptionItem(
                            icon = Icons.Default.ReportProblem,
                            title = "Lodge a Complaint / Support Desk",
                            subtitle = "Directly report issues to Servexa Admin",
                            onClick = { showComplaintDialog = true }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))

                        ProfileOptionItem(
                            icon = Icons.Default.Security,
                            title = "Privacy & Encryption",
                            subtitle = "End-to-end data safety & escrow guarantee",
                            onClick = { }
                        )
                    }
                }
            }

            // 5. Account Management Actions (Log In / Switch / Log Out)
            item {
                if (currentUser != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { showLoginDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("account_switch_dialog_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.SwitchAccount, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Switch Account / Log In as Another User", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.logout() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("profile_logout_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ServexaRose),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Log Out of Session", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = { viewModel.navigateTo(ScreenDestination.Auth) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("profile_login_full_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ServexaIndigo),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Login, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log In or Create Account", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 6. Discreet System Version & App Branding
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(54.dp),
                        shadowElevation = 2.dp
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_icon),
                            contentDescription = "Servexa App Icon",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Text(
                        text = "Servexa",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "The Modern On-Demand Service & Commerce Platform",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Version 2.4.0 • Enterprise Cloud Ecosystem",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }

    // Direct Complain Dialog to Admin
    if (showAddLocationDialog) {
        AddLocationDialog(
            viewModel = viewModel,
            initialAddress = userLocationName,
            onDismiss = { showAddLocationDialog = false },
            onLocationSet = { addr, lat, lng ->
                // Automatically updated in ViewModel
            }
        )
    }

    if (showComplaintDialog) {
        AlertDialog(
            onDismissRequest = { showComplaintDialog = false },
            title = { Text("Submit Complaint") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Your complaint will be logged into the disputes & resolutions queue immediately.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = complaintReason,
                        onValueChange = { complaintReason = it },
                        label = { Text("Complaint Topic") },
                        placeholder = { Text("e.g. Service quality, delay, payment issue") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = complaintDetails,
                        onValueChange = { complaintDetails = it },
                        label = { Text("Detailed Explanation") },
                        placeholder = { Text("Provide all relevant information for review...") },
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
                            bookingId = "GENERAL-COMPLAINT"
                        ) {
                            showComplaintDialog = false
                            complaintReason = ""
                            complaintDetails = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ServexaRose)
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showComplaintDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal In-Account Login & Switch Account Dialog
    if (showLoginDialog) {
        AccountLoginSwitcherDialog(
            viewModel = viewModel,
            onDismiss = { showLoginDialog = false }
        )
    }

    if (showKycUploadDialog) {
        KycDocumentUploadDialog(
            currentDoc = kycDoc,
            onSubmit = { docType, docNum, country, state, expiry, address, frontPhoto, backPhoto, selfiePhoto ->
                viewModel.submitKycDocument(
                    documentType = docType,
                    documentNumber = docNum,
                    issuingCountry = country,
                    issuingStateOrProvince = state,
                    expiryDate = expiry,
                    residentialAddress = address,
                    documentFrontPhotoUrl = frontPhoto,
                    documentBackPhotoUrl = backPhoto,
                    selfiePhotoUrl = selfiePhoto
                )
                showKycUploadDialog = false
            },
            onDismiss = { showKycUploadDialog = false }
        )
    }
}

@Composable
fun KycDocumentUploadDialog(
    currentDoc: com.example.data.local.entity.UserKycDocumentEntity?,
    onSubmit: (
        docType: String,
        docNum: String,
        country: String,
        state: String,
        expiry: String,
        address: String,
        frontPhoto: String,
        backPhoto: String,
        selfiePhoto: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(currentDoc?.documentType ?: "DRIVING_LICENSE") }
    var documentNumber by remember { mutableStateOf(currentDoc?.documentNumber ?: "DL-9382-7491") }
    var issuingCountry by remember { mutableStateOf(currentDoc?.issuingCountry ?: "United States") }
    var issuingState by remember { mutableStateOf(currentDoc?.issuingStateOrProvince ?: "California") }
    var expiryDate by remember { mutableStateOf(currentDoc?.expiryDate ?: "2028-11-30") }
    var address by remember { mutableStateOf(currentDoc?.residentialAddress ?: "742 Evergreen Terrace, San Francisco, CA") }

    var frontPhotoAttached by remember { mutableStateOf(true) }
    var backPhotoAttached by remember { mutableStateOf(true) }
    var selfieAttached by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Badge, contentDescription = null, tint = ServexaIndigo)
                Text(
                    text = "Identity Document Verification",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("kyc_upload_form_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "To enable wallet transactions, POS terminal credit allotments, and withdrawals, please submit your legal identification document.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    Text(
                        text = "Select Document Type:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedType == "DRIVING_LICENSE",
                            onClick = { selectedType = "DRIVING_LICENSE" },
                            label = { Text("Driving License", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).testTag("doc_type_dl")
                        )
                        FilterChip(
                            selected = selectedType == "NATIONAL_ID",
                            onClick = { selectedType = "NATIONAL_ID" },
                            label = { Text("National ID", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).testTag("doc_type_id")
                        )
                        FilterChip(
                            selected = selectedType == "PASSPORT",
                            onClick = { selectedType = "PASSPORT" },
                            label = { Text("Passport", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).testTag("doc_type_passport")
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = documentNumber,
                        onValueChange = { documentNumber = it },
                        label = { Text("Document / License Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_doc_number")
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = issuingCountry,
                            onValueChange = { issuingCountry = it },
                            label = { Text("Country") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("input_issuing_country")
                        )
                        OutlinedTextField(
                            value = issuingState,
                            onValueChange = { issuingState = it },
                            label = { Text("State / Province") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("input_issuing_state")
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { expiryDate = it },
                        label = { Text("Expiry Date (YYYY-MM-DD)") },
                        placeholder = { Text("2028-12-31") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_expiry_date")
                    )
                }

                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Registered Address") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth().testTag("input_residential_address")
                    )
                }

                item {
                    Text(
                        text = "Document Scans & Selfie Verification:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { frontPhotoAttached = !frontPhotoAttached },
                            modifier = Modifier.weight(1f).testTag("toggle_front_photo_btn"),
                            shape = RoundedCornerShape(8.dp),
                            colors = if (frontPhotoAttached) ButtonDefaults.outlinedButtonColors(contentColor = ServexaTeal) else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Icon(
                                if (frontPhotoAttached) Icons.Default.CheckCircle else Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (frontPhotoAttached) "Front ✓" else "Front Scan", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { backPhotoAttached = !backPhotoAttached },
                            modifier = Modifier.weight(1f).testTag("toggle_back_photo_btn"),
                            shape = RoundedCornerShape(8.dp),
                            colors = if (backPhotoAttached) ButtonDefaults.outlinedButtonColors(contentColor = ServexaTeal) else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Icon(
                                if (backPhotoAttached) Icons.Default.CheckCircle else Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (backPhotoAttached) "Back ✓" else "Back Scan", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { selfieAttached = !selfieAttached },
                            modifier = Modifier.weight(1f).testTag("toggle_selfie_photo_btn"),
                            shape = RoundedCornerShape(8.dp),
                            colors = if (selfieAttached) ButtonDefaults.outlinedButtonColors(contentColor = ServexaTeal) else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Icon(
                                if (selfieAttached) Icons.Default.CheckCircle else Icons.Default.Face,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (selfieAttached) "Selfie ✓" else "Selfie", fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        selectedType,
                        documentNumber.trim(),
                        issuingCountry.trim(),
                        issuingState.trim(),
                        expiryDate.trim(),
                        address.trim(),
                        if (frontPhotoAttached) "https://servexa.app/docs/front_${selectedType.lowercase()}.png" else "",
                        if (backPhotoAttached) "https://servexa.app/docs/back_${selectedType.lowercase()}.png" else "",
                        if (selfieAttached) "https://servexa.app/docs/selfie_match.png" else ""
                    )
                },
                enabled = documentNumber.isNotBlank() && issuingCountry.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ServexaIndigo),
                modifier = Modifier.testTag("submit_kyc_document_btn")
            ) {
                Text("Submit for Admin Review")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AccountLoginSwitcherDialog(
    viewModel: ServexaViewModel,
    onDismiss: () -> Unit
) {
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Log In & Switch Account", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Enter user credentials or choose a 1-tap test role to log into your account:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email or Username") },
                        placeholder = { Text("e.g. alex@customer.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_login_email")
                    )
                }

                item {
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password") },
                        placeholder = { Text("••••••••") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_login_password")
                    )
                }

                item {
                    Text(
                        text = "1-Tap Quick Role Login:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.switchDemoUser("CUSTOMER")
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Customer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.switchDemoUser("PROVIDER")
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Engineering, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Provider", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (emailInput.isNotBlank() && passwordInput.isNotBlank()) {
                        viewModel.login(emailInput.trim(), passwordInput.trim()) { success ->
                            if (success) {
                                onDismiss()
                            }
                        }
                    }
                },
                enabled = emailInput.isNotBlank() && passwordInput.isNotBlank(),
                modifier = Modifier.testTag("dialog_login_confirm_btn")
            ) {
                Text("Log In")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ProfileOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

