package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.ServexaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCatId by viewModel.selectedCategoryId.collectAsState()
    val minRating by viewModel.filterMinRating.collectAsState()
    val maxPrice by viewModel.filterMaxPrice.collectAsState()
    val verifiedOnly by viewModel.filterVerifiedOnly.collectAsState()
    val sortBy by viewModel.filterSortBy.collectAsState()

    val categories by viewModel.categories.collectAsState()
    val results by viewModel.searchResults.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ServexaTopBar(
                title = "Search & Discover",
                subtitle = "${results.size} professionals found",
                showBack = true,
                onBackClick = { viewModel.navigateBack() },
                actions = {
                    IconButton(
                        onClick = { showFilterSheet = true },
                        modifier = Modifier.testTag("search_filter_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filters",
                            tint = if (minRating > 0 || maxPrice < 1000 || verifiedOnly) ServexaIndigo else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_input_field"),
                placeholder = { Text("Search electrician, AC, plumbing, wood...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            // Live Location Indicator
            val userLocationName by viewModel.userLocationName.collectAsState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = ServexaTeal,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Sorted by proximity to: $userLocationName",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Category Filter Pills
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCatId == null,
                        onClick = { viewModel.selectedCategoryId.value = null },
                        label = { Text("All Categories") }
                    )
                }
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCatId == cat.id,
                        onClick = {
                            viewModel.selectedCategoryId.value = if (selectedCatId == cat.id) null else cat.id
                        },
                        label = { Text(cat.name) }
                    )
                }
            }

            // Results List
            if (results.isEmpty()) {
                EmptyStateView(
                    title = "No professionals found",
                    subtitle = "Try adjusting your keywords or clearing active filters.",
                    icon = Icons.Default.SearchOff,
                    actionButtonText = "Reset Filters",
                    onActionClick = {
                        viewModel.searchQuery.value = ""
                        viewModel.selectedCategoryId.value = null
                        viewModel.filterMinRating.value = 0.0
                        viewModel.filterMaxPrice.value = 1000.0
                        viewModel.filterVerifiedOnly.value = false
                        viewModel.filterSortBy.value = "DISTANCE"
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(results, key = { it.user.id }) { item ->
                        ProviderCardItem(
                            item = item,
                            onViewProfile = {
                                viewModel.navigateTo(ScreenDestination.ProviderProfile(item.user.id))
                            },
                            onBookNow = {
                                viewModel.navigateTo(ScreenDestination.BookingWorkflow(item.user.id))
                            },
                            onCall = {
                                viewModel.initiateCall(
                                    bookingId = "SEARCH-CALL",
                                    receiverId = item.user.id,
                                    receiverName = item.user.name,
                                    receiverRole = "PROVIDER"
                                )
                            },
                            onChat = {
                                viewModel.navigateTo(ScreenDestination.Chat(item.user.id, item.user.name))
                            }
                        )
                    }
                }
            }
        }
    }

    // Filter Bottom Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Filter & Sort", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    TextButton(onClick = {
                        viewModel.filterMinRating.value = 0.0
                        viewModel.filterMaxPrice.value = 1000.0
                        viewModel.filterVerifiedOnly.value = false
                        viewModel.filterSortBy.value = "RELEVANCE"
                    }) {
                        Text("Reset All")
                    }
                }

                // Minimum Rating
                Column {
                    Text("Minimum Rating (${"%.1f".format(minRating)}★)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = minRating.toFloat(),
                        onValueChange = { viewModel.filterMinRating.value = it.toDouble() },
                        valueRange = 0f..5f,
                        steps = 4
                    )
                }

                // Max Price
                Column {
                    Text("Max Starting Price ($${maxPrice.toInt()})", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = maxPrice.toFloat(),
                        onValueChange = { viewModel.filterMaxPrice.value = it.toDouble() },
                        valueRange = 30f..500f
                    )
                }

                // Verified Only Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Verified Providers Only", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Show only background-checked pros", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = verifiedOnly,
                        onCheckedChange = { viewModel.filterVerifiedOnly.value = it }
                    )
                }

                // Sort By
                Column {
                    Text("Sort By", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("DISTANCE", "RATING", "PRICE", "COMPLETED").forEach { sort ->
                            FilterChip(
                                selected = sortBy == sort,
                                onClick = { viewModel.filterSortBy.value = sort },
                                label = { Text(if (sort == "DISTANCE") "Nearest" else sort.lowercase().replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                }

                Button(
                    onClick = { showFilterSheet = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply Filters (${results.size} Results)")
                }
            }
        }
    }
}
