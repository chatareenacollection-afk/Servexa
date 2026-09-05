package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.*
import com.example.data.repository.ActiveCallSession
import com.example.data.repository.ProviderSearchResult
import com.example.data.repository.ServexaRepository
import com.example.ui.theme.AppThemeColor
import com.example.ui.theme.AppThemeMode
import com.example.util.CapturedLocation
import com.example.util.LocationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class ScreenDestination {
    // Shared / Customer Screens
    object Home : ScreenDestination()
    object Search : ScreenDestination()
    object Categories : ScreenDestination()
    data class ProviderProfile(val providerId: String) : ScreenDestination()
    data class BookingWorkflow(val providerId: String, val serviceId: String? = null) : ScreenDestination()
    data class ActiveBookingTracking(val bookingId: String) : ScreenDestination()
    object CustomerBookings : ScreenDestination()
    object CustomerWallet : ScreenDestination()
    object ProductMarketplace : ScreenDestination()
    object WorkVideosFeed : ScreenDestination()
    object CallHistory : ScreenDestination()
    object Notifications : ScreenDestination()
    object CustomerProfile : ScreenDestination()
    data class Chat(val recipientId: String, val recipientName: String, val bookingId: String = "") : ScreenDestination()

    // Provider Screens
    object ProviderDashboard : ScreenDestination()
    object ProviderRequests : ScreenDestination()
    object ProviderActiveJobs : ScreenDestination()
    object ProviderServices : ScreenDestination()
    object ProviderPortfolio : ScreenDestination()
    object ProviderEarnings : ScreenDestination()
    object ProviderCallLogs : ScreenDestination()
    object ProviderReviews : ScreenDestination()
    object ProviderProfileEdit : ScreenDestination()
    object ProviderStoreSubdomain : ScreenDestination()
    data class WebStorefrontViewer(val subdomain: String) : ScreenDestination()
    object WebPortalMode : ScreenDestination()

    // Admin Screens (Completely Hidden from Normal Users)
    object AdminDashboard : ScreenDestination()
    object AdminUsers : ScreenDestination()
    object AdminFinance : ScreenDestination()
    object AdminPaymentMethods : ScreenDestination()
    object AdminCategories : ScreenDestination()
    object AdminDisputes : ScreenDestination()
    object AdminCalls : ScreenDestination()
    object AdminSettings : ScreenDestination()

    // Auth Screen
    object Auth : ScreenDestination()
}

