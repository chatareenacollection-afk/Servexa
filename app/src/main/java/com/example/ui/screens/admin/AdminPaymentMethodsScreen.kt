package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MerchantGatewayAccountEntity
import com.example.data.local.entity.PaymentMethodEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ServexaTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.ServexaViewModel

@Composable
fun AdminPaymentMethodsScreen(
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier
) {
    val methods by viewModel.adminPaymentMethods.collectAsState()
    val merchantGateways by viewModel.adminMerchantGateways.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Merchant Gateways, 1: Deposit Channels

    var showEditMethodDialog by remember { mutableStateOf(false) }
    var selectedMethodForEdit by remember { mutableStateOf<PaymentMethodEntity?>(null) }

    var showEditGatewayDialog by remember { mutableStateOf(false) }
    var selectedGatewayForEdit by remember { mutableStateOf<MerchantGatewayAccountEntity?>(null) }

    Scaffold(
        topBar = {
            ServexaTopBar(
                title = "Payment Gateways & Capture",
                subtitle = "Attach merchant accounts & user deposit methods",
                showBack = true,
                onBackClick = { viewModel.navigateBack() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) {
                        selectedGatewayForEdit = null
                        showEditGatewayDialog = true
                    } else {
                        selectedMethodForEdit = null
                        showEditMethodDialog = true
                    }
                },
                containerColor = ServexaTeal,
                contentColor = Color.White,
                modifier = Modifier.testTag("admin_add_payment_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Merchant Gateways (${merchantGateways.size})", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Top-up Channels (${methods.size})", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                // MERCHANT GATEWAYS & PAYMENT CAPTURE
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
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
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text(
                                        text = "Direct Merchant Capture Engine",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Attach Stripe, PayPal, Razorpay, or Escrow accounts. Payments are captured into your merchant account, and customer contact details are stored in this admin panel.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }

                    if (merchantGateways.isEmpty()) {
                        item {
                            EmptyStateView(
                                title = "No Merchant Accounts Attached",
                                subtitle = "Click the '+' button below to attach a Stripe, PayPal, or Bank Gateway to capture payments directly.",
                                icon = Icons.Default.AccountBalance
                            )
                        }
                    } else {
                        items(merchantGateways, key = { it.id }) { gateway ->
                            MerchantGatewayAdminCard(
                                gateway = gateway,
                                onEdit = {
                                    selectedGatewayForEdit = gateway
                                    showEditGatewayDialog = true
                                },
                                onToggleActive = {
                                    viewModel.adminSaveMerchantGateway(gateway.copy(isActive = !gateway.isActive))
                                },
                                onSetDefault = {
                                    // Mark this one default
                                    merchantGateways.forEach { g ->
                                        viewModel.adminSaveMerchantGateway(g.copy(isDefault = g.id == gateway.id))
                                    }
                                },
                                onDelete = {
                                    viewModel.adminDeleteMerchantGateway(gateway.id)
                                },
                                onTestHandshake = {
                                    viewModel.adminTestMerchantHandshake(gateway) { _, _ -> }
                                }
                            )
                        }
                    }
                }
            } else {
                // USER TOP-UP & DEPOSIT CHANNELS
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.CreditScore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text(
                                        text = "Customer-Facing Wallet Deposit Channels",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "These instructions and accounts are displayed to customers when adding money to their wallet.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    if (methods.isEmpty()) {
                        item {
                            EmptyStateView(
                                title = "No Payment Methods Configured",
                                subtitle = "Click the '+' button below to add bank accounts, card gateways, crypto USDT addresses, or agent channels.",
                                icon = Icons.Default.Payments
                            )
                        }
                    } else {
                        items(methods, key = { it.id }) { method ->
                            PaymentMethodAdminCard(
                                method = method,
                                onEdit = {
                                    selectedMethodForEdit = method
                                    showEditMethodDialog = true
                                },
                                onToggleActive = {
                                    viewModel.adminSavePaymentMethod(method.copy(active = !method.active))
                                },
                                onDelete = {
                                    viewModel.adminDeletePaymentMethod(method.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEditMethodDialog) {
        EditPaymentMethodDialog(
            existing = selectedMethodForEdit,
            onDismiss = { showEditMethodDialog = false },
            onSave = { method ->
                viewModel.adminSavePaymentMethod(method) {
                    showEditMethodDialog = false
                }
            }
        )
    }

    if (showEditGatewayDialog) {
        EditMerchantGatewayDialog(
            existing = selectedGatewayForEdit,
            onDismiss = { showEditGatewayDialog = false },
            onSave = { gateway ->
                viewModel.adminSaveMerchantGateway(gateway) {
                    showEditGatewayDialog = false
                }
            }
        )
    }
}

@Composable
fun MerchantGatewayAdminCard(
    gateway: MerchantGatewayAccountEntity,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit,
    onTestHandshake: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (gateway.isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (gateway.isActive) 2.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                when (gateway.gatewayType) {
                                    "STRIPE" -> ServexaIndigo.copy(alpha = 0.15f)
                                    "PAYPAL" -> Color(0xFF003087).copy(alpha = 0.15f)
                                    "RAZORPAY" -> Color(0xFF0C2340).copy(alpha = 0.15f)
                                    else -> ServexaTeal.copy(alpha = 0.15f)
                                },
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (gateway.gatewayType) {
                                "STRIPE" -> Icons.Default.CreditCard
                                "PAYPAL" -> Icons.Default.Payments
                                "RAZORPAY" -> Icons.Default.Bolt
                                else -> Icons.Default.AccountBalance
                            },
                            contentDescription = null,
                            tint = when (gateway.gatewayType) {
                                "STRIPE" -> ServexaIndigo
                                "PAYPAL" -> Color(0xFF003087)
                                "RAZORPAY" -> ServexaTeal
                                else -> ServexaTeal
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = gateway.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (gateway.isDefault) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = ServexaTeal.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "DEFAULT",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ServexaTeal,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${gateway.gatewayType} • ${if (gateway.isLiveMode) "LIVE PRODUCTION" else "SANDBOX TEST"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (gateway.isLiveMode) ServexaGreen else ServexaAmber
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (gateway.isActive) ServexaGreen.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (gateway.isActive) "ACTIVE" else "INACTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (gateway.isActive) ServexaGreen else Color.Gray,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider()

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Merchant ID / Acct: ${gateway.merchantAccountId}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (gateway.publicKeyOrClientId.isNotBlank()) {
                    Text(
                        text = "Public / Client Key: ${gateway.publicKeyOrClientId.take(18)}...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (gateway.webhookEndpointUrl.isNotBlank()) {
                    Text(
                        text = "Webhook: ${gateway.webhookEndpointUrl}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (gateway.autoCapture) "⚡ Auto-Capture: ON" else "🔒 Escrow Hold: ON",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (gateway.autoCapture) ServexaGreen else ServexaIndigo
                    )
                    Text(
                        text = if (gateway.captureCustomerDetails) "👤 Customer Details: CAPTURED" else "👤 Details: OFF",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onTestHandshake,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test Handshake", fontSize = 12.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!gateway.isDefault) {
                        TextButton(onClick = onSetDefault) {
                            Text("Make Default", fontSize = 12.sp)
                        }
                    }
                    TextButton(onClick = onToggleActive) {
                        Text(if (gateway.isActive) "Disable" else "Enable", fontSize = 12.sp)
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ServexaRose)
                    }
                }
            }
        }
    }
}

@Composable
fun EditMerchantGatewayDialog(
    existing: MerchantGatewayAccountEntity?,
    onDismiss: () -> Unit,
    onSave: (MerchantGatewayAccountEntity) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var gatewayType by remember { mutableStateOf(existing?.gatewayType ?: "STRIPE") }
    var merchantAccountId by remember { mutableStateOf(existing?.merchantAccountId ?: "") }
    var publicKeyOrClientId by remember { mutableStateOf(existing?.publicKeyOrClientId ?: "") }
    var secretKeyOrApiKey by remember { mutableStateOf(existing?.secretKeyOrApiKey ?: "") }
    var webhookSecret by remember { mutableStateOf(existing?.webhookSecret ?: "") }
    var webhookEndpointUrl by remember { mutableStateOf(existing?.webhookEndpointUrl ?: "https://api.servexa.com/v1/payments/webhooks") }
    var isLiveMode by remember { mutableStateOf(existing?.isLiveMode ?: true) }
    var autoCapture by remember { mutableStateOf(existing?.autoCapture ?: true) }
    var captureCustomerDetails by remember { mutableStateOf(existing?.captureCustomerDetails ?: true) }
    var settlementCurrency by remember { mutableStateOf(existing?.settlementCurrency ?: "USD") }
    var platformFeePercentText by remember { mutableStateOf(existing?.platformFeePercent?.toString() ?: "5.0") }
    var isActive by remember { mutableStateOf(existing?.isActive ?: true) }
    var isDefault by remember { mutableStateOf(existing?.isDefault ?: false) }

    var showSecretKey by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existing != null) "Edit Merchant Gateway" else "Attach Merchant Account",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("Gateway Platform", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("STRIPE", "PAYPAL", "RAZORPAY", "BANK_ESCROW").forEach { t ->
                            FilterChip(
                                selected = gatewayType == t,
                                onClick = {
                                    gatewayType = t
                                    if (name.isBlank() || name.contains("Merchant")) {
                                        name = when (t) {
                                            "STRIPE" -> "Stripe Merchant Gateway"
                                            "PAYPAL" -> "PayPal Commerce Account"
                                            "RAZORPAY" -> "Razorpay Live Gateway"
                                            "BANK_ESCROW" -> "Servexa Escrow Bank Gateway"
                                            else -> "$t Merchant Gateway"
                                        }
                                    }
                                },
                                label = { Text(t.replace("_", " "), fontSize = 10.sp) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Merchant Account Label") },
                        placeholder = { Text("e.g. Stripe US Business") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = merchantAccountId,
                        onValueChange = { merchantAccountId = it },
                        label = { Text("Merchant Account ID / Email / IBAN") },
                        placeholder = { Text("e.g. acct_1Nz828xServexaMerc") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = publicKeyOrClientId,
                        onValueChange = { publicKeyOrClientId = it },
                        label = { Text("Public Key / Client ID") },
                        placeholder = { Text("pk_live_...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = secretKeyOrApiKey,
                        onValueChange = { secretKeyOrApiKey = it },
                        label = { Text("Secret Key / API Key / Secret Token") },
                        placeholder = { Text("sk_live_...") },
                        singleLine = true,
                        visualTransformation = if (showSecretKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showSecretKey = !showSecretKey }) {
                                Icon(
                                    imageVector = if (showSecretKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Secret"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = webhookSecret,
                        onValueChange = { webhookSecret = it },
                        label = { Text("Webhook Signing Secret (Optional)") },
                        placeholder = { Text("whsec_...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = webhookEndpointUrl,
                        onValueChange = { webhookEndpointUrl = it },
                        label = { Text("Webhook Callback URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = settlementCurrency,
                            onValueChange = { settlementCurrency = it },
                            label = { Text("Currency") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = platformFeePercentText,
                            onValueChange = { platformFeePercentText = it },
                            label = { Text("Platform Fee %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Live Production Mode", fontWeight = FontWeight.SemiBold)
                            Text("When OFF, runs in test sandbox mode", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = isLiveMode, onCheckedChange = { isLiveMode = it })
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Auto-Capture Payments", fontWeight = FontWeight.SemiBold)
                            Text("Instant capture directly into merchant account", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = autoCapture, onCheckedChange = { autoCapture = it })
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Store Customer Details", fontWeight = FontWeight.SemiBold)
                            Text("Save customer name, email, phone with transaction", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = captureCustomerDetails, onCheckedChange = { captureCustomerDetails = it })
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Active for Payments")
                        Switch(checked = isActive, onCheckedChange = { isActive = it })
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Default Gateway")
                        Switch(checked = isDefault, onCheckedChange = { isDefault = it })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val gatewayId = existing?.id ?: "gw_${gatewayType.lowercase()}_${System.currentTimeMillis()}"
                    val gateway = MerchantGatewayAccountEntity(
                        id = gatewayId,
                        name = name.trim(),
                        gatewayType = gatewayType,
                        merchantAccountId = merchantAccountId.trim(),
                        publicKeyOrClientId = publicKeyOrClientId.trim(),
                        secretKeyOrApiKey = secretKeyOrApiKey.trim(),
                        webhookSecret = webhookSecret.trim(),
                        webhookEndpointUrl = webhookEndpointUrl.trim(),
                        isLiveMode = isLiveMode,
                        autoCapture = autoCapture,
                        captureCustomerDetails = captureCustomerDetails,
                        settlementCurrency = settlementCurrency.trim(),
                        platformFeePercent = platformFeePercentText.toDoubleOrNull() ?: 5.0,
                        payoutDelayDays = 2,
                        isActive = isActive,
                        isDefault = isDefault,
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(gateway)
                },
                enabled = name.isNotBlank() && merchantAccountId.isNotBlank()
            ) {
                Text("Attach Gateway")
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
fun PaymentMethodAdminCard(
    method: PaymentMethodEntity,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (method.active) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (method.active) 2.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                when (method.type) {
                                    "BANK_TRANSFER" -> ServexaIndigo.copy(alpha = 0.15f)
                                    "CARD" -> ServexaTeal.copy(alpha = 0.15f)
                                    "CRYPTO" -> ServexaAmber.copy(alpha = 0.15f)
                                    "PAYPAL" -> Color(0xFF003087).copy(alpha = 0.15f)
                                    else -> Color.Gray.copy(alpha = 0.15f)
                                },
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (method.type) {
                                "BANK_TRANSFER" -> Icons.Default.AccountBalance
                                "CARD" -> Icons.Default.CreditCard
                                "CRYPTO" -> Icons.Default.CurrencyBitcoin
                                "PAYPAL" -> Icons.Default.Payments
                                else -> Icons.Default.Storefront
                            },
                            contentDescription = null,
                            tint = when (method.type) {
                                "BANK_TRANSFER" -> ServexaIndigo
                                "CARD" -> ServexaTeal
                                "CRYPTO" -> ServexaAmber
                                "PAYPAL" -> Color(0xFF003087)
                                else -> Color.Gray
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = method.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${method.bankOrProviderName} (${method.type})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (method.active) ServexaGreen.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (method.active) "ACTIVE" else "DISABLED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (method.active) ServexaGreen else Color.Gray,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider()

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Account / Address: ${method.accountNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (method.accountTitle.isNotBlank()) {
                    Text(
                        text = "Title / Beneficiary: ${method.accountTitle}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (method.routingOrSwift.isNotBlank()) {
                    Text(
                        text = method.routingOrSwift,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (method.instructions.isNotBlank()) {
                    Text(
                        text = "Memo: ${method.instructions}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Limits: $${"%.0f".format(method.minAmount)} - $${"%.0f".format(method.maxAmount)} | Fee: ${"%.1f".format(method.feePercent)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(onClick = onToggleActive) {
                        Text(if (method.active) "Disable" else "Enable")
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ServexaRose)
                    }
                }
            }
        }
    }
}

@Composable
fun EditPaymentMethodDialog(
    existing: PaymentMethodEntity?,
    onDismiss: () -> Unit,
    onSave: (PaymentMethodEntity) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var type by remember { mutableStateOf(existing?.type ?: "BANK_TRANSFER") }
    var accountTitle by remember { mutableStateOf(existing?.accountTitle ?: "") }
    var accountNumber by remember { mutableStateOf(existing?.accountNumber ?: "") }
    var bankOrProviderName by remember { mutableStateOf(existing?.bankOrProviderName ?: "") }
    var routingOrSwift by remember { mutableStateOf(existing?.routingOrSwift ?: "") }
    var instructions by remember { mutableStateOf(existing?.instructions ?: "") }
    var minAmountText by remember { mutableStateOf(existing?.minAmount?.toString() ?: "10") }
    var maxAmountText by remember { mutableStateOf(existing?.maxAmount?.toString() ?: "10000") }
    var feePercentText by remember { mutableStateOf(existing?.feePercent?.toString() ?: "5.0") }
    var active by remember { mutableStateOf(existing?.active ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existing != null) "Edit Payment Method" else "Add New Payment Method",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("Payment Type", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("BANK_TRANSFER", "CARD", "CRYPTO", "PAYPAL", "CASH").forEach { t ->
                            FilterChip(
                                selected = type == t,
                                onClick = { type = t },
                                label = { Text(t.replace("_", " "), fontSize = 10.sp) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Display Name") },
                        placeholder = { Text("e.g. JPMorgan Chase Bank / USDT TRC20") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = bankOrProviderName,
                        onValueChange = { bankOrProviderName = it },
                        label = { Text("Bank / Provider / Network") },
                        placeholder = { Text("e.g. Chase Bank, Stripe, TRON Network") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = accountTitle,
                        onValueChange = { accountTitle = it },
                        label = { Text("Beneficiary / Account Title") },
                        placeholder = { Text("e.g. Servexa Marketplace LLC") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it },
                        label = { Text("Account Number / Wallet Address / Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = routingOrSwift,
                        onValueChange = { routingOrSwift = it },
                        label = { Text("Routing / SWIFT / Network details") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        label = { Text("User Instructions / Memo Requirement") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = minAmountText,
                            onValueChange = { minAmountText = it },
                            label = { Text("Min ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = maxAmountText,
                            onValueChange = { maxAmountText = it },
                            label = { Text("Max ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = feePercentText,
                            onValueChange = { feePercentText = it },
                            label = { Text("Fee %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Active for all users")
                        Switch(checked = active, onCheckedChange = { active = it })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val methodId = existing?.id ?: "pm_${type.lowercase()}_${System.currentTimeMillis()}"
                    val method = PaymentMethodEntity(
                        id = methodId,
                        name = name.trim(),
                        type = type,
                        accountTitle = accountTitle.trim(),
                        accountNumber = accountNumber.trim(),
                        bankOrProviderName = bankOrProviderName.trim(),
                        routingOrSwift = routingOrSwift.trim(),
                        instructions = instructions.trim(),
                        minAmount = minAmountText.toDoubleOrNull() ?: 10.0,
                        maxAmount = maxAmountText.toDoubleOrNull() ?: 10000.0,
                        feePercent = feePercentText.toDoubleOrNull() ?: 5.0,
                        active = active,
                        orderIndex = existing?.orderIndex ?: 1,
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(method)
                },
                enabled = name.isNotBlank() && accountNumber.isNotBlank()
            ) {
                Text("Save Method")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

