package com.example.ui.screens.provider

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ServexaTopBar
import com.example.ui.viewmodel.ServexaViewModel

@Composable
fun ProviderRequestsScreen(
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier
) {
    val bookings by viewModel.providerBookings.collectAsState()
    val pendingRequests = remember(bookings) {
        bookings.filter { it.status == "REQUESTED" }
    }

    Scaffold(
        topBar = {
            ServexaTopBar(
                title = "Booking Requests",
                subtitle = "${pendingRequests.size} pending customer requests",
                showBack = true,
                onBackClick = { viewModel.navigateBack() }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (pendingRequests.isEmpty()) {
            EmptyStateView(
                title = "No Pending Requests",
                subtitle = "When customers book your services, their job requests will appear here for your confirmation.",
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
                items(pendingRequests, key = { it.id }) { req ->
                    ProviderRequestCard(
                        booking = req,
                        onAccept = { viewModel.updateBookingStatus(req.id, "ACCEPTED") },
                        onReject = { viewModel.updateBookingStatus(req.id, "REJECTED", "Provider schedule full") }
                    )
                }
            }
        }
    }
}
