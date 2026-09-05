package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.ProductEntity
import com.example.ui.components.CartBottomSheet
import com.example.ui.components.RatingBadge
import com.example.ui.components.ServexaTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.ServexaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductMarketplaceScreen(
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val wallet by viewModel.customerWallet.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("All") }
    var showCartSheet by remember { mutableStateOf(false) }

    val filterTags = listOf("All", "Phones & Gadgets", "Vehicles & Bikes", "Computers & Tech", "Home & Furniture", "Tools & Hardware")

    val filteredProducts = remember(products, searchQuery, selectedTag) {
        products.filter { prod ->
            val matchesSearch = searchQuery.isBlank() ||
                    prod.name.contains(searchQuery, ignoreCase = true) ||
                    prod.description.contains(searchQuery, ignoreCase = true)

            val matchesTag = when (selectedTag) {
                "Phones & Gadgets" -> prod.name.contains("phone", true) || prod.name.contains("headphone", true) || prod.name.contains("gadget", true)
                "Vehicles & Bikes" -> prod.name.contains("bike", true) || prod.name.contains("yamaha", true) || prod.name.contains("car", true) || prod.name.contains("motor", true)
                "Computers & Tech" -> prod.name.contains("macbook", true) || prod.name.contains("laptop", true) || prod.name.contains("mesh", true) || prod.name.contains("router", true) || prod.name.contains("wi-fi", true)
                "Home & Furniture" -> prod.name.contains("chair", true) || prod.name.contains("table", true) || prod.name.contains("furniture", true) || prod.name.contains("pet", true)
                "Tools & Hardware" -> prod.name.contains("drill", true) || prod.name.contains("tool", true) || prod.name.contains("dewalt", true) || prod.name.contains("kit", true)
                else -> true
            }
            matchesSearch && matchesTag
        }
    }

    val totalCartCount = remember(cartItems) {
        cartItems.sumOf { it.first.quantity }
    }

    Scaffold(
        topBar = {
            ServexaTopBar(
                title = "Buy & Sell Marketplace",
                subtitle = "Direct buying, gadgets, bikes, vehicles & goods",
                showBack = true,
                onBackClick = { viewModel.navigateBack() },
                actions = {
                    BadgedBox(
                        badge = {
                            if (totalCartCount > 0) {
                                Badge { Text("$totalCartCount") }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = { showCartSheet = true },
                            modifier = Modifier.testTag("open_cart_button")
                        ) {
                            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Cart")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (totalCartCount > 0) {
                ExtendedFloatingActionButton(
                    onClick = { showCartSheet = true },
                    containerColor = ServexaIndigo,
                    contentColor = Color.White
                ) {
                    Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Cart ($totalCartCount)")
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search bikes, phones, gadgets, tech...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Category Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterTags.take(3).forEach { tag ->
                    FilterChip(
                        selected = selectedTag == tag,
                        onClick = { selectedTag = tag },
                        label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No listings found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Try adjusting your search terms or filter tags.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductGridItem(
                            product = product,
                            onAddToCart = { viewModel.addToCart(product.id) }
                        )
                    }
                }
            }
        }
    }

    if (showCartSheet) {
        CartBottomSheet(
            cartItems = cartItems,
            wallet = wallet,
            viewModel = viewModel,
            onDismiss = { showCartSheet = false },
            onQuantityChange = { id, qty -> viewModel.updateCartQuantity(id, qty) },
            onRemoveItem = { id -> viewModel.removeFromCart(id) },
            onCheckout = { address ->
                viewModel.checkoutCart(address) { success ->
                    if (success) showCartSheet = false
                }
            }
        )
    }
}

@Composable
fun ProductGridItem(
    product: ProductEntity,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = remember(product.name) {
        val lower = product.name.lowercase()
        when {
            lower.contains("phone") || lower.contains("iphone") -> Icons.Default.Smartphone
            lower.contains("macbook") || lower.contains("laptop") -> Icons.Default.Laptop
            lower.contains("bike") || lower.contains("yamaha") || lower.contains("motor") -> Icons.Default.TwoWheeler
            lower.contains("headphone") || lower.contains("sony") -> Icons.Default.Headphones
            lower.contains("chair") || lower.contains("office") -> Icons.Default.Chair
            lower.contains("router") || lower.contains("wi-fi") || lower.contains("mesh") -> Icons.Default.Router
            lower.contains("drill") || lower.contains("dewalt") -> Icons.Default.Build
            lower.contains("pet") || lower.contains("dog") || lower.contains("cat") -> Icons.Default.Pets
            else -> Icons.Default.Storefront
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            RatingBadge(rating = product.rating, reviewCount = product.reviewCount)

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = product.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = product.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$${"%.2f".format(product.price)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ServexaIndigo
                )

                IconButton(
                    onClick = onAddToCart,
                    modifier = Modifier
                        .background(ServexaTeal, CircleShape)
                        .size(36.dp)
                        .testTag("add_product_to_cart_${product.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = "Add to Cart",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
