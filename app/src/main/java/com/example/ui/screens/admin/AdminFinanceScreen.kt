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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.WalletTransactionEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ServexaTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.ServexaViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminFinanceScreen(
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.adminAllTransactions.collectAsState()
    val allUsers by viewModel.adminAllUsers.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTxnForDetail by remember { mutableStateOf<WalletTransactionEntity?>(null) }
    var showPosAllotDialog by remember { mutableStateOf(false) }

    val filteredTxns = remember(transactions, selectedFilter, searchQuery) {
        transactions.filter { txn ->
            val matchesFilter = when (selectedFilter) {
                "PENDING" -> txn.status == "PENDING"
                "POS" -> txn.paymentChannel == "POS_TERMINAL"
                "TOP_UP" -> txn.type == "TOP_UP"
                "WITHDRAWAL" -> txn.type == "WITHDRAWAL"
                "BOOKING_PAYMENT" -> txn.type == "BOOKING_PAYMENT"
                "ORDER_CHECKOUT" -> txn.type == "ORDER_CHECKOUT"
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                    txn.id.contains(searchQuery, ignoreCase = true) ||
                    txn.userId.contains(searchQuery, ignoreCase = true) ||
                    txn.customerName.contains(searchQuery, ignoreCase = true) ||
                    txn.customerEmail.contains(searchQuery, ignoreCase = true) ||
                    txn.posTerminalId.contains(searchQuery, ignoreCase = true) ||
                    txn.posAuthCode.contains(searchQuery, ignoreCase = true) ||
                    txn.merchantCaptureRef.contains(searchQuery, ignoreCase = true) ||
                    txn.merchantGatewayName.contains(searchQuery, ignoreCase = true)

            matchesFilter && matchesSearch
        }
    }

    val pendingCount = remember(transactions) {
        transactions.count { it.status == "PENDING" }
    }
    val posCount = remember(transactions) {
        transactions.count { it.paymentChannel == "POS_TERMINAL" }
    }

    Scaffold(
        topBar = {
            ServexaTopBar(
                title = "Payment Audit & Ledger",
                subtitle = "${transactions.size} records • POS Terminal & Gateway Captures",
                showBack = true,
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
            // POS Action Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ServexaIndigo.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
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
                                .background(ServexaIndigo, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PointOfSale, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("POS Terminal System", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("$posCount POS credits recorded", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Button(
                        onClick = { showPosAllotDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ServexaIndigo),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("admin_allot_pos_credit_button")
                    ) {
                        Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Allot POS Credit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by customer, email, txn #, POS terminal, auth code...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("ALL", "POS", "PENDING", "TOP_UP", "BOOKING_PAYMENT", "WITHDRAWAL").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = {
                            val labelText = when (filter) {
                                "POS" -> "POS ($posCount)"
                                "PENDING" -> "Pending ($pendingCount)"
                                else -> filter.replace("_", " ")
                            }
                            Text(labelText, fontSize = 11.sp)
                        }
                    )
                }
            }

            if (filteredTxns.isEmpty()) {
                EmptyStateView(
                    title = "No transactions found",
                    subtitle = "No payment records match the current filter or search criteria.",
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val pendingList = filteredTxns.filter { it.status == "PENDING" }
                    if (pendingList.isNotEmpty()) {
                        item {
                            Text(
                                text = "Pending Review & Approvals (${pendingList.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ServexaRose
                            )
                        }
                        items(pendingList, key = { "pending_${it.id}" }) { txn ->
                            AdminPendingTxnCard(
                                txn = txn,
                                onApprove = { viewModel.adminApproveTransaction(txn.id) },
                                onReject = { viewModel.adminRejectTransaction(txn.id, "Rejected by administrator review") },
                                onClick = { selectedTxnForDetail = txn }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Complete Transaction & POS Stream (${filteredTxns.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    items(filteredTxns, key = { it.id }) { txn ->
                        AdminTransactionAuditRow(
                            txn = txn,
                            onClick = { selectedTxnForDetail = txn }
                        )
                    }
                }
            }
        }
    }

    if (showPosAllotDialog) {
        AdminAllotPosCreditDialog(
            users = allUsers.filter { it.role != "ADMIN" },
            onDismiss = { showPosAllotDialog = false },
            onConfirm = { userId, amount, terminalId, location, agentName, authCode, note ->
                viewModel.adminAllotPosCredit(
                    userId = userId,
                    amount = amount,
                    posTerminalId = terminalId,
                    posLocation = location,
                    posAgentName = agentName,
                    posAuthCode = authCode,
                    notes = note
                )
                showPosAllotDialog = false
            }
        )
    }

    if (selectedTxnForDetail != null) {
        TransactionCustomerDetailDialog(
            txn = selectedTxnForDetail!!,
            onDismiss = { selectedTxnForDetail = null }
        )
    }
}

@Composable
fun AdminTransactionAuditRow(
    txn: WalletTransactionEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCredit = txn.type in listOf("TOP_UP", "SERVICE_EARNING", "REFUND") || (txn.type == "ADJUSTMENT" && txn.netAmount > 0)
    val isPos = txn.paymentChannel == "POS_TERMINAL"
    val dateStr = remember(txn.createdAt) {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(txn.createdAt))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("admin_txn_row_${txn.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            if (isPos) ServexaIndigo.copy(alpha = 0.2f) else if (isCredit) ServexaGreen.copy(alpha = 0.15f) else ServexaRose.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPos) {
                            Icons.Default.PointOfSale
                        } else when (txn.type) {
                            "TOP_UP" -> Icons.Default.AddCard
                            "WITHDRAWAL" -> Icons.Default.ArrowOutward
                            "SERVICE_PAYMENT" -> Icons.Default.ShoppingBag
                            "BOOKING_PAYMENT" -> Icons.Default.Build
                            "ORDER_CHECKOUT" -> Icons.Default.Storefront
                            "REFUND" -> Icons.Default.Restore
                            else -> Icons.Default.Receipt
                        },
                        contentDescription = null,
                        tint = if (isPos) ServexaIndigo else if (isCredit) ServexaGreen else ServexaRose,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = txn.type.replace("_", " "),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isPos) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = ServexaIndigo.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "POS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ServexaIndigo,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "ID: ${txn.id.take(16)}... • $dateStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (txn.customerName.isNotBlank()) {
                        Text(
                            text = "Customer: ${txn.customerName} (${txn.customerEmail.ifBlank { txn.userId }})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isPos && txn.posTerminalId.isNotBlank()) {
                        Text(
                            text = "Terminal: ${txn.posTerminalId} • Agent: ${txn.posAgentName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ServexaIndigo
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isCredit) "+" else "-"} $${"%.2f".format(kotlin.math.abs(txn.netAmount))}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCredit) ServexaGreen else ServexaRose
                )
                if (txn.status == "PENDING") {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = ServexaAmber.copy(alpha = 0.2f)
                    ) {
                        Text("PENDING", style = MaterialTheme.typography.labelSmall, color = ServexaAmber, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminPendingTxnCard(
    txn: WalletTransactionEntity,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTopUp = txn.type == "TOP_UP"
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Surface(
                        color = if (isTopUp) ServexaTeal.copy(alpha = 0.15f) else ServexaRose.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isTopUp) "TOP-UP DEPOSIT REQUEST" else "WITHDRAWAL PAYOUT REQUEST",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isTopUp) ServexaTeal else ServexaRose,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Txn #${txn.id}", style = MaterialTheme.typography.labelMedium, color = ServexaIndigo)
                    Text("User ID: ${txn.userId}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    if (txn.customerName.isNotBlank()) {
                        Text("Customer: ${txn.customerName} (${txn.customerPhone})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${"%.2f".format(txn.grossAmount)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isTopUp) ServexaGreen else ServexaRose
                    )
                    if (isTopUp && txn.fee > 0) {
                        Text(
                            text = "Net: $${"%.2f".format(txn.netAmount)} (5% Fee: $${"%.2f".format(txn.fee)})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(txn.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ServexaRose)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isTopUp) "Reject Deposit" else "Reject & Refund")
                }

                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ServexaGreen)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isTopUp) "Approve & Credit" else "Approve Payout")
                }
            }
        }
    }
}

@Composable
fun TransactionCustomerDetailDialog(
    txn: WalletTransactionEntity,
    onDismiss: () -> Unit
) {
    val dateStr = remember(txn.createdAt) {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(txn.createdAt))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(text = "Admin Payment & POS Audit Ledger", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("Transaction Overview", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Transaction ID: ${txn.id}", style = MaterialTheme.typography.labelSmall)
                            Text("Type: ${txn.type}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("Payment Channel: ${txn.paymentChannel}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = ServexaIndigo)
                            Text("Gross Amount: $${"%.2f".format(txn.grossAmount)}", style = MaterialTheme.typography.bodySmall)
                            Text("Platform Fee (5%): $${"%.2f".format(txn.fee)}", style = MaterialTheme.typography.bodySmall)
                            Text("Net Amount: $${"%.2f".format(txn.netAmount)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ServexaGreen)
                            Text("Status: ${txn.status}", style = MaterialTheme.typography.labelSmall, color = if (txn.status == "COMPLETED") ServexaGreen else ServexaAmber)
                            Text("Created At: $dateStr", style = MaterialTheme.typography.labelSmall)
                            if (txn.note.isNotBlank()) {
                                Text("Note: ${txn.note}", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                // POS Details Block
                if (txn.paymentChannel == "POS_TERMINAL" || txn.posTerminalId.isNotBlank()) {
                    item {
                        Text("POS System Terminal Records", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ServexaIndigo)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = ServexaIndigo.copy(alpha = 0.12f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("POS Terminal ID: ${txn.posTerminalId.ifBlank { "POS-TERM-01" }}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Text("Terminal Location: ${txn.posLocation.ifBlank { "Main Hub" }}", style = MaterialTheme.typography.bodySmall)
                                Text("Agent / Cashier: ${txn.posAgentName.ifBlank { "Admin / Cashier Sarah" }}", style = MaterialTheme.typography.bodySmall)
                                Text("Authorization Code: ${txn.posAuthCode.ifBlank { "AUTH-POS-9928" }}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ServexaIndigo)
                            }
                        }
                    }
                }

                item {
                    Text("Customer Information", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Customer ID: ${txn.userId}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Text("Name: ${txn.customerName.ifBlank { "N/A" }}", style = MaterialTheme.typography.bodySmall)
                            Text("Email: ${txn.customerEmail.ifBlank { "N/A" }}", style = MaterialTheme.typography.bodySmall)
                            Text("Phone: ${txn.customerPhone.ifBlank { "N/A" }}", style = MaterialTheme.typography.bodySmall)
                            Text("Address: ${txn.customerAddress.ifBlank { "N/A" }}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                item {
                    Text("Merchant Payment Gateway Capture", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Merchant Gateway: ${txn.merchantGatewayName.ifBlank { if (txn.paymentChannel == "POS_TERMINAL") "Servexa POS System" else "Servexa Internal Ledger" }}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text("Merchant Account ID: ${txn.merchantAccountId.ifBlank { "Default Vault" }}", style = MaterialTheme.typography.bodySmall)
                            Text("Gateway Capture Ref: ${txn.merchantCaptureRef.ifBlank { txn.posAuthCode.ifBlank { "N/A" } }}", style = MaterialTheme.typography.labelSmall, color = ServexaIndigo)
                            Text("Capture Status: ${txn.captureStatus.ifBlank { "SETTLED" }}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ServexaGreen)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun AdminAllotPosCreditDialog(
    users: List<UserEntity>,
    onDismiss: () -> Unit,
    onConfirm: (userId: String, amount: Double, terminalId: String, location: String, agentName: String, authCode: String, note: String) -> Unit
) {
    var selectedUserId by remember { mutableStateOf(users.firstOrNull()?.id ?: "usr_customer_01") }
    var amountText by remember { mutableStateOf("100") }
    var terminalId by remember { mutableStateOf("POS-TERM-01 (Downtown Branch)") }
    var location by remember { mutableStateOf("Downtown Central Station Counter 4") }
    var agentName by remember { mutableStateOf("Admin Agent (Mr-Pirate)") }
    var authCode by remember { mutableStateOf("AUTH-POS-${(10000..99999).random()}") }
    var note by remember { mutableStateOf("Cash deposited at POS Terminal Counter") }

    val amount = amountText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.PointOfSale, contentDescription = null, tint = ServexaIndigo)
                Text("Allot POS System Credit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Direct physical POS terminal cash/card credit allotment with instant wallet deposit and complete audit trail.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    Text("Select Recipient Customer:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        users.take(5).forEach { user ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedUserId = user.id },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedUserId == user.id) ServexaIndigo.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(user.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text("${user.email} • ${user.phone}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (selectedUserId == user.id) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ServexaIndigo, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Credit Amount ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = terminalId,
                        onValueChange = { terminalId = it },
                        label = { Text("POS Terminal Device ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Terminal Physical Location") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = agentName,
                        onValueChange = { agentName = it },
                        label = { Text("Certified POS Agent / Cashier") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = authCode,
                        onValueChange = { authCode = it },
                        label = { Text("POS Authorization Code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Transaction Note / Reference") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (amount > 0 && selectedUserId.isNotBlank()) {
                        onConfirm(selectedUserId, amount, terminalId, location, agentName, authCode, note)
                    }
                },
                enabled = amount > 0 && selectedUserId.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ServexaIndigo)
            ) {
                Text("Allot $$amount via POS", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
