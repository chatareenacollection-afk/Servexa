package com.example.ui.screens.provider

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ServexaTopBar
import com.example.ui.theme.ServexaAmber
import com.example.ui.theme.ServexaIndigo
import com.example.ui.theme.ServexaTeal
import com.example.ui.viewmodel.ServexaViewModel

@Composable
fun ProviderServicesScreen(
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allProviders by viewModel.searchResults.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val currentProviderItem = remember(allProviders, currentUser) {
        allProviders.find { it.user.id == currentUser?.id }
    }

    val providerProducts by viewModel.getProductsForSeller(currentUser?.id ?: "").collectAsState(initial = emptyList())

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Services Packages", "Products & Hardware")

    // Service Dialog State
    var showAddServiceDialog by remember { mutableStateOf(false) }
    var newServiceTitle by remember { mutableStateOf("") }
    var newServiceDesc by remember { mutableStateOf("") }
    var newServicePrice by remember { mutableStateOf("95") }
    var newServiceDuration by remember { mutableStateOf("60") }
    var selectedCatId by remember { mutableStateOf(categories.firstOrNull()?.id ?: "cat_elec") }

    // Product Dialog State
    var showAddProductDialog by remember { mutableStateOf(false) }
    var newProductName by remember { mutableStateOf("") }
    var newProductDesc by remember { mutableStateOf("") }
    var newProductPrice by remember { mutableStateOf("45.00") }
    var newProductStock by remember { mutableStateOf("15") }
    var selectedProductCatId by remember { mutableStateOf(categories.firstOrNull()?.id ?: "cat_elec") }

    Scaffold(
        topBar = {
            ServexaTopBar(
                title = "Catalog & Inventory",
                subtitle = "Manage your service offerings & retail items",
                showBack = true,
                onBackClick = { viewModel.navigateBack() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) showAddServiceDialog = true else showAddProductDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("add_catalog_item_fab")
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
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            if (selectedTab == 0) {
                // Services List
                val services = currentProviderItem?.services ?: emptyList()

                if (services.isEmpty()) {
                    EmptyStateView(
                        title = "No services listed",
                        subtitle = "Add service packages so customers can book you instantly.",
                        icon = Icons.Default.Build,
                        actionButtonText = "+ Add First Service",
                        onActionClick = { showAddServiceDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(services, key = { it.id }) { srv ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(srv.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                        Text("$${"%.2f".format(srv.price)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(srv.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Estimated Duration: ${srv.durationMinutes} minutes", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            } else {
                // Products List
                if (providerProducts.isEmpty()) {
                    EmptyStateView(
                        title = "No products listed",
                        subtitle = "Add spare parts, filters, or hardware supplies for customers to buy.",
                        icon = Icons.Default.ShoppingBag,
                        actionButtonText = "+ Add Retail Product",
                        onActionClick = { showAddProductDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(providerProducts, key = { it.id }) { prod ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(prod.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = ServexaTeal.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "Stock: ${prod.inventory} units in stock",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = ServexaTeal,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Text("$${"%.2f".format(prod.price)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(prod.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("⭐ ${prod.rating} (${prod.reviewCount} customer reviews)", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Service Dialog
    if (showAddServiceDialog) {
        AlertDialog(
            onDismissRequest = { showAddServiceDialog = false },
            title = { Text("Add Service Package", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newServiceTitle,
                        onValueChange = { newServiceTitle = it },
                        label = { Text("Service Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newServiceDesc,
                        onValueChange = { newServiceDesc = it },
                        label = { Text("Service Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newServicePrice,
                            onValueChange = { newServicePrice = it },
                            label = { Text("Price ($)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newServiceDuration,
                            onValueChange = { newServiceDuration = it },
                            label = { Text("Duration (min)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val price = newServicePrice.toDoubleOrNull() ?: 0.0
                        val dur = newServiceDuration.toIntOrNull() ?: 60
                        if (newServiceTitle.isNotBlank() && price > 0) {
                            viewModel.addProviderService(selectedCatId, newServiceTitle, newServiceDesc, price, dur) {
                                showAddServiceDialog = false
                                newServiceTitle = ""
                                newServiceDesc = ""
                            }
                        }
                    }
                ) {
                    Text("Save Service")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddServiceDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add Product Dialog
    if (showAddProductDialog) {
        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            title = { Text("Add Hardware / Retail Product", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newProductName,
                        onValueChange = { newProductName = it },
                        label = { Text("Product Name") },
                        placeholder = { Text("e.g. Copper Pipe Fitting 3/4\"") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newProductDesc,
                        onValueChange = { newProductDesc = it },
                        label = { Text("Product Description") },
                        placeholder = { Text("Specifications, brand, model") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newProductPrice,
                            onValueChange = { newProductPrice = it },
                            label = { Text("Price ($)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newProductStock,
                            onValueChange = { newProductStock = it },
                            label = { Text("Stock Qty") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val price = newProductPrice.toDoubleOrNull() ?: 0.0
                        val stock = newProductStock.toIntOrNull() ?: 10
                        if (newProductName.isNotBlank() && price > 0) {
                            viewModel.addProviderProduct(selectedProductCatId, newProductName, newProductDesc, price, stock) {
                                showAddProductDialog = false
                                newProductName = ""
                                newProductDesc = ""
                            }
                        }
                    }
                ) {
                    Text("Save Product")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductDialog = false }) { Text("Cancel") }
            }
        )
    }
}