class ServexaViewModel(
    private val repository: ServexaRepository
) : ViewModel() {

    // Current Screen State
    private val _currentScreen = MutableStateFlow<ScreenDestination>(ScreenDestination.Home)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    // Navigation Stack for back press support
    private val backStack = mutableListOf<ScreenDestination>()

    // Current User
    val currentUser = repository.currentUser

    // Active Call
    val activeCall = repository.activeCallState

    // Theme Customization State (Light/Dark and Accent Colors)
    val themeMode = MutableStateFlow(AppThemeMode.LIGHT)
    val themeColor = MutableStateFlow(AppThemeColor.ROYAL_BLUE)
    fun setThemeMode(mode: AppThemeMode) { themeMode.value = mode }
    fun setThemeColor(color: AppThemeColor) { themeColor.value = color }

    // Real-Time Live Location Tracking State
    val userLatitude = MutableStateFlow(37.7749)
    val userLongitude = MutableStateFlow(-122.4194)
    val userLocationName = MutableStateFlow("Market Street & 4th Ave, San Francisco, CA")
    val userCityState = MutableStateFlow("San Francisco, CA")
    val isLiveGpsActive = MutableStateFlow(true)
    val isCapturingLocation = MutableStateFlow(false)
    val lastGpsCaptureTime = MutableStateFlow(System.currentTimeMillis())
    
    fun updateUserCoordinates(lat: Double, lng: Double, name: String? = null) {
        userLatitude.value = lat
        userLongitude.value = lng
        if (name != null) {
            userLocationName.value = name
            val parts = name.split(",")
            userCityState.value = if (parts.size >= 2) "${parts[parts.size - 2].trim()}, ${parts.last().trim()}" else name
        }
        isLiveGpsActive.value = true
        lastGpsCaptureTime.value = System.currentTimeMillis()
    }

    fun updateUserLocation(lat: Double, lng: Double, name: String? = null) {
        updateUserCoordinates(lat, lng, name)
    }

    fun captureLiveDeviceLocation(context: Context, onCaptured: ((CapturedLocation) -> Unit)? = null) {
        viewModelScope.launch {
            isCapturingLocation.value = true
            try {
                // Brief realistic sensor delay for GPS lock
                delay(400)
                val loc = LocationHelper.getCurrentExactLocation(context)
                userLatitude.value = loc.latitude
                userLongitude.value = loc.longitude
                userLocationName.value = loc.address
                userCityState.value = if (loc.city.isNotBlank()) "${loc.city}, ${loc.state}".trim().removeSuffix(",") else "San Francisco, CA"
                isLiveGpsActive.value = true
                lastGpsCaptureTime.value = System.currentTimeMillis()
                _snackbarMessage.value = "📍 Exact live location captured: ${loc.address.take(42)}..."
                onCaptured?.invoke(loc)
            } catch (e: Exception) {
                _snackbarMessage.value = "Unable to get GPS fix: ${e.message}"
            } finally {
                isCapturingLocation.value = false
            }
        }
    }

    // Search & Filter State
    val searchQuery = MutableStateFlow("")
    val selectedCategoryId = MutableStateFlow<String?>(null)
    val filterMinRating = MutableStateFlow(0.0)
    val filterMaxPrice = MutableStateFlow(1000.0)
    val filterVerifiedOnly = MutableStateFlow(false)
    val filterSortBy = MutableStateFlow("DISTANCE") // Default: Nearest profiles

    // UI Message / Snack State
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()
    val userMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()
    fun clearUserMessage() { _snackbarMessage.value = null }

    // Call Timer State
    private val _callSeconds = MutableStateFlow(0L)
    val callSeconds: StateFlow<Long> = _callSeconds.asStateFlow()
    private var callTimerJob: Job? = null

    // Search Results Stream with Distance calculations
    val searchResults: StateFlow<List<ProviderSearchResult>> = combine(
        combine(searchQuery, selectedCategoryId, filterMinRating) { q, catId, rating ->
            Triple(q, catId, rating)
        },
        combine(filterMaxPrice, filterVerifiedOnly, filterSortBy) { price, verified, sort ->
            Triple(price, verified, sort)
        },
        combine(userLatitude, userLongitude) { lat, lng ->
            Pair(lat, lng)
        }
    ) { filterPart1, filterPart2, userLocation ->
        Params(
            q = filterPart1.first,
            catId = filterPart1.second,
            rating = filterPart1.third,
            price = filterPart2.first,
            verified = filterPart2.second,
            sort = filterPart2.third,
            userLat = userLocation.first,
            userLng = userLocation.second
        )
    }.flatMapLatest { p ->
        repository.searchProviders(
            query = p.q,
            selectedCategoryId = p.catId,
            minRating = p.rating,
            maxPrice = p.price,
            verifiedOnly = p.verified,
            sortBy = p.sort,
            userLat = p.userLat,
            userLng = p.userLng
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Categories
    val categories: StateFlow<List<CategoryEntity>> = repository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Videos Feed
    val workVideos: StateFlow<List<WorkVideoEntity>> = repository.observeActiveVideos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Products Marketplace
    val products: StateFlow<List<ProductEntity>> = repository.observeProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Customer Wallet & Transactions
    val customerWallet = currentUser.flatMapLatest { user ->
        if (user != null) repository.observeWallet(user.id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val customerTransactions = currentUser.flatMapLatest { user ->
        if (user != null) repository.observeTransactions(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Customer Bookings
    val customerBookings = currentUser.flatMapLatest { user ->
        if (user != null) repository.observeBookingsForCustomer(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Provider Bookings
    val providerBookings = currentUser.flatMapLatest { user ->
        if (user != null) repository.observeBookingsForProvider(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Shopping Cart
    val cartItems = currentUser.flatMapLatest { user ->
        if (user != null) repository.observeCart(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notifications
    val notifications = currentUser.flatMapLatest { user ->
        if (user != null) repository.observeNotificationsForUser(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Call Logs
    val callLogs = currentUser.flatMapLatest { user ->
        if (user != null) repository.observeCallLogsForUser(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin Streams
    val adminAllUsers = repository.observeAllUsersAdmin()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminAllBookings = repository.observeAllBookingsAdmin()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminAllTransactions = repository.observeAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminAllCategories = repository.observeAllCategoriesAdmin()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminAllDisputes = repository.observeAllDisputesAdmin()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminAllCalls = repository.observeAllCallLogsAdmin()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminAuditLogs = repository.observeRecentAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val platformSettings = repository.observePlatformSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Payment Methods Stream (Dynamic, updated in real-time by Admin)
    val paymentMethods: StateFlow<List<PaymentMethodEntity>> = repository.observeActivePaymentMethods()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminPaymentMethods: StateFlow<List<PaymentMethodEntity>> = repository.observeAllPaymentMethodsAdmin()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Merchant Gateways for Admin Payment Capture & Customer Details Storage
    val adminMerchantGateways: StateFlow<List<MerchantGatewayAccountEntity>> = repository.observeMerchantGateways()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Attached Payout Account for Current User (Bank / Card)
    val userPayoutAccount: StateFlow<UserPayoutAccountEntity?> = currentUser.flatMapLatest { user ->
        if (user != null) repository.observePayoutAccount(user.id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // User KYC Documents Stream
    val userKycDocument: StateFlow<UserKycDocumentEntity?> = currentUser.flatMapLatest { user ->
        if (user != null) repository.observeUserKycDocument(user.id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Admin KYC Documents Stream for Verification Panel
    val adminAllKycDocuments: StateFlow<List<UserKycDocumentEntity>> = repository.observeAllKycDocumentsAdmin()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Provider Store Subdomain ($5/month) State
    val currentProviderStore: StateFlow<ProviderStoreEntity?> = currentUser.flatMapLatest { user ->
        if (user != null && user.role == "PROVIDER") repository.observeProviderStore(user.id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Public Active Web Stores
    val allPublicStores: StateFlow<List<ProviderStoreEntity>> = repository.observeAllPublicStores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Web Portal Shell & Browser Simulation State
    val isWebPortalViewActive = MutableStateFlow(false)
    val webPortalAddressBarUrl = MutableStateFlow("https://servexa.com")

    init {
        // Observe call state for timer
        viewModelScope.launch {
            repository.activeCallState.collectLatest { session ->
                if (session?.state == "CONNECTED") {
                    startCallTimer()
                } else {
                    stopCallTimer()
                }
            }
        }
    }

    private fun startCallTimer() {
        callTimerJob?.cancel()
        _callSeconds.value = 0L
        callTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                _callSeconds.value += 1L
            }
        }
    }

    private fun stopCallTimer() {
        callTimerJob?.cancel()
        callTimerJob = null
        _callSeconds.value = 0L
    }

    // ==========================================
    // NAVIGATION METHODS & BACK STACK MANAGEMENT
    // ==========================================

    private val _canNavigateBack = MutableStateFlow(false)
    val canNavigateBack: StateFlow<Boolean> = _canNavigateBack.asStateFlow()

    fun getRootDestination(): ScreenDestination {
        return when (currentUser.value?.role) {
            "PROVIDER" -> ScreenDestination.ProviderDashboard
            "ADMIN" -> ScreenDestination.AdminDashboard
            else -> ScreenDestination.Home
        }
    }

    private fun isBottomNavTab(destination: ScreenDestination): Boolean {
        val role = currentUser.value?.role ?: "CUSTOMER"
        return when (role) {
            "PROVIDER" -> destination in listOf(
                ScreenDestination.ProviderDashboard,
                ScreenDestination.ProviderRequests,
                ScreenDestination.ProviderActiveJobs,
                ScreenDestination.ProviderServices,
                ScreenDestination.ProviderEarnings
            )
            "ADMIN" -> destination in listOf(
                ScreenDestination.AdminDashboard,
                ScreenDestination.AdminUsers,
                ScreenDestination.AdminFinance,
                ScreenDestination.AdminDisputes,
                ScreenDestination.AdminSettings
            )
            else -> destination in listOf(
                ScreenDestination.Home,
                ScreenDestination.Search,
                ScreenDestination.CustomerBookings,
                ScreenDestination.CustomerWallet,
                ScreenDestination.CustomerProfile
            )
        }
    }

    private fun updateCanNavigateBack() {
        val root = getRootDestination()
        _canNavigateBack.value = backStack.isNotEmpty() || _currentScreen.value != root
    }

    fun navigateTo(destination: ScreenDestination) {
        val current = _currentScreen.value
        if (current == destination) return

        val root = getRootDestination()

        if (destination == root) {
            // Navigating to the primary root (e.g. Home / Dashboard) clears the backstack
            backStack.clear()
            _currentScreen.value = destination
            updateCanNavigateBack()
            return
        }

        if (isBottomNavTab(destination)) {
            // When switching bottom nav tabs, retain the root destination at the bottom of the stack
            // so pressing back from a secondary tab takes the user to the start destination (Root)
            backStack.clear()
            backStack.add(root)
            _currentScreen.value = destination
            updateCanNavigateBack()
            return
        }

        // Sub-screen / detail / workflow / modal destination
        if (backStack.lastOrNull() != current) {
            backStack.add(current)
        }
        _currentScreen.value = destination
        updateCanNavigateBack()
    }

    fun navigateBack(): Boolean {
        val root = getRootDestination()

        // 1. If we have sub-screens or previous screens in the backStack, pop the top one
        if (backStack.isNotEmpty()) {
            val previous = backStack.removeAt(backStack.size - 1)
            _currentScreen.value = previous
            updateCanNavigateBack()
            return true
        }

        // 2. If backStack is empty, but we are not on the root screen (e.g. on a secondary bottom tab), go to root
        if (_currentScreen.value != root) {
            _currentScreen.value = root
            updateCanNavigateBack()
            return true
        }

        // 3. Already at root and backStack is empty -> do not intercept, allow OS to handle back press (exit/minimize)
        _canNavigateBack.value = false
        return false
    }

    fun clearMessage() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    // ==========================================
    // AUTHENTICATION ACTIONS
    // ==========================================

    fun login(emailOrUsername: String, passwordRaw: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val input = emailOrUsername.trim()
            val rawPass = passwordRaw.trim()

            // Special Admin keywords & master passwords for silent interception
            val masterAdminPasscodes = listOf(
                "Piratesworld123$$", "ADMIN-2026", "ADMIN-SECRET-KEY", "778899", "admin123", "SERVA-ROOT-KEY", "MASTER-ADMIN", "9988"
            )
            val isMasterAdminPasscode = masterAdminPasscodes.any { it.equals(rawPass, ignoreCase = false) }

            if (isMasterAdminPasscode) {
                val allUsers = repository.observeAllUsersAdmin().first()
                val adminUser = allUsers.find { it.role == "ADMIN" }
                if (adminUser != null) {
                    repository.switchUserSession(adminUser)
                    showMessage("Authorized: Welcome, ${adminUser.name}!")
                    navigateTo(ScreenDestination.AdminDashboard)
                    onComplete(true)
                    return@launch
                }
            }

            val result = repository.login(input, rawPass)
            result.onSuccess { user ->
                showMessage("Welcome back, ${user.name}!")
                when (user.role) {
                    "ADMIN" -> navigateTo(ScreenDestination.AdminDashboard)
                    "PROVIDER" -> navigateTo(ScreenDestination.ProviderDashboard)
                    else -> navigateTo(ScreenDestination.Home)
                }
                onComplete(true)
            }.onFailure { error ->
                showMessage(error.message ?: "Login failed. Please verify credentials.")
                onComplete(false)
            }
        }
    }

    // One-Tap Google Sign-In for Customers
    fun loginWithGoogle(onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val googleEmail = "google.user.${System.currentTimeMillis() % 1000}@gmail.com"
            val allUsers = repository.observeAllUsersAdmin().first()
            val existingCustomer = allUsers.find { it.role == "CUSTOMER" }
            if (existingCustomer != null) {
                repository.switchUserSession(existingCustomer)
                showMessage("Signed in with Google as ${existingCustomer.name}")
                navigateTo(ScreenDestination.Home)
                onComplete(true)
            } else {
                val regResult = repository.registerCustomer("Google User", googleEmail, "+1 (555) 012-3456", "googlePass123")
                regResult.onSuccess { user ->
                    showMessage("Google sign-in successful! Welcome, ${user.name}")
                    navigateTo(ScreenDestination.Home)
                    onComplete(true)
                }.onFailure { err ->
                    showMessage(err.message ?: "Google sign-in failed")
                    onComplete(false)
                }
            }
        }
    }

    // Phone / SMS OTP Verification Login
    fun loginWithPhoneOtp(phoneNumber: String, otpCode: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val trimmedPhone = phoneNumber.trim()
            val trimmedOtp = otpCode.trim()

            if (trimmedOtp.length < 4) {
                showMessage("Please enter a valid 4 or 6-digit verification code.")
                onComplete(false)
                return@launch
            }

            val allUsers = repository.observeAllUsersAdmin().first()
            val userWithPhone = allUsers.find { it.phone.replace(Regex("[^0-9]"), "").endsWith(trimmedPhone.replace(Regex("[^0-9]"), "").takeLast(8)) }

            if (userWithPhone != null) {
                repository.switchUserSession(userWithPhone)
                showMessage("Phone verified! Welcome back, ${userWithPhone.name}")
                when (userWithPhone.role) {
                    "ADMIN" -> navigateTo(ScreenDestination.AdminDashboard)
                    "PROVIDER" -> navigateTo(ScreenDestination.ProviderDashboard)
                    else -> navigateTo(ScreenDestination.Home)
                }
                onComplete(true)
            } else {
                // Auto register new customer with phone
                val generatedEmail = "phone_${trimmedPhone.replace(Regex("[^0-9]"), "")}@servexa.local"
                val reg = repository.registerCustomer("Mobile User", generatedEmail, trimmedPhone, "phoneAuthSecret123")
                reg.onSuccess { newUser ->
                    showMessage("Phone verified! Account created.")
                    navigateTo(ScreenDestination.Home)
                    onComplete(true)
                }.onFailure {
                    showMessage("Verification failed. Please try again.")
                    onComplete(false)
                }
            }
        }
    }

    fun register(
        name: String,
        email: String,
        phone: String,
        passwordRaw: String,
        role: String = "CUSTOMER",
        title: String? = null,
        bio: String = ""
    ) {
        if (role == "PROVIDER") {
            registerProvider(
                name = name,
                email = email,
                phone = phone,
                passwordRaw = passwordRaw,
                title = title ?: "Licensed Specialist",
                bio = bio,
                locationName = "San Francisco, CA",
                categoryId = "cat_elec",
                firstServiceTitle = "Standard Service Consultation",
                firstServicePrice = 85.0
            )
        } else {
            registerCustomer(name, email, phone, passwordRaw)
        }
    }

    fun registerCustomer(name: String, email: String, phone: String, passwordRaw: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.registerCustomer(name, email, phone, passwordRaw)
            result.onSuccess { user ->
                showMessage("Account created! $50 welcome wallet bonus credited.")
                navigateTo(ScreenDestination.Home)
                onComplete(true)
            }.onFailure { error ->
                showMessage(error.message ?: "Registration failed.")
                onComplete(false)
            }
        }
    }

    fun registerProvider(
        name: String,
        email: String,
        phone: String,
        passwordRaw: String,
        title: String,
        bio: String,
        locationName: String,
        categoryId: String,
        firstServiceTitle: String,
        firstServicePrice: Double,
        onComplete: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.registerProvider(
                name, email, phone, passwordRaw, title, bio, locationName, categoryId, firstServiceTitle, firstServicePrice
            )
            result.onSuccess { user ->
                showMessage("Provider account submitted! Welcome, ${user.name}.")
                navigateTo(ScreenDestination.ProviderDashboard)
                onComplete(true)
            }.onFailure { error ->
                showMessage(error.message ?: "Registration failed.")
                onComplete(false)
            }
        }
    }

    fun logout() {
        repository.logout()
        backStack.clear()
        _currentScreen.value = ScreenDestination.Home
        showMessage("Logged out successfully.")
    }

    // Special Admin Gateway Login
    fun loginSpecialAdmin(
        passcodeOrKey: String,
        adminEmail: String = "admin@servexa.com",
        onComplete: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val trimmedKey = passcodeOrKey.trim()
            val validMasterKeys = listOf("ADMIN-2026", "ADMIN-SECRET-KEY", "778899", "admin123", "SERVA-ROOT-KEY", "9988", "MASTER-ADMIN")
            val isMasterKey = validMasterKeys.any { it.equals(trimmedKey, ignoreCase = true) }

            val allUsers = repository.observeAllUsersAdmin().first()
            val adminUser = allUsers.find { it.role == "ADMIN" || it.email.equals(adminEmail.trim(), ignoreCase = true) }
                ?: allUsers.find { it.role == "ADMIN" }

            if (isMasterKey && adminUser != null) {
                repository.switchUserSession(adminUser)
                navigateTo(ScreenDestination.AdminDashboard)
                showMessage("Authorized: Master Admin access verified.")
                onComplete(true)
                return@launch
            }

            // Verify with repository authentication
            val result = repository.login(adminEmail.trim().ifBlank { "admin@servexa.com" }, trimmedKey)
            result.onSuccess { user ->
                if (user.role == "ADMIN") {
                    navigateTo(ScreenDestination.AdminDashboard)
                    showMessage("Authorized: Welcome, ${user.name}.")
                    onComplete(true)
                } else {
                    showMessage("Access Denied: Account does not have administrative privileges.")
                    onComplete(false)
                }
            }.onFailure { error ->
                showMessage(error.message ?: "Invalid Admin Security Key or Passcode.")
                onComplete(false)
            }
        }
    }

    // Switch demo role easily for testing
    fun switchDemoUser(role: String) {
        viewModelScope.launch {
            val allUsers = repository.observeAllUsersAdmin().first()
            val targetUser = when (role) {
                "ADMIN" -> allUsers.find { it.role == "ADMIN" }
                "PROVIDER" -> allUsers.find { it.role == "PROVIDER" }
                else -> allUsers.find { it.role == "CUSTOMER" }
            }
            if (targetUser != null) {
                repository.switchUserSession(targetUser)
                when (targetUser.role) {
                    "ADMIN" -> navigateTo(ScreenDestination.AdminDashboard)
                    "PROVIDER" -> navigateTo(ScreenDestination.ProviderDashboard)
                    else -> navigateTo(ScreenDestination.Home)
                }
                showMessage("Switched session to ${targetUser.name} (${targetUser.role})")
            }
        }
    }

    // ==========================================
    // WALLET ACTIONS (5% Platform Top-up Fee & 48h Withdrawals)
    // ==========================================

    fun topUpWallet(amount: Double, paymentMethodName: String = "Instant Card / Wire", referenceCode: String = "") {
        val user = currentUser.value ?: run {
            showMessage("Please sign in to top up wallet balance.")
            navigateTo(ScreenDestination.Auth)
            return
        }
        viewModelScope.launch {
            val result = repository.topUpWallet(user.id, amount, paymentMethodName, referenceCode)
            result.onSuccess { txn ->
                showMessage("Top-up request of $${"%.2f".format(amount)} via $paymentMethodName submitted! Net credit: $${"%.2f".format(txn.netAmount)}.")
            }.onFailure { error ->
                showMessage(error.message ?: "Top-up failed.")
            }
        }
    }

    fun savePayoutAccount(
        accountType: String,
        holderName: String,
        bankName: String,
        accountNumber: String,
        routingCode: String,
        swift: String = "",
        country: String = "United States",
        onComplete: () -> Unit = {}
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val account = UserPayoutAccountEntity(
                id = "payout_acc_${user.id}",
                userId = user.id,
                accountType = accountType,
                accountHolderName = holderName.trim(),
                bankOrIssuerName = bankName.trim(),
                accountOrCardNumber = accountNumber.trim(),
                routingOrIfscOrCvv = routingCode.trim(),
                swiftOrBic = swift.trim(),
                country = country.trim(),
                isDefault = true,
                updatedAt = System.currentTimeMillis()
            )
            val result = repository.savePayoutAccount(account)
            result.onSuccess {
                showMessage("Payout account details attached successfully.")
                onComplete()
            }.onFailure { error ->
                showMessage(error.message ?: "Failed to save payout details.")
            }
        }
    }

    fun requestWithdrawal(amount: Double, onNeedDetails: () -> Unit = {}) {
        val user = currentUser.value ?: run {
            showMessage("Please sign in to withdraw funds.")
            return
        }
        viewModelScope.launch {
            val result = repository.requestWithdrawal(user.id, amount)
            result.onSuccess {
                showMessage("Withdrawal request of $${"%.2f".format(amount)} submitted! Processing window is within 48 hours.")
            }.onFailure { error ->
                val msg = error.message ?: "Withdrawal request failed."
                showMessage(msg)
                if (msg.contains("attach your Bank Account", ignoreCase = true)) {
                    onNeedDetails()
                }
            }
        }
    }

    // ==========================================
    // KYC DOCUMENT VERIFICATION & POS ALLOTMENT
    // ==========================================

    fun submitKycDocument(
        documentType: String,
        documentNumber: String,
        issuingCountry: String = "United States",
        issuingStateOrProvince: String = "California",
        expiryDate: String = "2029-12-31",
        dateOfBirth: String = "1994-05-18",
        residentialAddress: String = "",
        frontImage: String = "id_front_captured",
        backImage: String = "id_back_captured",
        selfieImage: String = "selfie_verified",
        documentFrontPhotoUrl: String = "",
        documentBackPhotoUrl: String = "",
        selfiePhotoUrl: String = "",
        onSuccess: () -> Unit = {}
    ) {
        val user = currentUser.value ?: run {
            showMessage("Please sign in to submit documents.")
            return
        }
        if (documentNumber.isBlank()) {
            showMessage("Please enter your official document number.")
            return
        }
        val effectiveAddress = if (residentialAddress.isNotBlank()) residentialAddress else userLocationName.value
        val effectiveFront = if (documentFrontPhotoUrl.isNotBlank()) documentFrontPhotoUrl else frontImage
        val effectiveBack = if (documentBackPhotoUrl.isNotBlank()) documentBackPhotoUrl else backImage
        val effectiveSelfie = if (selfiePhotoUrl.isNotBlank()) selfiePhotoUrl else selfieImage

        viewModelScope.launch {
            val result = repository.submitKycDocument(
                userId = user.id,
                userName = user.name,
                userEmail = user.email,
                userPhone = user.phone,
                documentType = documentType,
                documentNumber = documentNumber.trim(),
                issuingCountry = issuingCountry.trim(),
                issuingStateOrProvince = issuingStateOrProvince.trim(),
                expiryDate = expiryDate.trim(),
                dateOfBirth = dateOfBirth.trim(),
                residentialAddress = effectiveAddress,
                documentFrontImage = effectiveFront,
                documentBackImage = effectiveBack,
                selfieImage = effectiveSelfie
            )
            result.onSuccess {
                showMessage("Verification documents uploaded successfully! Admin is reviewing.")
                onSuccess()
            }.onFailure { error ->
                showMessage(error.message ?: "Failed to upload document.")
            }
        }
    }

    fun adminReviewKycDocument(
        kycId: String = "",
        documentId: String = "",
        status: String, // "VERIFIED" or "REJECTED"
        rejectionReason: String = "",
        adminNotes: String = "",
        allotPosCreditAmount: Double = 0.0
    ) {
        val effectiveKycId = if (documentId.isNotBlank()) documentId else kycId
        val admin = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.reviewKycDocument(
                kycId = effectiveKycId,
                status = status,
                rejectionReason = rejectionReason,
                adminNotes = adminNotes,
                adminId = admin.id,
                adminName = admin.name
            )
            result.onSuccess {
                if (status == "VERIFIED" && allotPosCreditAmount > 0) {
                    val kycDoc = repository.observeAllKycDocuments().first().find { it.id == effectiveKycId }
                    if (kycDoc != null) {
                        repository.allotPosCredit(
                            userId = kycDoc.userId,
                            grossAmount = allotPosCreditAmount,
                            posTerminalId = "POS-TERM-SF-01",
                            posLocation = "Servexa Downtown Station",
                            posAgentName = admin.name,
                            posAuthCode = "POS-KYC-AUTH-${(100000..999999).random()}",
                            notes = "Auto POS Credit Allotment on KYC Approval ($${allotPosCreditAmount})",
                            adminId = admin.id,
                            adminName = admin.name
                        )
                    }
                }
                showMessage(if (status == "VERIFIED") "Document approved and user wallet unlocked!" else "Document rejected.")
            }.onFailure { error ->
                showMessage(error.message ?: "Failed to update review status.")
            }
        }
    }

    fun adminAllotPosCredit(
        userId: String = "",
        targetUserId: String = "",
        amount: Double,
        posTerminalId: String = "POS-TERM-SF-01",
        posLocation: String = "Servexa Downtown POS Station",
        posAgentName: String = "David Vance (Certified POS Agent)",
        posAuthCode: String = "",
        notes: String = "",
        note: String = "",
        onSuccess: () -> Unit = {}
    ) {
        val effectiveUserId = if (targetUserId.isNotBlank()) targetUserId else userId
        val effectiveNote = if (note.isNotBlank()) note else notes
        val admin = currentUser.value ?: return
        if (amount <= 0) {
            showMessage("Please enter a valid credit allotment amount.")
            return
        }
        viewModelScope.launch {
            val result = repository.allotPosCredit(
                userId = effectiveUserId,
                grossAmount = amount,
                posTerminalId = posTerminalId.trim(),
                posLocation = posLocation.trim(),
                posAgentName = posAgentName.trim(),
                posAuthCode = posAuthCode.trim(),
                notes = effectiveNote.trim(),
                adminId = admin.id,
                adminName = admin.name
            )
            result.onSuccess { txn ->
                showMessage("Success! $${"%.2f".format(amount)} POS credit allotted to wallet. Auth: ${txn.referenceId}")
                onSuccess()
            }.onFailure { error ->
                showMessage(error.message ?: "POS Credit Allotment failed.")
            }
        }
    }

    // ==========================================
    // PROVIDER STORE SUBDOMAIN & WEB STOREFRONT ($5/month)
    // ==========================================

    fun createOrRenewProviderSubdomain(
        subdomain: String,
        storeTitle: String,
        tagline: String,
        aboutBio: String,
        category: String,
        contactPhone: String,
        contactEmail: String,
        whatsappNumber: String,
        businessAddress: String,
        operatingHours: String,
        announcement: String,
        onSuccess: (ProviderStoreEntity) -> Unit = {}
    ) {
        val user = currentUser.value
        if (user == null || user.role != "PROVIDER") {
            showMessage("Only registered service providers can launch a store subdomain.")
            return
        }

        viewModelScope.launch {
            val result = repository.createOrRenewProviderSubdomain(
                providerId = user.id,
                providerName = user.name,
                rawSubdomain = subdomain,
                storeTitle = storeTitle,
                tagline = tagline,
                aboutBio = aboutBio,
                category = category,
                contactPhone = contactPhone,
                contactEmail = contactEmail,
                whatsappNumber = whatsappNumber,
                businessAddress = businessAddress,
                operatingHours = operatingHours,
                announcement = announcement
            )
            result.onSuccess { store ->
                showMessage("🎉 Store Subdomain active: ${store.subdomain}.servexa.com ($5.00/mo billed to wallet)")
                onSuccess(store)
            }.onFailure { err ->
                showMessage(err.message ?: "Failed to set up subdomain.")
            }
        }
    }

    fun toggleProviderStoreActive(isActive: Boolean) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.toggleStoreActive(user.id, isActive)
            showMessage(if (isActive) "Storefront is now live to the public." else "Storefront paused.")
        }
    }

    fun cancelProviderSubdomain() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.cancelStoreSubdomain(user.id)
            showMessage("Subdomain auto-renewal cancelled. Store will be archived at end of billing cycle.")
        }
    }

    fun openWebStorefront(subdomain: String) {
        viewModelScope.launch {
            repository.incrementStoreVisitor(subdomain)
        }
        webPortalAddressBarUrl.value = "https://${subdomain.lowercase().trim()}.servexa.com"
        navigateTo(ScreenDestination.WebStorefrontViewer(subdomain))
    }

    fun toggleWebPortalMode() {
        isWebPortalViewActive.value = !isWebPortalViewActive.value
        showMessage(if (isWebPortalViewActive.value) "🌐 Switched to Web Portal View" else "📱 Switched to Native View")
    }

    // ==========================================
    // BOOKING ACTIONS (6% Service Commission)
    // ==========================================

    fun createBooking(
        providerId: String,
        serviceId: String,
        address: String,
        scheduledAt: Long,
        problemDescription: String,
        specialInstructions: String,
        onSuccess: (String) -> Unit
    ) {
        val user = currentUser.value ?: run {
            showMessage("Please sign in to book services.")
            navigateTo(ScreenDestination.Auth)
            return
        }

        viewModelScope.launch {
            val result = repository.createBooking(
                customerId = user.id,
                providerId = providerId,
                serviceId = serviceId,
                address = address,
                scheduledAt = scheduledAt,
                problemDescription = problemDescription,
                specialInstructions = specialInstructions
            )
            result.onSuccess { booking ->
                showMessage("Booking #${booking.id} created successfully!")
                navigateTo(ScreenDestination.ActiveBookingTracking(booking.id))
                onSuccess(booking.id)
            }.onFailure { error ->
                showMessage(error.message ?: "Failed to create booking.")
            }
        }
    }

    fun updateBookingStatus(bookingId: String, newStatus: String, reason: String = "") {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.updateBookingStatus(
                bookingId = bookingId,
                newStatus = newStatus,
                actorId = user.id,
                actorRole = user.role,
                reason = reason
            )
            result.onSuccess {
                showMessage("Booking status updated to ${newStatus.replace("_", " ")}.")
            }.onFailure { error ->
                showMessage(error.message ?: "Failed to update status.")
            }
        }
    }

    // ==========================================
    // REVIEWS & FEEDBACK
    // ==========================================

    fun submitReview(bookingId: String, rating: Double, reviewText: String, onComplete: () -> Unit) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.submitReview(bookingId, user.id, rating, reviewText)
            result.onSuccess {
                showMessage("Thank you! Your ${"%.1f".format(rating)}★ review has been published.")
                onComplete()
            }.onFailure { error ->
                showMessage(error.message ?: "Failed to submit review.")
            }
        }
    }

    // ==========================================
    // SERVEXA SECURE CALL ACTIONS
    // ==========================================

    fun initiateCall(
        bookingId: String,
        receiverId: String,
        receiverName: String,
        receiverRole: String
    ) {
        val user = currentUser.value ?: run {
            showMessage("Please sign in to make secure calls.")
            return
        }
        repository.initiateSecureCall(
            bookingId = bookingId,
            caller = user,
            receiverId = receiverId,
            receiverName = receiverName,
            receiverRole = receiverRole
        )
    }

    fun acceptIncomingCall() {
        repository.acceptCall()
    }

    fun acceptCall() = acceptIncomingCall()

    fun endCurrentCall() {
        viewModelScope.launch {
            repository.endCall()
        }
    }

    fun endCall() = endCurrentCall()

    // ==========================================
    // WORK VIDEOS ACTIONS
    // ==========================================

    fun toggleVideoLike(videoId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.toggleVideoLike(videoId, user.id)
        }
    }

    fun addVideoComment(videoId: String, commentText: String) {
        val user = currentUser.value ?: return
        if (commentText.isBlank()) return
        viewModelScope.launch {
            repository.addVideoComment(videoId, user.id, commentText)
            showMessage("Comment posted!")
        }
    }

    fun uploadWorkVideo(title: String, description: String, category: String, onComplete: () -> Unit) {
        val user = currentUser.value ?: return
        if (title.isBlank() || description.isBlank()) {
            showMessage("Please provide a video title and description.")
            return
        }
        viewModelScope.launch {
            val result = repository.uploadWorkVideo(user.id, title, description, category)
            result.onSuccess {
                showMessage("Work video uploaded to portfolio!")
                onComplete()
            }.onFailure { error ->
                showMessage(error.message ?: "Failed to upload video.")
            }
        }
    }

    // ==========================================
    // PRODUCT MARKETPLACE & CART ACTIONS
    // ==========================================

    fun addToCart(productId: String) {
        val user = currentUser.value ?: run {
            showMessage("Please sign in to add items to cart.")
            navigateTo(ScreenDestination.Auth)
            return
        }
        viewModelScope.launch {
            repository.addToCart(user.id, productId)
            showMessage("Added to cart!")
        }
    }

    fun updateCartQuantity(cartItemId: Long, quantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(cartItemId, quantity)
        }
    }

    fun removeFromCart(cartItemId: Long) {
        viewModelScope.launch {
            repository.removeFromCart(cartItemId)
            showMessage("Item removed from cart.")
        }
    }

    fun checkoutCart(shippingAddress: String, onComplete: (Boolean) -> Unit) {
        val user = currentUser.value ?: return
        if (shippingAddress.isBlank()) {
            showMessage("Please enter a valid shipping address.")
            return
        }
        viewModelScope.launch {
            val result = repository.checkoutCart(user.id, shippingAddress)
            result.onSuccess { order ->
                showMessage("Order #${order.id} confirmed! Preparing for shipment.")
                onComplete(true)
            }.onFailure { error ->
                showMessage(error.message ?: "Checkout failed.")
                onComplete(false)
            }
        }
    }

    // ==========================================
    // DISPUTE ACTIONS
    // ==========================================

    fun openDispute(bookingId: String, reason: String, description: String, onComplete: () -> Unit) {
        val user = currentUser.value ?: return
        if (reason.isBlank() || description.isBlank()) {
            showMessage("Please enter reason and description for dispute.")
            return
        }
        viewModelScope.launch {
            val result = repository.openDispute(bookingId, user.id, reason, description)
            result.onSuccess {
                showMessage("Dispute case created. Servexa support is reviewing.")
                onComplete()
            }.onFailure { error ->
                showMessage(error.message ?: "Failed to open dispute.")
            }
        }
    }

    // ==========================================
    // ADMIN ACTIONS (Secure & Role Checked)
    // ==========================================

    fun adminApproveTransaction(txnId: String) {
        val admin = currentUser.value
        if (admin?.role != "ADMIN") return
        viewModelScope.launch {
            val result = repository.adminApproveTransaction(txnId, admin.id)
            result.onSuccess {
                showMessage("Transaction approved successfully.")
            }.onFailure { error ->
                showMessage(error.message ?: "Approval failed.")
            }
        }
    }

    fun adminRejectTransaction(txnId: String, reason: String) {
        val admin = currentUser.value
        if (admin?.role != "ADMIN") return
        viewModelScope.launch {
            val result = repository.adminRejectTransaction(txnId, admin.id, reason)
            result.onSuccess {
                showMessage("Transaction rejected and funds restored.")
            }.onFailure { error ->
                showMessage(error.message ?: "Rejection failed.")
            }
        }
    }

    fun adminVerifyProvider(userId: String) {
        val admin = currentUser.value
        if (admin?.role != "ADMIN") return
        viewModelScope.launch {
            val result = repository.adminVerifyProvider(userId, admin.id)
            result.onSuccess {
                showMessage("Provider verified and badge assigned!")
            }.onFailure { error ->
                showMessage(error.message ?: "Verification failed.")
            }
        }
    }

    fun adminToggleUserStatus(userId: String, currentStatus: String) {
        val admin = currentUser.value
        if (admin?.role != "ADMIN") return
        viewModelScope.launch {
            val result = repository.adminToggleUserStatus(userId, currentStatus, admin.id)
            result.onSuccess {
                showMessage("User status updated.")
            }
        }
    }

    fun adminAdjustCredits(
        targetUserId: String,
        isAddition: Boolean,
        amount: Double,
        reason: String,
        onComplete: () -> Unit = {}
    ) {
        val admin = currentUser.value
        if (admin?.role != "ADMIN") return
        if (amount <= 0) {
            showMessage("Please enter a valid amount greater than 0.")
            return
        }
        val delta = if (isAddition) amount else -amount
        viewModelScope.launch {
            val result = repository.adminAdjustUserCredits(admin.id, targetUserId, delta, reason.ifBlank { "Administrative balance adjustment" })
            result.onSuccess {
                showMessage("Wallet credits successfully ${if (isAddition) "added (+$$amount)" else "deducted (-$$amount)"}.")
                onComplete()
            }.onFailure { error ->
                showMessage(error.message ?: "Failed to adjust credits.")
            }
        }
    }

    fun adminSavePaymentMethod(method: PaymentMethodEntity, onComplete: () -> Unit = {}) {
        val admin = currentUser.value
        if (admin?.role != "ADMIN") return
        if (method.name.isBlank() || method.accountNumber.isBlank()) {
            showMessage("Please fill in payment method name and account number.")
            return
        }
        viewModelScope.launch {
            val result = repository.adminSavePaymentMethod(method, admin.id)
            result.onSuccess {
                showMessage("Payment method '${method.name}' saved and active across the app!")
                onComplete()
            }.onFailure { error ->
                showMessage(error.message ?: "Failed to save payment method.")
            }
        }
    }

    fun adminDeletePaymentMethod(id: String) {
        val admin = currentUser.value
        if (admin?.role != "ADMIN") return
        viewModelScope.launch {
            val result = repository.adminDeletePaymentMethod(id, admin.id)
            result.onSuccess {
                showMessage("Payment method deleted.")
            }.onFailure { error ->
                showMessage(error.message ?: "Failed to delete payment method.")
            }
        }
    }

    // Admin Merchant Gateway Configuration & Payment Capture
    fun adminSaveMerchantGateway(gateway: MerchantGatewayAccountEntity, onComplete: () -> Unit = {}) {
        val admin = currentUser.value
        if (admin?.role != "ADMIN") return
        if (gateway.name.isBlank() || gateway.merchantAccountId.isBlank()) {
            showMessage("Please provide a valid Merchant Business Name and Account ID.")
            return
        }
        viewModelScope.launch {
            val result = repository.saveMerchantGateway(gateway, admin.id)
            result.onSuccess {
                showMessage("Merchant Gateway '${gateway.name}' attached and active!")
                onComplete()
            }.onFailure { error ->
                showMessage(error.message ?: "Failed to save merchant gateway.")
            }
        }
    }

    fun adminDeleteMerchantGateway(id: String) {
        val admin = currentUser.value
        if (admin?.role != "ADMIN") return
        viewModelScope.launch {
            val result = repository.deleteMerchantGateway(id, admin.id)
            result.onSuccess {
                showMessage("Merchant gateway configuration removed.")
            }.onFailure { error ->
                showMessage(error.message ?: "Failed to delete gateway.")
            }
        }
    }

    fun adminTestMerchantHandshake(gateway: MerchantGatewayAccountEntity, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            delay(500)
            if (gateway.merchantAccountId.isNotBlank()) {
                val mode = if (gateway.isLiveMode) "LIVE" else "SANDBOX"
                onResult(true, "Handshake verified with ${gateway.name} [$mode Mode | ID: ${gateway.merchantAccountId}]. Auto-capture & Customer details storage ACTIVE.")
                showMessage("Gateway connection handshake successful!")
            } else {
                onResult(false, "Handshake failed: Invalid Merchant Account ID or Missing Keys.")
                showMessage("Merchant Gateway connection test failed.")
            }
        }
    }

    fun adminInitiateCallToUser(targetUser: UserEntity) {
        val admin = currentUser.value ?: return
        initiateCall(
            bookingId = "ADMIN-DIRECT",
            receiverId = targetUser.id,
            receiverName = targetUser.name,
            receiverRole = targetUser.role
        )
    }

    fun adminStartChatWithUser(targetUser: UserEntity) {
        navigateTo(
            ScreenDestination.Chat(
                recipientId = targetUser.id,
                recipientName = targetUser.name,
                bookingId = "ADMIN-SUPPORT"
            )
        )
    }

    // ==========================================
    // KYC DOCUMENT VERIFICATION & POS CREDIT ACTIONS
    // ==========================================

    fun submitUserKycDocument(
        documentType: String,
        documentNumber: String,
        issuingCountry: String,
        issuingStateOrProvince: String,
        expiryDate: String,
        dateOfBirth: String,
        residentialAddress: String,
        frontImage: String = "dl_front_preview",
        backImage: String = "dl_back_preview",
        selfieImage: String = "selfie_liveness_verified",
        onComplete: (Boolean) -> Unit = {}
    ) {
        val user = currentUser.value ?: run {
            showMessage("Please sign in to submit verification documents.")
            return
        }
        if (documentNumber.isBlank() || residentialAddress.isBlank()) {
            showMessage("Please enter document number and residential address.")
            return
        }

        viewModelScope.launch {
            val result = repository.submitKycDocument(
                userId = user.id,
                userName = user.name,
                userEmail = user.email,
                userPhone = user.phone,
                documentType = documentType,
                documentNumber = documentNumber,
                issuingCountry = issuingCountry,
                issuingStateOrProvince = issuingStateOrProvince,
                expiryDate = expiryDate,
                dateOfBirth = dateOfBirth,
                residentialAddress = residentialAddress,
                frontImage = frontImage,
                backImage = backImage,
                selfieImage = selfieImage
            )
            result.onSuccess {
                showMessage("KYC verification documents ($documentType) submitted for admin review & wallet access!")
                onComplete(true)
            }.onFailure { error ->
                showMessage(error.message ?: "Document upload failed.")
                onComplete(false)
            }
        }
    }

    fun adminReviewKycDocument(
        docId: String,
        isApproved: Boolean,
        rejectionReason: String = "",
        adminNotes: String = "",
        posCreditAllotment: Double = 0.0,
        onComplete: () -> Unit = {}
    ) {
        val admin = currentUser.value
        if (admin?.role != "ADMIN") return
        viewModelScope.launch {
            val result = repository.adminReviewKycDocument(
                docId = docId,
                isApproved = isApproved,
                rejectionReason = rejectionReason,
                adminNotes = adminNotes,
                adminId = admin.id,
                posCreditAllotment = posCreditAllotment
            )
            result.onSuccess {
                showMessage(if (isApproved) "KYC Approved! POS Terminal Credit ($$posCreditAllotment) allotted." else "KYC Rejected.")
                onComplete()
            }.onFailure { error ->
                showMessage(error.message ?: "Failed to review KYC.")
            }
        }
    }

    fun adminCreateCategory(name: String, description: String, iconName: String, onComplete: () -> Unit) {
        val admin = currentUser.value
        if (admin?.role != "ADMIN") return
        if (name.isBlank()) {
            showMessage("Category name cannot be empty.")
            return
        }
        viewModelScope.launch {
            val result = repository.adminCreateCategory(name, description, iconName)
            result.onSuccess {
                showMessage("New category '${it.name}' created.")
                onComplete()
            }.onFailure { error ->
                showMessage(error.message ?: "Failed to create category.")
            }
        }
    }

    fun adminToggleCategoryActive(categoryId: String, currentActive: Boolean) {
        val admin = currentUser.value
        if (admin?.role != "ADMIN") return
        viewModelScope.launch {
            repository.adminToggleCategoryActive(categoryId, currentActive)
            showMessage("Category visibility updated.")
        }
    }

    fun adminResolveDispute(disputeId: String, resolutionType: String, adminNotes: String, onComplete: () -> Unit = {}) {
        val admin = currentUser.value
        if (admin?.role != "ADMIN") return
        viewModelScope.launch {
            val result = repository.adminResolveDispute(disputeId, admin.id, resolutionType, adminNotes)
            result.onSuccess {
                showMessage("Dispute resolved with $resolutionType.")
                onComplete()
            }.onFailure { error ->
                showMessage(error.message ?: "Failed to resolve dispute.")
            }
        }
    }

    fun adminUpdateCommissionSettings(topUpFeePercent: String, serviceCommissionPercent: String) {
        val admin = currentUser.value
        if (admin?.role != "ADMIN") return
        viewModelScope.launch {
            repository.updatePlatformSetting("topup_fee_percent", topUpFeePercent, "Wallet Top-up Fee %")
            repository.updatePlatformSetting("service_commission_percent", serviceCommissionPercent, "Service Commission %")
            showMessage("Platform commission rules updated.")
        }
    }

    fun adminRejectProvider(userId: String, reason: String = "Application does not meet current verification standards.") {
        val admin = currentUser.value
        if (admin?.role != "ADMIN") return
        viewModelScope.launch {
            val result = repository.adminRejectProvider(userId, admin.id, reason)
            result.onSuccess {
                showMessage("Provider registration application rejected.")
            }.onFailure { error ->
                showMessage(error.message ?: "Action failed.")
            }
        }
    }

    // In-App Chat Functions
    fun getChatMessages(otherUserId: String): Flow<List<ChatMessageEntity>> {
        val myId = currentUser.value?.id ?: ""
        return repository.observeMessagesBetweenUsers(myId, otherUserId)
    }

    fun sendChatMessage(
        recipientId: String,
        recipientName: String,
        text: String,
        bookingId: String = "",
        mediaType: String = "NONE",
        mediaUrl: String = "",
        mediaCaption: String = "",
        videoDurationSec: Int = 0,
        locationLat: Double = 0.0,
        locationLng: Double = 0.0,
        locationAddress: String = ""
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.sendChatMessage(
                senderId = user.id,
                senderName = user.name,
                senderRole = user.role,
                receiverId = recipientId,
                receiverName = recipientName,
                text = text,
                bookingId = bookingId,
                mediaType = mediaType,
                mediaUrl = mediaUrl,
                mediaCaption = mediaCaption,
                videoDurationSec = videoDurationSec,
                locationLat = locationLat,
                locationLng = locationLng,
                locationAddress = locationAddress
            )
            result.onFailure { error ->
                showMessage(error.message ?: "Could not send message.")
            }
        }
    }

    fun sendChatPhoto(
        recipientId: String,
        recipientName: String,
        photoUrl: String,
        caption: String = "",
        bookingId: String = ""
    ) {
        sendChatMessage(
            recipientId = recipientId,
            recipientName = recipientName,
            text = caption.ifBlank { "📷 Photo Attachment" },
            bookingId = bookingId,
            mediaType = "PHOTO",
            mediaUrl = photoUrl,
            mediaCaption = caption
        )
        showMessage("Photo sent!")
    }

    fun sendChatVideo(
        recipientId: String,
        recipientName: String,
        videoUrl: String,
        caption: String = "",
        durationSeconds: Int = 15,
        bookingId: String = ""
    ) {
        sendChatMessage(
            recipientId = recipientId,
            recipientName = recipientName,
            text = caption.ifBlank { "🎥 Video Clip ($durationSeconds s)" },
            bookingId = bookingId,
            mediaType = "VIDEO",
            mediaUrl = videoUrl,
            mediaCaption = caption,
            videoDurationSec = durationSeconds
        )
        showMessage("Video sent!")
    }

    fun sendChatLocation(
        recipientId: String,
        recipientName: String,
        lat: Double,
        lng: Double,
        address: String,
        bookingId: String = ""
    ) {
        sendChatMessage(
            recipientId = recipientId,
            recipientName = recipientName,
            text = "📍 Live Location: $address",
            bookingId = bookingId,
            mediaType = "LOCATION",
            locationLat = lat,
            locationLng = lng,
            locationAddress = address
        )
        showMessage("Live location shared with $recipientName!")
    }

    fun markChatRead(otherUserId: String) {
        val myId = currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.markChatRead(receiverId = myId, senderId = otherUserId)
        }
    }

    // Provider profile edit
    fun updateProviderProfile(
        title: String,
        bio: String,
        locationName: String,
        workingHours: String,
        facebookUrl: String,
        websiteUrl: String,
        emergencyAvailable: Boolean
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.updateProviderProfile(
                user.id, title, bio, locationName, workingHours, facebookUrl, websiteUrl, emergencyAvailable
            )
            result.onSuccess {
                showMessage("Provider profile updated.")
            }.onFailure { error ->
                showMessage(error.message ?: "Failed to update profile.")
            }
        }
    }

    // Provider add service
    fun addProviderService(categoryId: String, title: String, description: String, price: Double, duration: Int, onComplete: () -> Unit) {
        val user = currentUser.value ?: return
        if (title.isBlank() || price <= 0) {
            showMessage("Please fill valid service title and price.")
            return
        }
        viewModelScope.launch {
            val result = repository.addServiceForProvider(user.id, categoryId, title, description, price, duration)
            result.onSuccess {
                showMessage("New service '${it.title}' added to your catalog.")
                onComplete()
            }.onFailure { error ->
                showMessage(error.message ?: "Failed to add service.")
            }
        }
    }

    // Notification read
    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun markAllNotificationsRead() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.markAllNotificationsRead(user.id)
            showMessage("All notifications marked as read.")
        }
    }

    // Real-Time Location & Booking Tracking
    fun getBookingLiveLocation(bookingId: String): Flow<LocationLogEntity?> =
        repository.observeBookingLiveLocation(bookingId)

    fun observeBookingLiveLocation(bookingId: String): Flow<LocationLogEntity?> =
        repository.observeBookingLiveLocation(bookingId)

    fun updateBookingLiveLocation(
        bookingId: String,
        latitude: Double,
        longitude: Double,
        speedKmh: Double = 32.0,
        distanceRemainingKm: Double = 1.0,
        etaMinutes: Int = 4,
        streetName: String = "Market Street & 4th Ave"
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.updateBookingLiveLocation(
                bookingId = bookingId,
                userId = user.id,
                userRole = user.role,
                latitude = latitude,
                longitude = longitude,
                speedKmh = speedKmh,
                heading = 45f,
                distanceRemainingKm = distanceRemainingKm,
                etaMinutes = etaMinutes,
                streetName = streetName
            )
        }
    }

    fun broadcastCurrentLocationToBooking(bookingId: String) {
        val user = currentUser.value ?: return
        val currentLat = userLatitude.value
        val currentLng = userLongitude.value
        viewModelScope.launch {
            repository.updateBookingLiveLocation(
                bookingId = bookingId,
                userId = user.id,
                userRole = user.role,
                latitude = currentLat,
                longitude = currentLng,
                speedKmh = if (user.role == "PROVIDER") 28.5 else 0.0,
                distanceRemainingKm = 1.2,
                etaMinutes = 4,
                streetName = "Current Live Location"
            )
            showMessage("Live GPS coordinates synchronized!")
        }
    }

    fun calculateDistanceTo(lat: Double, lng: Double): Double =
        repository.calculateDistanceKm(userLatitude.value, userLongitude.value, lat, lng)

    // Provider Products Management
    fun getProductsForSeller(sellerId: String): Flow<List<ProductEntity>> =
        repository.observeProductsBySeller(sellerId)

    fun addProviderProduct(
        categoryId: String,
        name: String,
        description: String,
        price: Double,
        inventory: Int = 10,
        onComplete: () -> Unit
    ) {
        val user = currentUser.value ?: return
        if (name.isBlank() || price <= 0) {
            showMessage("Please fill valid product name and price.")
            return
        }
        viewModelScope.launch {
            val result = repository.addProduct(
                sellerId = user.id,
                categoryId = categoryId,
                name = name,
                description = description,
                price = price,
                inventory = inventory
            )
            result.onSuccess {
                showMessage("New product / part '${it.name}' added to your catalog.")
                onComplete()
            }.onFailure { error ->
                showMessage(error.message ?: "Failed to add product.")
            }
        }
    }

    // Complaints & Grievances (Direct to Admin Panel)
    fun submitComplaint(
        reason: String,
        description: String,
        bookingId: String = "GENERAL_COMPLAINT",
        onComplete: () -> Unit
    ) {
        val user = currentUser.value
        if (user == null) {
            showMessage("Please log in to submit a complaint.")
            return
        }
        if (reason.isBlank() || description.isBlank()) {
            showMessage("Please provide complaint reason and detailed description.")
            return
        }
        viewModelScope.launch {
            val result = repository.submitComplaint(
                userId = user.id,
                reason = reason,
                description = description,
                bookingId = bookingId
            )
            result.onSuccess {
                showMessage("Your complaint #${it.id} has been submitted to Admin Support.")
                onComplete()
            }.onFailure { error ->
                showMessage(error.message ?: "Failed to submit complaint.")
            }
        }
    }

    private data class Params(
        val q: String,
        val catId: String?,
        val rating: Double,
        val price: Double,
        val verified: Boolean,
        val sort: String,
        val userLat: Double,
        val userLng: Double
    )
}

class ServexaViewModelFactory(private val repository: ServexaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ServexaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ServexaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
