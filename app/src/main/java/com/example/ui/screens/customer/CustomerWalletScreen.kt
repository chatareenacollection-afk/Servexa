package com.example.ui.screens.customer

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
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.WalletTransactionEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.ServexaViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CustomerWalletScreen(
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val wallet by viewModel.customerWallet.collectAsState()
    val transactions by viewModel.customerTransactions.collectAsState()
    val paymentMethods by viewModel.paymentMethods.collectAsState()
    val payoutAccount by viewModel.userPayoutAccount.collectAsState()
    val kycDoc by viewModel.userKycDocument.collectAsState()

    var showTopUpDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showAttachPayoutDialog by remember { mutableStateOf(false) }
    var showKycDialog by remember { mutableStateOf(false) }
    var selectedDetailTxn by remember { mutableStateOf<WalletTransactionEntity?>(null) }

    val available = wallet?.availableBalance ?: 0.0
    val pending = wallet?.pendingBalance ?: 0.0
    val isKycVerified = kycDoc?.verificationStatus == "VERIFIED"
    val canNavigateBack by viewModel.canNavigateBack.collectAsState()

    Scaffold(
        topBar = {
            ServexaTopBar(
                title = "Servexa Pay Wallet",
                subtitle = if (isKycVerified) "Verified Wallet • Balance & POS Ledger" else "Identity Verification Required",
                showBack = canNavigateBack,
                onBackClick = { viewModel.navigateBack() }
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
            // 0. MANDATORY KYC DOCUMENT UPLOAD / VERIFICATION STATUS BANNER
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wallet_kyc_status_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isKycVerified) {
                            ServexaTeal.copy(alpha = 0.12f)
                        } else if (kycDoc?.verificationStatus == "PENDING") {
                            ServexaAmber.copy(alpha = 0.15f)
                        } else {
                            ServexaIndigo.copy(alpha = 0.12f)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                        .size(38.dp)
                                        .background(
                                            if (isKycVerified) ServexaTeal.copy(alpha = 0.2f) else ServexaIndigo.copy(alpha = 0.2f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isKycVerified) Icons.Default.VerifiedUser else Icons.Default.Badge,
                                        contentDescription = null,
                                        tint = if (isKycVerified) ServexaTeal else ServexaIndigo
                                    )
                                }

                                Column {
                                    Text(
                                        text = if (isKycVerified) "Wallet Verified & Active" else "Document Verification Required",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isKycVerified) "Driving License / ID / Passport Approved" else "Required for POS credit, deposits & payouts",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (kycDoc?.verificationStatus) {
                                    "VERIFIED" -> ServexaTeal
                                    "PENDING" -> ServexaAmber
                                    "REJECTED" -> ServexaRose
                                    else -> ServexaIndigo
                                }
                            ) {
                                Text(
                                    text = when (kycDoc?.verificationStatus) {
                                        "VERIFIED" -> "VERIFIED"
                                        "PENDING" -> "PENDING"
                                        "REJECTED" -> "REJECTED"
                                        else -> "REQUIRED"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        if (!isKycVerified) {
                            Text(
                                text = if (kycDoc?.verificationStatus == "PENDING") {
                                    "Your ${kycDoc?.documentType?.replace("_", " ")} (${kycDoc?.documentNumber}) has been submitted and is currently being reviewed by the platform administration."
                                } else {
                                    "Financial regulations and POS terminal credit allocations require customers to upload a valid Driving License, National ID Card, or Passport before transactions can be processed."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedButton(
                                onClick = { showKycDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("wallet_upload_document_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (kycDoc != null) "View / Update Uploaded Documents" else "Upload Driving License / ID / Passport",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 1. Wallet Balance Hero Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wallet_balance_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(ServexaIndigoDark, ServexaIndigo, Color(0xFF1E1B4B))
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
                                Text(
                                    text = "AVAILABLE BALANCE",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.8f),
                                    letterSpacing = 1.sp
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ServexaTeal.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = "POS & GATEWAY READY",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ServexaTealLight,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "$${"%.2f".format(available)}",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )

                            if (pending > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Pending in processing: $${"%.2f".format(pending)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ServexaAmber
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (!isKycVerified) {
                                            showKycDialog = true
                                        } else {
                                            showTopUpDialog = true
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("wallet_topup_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ServexaTeal)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add Money", color = Color.White, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        if (!isKycVerified) {
                                            showKycDialog = true
                                        } else {
                                            showWithdrawDialog = true
                                        }
                                    },
                                    modifier = Modifier.weight(1f).testTag("wallet_withdraw_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                ) {
                                    Icon(Icons.Default.ArrowOutward, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Withdraw")
                                }
                            }
                        }
                    }
                }
            }

            // 2. Linked Payout Account (Bank / Card Details)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAttachPayoutDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (payoutAccount != null) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (payoutAccount != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (payoutAccount != null) Icons.Default.AccountBalance else Icons.Default.AddCard,
                                    contentDescription = null,
                                    tint = if (payoutAccount != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }
                            Column {
                                Text(
                                    text = if (payoutAccount != null) "Attached Payout Account" else "Attach Bank / Card Details",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (payoutAccount != null) {
                                        val mask = if (payoutAccount!!.accountOrCardNumber.length >= 4) "****${payoutAccount!!.accountOrCardNumber.takeLast(4)}" else payoutAccount!!.accountOrCardNumber
                                        "${payoutAccount!!.bankOrIssuerName} ($mask) • 48h settlement"
                                    } else {
                                        "Required before requesting withdrawals (48-hour processing)"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 3. Fee & POS System Policy Notice
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.PointOfSale, contentDescription = null, tint = ServexaIndigo)
                        Text(
                            text = "POS terminal credits are instantly deposited with zero fee. Top-ups deduct 5% platform gateway fee. Tap any transaction below for complete payment metadata.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 4. Transactions Section Header
            item {
                Text(
                    text = "Transaction History & POS Receipts",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (transactions.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No transactions yet",
                        subtitle = "POS credits, gateway top-ups, bookings, and refunds will appear here in chronological order.",
                        icon = Icons.Default.ReceiptLong
                    )
                }
            } else {
                items(transactions, key = { it.id }) { txn ->
                    TransactionItemRow(
                        txn = txn,
                        onClick = { selectedDetailTxn = txn }
                    )
                }
            }
        }
    }

    if (showTopUpDialog) {
        WalletTopUpDialog(
            currentBalance = available,
            availablePaymentMethods = paymentMethods,
            onDismiss = { showTopUpDialog = false },
            onTopUp = { amount, method, ref -> viewModel.topUpWallet(amount, method, ref) }
        )
    }

    if (showWithdrawDialog) {
        WithdrawalRequestDialog(
            availableBalance = available,
            payoutAccount = payoutAccount,
            onDismiss = { showWithdrawDialog = false },
            onAttachDetails = { showAttachPayoutDialog = true },
            onSubmit = { amount -> viewModel.requestWithdrawal(amount) }
        )
    }

    if (showAttachPayoutDialog) {
        AttachPayoutAccountDialog(
            existingAccount = payoutAccount,
            onDismiss = { showAttachPayoutDialog = false },
            onSave = { type, name, bank, acc, routing, swift, country ->
                viewModel.savePayoutAccount(type, name, bank, acc, routing, swift, country) {
                    showAttachPayoutDialog = false
                }
            }
        )
    }

    if (showKycDialog) {
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
                showKycDialog = false
            },
            onDismiss = { showKycDialog = false }
        )
    }

    if (selectedDetailTxn != null) {
        PaymentTransactionDetailDialog(
            txn = selectedDetailTxn!!,
            onDismiss = { selectedDetailTxn = null }
        )
    }
}

@Composable
fun TransactionItemRow(
    txn: WalletTransactionEntity,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isCredit = txn.type in listOf("TOP_UP", "SERVICE_EARNING", "REFUND") || (txn.type == "ADJUSTMENT" && txn.netAmount > 0)
    val isPos = txn.paymentChannel == "POS_TERMINAL"
    val dateStr = remember(txn.createdAt) {
        SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(txn.createdAt))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("txn_item_${txn.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (isPos) ServexaIndigo.copy(alpha = 0.18f) else if (isCredit) ServexaGreen.copy(alpha = 0.15f) else ServexaRose.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPos) {
                            Icons.Default.PointOfSale
                        } else when (txn.type) {
                            "TOP_UP" -> Icons.Default.AddCard
                            "SERVICE_PAYMENT" -> Icons.Default.ShoppingBag
                            "SERVICE_EARNING" -> Icons.Default.Payments
                            "REFUND" -> Icons.Default.Restore
                            "ADJUSTMENT" -> Icons.Default.Tune
                            else -> Icons.Default.AccountBalanceWallet
                        },
                        contentDescription = txn.type,
                        tint = if (isPos) ServexaIndigo else if (isCredit) ServexaGreen else ServexaRose,
                        modifier = Modifier.size(20.dp)
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
                                color = ServexaIndigo.copy(alpha = 0.15f)
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
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (txn.note.isNotEmpty()) {
                        Text(
                            text = txn.note,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
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
                        shape = RoundedCornerShape(6.dp),
                        color = ServexaAmber.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "PENDING",
                            style = MaterialTheme.typography.labelSmall,
                            color = ServexaAmber,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentTransactionDetailDialog(
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
                Icon(Icons.Default.Receipt, contentDescription = null, tint = ServexaTeal)
                Text("Full Payment & POS Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Transaction ID:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(txn.id, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Type:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(txn.type.replace("_", " "), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Status:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(txn.status, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (txn.status == "COMPLETED") ServexaGreen else ServexaAmber)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Payment Channel:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(txn.paymentChannel, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Gross Amount:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$${"%.2f".format(txn.grossAmount)}", style = MaterialTheme.typography.labelMedium)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Platform Fee (5%):", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$${"%.2f".format(txn.fee)}", style = MaterialTheme.typography.labelMedium)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Net Credited:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$${"%.2f".format(txn.netAmount)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ServexaGreen)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Date & Time:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(dateStr, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                if (txn.paymentChannel == "POS_TERMINAL" || txn.posTerminalId.isNotBlank()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = ServexaIndigo.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("POS System Details:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ServexaIndigo)
                                Text("Terminal ID: ${txn.posTerminalId.ifBlank { "POS-TERM-01" }}", style = MaterialTheme.typography.bodySmall)
                                Text("Station Location: ${txn.posLocation.ifBlank { "Main Downtown Hub" }}", style = MaterialTheme.typography.bodySmall)
                                Text("Certified Agent / Cashier: ${txn.posAgentName.ifBlank { "Admin / Store Manager" }}", style = MaterialTheme.typography.bodySmall)
                                Text("Auth Code: ${txn.posAuthCode.ifBlank { "AUTH-POS-9928" }}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                if (txn.note.isNotBlank()) {
                    item {
                        Text("Notes: ${txn.note}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
