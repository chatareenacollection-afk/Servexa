package com.example.ui.screens.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ServexaTopBar
import com.example.ui.theme.ServexaGreen
import com.example.ui.theme.ServexaRose
import com.example.ui.viewmodel.ServexaViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CallHistoryScreen(
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier
) {
    val calls by viewModel.callLogs.collectAsState()

    Scaffold(
        topBar = {
            ServexaTopBar(
                title = "Secure Voice Call Logs",
                subtitle = "${calls.size} in-app calls recorded",
                showBack = true,
                onBackClick = { viewModel.navigateBack() }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (calls.isEmpty()) {
            EmptyStateView(
                title = "No call logs yet",
                subtitle = "When you make or receive in-app encrypted calls with service professionals, logs will appear here.",
                icon = Icons.Default.Call,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(calls, key = { it.id }) { call ->
                    val dateStr = remember(call.startTime) {
                        SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(call.startTime))
                    }
                    val mins = call.durationSeconds / 60
                    val secs = call.durationSeconds % 60
                    val durationText = "${mins}m ${secs}s"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (call.status == "ENDED") Icons.Default.Call else Icons.Default.CallMissed,
                                    contentDescription = null,
                                    tint = if (call.status == "ENDED") ServexaGreen else ServexaRose
                                )
                                Column {
                                    Text(
                                        text = "${call.callerName} ➔ ${call.receiverName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$dateStr • Duration: $durationText",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = call.status,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
