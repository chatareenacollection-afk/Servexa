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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.AttachPayoutAccountDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ServexaTopBar
import com.example.ui.components.WithdrawalRequestDialog
import com.example.ui.screens.customer.TransactionItemRow
import com.example.ui.theme.*
import com.example.ui.viewmodel.ServexaViewModel

@Composable
fun ProviderEarningsScreen(
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier
) {
    val wallet by viewModel.customerWallet.collectAsState()
    val transactions by viewModel.customerTransactions.collectAsState()
    val payoutAccount by viewModel.userPayoutAccount.collectAsState()

    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showAttachPayoutDialog by remember { mutableStateOf(false) }

    val available = wallet?.availableBalance ?: 0.0
    val pending = wallet?.pendingBalance ?: 0.0

    Scaffold(
        topBar = {
            ServexaTopBar(
                title = "Earnings & Payouts",
                subtitle = "Servexa 6% Commission & 48h Settlements",
                showBack = true,
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
            // Hero Earnings Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "AVAILABLE NET EARNINGS (94%)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ServexaTealLight
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$${"%.2f".format(available)}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (pending > 0) {
                            Text("Pending in 48h withdrawal: $${"%.2f".format(pending)}", style = MaterialTheme.typography.bodySmall, color = ServexaAmber)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showWithdrawDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ServexaTeal)
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Request Payout (48h Window)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Linked Payout Account (Bank / Card)
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
                                    text = if (payoutAccount != null) "Attached Provider Bank Details" else "Attach Payout Bank Account",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (payoutAccount != null) {
                                        val mask = if (payoutAccount!!.accountOrCardNumber.length >= 4) "****${payoutAccount!!.accountOrCardNumber.takeLast(4)}" else payoutAccount!!.accountOrCardNumber
                                        "${payoutAccount!!.bankOrIssuerName} ($mask) • ${payoutAccount!!.accountHolderName}"
                                    } else {
                                        "Must attach bank or card details before requesting withdrawal"
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

            // Commission & 48h Settlement Explanation Notice
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = ServexaIndigo)
                            Text("Platform Commission (6%) & 48h Settlements", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Text(
                            "When you complete a booking, Servexa deducts a 6% commission for platform escrow, insurance coverage, and customer support. 94% net amount is instantly in your wallet. Withdrawals to your attached account are processed within 48 hours.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Transactions Stream
            item {
                Text(
                    text = "Payouts & Earning History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (transactions.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No earnings yet",
                        subtitle = "Completed jobs and payouts will appear here.",
                        icon = Icons.Default.Payments
                    )
                }
            } else {
                items(transactions, key = { it.id }) { txn ->
                    TransactionItemRow(txn = txn)
                }
            }
        }
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
}
