package com.example.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.DisputeEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ServexaTopBar
import com.example.ui.theme.ServexaGreen
import com.example.ui.theme.ServexaRose
import com.example.ui.viewmodel.ServexaViewModel

@Composable
fun AdminDisputesScreen(
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier
) {
    val disputes by viewModel.adminAllDisputes.collectAsState()

    Scaffold(
        topBar = {
            ServexaTopBar(
                title = "Dispute Resolution Center",
                subtitle = "${disputes.size} total reported cases",
                showBack = true,
                onBackClick = { viewModel.navigateBack() }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (disputes.isEmpty()) {
            EmptyStateView(
                title = "No Disputes Reported",
                subtitle = "All bookings are smoothly running without active escalations.",
                icon = Icons.Default.Gavel,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(disputes, key = { it.id }) { dispute ->
                    AdminDisputeCard(
                        dispute = dispute,
                        onResolve = { resolution ->
                            viewModel.adminResolveDispute(dispute.id, resolution, "Admin arbitrated in favor of $resolution")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminDisputeCard(
    dispute: DisputeEntity,
    onResolve: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Case #${dispute.id}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (dispute.status == "OPEN") ServexaRose.copy(alpha = 0.15f) else ServexaGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = dispute.status,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (dispute.status == "OPEN") ServexaRose else ServexaGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Reason: ${dispute.reason}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(dispute.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (dispute.adminNotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("Resolution: ${dispute.resolution} (${dispute.adminNotes})", style = MaterialTheme.typography.labelMedium, color = ServexaGreen)
            }

            if (dispute.status == "OPEN") {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onResolve("REFUND_CUSTOMER") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ServexaRose)
                    ) {
                        Text("Refund Customer", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = { onResolve("RELEASE_PROVIDER") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ServexaGreen)
                    ) {
                        Text("Release to Pro", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
