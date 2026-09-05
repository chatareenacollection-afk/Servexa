package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.ui.components.ProfileOptionItem
import com.example.ui.components.ServexaTopBar
import com.example.ui.components.StatCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.ServexaViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminDashboardScreen(
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val users by viewModel.adminAllUsers.collectAsState()
    val bookings by viewModel.adminAllBookings.collectAsState()
    val transactions by viewModel.adminAllTransactions.collectAsState()
    val disputes by viewModel.adminAllDisputes.collectAsState()
    val calls by viewModel.adminAllCalls.collectAsState()
    val auditLogs by viewModel.adminAuditLogs.collectAsState()
    val kycDocs by viewModel.adminAllKycDocuments.collectAsState()

    val totalGMV = remember(bookings) {
        bookings.filter { it.status == "COMPLETED" }.sumOf { it.price }
    }
    val topUpRevenue = remember(transactions) {
        transactions.filter { it.type == "TOP_UP" }.sumOf { it.fee }
    }
    val commissionRevenue = remember(bookings) {
        bookings.filter { it.status == "COMPLETED" }.sumOf { it.platformCommission }
    }
    val totalPlatformRevenue = topUpRevenue + commissionRevenue

    val pendingVerifications = remember(users) {
        users.filter { it.role == "PROVIDER" && it.verificationStatus == "PENDING" }
    }
    val pendingKycDocs = remember(kycDocs) {
        kycDocs.filter { it.verificationStatus == "PENDING" }
    }
    val pendingWithdrawals = remember(transactions) {
        transactions.filter { it.type == "WITHDRAWAL" && it.status == "PENDING" }
    }
    val posTransactions = remember(transactions) {
        transactions.filter { it.paymentChannel == "POS_TERMINAL" }
    }
    val openDisputes = remember(disputes) {
        disputes.filter { it.status == "OPEN" }
    }

    Scaffold(
        topBar = {
            ServexaTopBar(
                title = "Servexa Admin Console",
                subtitle = "Logged in as ${currentUser?.name ?: "Mr-Pirate"} (Root Admin)",
                actions = {
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("admin_logout_button")
                    ) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "Log Out", tint = ServexaRose)
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
            // 1. Revenue & Platform GMV Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF0F172A), ServexaIndigoDark, Color(0xFF1E1B4B))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(
                                text = "TOTAL PLATFORM REVENUE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ServexaTealLight,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$${"%.2f".format(totalPlatformRevenue)}",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "5% Top-Up Fees: $${"%.2f".format(topUpRevenue)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondaryDark
                                )
                                Text(
                                    text = "6% Service Comm: $${"%.2f".format(commissionRevenue)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondaryDark
                                )
                            }
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
                        title = "KYC Documents",
                        value = "${kycDocs.size}",
                        subtitle = "${pendingKycDocs.size} Pending Review",
                        icon = Icons.Default.Badge,
                        color = ServexaIndigo,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "POS Transactions",
                        value = "${posTransactions.size}",
                        subtitle = "$${"%.0f".format(posTransactions.sumOf { it.netAmount })} Allotted",
                        icon = Icons.Default.PointOfSale,
                        color = ServexaTeal,
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
                        title = "Total Users",
                        value = "${users.size}",
                        subtitle = "${pendingVerifications.size} Pending Pros",
                        icon = Icons.Default.People,
                        color = ServexaIndigo,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Service Orders",
                        value = "${bookings.size}",
                        subtitle = "GMV $${"%.0f".format(totalGMV)}",
                        icon = Icons.Default.ReceiptLong,
                        color = ServexaAmber,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. Admin Management Modules
            item {
                Text(
                    text = "Control Panels",
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
                            icon = Icons.Default.Badge,
                            title = "User Directory & KYC Identity Docs (${pendingKycDocs.size} pending)",
                            subtitle = "Verify driving licenses, national IDs, passports & approve wallet access",
                            onClick = { viewModel.navigateTo(ScreenDestination.AdminUsers) }
                        )
                        Divider(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        ProfileOptionItem(
                            icon = Icons.Default.PointOfSale,
                            title = "POS Terminal Credit & Payment Audit Ledger",
                            subtitle = "Allot POS credits, inspect full payment receipts & merchant captures",
                            onClick = { viewModel.navigateTo(ScreenDestination.AdminFinance) }
                        )
                        Divider(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        ProfileOptionItem(
                            icon = Icons.Default.Payment,
                            title = "Payment Methods & Deposit Gateways",
                            subtitle = "Manage bank, card, crypto USDT, PayPal & cash channels",
                            onClick = { viewModel.navigateTo(ScreenDestination.AdminPaymentMethods) }
                        )
                        Divider(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        ProfileOptionItem(
                            icon = Icons.Default.Category,
                            title = "Dynamic Categories & Trades Taxonomy",
                            subtitle = "Add, edit, disable trade categories and sub-services",
                            onClick = { viewModel.navigateTo(ScreenDestination.AdminCategories) }
                        )
                        Divider(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        ProfileOptionItem(
                            icon = Icons.Default.Gavel,
                            title = "Dispute Resolution Center (${openDisputes.size} open)",
                            subtitle = "Arbitrate disputes and grant customer refunds / payouts",
                            onClick = { viewModel.navigateTo(ScreenDestination.AdminDisputes) }
                        )
                        Divider(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        ProfileOptionItem(
                            icon = Icons.Default.Settings,
                            title = "Platform Commission & Rules Configuration",
                            subtitle = "Manage 5% top-up fee & 6% commission parameters",
                            onClick = { viewModel.navigateTo(ScreenDestination.AdminSettings) }
                        )
                    }
                }
            }

            // 4. Audit Logs Stream
            item {
                Text(
                    text = "System Audit Logs (Immutable Trail)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(auditLogs.take(6), key = { it.id }) { log ->
                val dateStr = remember(log.timestamp) {
                    SimpleDateFormat("MMM dd, hh:mm:ss a", Locale.getDefault()).format(Date(log.timestamp))
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${log.actorName} (${log.action})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(log.metadata, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
