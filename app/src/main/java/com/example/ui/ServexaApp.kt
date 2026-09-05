package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.ui.components.CallingOverlay
import com.example.ui.screens.admin.*
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.customer.*
import com.example.ui.screens.provider.*
import com.example.ui.screens.web.*
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.ServexaViewModel

data class NavigationItem(
    val label: String,
    val icon: ImageVector,
    val destination: ScreenDestination
)

@Composable
fun ServexaApp(
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeCall by viewModel.activeCall.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val canNavigateBack by viewModel.canNavigateBack.collectAsState()

    // Intercept back button to navigate back in session history instead of quitting the application
    BackHandler(enabled = canNavigateBack) {
        viewModel.navigateBack()
    }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (currentScreen !is ScreenDestination.ActiveBookingTracking && currentScreen !is ScreenDestination.Auth && currentScreen !is ScreenDestination.WebStorefrontViewer) {
                if (currentUser == null || currentUser?.role == "CUSTOMER") {
                    CustomerNavigationBar(
                        currentScreen = currentScreen,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                } else if (currentUser?.role == "PROVIDER") {
                    ProviderNavigationBar(
                        currentScreen = currentScreen,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                } else if (currentUser?.role == "ADMIN") {
                    AdminNavigationBar(
                        currentScreen = currentScreen,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    // Customer / Guest Marketplace Screens
                    is ScreenDestination.Home -> HomeScreen(viewModel = viewModel)
                    is ScreenDestination.Search -> SearchScreen(viewModel = viewModel)
                    is ScreenDestination.Categories -> CategoriesScreen(viewModel = viewModel)
                    is ScreenDestination.ProviderProfile -> ProviderProfileScreen(
                        providerId = screen.providerId,
                        viewModel = viewModel
                    )
                    is ScreenDestination.BookingWorkflow -> BookingWorkflowScreen(
                        providerId = screen.providerId,
                        initialServiceId = screen.serviceId,
                        viewModel = viewModel
                    )
                    is ScreenDestination.CustomerBookings -> CustomerBookingsScreen(viewModel = viewModel)
                    is ScreenDestination.ActiveBookingTracking -> ActiveBookingTrackingScreen(
                        bookingId = screen.bookingId,
                        viewModel = viewModel
                    )
                    is ScreenDestination.CustomerWallet -> CustomerWalletScreen(viewModel = viewModel)
                    is ScreenDestination.ProductMarketplace -> ProductMarketplaceScreen(viewModel = viewModel)
                    is ScreenDestination.WorkVideosFeed -> WorkVideosFeedScreen(viewModel = viewModel)
                    is ScreenDestination.CustomerProfile -> CustomerProfileScreen(viewModel = viewModel)
                    is ScreenDestination.CallHistory -> CallHistoryScreen(viewModel = viewModel)
                    is ScreenDestination.Notifications -> CustomerProfileScreen(viewModel = viewModel)
                    is ScreenDestination.Chat -> ChatScreen(
                        recipientId = screen.recipientId,
                        recipientName = screen.recipientName,
                        bookingId = screen.bookingId,
                        viewModel = viewModel
                    )

                    // Provider Screens
                    is ScreenDestination.ProviderDashboard -> ProviderDashboardScreen(viewModel = viewModel)
                    is ScreenDestination.ProviderRequests -> ProviderRequestsScreen(viewModel = viewModel)
                    is ScreenDestination.ProviderActiveJobs -> ProviderActiveJobsScreen(viewModel = viewModel)
                    is ScreenDestination.ProviderServices -> ProviderServicesScreen(viewModel = viewModel)
                    is ScreenDestination.ProviderPortfolio -> ProviderPortfolioScreen(viewModel = viewModel)
                    is ScreenDestination.ProviderEarnings -> ProviderEarningsScreen(viewModel = viewModel)
                    is ScreenDestination.ProviderCallLogs -> CallHistoryScreen(viewModel = viewModel)
                    is ScreenDestination.ProviderReviews -> ProviderProfileScreen(
                        providerId = currentUser?.id ?: "prov_marcus_1",
                        viewModel = viewModel
                    )
                    is ScreenDestination.ProviderProfileEdit -> ProviderDashboardScreen(viewModel = viewModel)
                    is ScreenDestination.ProviderStoreSubdomain -> ProviderStoreSubdomainScreen(viewModel = viewModel)
                    is ScreenDestination.WebStorefrontViewer -> ProviderWebStorefrontScreen(
                        subdomain = screen.subdomain,
                        viewModel = viewModel
                    )
                    is ScreenDestination.WebPortalMode -> WebPortalDirectoryScreen(viewModel = viewModel)

                    // Admin Screens
                    is ScreenDestination.AdminDashboard -> AdminDashboardScreen(viewModel = viewModel)
                    is ScreenDestination.AdminUsers -> AdminUsersScreen(viewModel = viewModel)
                    is ScreenDestination.AdminFinance -> AdminFinanceScreen(viewModel = viewModel)
                    is ScreenDestination.AdminPaymentMethods -> AdminPaymentMethodsScreen(viewModel = viewModel)
                    is ScreenDestination.AdminCategories -> AdminCategoriesScreen(viewModel = viewModel)
                    is ScreenDestination.AdminDisputes -> AdminDisputesScreen(viewModel = viewModel)
                    is ScreenDestination.AdminCalls -> CallHistoryScreen(viewModel = viewModel)
                    is ScreenDestination.AdminSettings -> AdminSettingsScreen(viewModel = viewModel)

                    // Auth Screen
                    is ScreenDestination.Auth -> AuthScreen(viewModel = viewModel)
                }
            }

            // Global Overlay for Active Secure Voice Calls
            val callSeconds by viewModel.callSeconds.collectAsState()
            if (activeCall != null) {
                CallingOverlay(
                    activeCall = activeCall,
                    callSeconds = callSeconds,
                    onAccept = { viewModel.acceptCall() },
                    onEnd = { viewModel.endCall() }
                )
            }
        }
    }
}

@Composable
fun CustomerNavigationBar(
    currentScreen: ScreenDestination,
    onNavigate: (ScreenDestination) -> Unit
) {
    val items = listOf(
        NavigationItem("Explore", Icons.Default.Home, ScreenDestination.Home),
        NavigationItem("Search", Icons.Default.Search, ScreenDestination.Search),
        NavigationItem("Bookings", Icons.Default.CalendarMonth, ScreenDestination.CustomerBookings),
        NavigationItem("Wallet", Icons.Default.AccountBalanceWallet, ScreenDestination.CustomerWallet),
        NavigationItem("Account", Icons.Default.Person, ScreenDestination.CustomerProfile)
    )

    NavigationBar(modifier = Modifier.testTag("customer_bottom_bar")) {
        items.forEach { item ->
            val isSelected = when (item.destination) {
                is ScreenDestination.Home -> currentScreen is ScreenDestination.Home
                is ScreenDestination.Search -> currentScreen is ScreenDestination.Search || currentScreen is ScreenDestination.Categories
                is ScreenDestination.CustomerBookings -> currentScreen is ScreenDestination.CustomerBookings
                is ScreenDestination.CustomerWallet -> currentScreen is ScreenDestination.CustomerWallet
                is ScreenDestination.CustomerProfile -> currentScreen is ScreenDestination.CustomerProfile
                else -> false
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.destination) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

@Composable
fun ProviderNavigationBar(
    currentScreen: ScreenDestination,
    onNavigate: (ScreenDestination) -> Unit
) {
    val items = listOf(
        NavigationItem("Console", Icons.Default.Dashboard, ScreenDestination.ProviderDashboard),
        NavigationItem("Requests", Icons.Default.PendingActions, ScreenDestination.ProviderRequests),
        NavigationItem("Active Jobs", Icons.Default.Engineering, ScreenDestination.ProviderActiveJobs),
        NavigationItem("Services", Icons.Default.Build, ScreenDestination.ProviderServices),
        NavigationItem("Earnings", Icons.Default.Payments, ScreenDestination.ProviderEarnings)
    )

    NavigationBar(modifier = Modifier.testTag("provider_bottom_bar")) {
        items.forEach { item ->
            val isSelected = when (item.destination) {
                is ScreenDestination.ProviderDashboard -> currentScreen is ScreenDestination.ProviderDashboard
                is ScreenDestination.ProviderRequests -> currentScreen is ScreenDestination.ProviderRequests
                is ScreenDestination.ProviderActiveJobs -> currentScreen is ScreenDestination.ProviderActiveJobs
                is ScreenDestination.ProviderServices -> currentScreen is ScreenDestination.ProviderServices
                is ScreenDestination.ProviderEarnings -> currentScreen is ScreenDestination.ProviderEarnings
                else -> false
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.destination) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

@Composable
fun AdminNavigationBar(
    currentScreen: ScreenDestination,
    onNavigate: (ScreenDestination) -> Unit
) {
    val items = listOf(
        NavigationItem("Overview", Icons.Default.SpaceDashboard, ScreenDestination.AdminDashboard),
        NavigationItem("Users", Icons.Default.People, ScreenDestination.AdminUsers),
        NavigationItem("Finance", Icons.Default.AccountBalance, ScreenDestination.AdminFinance),
        NavigationItem("Disputes", Icons.Default.Gavel, ScreenDestination.AdminDisputes),
        NavigationItem("Settings", Icons.Default.Settings, ScreenDestination.AdminSettings)
    )

    NavigationBar(modifier = Modifier.testTag("admin_bottom_bar")) {
        items.forEach { item ->
            val isSelected = when (item.destination) {
                is ScreenDestination.AdminDashboard -> currentScreen is ScreenDestination.AdminDashboard
                is ScreenDestination.AdminUsers -> currentScreen is ScreenDestination.AdminUsers
                is ScreenDestination.AdminFinance -> currentScreen is ScreenDestination.AdminFinance
                is ScreenDestination.AdminDisputes -> currentScreen is ScreenDestination.AdminDisputes
                is ScreenDestination.AdminSettings -> currentScreen is ScreenDestination.AdminSettings
                else -> false
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.destination) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
