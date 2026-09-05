package com.example.data.repository

import android.content.Context
import com.example.data.local.db.SeedDataHelper
import com.example.data.local.db.ServexaDatabase
import com.example.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

class ServexaRepository(
    private val db: ServexaDatabase,
    private val context: Context? = null
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    // Current Authenticated User State Flow
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Active Secure Call Session State
    private val _activeCallState = MutableStateFlow<ActiveCallSession?>(null)
    val activeCallState: StateFlow<ActiveCallSession?> = _activeCallState.asStateFlow()

    init {
        // Ensure base categories, providers, marketplace goods and admin account are seeded and up-to-date
        scope.launch {
            SeedDataHelper.ensureSeedData(db)
            if (_currentUser.value == null) {
                val defaultCust = db.userDao().getUserById("usr_customer_01")
                if (defaultCust != null) {
                    _currentUser.value = defaultCust
                }
            }
        }
    }

    // ==========================================
    // AUTHENTICATION & SESSION MANAGEMENT
    // ==========================================

    suspend fun login(emailOrUsername: String, passwordRaw: String): Result<UserEntity> {
        val input = emailOrUsername.trim()
        val hash = ServexaDatabase.hashPassword(passwordRaw.trim())

        // Search user by email or by name (for admin 'Mr-Pirate')
        val allUsers = db.userDao().getAllUsers().first()
        val matchedUser = allUsers.find { 
            (it.email.equals(input, ignoreCase = true) || it.name.equals(input, ignoreCase = true)) &&
            it.passwordHash == hash
        }

        return if (matchedUser != null) {
            if (matchedUser.status == "SUSPENDED") {
                Result.failure(Exception("Account is suspended. Please contact platform support."))
            } else {
                _currentUser.value = matchedUser
                // Log audit for admin
                if (matchedUser.role == "ADMIN") {
                    recordAuditLog(matchedUser.id, matchedUser.name, "ADMIN", "LOGIN", "AUTH", matchedUser.id, "Admin logged into console")
                }
                Result.success(matchedUser)
            }
        } else {
            Result.failure(Exception("Invalid credentials. Please check your username/email and password."))
        }
    }

    suspend fun registerCustomer(name: String, email: String, phone: String, passwordRaw: String): Result<UserEntity> {
        val existing = db.userDao().getUserByEmail(email.trim())
        if (existing != null) {
            return Result.failure(Exception("An account with this email already exists."))
        }

        val userId = "cust_${System.currentTimeMillis() % 1000000}"
        val user = UserEntity(
            id = userId,
            role = "CUSTOMER",
            name = name.trim(),
            email = email.trim(),
            phone = phone.trim(),
            passwordHash = ServexaDatabase.hashPassword(passwordRaw.trim()),
            profileImage = "",
            status = "ACTIVE",
            verificationStatus = "VERIFIED"
        )
        db.userDao().insertUser(user)

        // Initialize wallet
        val wallet = WalletEntity(
            id = "wall_$userId",
            userId = userId,
            availableBalance = 50.0, // Welcome credit
            pendingBalance = 0.0
        )
        db.walletDao().insertWallet(wallet)
        db.walletDao().insertTransaction(
            WalletTransactionEntity(
                id = "TXN-WELCOME-${System.currentTimeMillis() % 10000}",
                walletId = wallet.id,
                userId = userId,
                type = "TOP_UP",
                grossAmount = 52.63,
                fee = 2.63, // 5% fee
                netAmount = 50.0,
                status = "COMPLETED",
                note = "Welcome Bonus Credit ($50.00 net)"
            )
        )

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_reg_$userId",
                userId = userId,
                type = "WELCOME",
                title = "Welcome to Servexa, ${user.name}!",
                message = "Your customer account has been created. Start discovering top professionals near you.",
                read = false
            )
        )

        _currentUser.value = user
        return Result.success(user)
    }

    suspend fun registerProvider(
        name: String,
        email: String,
        phone: String,
        passwordRaw: String,
        title: String,
        bio: String,
        locationName: String,
        primaryCategoryId: String,
        firstServiceTitle: String,
        firstServicePrice: Double
    ): Result<UserEntity> {
        val existing = db.userDao().getUserByEmail(email.trim())
        if (existing != null) {
            return Result.failure(Exception("An account with this email already exists."))
        }

        val userId = "prov_${System.currentTimeMillis() % 1000000}"
        val user = UserEntity(
            id = userId,
            role = "PROVIDER",
            name = name.trim(),
            email = email.trim(),
            phone = phone.trim(),
            passwordHash = ServexaDatabase.hashPassword(passwordRaw.trim()),
            profileImage = "",
            status = "ACTIVE",
            verificationStatus = "PENDING" // Requires admin approval
        )
        db.userDao().insertUser(user)

        val profile = ProviderProfileEntity(
            id = "prof_$userId",
            userId = userId,
            title = title.trim(),
            bio = bio.trim(),
            locationName = locationName.trim(),
            serviceArea = "25 km radius",
            workingHours = "Mon - Sat: 8:00 AM - 6:00 PM",
            emergencyAvailable = true,
            rating = 5.0,
            reviewCount = 0,
            completedJobs = 0,
            verificationStatus = "PENDING",
            verificationDocuments = "Standard Professional Verification Application"
        )
        db.providerProfileDao().insertProfile(profile)

        // Add initial service
        if (firstServiceTitle.isNotBlank() && firstServicePrice > 0) {
            val service = ServiceEntity(
                id = "srv_${System.currentTimeMillis() % 100000}",
                providerId = userId,
                categoryId = primaryCategoryId,
                title = firstServiceTitle.trim(),
                description = "High quality professional service delivered by certified expert.",
                price = firstServicePrice,
                durationMinutes = 60
            )
            db.serviceDao().insertService(service)
        }

        // Initialize wallet
        val wallet = WalletEntity(
            id = "wall_$userId",
            userId = userId,
            availableBalance = 0.0,
            pendingBalance = 0.0
        )
        db.walletDao().insertWallet(wallet)

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_prov_reg_$userId",
                userId = userId,
                type = "APPROVAL",
                title = "Provider Application Submitted",
                message = "Your professional profile has been submitted and is currently pending verification review.",
                read = false
            )
        )

        _currentUser.value = user
        return Result.success(user)
    }

    fun switchUserSession(user: UserEntity) {
        _currentUser.value = user
    }

    fun logout() {
        _currentUser.value = null
    }

    // ==========================================
    // WALLET & FINANCIAL ENGINE
    // ==========================================

    fun observeWallet(userId: String): Flow<WalletEntity?> = db.walletDao().observeWalletByUserId(userId)
    fun observeTransactions(userId: String): Flow<List<WalletTransactionEntity>> = db.walletDao().getTransactionsByUserId(userId)
    fun observeAllTransactions(): Flow<List<WalletTransactionEntity>> = db.walletDao().getAllTransactions()

    // Dynamic Payment Methods (Synced real-time with DB)
    fun observeActivePaymentMethods(): Flow<List<PaymentMethodEntity>> = db.paymentMethodDao().getActivePaymentMethods()
    fun observeAllPaymentMethodsAdmin(): Flow<List<PaymentMethodEntity>> = db.paymentMethodDao().getAllPaymentMethods()

    suspend fun adminSavePaymentMethod(method: PaymentMethodEntity, adminId: String): Result<Unit> {
        val existing = db.paymentMethodDao().getPaymentMethodById(method.id)
        if (existing != null) {
            db.paymentMethodDao().updatePaymentMethod(method.copy(updatedAt = System.currentTimeMillis()))
            recordAuditLog(adminId, "Admin", "ADMIN", "UPDATE_PAYMENT_METHOD", "PAYMENT_METHOD", method.id, "Updated payment method: ${method.name}")
        } else {
            db.paymentMethodDao().insertPaymentMethod(method.copy(updatedAt = System.currentTimeMillis()))
            recordAuditLog(adminId, "Admin", "ADMIN", "CREATE_PAYMENT_METHOD", "PAYMENT_METHOD", method.id, "Created payment method: ${method.name}")
        }
        return Result.success(Unit)
    }

    suspend fun adminDeletePaymentMethod(id: String, adminId: String): Result<Unit> {
        db.paymentMethodDao().deletePaymentMethod(id)
        recordAuditLog(adminId, "Admin", "ADMIN", "DELETE_PAYMENT_METHOD", "PAYMENT_METHOD", id, "Deleted payment method ID: $id")
        return Result.success(Unit)
    }

    // Attached Payout Accounts (Bank Details / Card Details)
    fun observePayoutAccount(userId: String): Flow<UserPayoutAccountEntity?> = db.userPayoutAccountDao().getPayoutAccountByUserId(userId)
    suspend fun getPayoutAccountDirect(userId: String): UserPayoutAccountEntity? = db.userPayoutAccountDao().getPayoutAccountDirect(userId)

    suspend fun savePayoutAccount(account: UserPayoutAccountEntity): Result<Unit> {
        if (account.accountHolderName.isBlank() || account.bankOrIssuerName.isBlank() || account.accountOrCardNumber.isBlank()) {
            return Result.failure(Exception("Please fill in all required payout account details."))
        }
        db.userPayoutAccountDao().insertPayoutAccount(account.copy(updatedAt = System.currentTimeMillis()))
        return Result.success(Unit)
    }

    // ==========================================
    // KYC & IDENTITY DOCUMENT VERIFICATION
    // ==========================================
    fun observeKycDocument(userId: String): Flow<UserKycDocumentEntity?> = db.userKycDocumentDao().observeKycDocumentByUserId(userId)
    fun observeUserKycDocument(userId: String): Flow<UserKycDocumentEntity?> = observeKycDocument(userId)
    fun observeAllKycDocuments(): Flow<List<UserKycDocumentEntity>> = db.userKycDocumentDao().getAllKycDocuments()
    fun observeAllKycDocumentsAdmin(): Flow<List<UserKycDocumentEntity>> = observeAllKycDocuments()

    suspend fun submitKycDocument(
        userId: String,
        userName: String,
        userEmail: String,
        userPhone: String,
        documentType: String,
        documentNumber: String,
        issuingCountry: String,
        issuingStateOrProvince: String,
        expiryDate: String,
        dateOfBirth: String,
        residentialAddress: String,
        documentFrontImage: String = "",
        documentBackImage: String = "",
        selfieImage: String = "",
        frontImage: String = "",
        backImage: String = ""
    ): Result<UserKycDocumentEntity> {
        val effectiveFront = if (documentFrontImage.isNotBlank()) documentFrontImage else if (frontImage.isNotBlank()) frontImage else "front_preview"
        val effectiveBack = if (documentBackImage.isNotBlank()) documentBackImage else if (backImage.isNotBlank()) backImage else "back_preview"
        val kycId = "KYC-${System.currentTimeMillis() % 100000}"
        val doc = UserKycDocumentEntity(
            id = kycId,
            userId = userId,
            userName = userName,
            userEmail = userEmail,
            userPhone = userPhone,
            documentType = documentType,
            documentNumber = documentNumber,
            issuingCountry = issuingCountry,
            issuingStateOrProvince = issuingStateOrProvince,
            expiryDate = expiryDate,
            dateOfBirth = dateOfBirth,
            residentialAddress = residentialAddress,
            documentFrontImage = effectiveFront,
            documentBackImage = effectiveBack,
            selfieImage = if (selfieImage.isNotBlank()) selfieImage else "selfie_verified",
            verificationStatus = "PENDING",
            rejectionReason = "",
            adminNotes = "Submitted by user. Awaiting admin review.",
            submittedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        db.userKycDocumentDao().insertKycDocument(doc)
        db.userDao().updateUserVerification(userId, "PENDING")

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_kyc_$kycId",
                userId = userId,
                type = "APPROVAL",
                title = "Identity Documents Submitted",
                message = "Your $documentType ($documentNumber) has been submitted for admin verification. Wallet features will be unlocked upon approval.",
                read = false
            )
        )
        recordAuditLog(userId, userName, "CUSTOMER", "SUBMIT_KYC", "KYC_DOCUMENT", kycId, "Submitted $documentType for wallet verification")
        return Result.success(doc)
    }

    suspend fun reviewKycDocument(
        kycId: String,
        status: String,
        rejectionReason: String,
        adminNotes: String,
        adminId: String,
        adminName: String
    ): Result<Unit> {
        val doc = db.userKycDocumentDao().getKycDocumentById(kycId) ?: return Result.failure(Exception("KYC record not found"))
        val now = System.currentTimeMillis()
        db.userKycDocumentDao().reviewKycDocument(kycId, status, rejectionReason, adminNotes, adminId, now)
        db.userDao().updateUserVerification(doc.userId, status)

        val notifTitle = if (status == "VERIFIED") "Identity Verified Successfully! 🎉" else "Identity Verification Requires Attention"
        val notifMsg = if (status == "VERIFIED") {
            "Your ${doc.documentType} has been verified by administration. All wallet operations (Top-ups, POS credits, withdrawals, and payments) are now fully unlocked."
        } else {
            "Your identity document submission was rejected. Reason: $rejectionReason. Please resubmit your document."
        }

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_kyc_review_${System.currentTimeMillis() % 10000}",
                userId = doc.userId,
                type = "APPROVAL",
                title = notifTitle,
                message = notifMsg,
                read = false
            )
        )
        recordAuditLog(adminId, adminName, "ADMIN", "REVIEW_KYC", "KYC_DOCUMENT", kycId, "Reviewed KYC for ${doc.userName}: Status = $status")
        return Result.success(Unit)
    }

    suspend fun adminReviewKycDocument(
        docId: String,
        isApproved: Boolean,
        rejectionReason: String = "",
        adminNotes: String = "",
        adminId: String,
        posCreditAllotment: Double = 0.0
    ): Result<Unit> {
        val status = if (isApproved) "VERIFIED" else "REJECTED"
        val res = reviewKycDocument(
            kycId = docId,
            status = status,
            rejectionReason = rejectionReason,
            adminNotes = adminNotes,
            adminId = adminId,
            adminName = "Admin"
        )
        if (isApproved && posCreditAllotment > 0) {
            val doc = db.userKycDocumentDao().getKycDocumentById(docId)
            if (doc != null) {
                allotPosCredit(
                    userId = doc.userId,
                    grossAmount = posCreditAllotment,
                    posTerminalId = "POS-TERM-SF-01",
                    posLocation = "Servexa Downtown Station",
                    posAgentName = "Admin",
                    posAuthCode = "POS-KYC-AUTH-${(100000..999999).random()}",
                    notes = "POS Credit Allotment on KYC Approval ($$posCreditAllotment)",
                    adminId = adminId,
                    adminName = "Admin"
                )
            }
        }
        return res
    }

    suspend fun allotPosCredit(
        userId: String,
        grossAmount: Double,
        posTerminalId: String,
        posLocation: String,
        posAgentName: String,
        posAuthCode: String,
        notes: String,
        adminId: String,
        adminName: String
    ): Result<WalletTransactionEntity> {
        if (grossAmount <= 0) return Result.failure(Exception("Credit amount must be greater than zero"))
        val user = db.userDao().getUserById(userId) ?: return Result.failure(Exception("User not found"))
        val wallet = db.walletDao().getWalletByUserId(userId) ?: WalletEntity(
            id = "wall_$userId",
            userId = userId,
            availableBalance = 0.0,
            pendingBalance = 0.0
        ).also { db.walletDao().insertWallet(it) }

        val fee = 0.0
        val netAmount = grossAmount
        val txnId = "TXN-POS-${System.currentTimeMillis() % 100000}"

        val txn = WalletTransactionEntity(
            id = txnId,
            walletId = wallet.id,
            userId = userId,
            type = "POS_CREDIT_ALLOTMENT",
            grossAmount = grossAmount,
            fee = fee,
            netAmount = netAmount,
            status = "COMPLETED",
            referenceId = if (posAuthCode.isNotBlank()) posAuthCode else "POS-AUTH-${(100000..999999).random()}",
            note = if (notes.isNotBlank()) notes else "POS Credit Allotment via Terminal $posTerminalId",
            customerName = user.name,
            customerEmail = user.email,
            customerPhone = user.phone,
            customerAddress = "POS Station: $posLocation",
            merchantGatewayName = "Servexa POS Network Terminal",
            merchantAccountId = posTerminalId,
            merchantCaptureRef = "POS_TERM_CAP_${System.currentTimeMillis()}",
            captureStatus = "CAPTURED",
            paymentChannel = "POS_TERMINAL",
            posTerminalId = posTerminalId,
            posLocation = posLocation,
            posAgentName = posAgentName,
            posAuthCode = posAuthCode,
            createdAt = System.currentTimeMillis()
        )

        db.walletDao().insertTransaction(txn)
        db.walletDao().updateWallet(wallet.copy(availableBalance = wallet.availableBalance + netAmount, updatedAt = System.currentTimeMillis()))

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_pos_${txn.id}",
                userId = userId,
                type = "PAYMENT",
                title = "POS Credit Allotted: $${String.format(Locale.US, "%.2f", netAmount)}",
                message = "Your wallet has been credited via POS Terminal $posTerminalId at $posLocation.",
                read = false
            )
        )

        recordAuditLog(adminId, adminName, "ADMIN", "POS_CREDIT_ALLOT", "WALLET", wallet.id, "Allotted $${grossAmount} via POS Terminal $posTerminalId to ${user.name}")
        return Result.success(txn)
    }

    // ==========================================
    // MERCHANT GATEWAY & PAYMENT CAPTURE ENGINE
    // ==========================================

    fun observeMerchantGateways(): Flow<List<MerchantGatewayAccountEntity>> = db.merchantGatewayDao().getAllMerchantGateways()
    fun observeActiveMerchantGateways(): Flow<List<MerchantGatewayAccountEntity>> = db.merchantGatewayDao().getActiveMerchantGateways()

    suspend fun saveMerchantGateway(gateway: MerchantGatewayAccountEntity, adminId: String): Result<Unit> {
        if (gateway.name.isBlank() || gateway.merchantAccountId.isBlank()) {
            return Result.failure(Exception("Please provide a valid Merchant Name and Merchant Account ID."))
        }
        db.merchantGatewayDao().insertMerchantGateway(gateway.copy(updatedAt = System.currentTimeMillis()))
        recordAuditLog(
            adminId,
            "Admin",
            "ADMIN",
            "SAVE_MERCHANT_GATEWAY",
            "MERCHANT_GATEWAY",
            gateway.id,
            "Configured merchant account ${gateway.name} (${gateway.merchantAccountId}) with capture mode ${if (gateway.autoCapture) "Auto-Capture" else "Escrow Hold"}"
        )
        return Result.success(Unit)
    }

    suspend fun deleteMerchantGateway(id: String, adminId: String): Result<Unit> {
        db.merchantGatewayDao().deleteMerchantGateway(id)
        recordAuditLog(adminId, "Admin", "ADMIN", "DELETE_MERCHANT_GATEWAY", "MERCHANT_GATEWAY", id, "Deleted merchant gateway ID: $id")
        return Result.success(Unit)
    }

    suspend fun topUpWallet(
        userId: String,
        grossAmount: Double,
        paymentMethodName: String = "Card / Wire",
        referenceCode: String = ""
    ): Result<WalletTransactionEntity> {
        if (grossAmount <= 0) return Result.failure(Exception("Deposit amount must be greater than 0."))
        
        // Exact rule: 5% platform fee deducted from top-up amount
        val fee = (grossAmount * 0.05 * 100.0).roundToInt() / 100.0
        val netCredit = grossAmount - fee

        var wallet = db.walletDao().getWalletByUserId(userId)
        if (wallet == null) {
            wallet = WalletEntity(id = "wall_$userId", userId = userId, availableBalance = 0.0)
            db.walletDao().insertWallet(wallet)
        }

        // Fetch customer profile details to capture snapshot for Admin Panel
        val customer = db.userDao().getUserById(userId)
        val customerName = customer?.name ?: "Customer #$userId"
        val customerEmail = customer?.email ?: ""
        val customerPhone = customer?.phone ?: ""

        // Fetch active merchant gateway
        val defaultGateway = db.merchantGatewayDao().getDefaultMerchantGateway()
        val merchantName = defaultGateway?.name ?: "$paymentMethodName Gateway"
        val merchantAcc = defaultGateway?.merchantAccountId ?: "acct_servexa_primary"
        val autoRef = "ch_capt_${System.currentTimeMillis() % 10000000}"
        val finalMerchantRef = if (referenceCode.isNotBlank()) referenceCode.trim() else autoRef

        val refSuffix = if (referenceCode.isNotBlank()) " [Ref: ${referenceCode.trim()}]" else ""
        val txn = WalletTransactionEntity(
            id = "TXN-TOP-${System.currentTimeMillis() % 10000000}",
            walletId = wallet.id,
            userId = userId,
            type = "TOP_UP",
            grossAmount = grossAmount,
            fee = fee,
            netAmount = netCredit,
            status = "PENDING", // Requires Admin Approval
            note = "Top-up via $paymentMethodName: Gross $${"%.2f".format(grossAmount)} - 5% Fee ($${"%.2f".format(fee)}) = Net $${"%.2f".format(netCredit)}$refSuffix",
            customerName = customerName,
            customerEmail = customerEmail,
            customerPhone = customerPhone,
            customerAddress = "",
            merchantGatewayName = merchantName,
            merchantAccountId = merchantAcc,
            merchantCaptureRef = finalMerchantRef,
            captureStatus = "CAPTURED",
            createdAt = System.currentTimeMillis()
        )
        db.walletDao().insertTransaction(txn)

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_${txn.id}",
                userId = userId,
                type = "APPROVAL",
                title = "Top-Up Request Submitted",
                message = "Your deposit of $${"%.2f".format(grossAmount)} via $paymentMethodName was sent for Admin approval.",
                referenceId = txn.id
            )
        )

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_adm_top_${txn.id}",
                userId = "admin_root_1",
                type = "ADMIN_ALERT",
                title = "Pending Top-Up Request",
                message = "User $userId submitted a top-up of $${"%.2f".format(grossAmount)} via $paymentMethodName.",
                referenceId = txn.id
            )
        )

        return Result.success(txn)
    }

    suspend fun requestWithdrawal(userId: String, amount: Double): Result<WalletTransactionEntity> {
        val wallet = db.walletDao().getWalletByUserId(userId) ?: return Result.failure(Exception("Wallet not found."))
        if (amount <= 0) return Result.failure(Exception("Withdrawal amount must be greater than 0."))
        if (wallet.availableBalance < amount) return Result.failure(Exception("Insufficient available balance."))

        // Verify that user has attached bank / card details
        val payoutAccount = db.userPayoutAccountDao().getPayoutAccountDirect(userId)
            ?: return Result.failure(Exception("Please attach your Bank Account or Card details before requesting a withdrawal. Withdrawals are processed within 48 hours to your attached account."))

        val updatedWallet = wallet.copy(
            availableBalance = wallet.availableBalance - amount,
            pendingBalance = wallet.pendingBalance + amount,
            updatedAt = System.currentTimeMillis()
        )
        db.walletDao().updateWallet(updatedWallet)

        val accountMasked = if (payoutAccount.accountOrCardNumber.length >= 4) {
            "****${payoutAccount.accountOrCardNumber.takeLast(4)}"
        } else {
            payoutAccount.accountOrCardNumber
        }

        val txn = WalletTransactionEntity(
            id = "TXN-WTH-${System.currentTimeMillis() % 1000000}",
            walletId = wallet.id,
            userId = userId,
            type = "WITHDRAWAL",
            grossAmount = amount,
            fee = 0.0,
            netAmount = amount,
            status = "PENDING", // Sent to admin approval panel
            note = "Withdrawal to ${payoutAccount.bankOrIssuerName} (${payoutAccount.accountType}: $accountMasked, Holder: ${payoutAccount.accountHolderName}) - Processed in 48 hours"
        )
        db.walletDao().insertTransaction(txn)

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_wth_${txn.id}",
                userId = userId,
                type = "APPROVAL",
                title = "Withdrawal Submitted (48-Hour Processing)",
                message = "Your withdrawal request of $${"%.2f".format(amount)} has been submitted. It will be verified and disbursed to your ${payoutAccount.bankOrIssuerName} account within 48 hours.",
                referenceId = txn.id
            )
        )

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_adm_wth_${txn.id}",
                userId = "admin_root_1",
                type = "ADMIN_ALERT",
                title = "Pending Withdrawal Payout (48h)",
                message = "User $userId requested withdrawal of $${"%.2f".format(amount)} to ${payoutAccount.bankOrIssuerName}.",
                referenceId = txn.id
            )
        )

        return Result.success(txn)
    }

    // Admin Adjust Credits (Increase or Decrease Credits manually)
    suspend fun adminAdjustUserCredits(
        adminId: String,
        targetUserId: String,
        amountDelta: Double,
        reason: String
    ): Result<Unit> {
        val targetUser = db.userDao().getUserById(targetUserId)
            ?: return Result.failure(Exception("Target user not found."))

        var wallet = db.walletDao().getWalletByUserId(targetUserId)
        if (wallet == null) {
            wallet = WalletEntity(id = "wall_$targetUserId", userId = targetUserId, availableBalance = 0.0)
            db.walletDao().insertWallet(wallet)
        }

        val newBalance = wallet.availableBalance + amountDelta
        if (newBalance < 0) {
            return Result.failure(Exception("Cannot deduct $${"%.2f".format(-amountDelta)}. Current balance is only $${"%.2f".format(wallet.availableBalance)}."))
        }

        db.walletDao().updateWallet(
            wallet.copy(
                availableBalance = newBalance,
                updatedAt = System.currentTimeMillis()
            )
        )

        val isAddition = amountDelta >= 0
        val txnId = "TXN-ADJ-${System.currentTimeMillis() % 1000000}"
        db.walletDao().insertTransaction(
            WalletTransactionEntity(
                id = txnId,
                walletId = wallet.id,
                userId = targetUserId,
                type = "ADJUSTMENT",
                grossAmount = kotlin.math.abs(amountDelta),
                fee = 0.0,
                netAmount = amountDelta,
                status = "COMPLETED",
                note = "Admin Credit Adjustment: ${if (isAddition) "+$" else "-$"}${"%.2f".format(kotlin.math.abs(amountDelta))} by Admin ($reason)"
            )
        )

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_adj_$txnId",
                userId = targetUserId,
                type = "PAYMENT",
                title = if (isAddition) "Credits Added by Admin" else "Credits Deducted by Admin",
                message = "Your wallet was adjusted by ${if (isAddition) "+$" else "-$"}${"%.2f".format(kotlin.math.abs(amountDelta))}. Reason: $reason",
                referenceId = txnId
            )
        )

        recordAuditLog(
            adminId,
            "Admin",
            "ADMIN",
            "ADJUST_CREDITS",
            "WALLET",
            targetUserId,
            "Adjusted credits by $amountDelta for user ${targetUser.name} (${targetUser.email}). Reason: $reason"
        )

        return Result.success(Unit)
    }

    suspend fun adminApproveTransaction(txnId: String, adminId: String): Result<Unit> {
        val allTxns = db.walletDao().getAllTransactions().first()
        val txn = allTxns.find { it.id == txnId } ?: return Result.failure(Exception("Transaction not found."))
        if (txn.status != "PENDING") return Result.failure(Exception("Transaction is not pending."))

        val wallet = db.walletDao().getWalletByUserId(txn.userId) ?: return Result.failure(Exception("Wallet not found."))
        
        if (txn.type == "TOP_UP") {
            val updatedWallet = wallet.copy(
                availableBalance = wallet.availableBalance + txn.netAmount,
                updatedAt = System.currentTimeMillis()
            )
            db.walletDao().updateWallet(updatedWallet)
        } else if (txn.type == "WITHDRAWAL") {
            val updatedWallet = wallet.copy(
                pendingBalance = maxOf(0.0, wallet.pendingBalance - txn.netAmount),
                updatedAt = System.currentTimeMillis()
            )
            db.walletDao().updateWallet(updatedWallet)
        }

        val updatedTxn = txn.copy(status = "COMPLETED")
        db.walletDao().updateTransaction(updatedTxn)

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_appr_${txn.id}",
                userId = txn.userId,
                type = "APPROVAL",
                title = if (txn.type == "TOP_UP") "Top-Up Approved!" else "Withdrawal Approved",
                message = if (txn.type == "TOP_UP")
                    "Your deposit of $${"%.2f".format(txn.grossAmount)} has been approved. $${"%.2f".format(txn.netAmount)} credited to your wallet balance."
                else
                    "Your withdrawal of $${"%.2f".format(txn.netAmount)} has been approved and processed.",
                referenceId = txn.id
            )
        )

        recordAuditLog(adminId, "Admin", "ADMIN", "APPROVE_TRANSACTION", "WALLET_TRANSACTION", txnId, "Approved ${txn.type} of $${txn.netAmount}")
        return Result.success(Unit)
    }

    suspend fun adminRejectTransaction(txnId: String, adminId: String, reason: String): Result<Unit> {
        val allTxns = db.walletDao().getAllTransactions().first()
        val txn = allTxns.find { it.id == txnId } ?: return Result.failure(Exception("Transaction not found."))
        if (txn.status != "PENDING") return Result.failure(Exception("Transaction is not pending."))

        val wallet = db.walletDao().getWalletByUserId(txn.userId) ?: return Result.failure(Exception("Wallet not found."))

        if (txn.type == "WITHDRAWAL") {
            val updatedWallet = wallet.copy(
                availableBalance = wallet.availableBalance + txn.netAmount,
                pendingBalance = maxOf(0.0, wallet.pendingBalance - txn.netAmount),
                updatedAt = System.currentTimeMillis()
            )
            db.walletDao().updateWallet(updatedWallet)
        }

        val updatedTxn = txn.copy(status = "REJECTED", note = "${txn.note} [Rejected: $reason]")
        db.walletDao().updateTransaction(updatedTxn)

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_rej_${txn.id}",
                userId = txn.userId,
                type = "APPROVAL",
                title = if (txn.type == "TOP_UP") "Top-Up Request Rejected" else "Withdrawal Rejected",
                message = "${txn.type} request was rejected: $reason.",
                referenceId = txn.id
            )
        )

        recordAuditLog(adminId, "Admin", "ADMIN", "REJECT_TRANSACTION", "WALLET_TRANSACTION", txnId, "Rejected ${txn.type}: $reason")
        return Result.success(Unit)
    }

    // ==========================================
    // SEARCH & DISCOVERY ENGINE
    // ==========================================

    fun observeCategories(): Flow<List<CategoryEntity>> = db.categoryDao().getActiveCategories()
    fun observeAllCategoriesAdmin(): Flow<List<CategoryEntity>> = db.categoryDao().getAllCategories()
    fun observeAllProviders(): Flow<List<Pair<UserEntity, ProviderProfileEntity>>> = 
        combine(db.userDao().getAllUsers(), db.providerProfileDao().getAllProfiles()) { users, profiles ->
            val providerUsers = users.filter { it.role == "PROVIDER" }
            providerUsers.mapNotNull { user ->
                val profile = profiles.find { it.userId == user.id }
                if (profile != null) Pair(user, profile) else null
            }
        }

    fun observeServicesForProvider(providerId: String): Flow<List<ServiceEntity>> = db.serviceDao().getServicesByProvider(providerId)
    fun observeAllActiveServices(): Flow<List<ServiceEntity>> = db.serviceDao().getAllActiveServices()

    // Geo-distance calculator (Haversine formula)
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distance = r * c
        return if (distance < 0.1) 0.5 else (distance * 10.0).roundToInt() / 10.0
    }

    // Smart natural-language search with nearest provider ranking
    fun searchProviders(
        query: String,
        selectedCategoryId: String? = null,
        minRating: Double = 0.0,
        maxPrice: Double = 1000.0,
        verifiedOnly: Boolean = false,
        sortBy: String = "DISTANCE", // "DISTANCE", "RATING", "PRICE", "COMPLETED"
        userLat: Double = 37.7749,
        userLng: Double = -122.4194
    ): Flow<List<ProviderSearchResult>> {
        return combine(
            db.userDao().getAllUsers(),
            db.providerProfileDao().getAllProfiles(),
            db.serviceDao().getAllActiveServices(),
            db.categoryDao().getAllCategories()
        ) { users, profiles, services, categories ->
            val q = query.trim().lowercase(Locale.ROOT)

            // Category keyword expansion
            val matchedCategoryIds = categories.filter { cat ->
                cat.name.lowercase().contains(q) ||
                cat.description.lowercase().contains(q) ||
                (q.contains("ac") && cat.id == "cat_hvac") ||
                (q.contains("fix") && q.contains("air") && cat.id == "cat_hvac") ||
                (q.contains("electric") && cat.id == "cat_elec") ||
                (q.contains("wire") && cat.id == "cat_elec") ||
                (q.contains("pipe") && cat.id == "cat_plumb") ||
                (q.contains("leak") && cat.id == "cat_plumb") ||
                (q.contains("plumb") && cat.id == "cat_plumb") ||
                (q.contains("wood") && cat.id == "cat_carp") ||
                (q.contains("door") && cat.id == "cat_carp") ||
                (q.contains("clean") && cat.id == "cat_clean") ||
                (q.contains("paint") && cat.id == "cat_paint") ||
                (q.contains("car") && cat.id == "cat_auto") ||
                (q.contains("brake") && cat.id == "cat_auto") ||
                (q.contains("food") && cat.id == "cat_restaurant") ||
                (q.contains("dine") && cat.id == "cat_restaurant") ||
                (q.contains("restaur") && cat.id == "cat_restaurant") ||
                (q.contains("chef") && cat.id == "cat_restaurant") ||
                (q.contains("hotel") && cat.id == "cat_hotels") ||
                (q.contains("stay") && cat.id == "cat_hotels") ||
                (q.contains("room") && cat.id == "cat_hotels") ||
                (q.contains("suite") && cat.id == "cat_hotels") ||
                (q.contains("taxi") && (cat.id == "cat_taxis" || cat.id == "cat_userride")) ||
                (q.contains("cab") && (cat.id == "cat_taxis" || cat.id == "cat_userride")) ||
                (q.contains("ride") && (cat.id == "cat_taxis" || cat.id == "cat_bikes" || cat.id == "cat_userride")) ||
                (q.contains("carpool") && cat.id == "cat_userride") ||
                (q.contains("bike") && cat.id == "cat_bikes") ||
                (q.contains("moto") && cat.id == "cat_bikes") ||
                (q.contains("scooter") && cat.id == "cat_bikes") ||
                (q.contains("buy") && cat.id == "cat_buysell") ||
                (q.contains("sell") && cat.id == "cat_buysell") ||
                (q.contains("shop") && cat.id == "cat_buysell") ||
                (q.contains("it") && cat.id == "cat_itspecialist") ||
                (q.contains("tech") && cat.id == "cat_itspecialist") ||
                (q.contains("code") && cat.id == "cat_itspecialist") ||
                (q.contains("software") && cat.id == "cat_itspecialist") ||
                (q.contains("computer") && cat.id == "cat_itspecialist") ||
                (q.contains("pc") && cat.id == "cat_itspecialist") ||
                (q.contains("wifi") && cat.id == "cat_itspecialist") ||
                (q.contains("doctor") && cat.id == "cat_doctors") ||
                (q.contains("health") && cat.id == "cat_doctors") ||
                (q.contains("clinic") && cat.id == "cat_doctors") ||
                (q.contains("physician") && cat.id == "cat_doctors") ||
                (q.contains("medicine") && cat.id == "cat_doctors") ||
                (q.contains("vet") && cat.id == "cat_veterinary") ||
                (q.contains("animal") && cat.id == "cat_veterinary") ||
                (q.contains("pet") && cat.id == "cat_veterinary") ||
                (q.contains("dog") && cat.id == "cat_veterinary") ||
                (q.contains("cat") && cat.id == "cat_veterinary") ||
                (q.contains("hair") && cat.id == "cat_beauty") ||
                (q.contains("salon") && cat.id == "cat_beauty") ||
                (q.contains("beauty") && cat.id == "cat_beauty") ||
                (q.contains("spa") && cat.id == "cat_beauty") ||
                (q.contains("mov") && cat.id == "cat_moving") ||
                (q.contains("pack") && cat.id == "cat_moving") ||
                (q.contains("haul") && cat.id == "cat_moving") ||
                (q.contains("roof") && cat.id == "cat_roofing") ||
                (q.contains("lock") && cat.id == "cat_locksmith") ||
                (q.contains("key") && cat.id == "cat_locksmith") ||
                (q.contains("pest") && cat.id == "cat_pest") ||
                (q.contains("bug") && cat.id == "cat_pest") ||
                (q.contains("laundry") && cat.id == "cat_laundry") ||
                (q.contains("wash") && (cat.id == "cat_laundry" || cat.id == "cat_clean")) ||
                (q.contains("tutor") && cat.id == "cat_tutoring") ||
                (q.contains("teach") && cat.id == "cat_tutoring") ||
                (q.contains("lesson") && cat.id == "cat_tutoring") ||
                (q.contains("photo") && cat.id == "cat_photo") ||
                (q.contains("camera") && cat.id == "cat_photo") ||
                (q.contains("event") && cat.id == "cat_event") ||
                (q.contains("party") && cat.id == "cat_event") ||
                (q.contains("law") && cat.id == "cat_legal") ||
                (q.contains("legal") && cat.id == "cat_legal") ||
                (q.contains("solar") && cat.id == "cat_solar") ||
                (q.contains("baby") && cat.id == "cat_childcare") ||
                (q.contains("nanny") && cat.id == "cat_childcare") ||
                (q.contains("child") && cat.id == "cat_childcare")
            }.map { it.id }.toSet()

            val providerUsers = users.filter { it.role == "PROVIDER" && it.status == "ACTIVE" }

            val results = providerUsers.mapNotNull { user ->
                val profile = profiles.find { it.userId == user.id } ?: return@mapNotNull null
                val userServices = services.filter { it.providerId == user.id }
                val startingPrice = userServices.minOfOrNull { it.price } ?: 60.0

                // Filter checks
                if (selectedCategoryId != null && userServices.none { it.categoryId == selectedCategoryId }) {
                    return@mapNotNull null
                }
                if (profile.rating < minRating) return@mapNotNull null
                if (startingPrice > maxPrice) return@mapNotNull null
                if (verifiedOnly && profile.verificationStatus != "VERIFIED") return@mapNotNull null

                // Search query matching
                val matchesQuery = q.isEmpty() ||
                        user.name.lowercase().contains(q) ||
                        profile.title.lowercase().contains(q) ||
                        profile.bio.lowercase().contains(q) ||
                        profile.locationName.lowercase().contains(q) ||
                        userServices.any { srv ->
                            srv.title.lowercase().contains(q) ||
                            srv.description.lowercase().contains(q) ||
                            matchedCategoryIds.contains(srv.categoryId)
                        } ||
                        userServices.any { matchedCategoryIds.contains(it.categoryId) }

                if (!matchesQuery) return@mapNotNull null

                val primaryCategory = categories.find { cat ->
                    userServices.any { it.categoryId == cat.id }
                }

                // Compute real-time distance from user's live coordinates to provider's location
                val dist = calculateDistanceKm(userLat, userLng, profile.latitude, profile.longitude)

                ProviderSearchResult(
                    user = user,
                    profile = profile,
                    services = userServices,
                    categoryName = primaryCategory?.name ?: "Professional Services",
                    startingPrice = startingPrice,
                    estimatedDistanceKm = dist
                )
            }

            when (sortBy) {
                "DISTANCE" -> results.sortedBy { it.estimatedDistanceKm }
                "RATING" -> results.sortedByDescending { it.profile.rating }
                "PRICE" -> results.sortedBy { it.startingPrice }
                "COMPLETED" -> results.sortedByDescending { it.profile.completedJobs }
                else -> results.sortedBy { it.estimatedDistanceKm }
            }
        }
    }

    // ==========================================
    // BOOKING LIFECYCLE & 6% SERVICE COMMISSION
    // ==========================================

    fun observeBookingsForCustomer(customerId: String): Flow<List<BookingEntity>> = db.bookingDao().getBookingsByCustomer(customerId)
    fun observeBookingsForProvider(providerId: String): Flow<List<BookingEntity>> = db.bookingDao().getBookingsByProvider(providerId)
    fun observeAllBookingsAdmin(): Flow<List<BookingEntity>> = db.bookingDao().getAllBookings()
    fun observeBookingById(bookingId: String): Flow<BookingEntity?> = db.bookingDao().observeBookingById(bookingId)
    fun observeStatusHistory(bookingId: String): Flow<List<BookingStatusHistoryEntity>> = db.bookingDao().getStatusHistory(bookingId)

    suspend fun createBooking(
        customerId: String,
        providerId: String,
        serviceId: String,
        address: String,
        scheduledAt: Long,
        problemDescription: String,
        specialInstructions: String
    ): Result<BookingEntity> {
        val service = db.serviceDao().getServiceById(serviceId) ?: return Result.failure(Exception("Service not found."))
        val customer = db.userDao().getUserById(customerId) ?: return Result.failure(Exception("Customer not found."))
        val provider = db.userDao().getUserById(providerId) ?: return Result.failure(Exception("Provider not found."))
        val customerWallet = db.walletDao().getWalletByUserId(customerId) ?: return Result.failure(Exception("Customer wallet not found."))

        val price = service.price
        if (customerWallet.availableBalance < price) {
            return Result.failure(Exception("Insufficient wallet balance ($${"%.2f".format(customerWallet.availableBalance)}). Please top up at least $${"%.2f".format(price - customerWallet.availableBalance)} to proceed."))
        }

        // Exact Rule: 6% Platform commission deducted from completed service
        val commission = (price * 0.06 * 100.0).roundToInt() / 100.0
        val providerNetAmount = price - commission

        // Deduct payment from customer wallet (Held in escrow/service reservation)
        val updatedCustomerWallet = customerWallet.copy(
            availableBalance = customerWallet.availableBalance - price,
            updatedAt = System.currentTimeMillis()
        )
        db.walletDao().updateWallet(updatedCustomerWallet)

        val bookingId = "SVX-2026-${(System.currentTimeMillis() % 900000 + 100000)}"

        val booking = BookingEntity(
            id = bookingId,
            customerId = customerId,
            providerId = providerId,
            serviceId = serviceId,
            serviceTitle = service.title,
            address = address.trim(),
            scheduledAt = scheduledAt,
            status = "REQUESTED",
            price = price,
            platformCommission = commission,
            providerNetAmount = providerNetAmount,
            problemDescription = problemDescription.trim(),
            specialInstructions = specialInstructions.trim(),
            createdAt = System.currentTimeMillis()
        )
        db.bookingDao().insertBooking(booking)

        // Log payment transaction
        val defGateway = db.merchantGatewayDao().getDefaultMerchantGateway()
        db.walletDao().insertTransaction(
            WalletTransactionEntity(
                id = "TXN-PAY-${bookingId.substring(4)}",
                walletId = customerWallet.id,
                userId = customerId,
                type = "SERVICE_PAYMENT",
                grossAmount = price,
                fee = 0.0,
                netAmount = price,
                status = "COMPLETED",
                referenceId = bookingId,
                note = "Service Reservation for: ${service.title} (Booking #$bookingId)",
                customerName = customer.name,
                customerEmail = customer.email,
                customerPhone = customer.phone,
                customerAddress = address,
                merchantGatewayName = defGateway?.name ?: "Servexa Escrow Gateway",
                merchantAccountId = defGateway?.merchantAccountId ?: "acct_servexa_primary",
                merchantCaptureRef = "ch_escrow_${bookingId.substring(4)}",
                captureStatus = "ESCROW_HELD",
                createdAt = System.currentTimeMillis()
            )
        )

        // Status history
        db.bookingDao().insertStatusHistory(
            BookingStatusHistoryEntity(
                bookingId = bookingId,
                previousStatus = "NONE",
                newStatus = "REQUESTED",
                actorId = customerId,
                actorRole = "CUSTOMER",
                reason = "Customer initiated booking request."
            )
        )

        // Notifications
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_book_prov_${bookingId}",
                userId = providerId,
                type = "BOOKING_UPDATE",
                title = "New Service Request!",
                message = "${customer.name} requested '${service.title}' ($${"%.2f".format(price)}).",
                referenceId = bookingId
            )
        )
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_book_cust_${bookingId}",
                userId = customerId,
                type = "BOOKING_UPDATE",
                title = "Booking Requested: $bookingId",
                message = "Your request for '${service.title}' was sent to ${provider.name}.",
                referenceId = bookingId
            )
        )

        return Result.success(booking)
    }

    suspend fun updateBookingStatus(
        bookingId: String,
        newStatus: String,
        actorId: String,
        actorRole: String,
        reason: String = ""
    ): Result<BookingEntity> {
        val booking = db.bookingDao().getBookingById(bookingId) ?: return Result.failure(Exception("Booking not found."))
        val prevStatus = booking.status

        var completedTime = booking.completedAt
        if (newStatus == "COMPLETED") {
            completedTime = System.currentTimeMillis()

            // 6% Platform Commission is finalized, 94% net earnings credited to provider wallet
            var providerWallet = db.walletDao().getWalletByUserId(booking.providerId)
            if (providerWallet == null) {
                providerWallet = WalletEntity(id = "wall_${booking.providerId}", userId = booking.providerId, availableBalance = 0.0)
                db.walletDao().insertWallet(providerWallet)
            }

            val updatedProviderWallet = providerWallet.copy(
                availableBalance = providerWallet.availableBalance + booking.providerNetAmount,
                updatedAt = System.currentTimeMillis()
            )
            db.walletDao().updateWallet(updatedProviderWallet)

            // Provider earnings transaction (Net 94%, Fee 6%)
            db.walletDao().insertTransaction(
                WalletTransactionEntity(
                    id = "TXN-EARN-${bookingId.substring(4)}",
                    walletId = providerWallet.id,
                    userId = booking.providerId,
                    type = "SERVICE_EARNING",
                    grossAmount = booking.price,
                    fee = booking.platformCommission, // 6% Servexa Commission
                    netAmount = booking.providerNetAmount,
                    status = "COMPLETED",
                    referenceId = bookingId,
                    note = "Earnings for completed job: Gross $${"%.2f".format(booking.price)} - 6% Servexa Commission ($${"%.2f".format(booking.platformCommission)}) = Net $${"%.2f".format(booking.providerNetAmount)}"
                )
            )

            // Increment completed jobs in provider profile
            val profile = db.providerProfileDao().getProfileByUserId(booking.providerId)
            if (profile != null) {
                db.providerProfileDao().updateProfile(profile.copy(completedJobs = profile.completedJobs + 1))
            }

            // Customer notification to leave a review
            db.notificationDao().insertNotification(
                NotificationEntity(
                    id = "notif_rev_prompt_$bookingId",
                    userId = booking.customerId,
                    type = "BOOKING_UPDATE",
                    title = "Service Completed!",
                    message = "Your service '${booking.serviceTitle}' is complete. Please rate and review your experience.",
                    referenceId = bookingId
                )
            )
        } else if (newStatus == "REJECTED" || newStatus == "CANCELLED" || newStatus == "REFUNDED") {
            // Refund the customer's wallet
            val customerWallet = db.walletDao().getWalletByUserId(booking.customerId)
            if (customerWallet != null) {
                val updatedWallet = customerWallet.copy(
                    availableBalance = customerWallet.availableBalance + booking.price,
                    updatedAt = System.currentTimeMillis()
                )
                db.walletDao().updateWallet(updatedWallet)

                db.walletDao().insertTransaction(
                    WalletTransactionEntity(
                        id = "TXN-REF-${bookingId.substring(4)}",
                        walletId = customerWallet.id,
                        userId = booking.customerId,
                        type = "REFUND",
                        grossAmount = booking.price,
                        fee = 0.0,
                        netAmount = booking.price,
                        status = "COMPLETED",
                        referenceId = bookingId,
                        note = "Full Refund for $newStatus booking #$bookingId"
                    )
                )
            }
        }

        val updatedBooking = booking.copy(
            status = newStatus,
            completedAt = completedTime
        )
        db.bookingDao().updateBooking(updatedBooking)

        // Insert history entry
        db.bookingDao().insertStatusHistory(
            BookingStatusHistoryEntity(
                bookingId = bookingId,
                previousStatus = prevStatus,
                newStatus = newStatus,
                actorId = actorId,
                actorRole = actorRole,
                reason = reason
            )
        )

        // Real-time notifications
        val targetNotifyUser = if (actorRole == "PROVIDER") booking.customerId else booking.providerId
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_status_${bookingId}_${System.currentTimeMillis() % 10000}",
                userId = targetNotifyUser,
                type = "BOOKING_UPDATE",
                title = "Booking Status Updated",
                message = "Booking #$bookingId is now: ${newStatus.replace("_", " ")}.",
                referenceId = bookingId
            )
        )

        return Result.success(updatedBooking)
    }

    // ==========================================
    // REVIEWS & RATINGS (One review per completed booking)
    // ==========================================

    fun observeReviewsForProvider(providerId: String): Flow<List<ReviewEntity>> = db.reviewDao().getReviewsForProvider(providerId)
    fun observeAllReviewsAdmin(): Flow<List<ReviewEntity>> = db.reviewDao().getAllReviews()
    suspend fun getReviewForBooking(bookingId: String): ReviewEntity? = db.reviewDao().getReviewForBooking(bookingId)

    suspend fun submitReview(
        bookingId: String,
        customerId: String,
        rating: Double,
        reviewText: String
    ): Result<ReviewEntity> {
        val booking = db.bookingDao().getBookingById(bookingId) ?: return Result.failure(Exception("Booking not found."))
        if (booking.status != "COMPLETED") return Result.failure(Exception("Reviews are only permitted on completed services."))
        
        val existing = db.reviewDao().getReviewForBooking(bookingId)
        if (existing != null) return Result.failure(Exception("You have already submitted a review for this booking."))

        val customer = db.userDao().getUserById(customerId) ?: return Result.failure(Exception("Customer not found."))

        val review = ReviewEntity(
            id = "REV-${bookingId.substring(4)}",
            bookingId = bookingId,
            customerId = customerId,
            customerName = customer.name,
            customerAvatar = customer.profileImage,
            providerId = booking.providerId,
            rating = rating.coerceIn(1.0, 5.0),
            reviewText = reviewText.trim(),
            createdAt = System.currentTimeMillis()
        )
        db.reviewDao().insertReview(review)

        // Recalculate provider aggregate rating and review count
        val allProviderReviews = db.reviewDao().getReviewsForProvider(booking.providerId).first()
        val totalReviews = allProviderReviews.size
        val avgRating = if (totalReviews > 0) {
            allProviderReviews.map { it.rating }.average()
        } else rating

        val profile = db.providerProfileDao().getProfileByUserId(booking.providerId)
        if (profile != null) {
            val roundedAvg = (avgRating * 100.0).roundToInt() / 100.0
            db.providerProfileDao().updateProfile(
                profile.copy(
                    rating = roundedAvg,
                    reviewCount = totalReviews
                )
            )
        }

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_rev_${review.id}",
                userId = booking.providerId,
                type = "REVIEW",
                title = "New Review Received!",
                message = "${customer.name} left a ${"%.1f".format(rating)}★ review for your service.",
                referenceId = bookingId
            )
        )

        return Result.success(review)
    }

    suspend fun providerReplyToReview(reviewId: String, replyText: String): Result<Unit> {
        val allReviews = db.reviewDao().getAllReviews().first()
        val rev = allReviews.find { it.id == reviewId } ?: return Result.failure(Exception("Review not found."))
        db.reviewDao().updateReview(rev.copy(providerReply = replyText.trim()))
        return Result.success(Unit)
    }

    // ==========================================
    // SERVEXA SECURE CALLING ENGINE
    // ==========================================

    fun observeCallLogsForUser(userId: String): Flow<List<CallLogEntity>> = db.callLogDao().getCallsForUser(userId)
    fun observeAllCallLogsAdmin(): Flow<List<CallLogEntity>> = db.callLogDao().getAllCalls()

    fun initiateSecureCall(
        bookingId: String,
        caller: UserEntity,
        receiverId: String,
        receiverName: String,
        receiverRole: String
    ) {
        val callId = "CALL-SVX-${System.currentTimeMillis() % 1000000}"
        _activeCallState.value = ActiveCallSession(
            callId = callId,
            bookingId = bookingId,
            callerId = caller.id,
            callerName = caller.name,
            callerRole = caller.role,
            receiverId = receiverId,
            receiverName = receiverName,
            receiverRole = receiverRole,
            state = "CONNECTING",
            startTime = System.currentTimeMillis()
        )
    }

    fun acceptCall() {
        _activeCallState.value?.let { current ->
            _activeCallState.value = current.copy(
                state = "CONNECTED",
                answerTime = System.currentTimeMillis()
            )
        }
    }

    suspend fun endCall(status: String = "COMPLETED") {
        val current = _activeCallState.value ?: return
        val endTime = System.currentTimeMillis()
        val duration = if (current.answerTime != null && current.state == "CONNECTED") {
            (endTime - current.answerTime) / 1000
        } else {
            0
        }

        val log = CallLogEntity(
            id = current.callId,
            bookingId = current.bookingId,
            callerId = current.callerId,
            callerName = current.callerName,
            callerRole = current.callerRole,
            receiverId = current.receiverId,
            receiverName = current.receiverName,
            receiverRole = current.receiverRole,
            startTime = current.startTime,
            answerTime = current.answerTime,
            endTime = endTime,
            durationSeconds = duration,
            status = if (duration > 0) "COMPLETED" else status,
            direction = "OUTGOING"
        )
        db.callLogDao().insertCall(log)

        _activeCallState.value = null
    }

    // ==========================================
    // WORK VIDEOS & COMMUNITY
    // ==========================================

    fun observeActiveVideos(): Flow<List<WorkVideoEntity>> = db.workVideoDao().getActiveVideos()
    fun observeVideosForProvider(providerId: String): Flow<List<WorkVideoEntity>> = db.workVideoDao().getVideosByProvider(providerId)
    fun observeAllVideosAdmin(): Flow<List<WorkVideoEntity>> = db.workVideoDao().getAllVideos()
    fun observeCommentsForVideo(videoId: String): Flow<List<VideoCommentEntity>> = db.workVideoDao().getCommentsForVideo(videoId)
    fun observeUserLikes(userId: String): Flow<List<VideoLikeEntity>> = db.workVideoDao().getUserLikes(userId)

    suspend fun toggleVideoLike(videoId: String, userId: String): Result<Boolean> {
        val existingLike = db.workVideoDao().getLike(videoId, userId)
        val allVids = db.workVideoDao().getAllVideos().first()
        val video = allVids.find { it.id == videoId } ?: return Result.failure(Exception("Video not found."))

        return if (existingLike != null) {
            db.workVideoDao().deleteLike(videoId, userId)
            db.workVideoDao().updateVideo(video.copy(likesCount = maxOf(0, video.likesCount - 1)))
            Result.success(false)
        } else {
            db.workVideoDao().insertLike(VideoLikeEntity(id = "${videoId}_$userId", videoId = videoId, userId = userId))
            db.workVideoDao().updateVideo(video.copy(likesCount = video.likesCount + 1))
            Result.success(true)
        }
    }

    suspend fun addVideoComment(videoId: String, userId: String, commentText: String): Result<VideoCommentEntity> {
        val user = db.userDao().getUserById(userId) ?: return Result.failure(Exception("User not found."))
        val allVids = db.workVideoDao().getAllVideos().first()
        val video = allVids.find { it.id == videoId } ?: return Result.failure(Exception("Video not found."))

        val comment = VideoCommentEntity(
            id = "COMM-${System.currentTimeMillis() % 1000000}",
            videoId = videoId,
            userId = userId,
            userName = user.name,
            userAvatar = user.profileImage,
            comment = commentText.trim()
        )
        db.workVideoDao().insertComment(comment)
        db.workVideoDao().updateVideo(video.copy(commentsCount = video.commentsCount + 1))

        return Result.success(comment)
    }

    suspend fun uploadWorkVideo(
        providerId: String,
        title: String,
        description: String,
        category: String
    ): Result<WorkVideoEntity> {
        val provider = db.userDao().getUserById(providerId) ?: return Result.failure(Exception("Provider not found."))
        val video = WorkVideoEntity(
            id = "VID-${System.currentTimeMillis() % 1000000}",
            providerId = providerId,
            providerName = provider.name,
            providerAvatar = provider.profileImage,
            title = title.trim(),
            description = description.trim(),
            category = category.trim(),
            likesCount = 0,
            commentsCount = 0,
            viewsCount = 1
        )
        db.workVideoDao().insertVideo(video)
        return Result.success(video)
    }

    // ==========================================
    // PRODUCT MARKETPLACE & SHOPPING CART
    // ==========================================

    fun observeProducts(): Flow<List<ProductEntity>> = db.productDao().getAllProducts()
    fun observeProductsBySeller(sellerId: String): Flow<List<ProductEntity>> = db.productDao().getProductsBySeller(sellerId)
    fun observeProductsByCategory(categoryId: String): Flow<List<ProductEntity>> = db.productDao().getProductsByCategory(categoryId)

    suspend fun addProduct(
        sellerId: String,
        categoryId: String,
        name: String,
        description: String,
        price: Double,
        inventory: Int = 10,
        imageUrl: String = "https://images.unsplash.com/photo-1581244277943-fe4a9c77d389?w=400"
    ): Result<ProductEntity> {
        val prodId = "PROD-${System.currentTimeMillis() % 1000000}"
        val product = ProductEntity(
            id = prodId,
            sellerId = sellerId,
            categoryId = categoryId,
            name = name.trim(),
            description = description.trim(),
            price = price,
            inventory = inventory,
            imageUrl = imageUrl,
            rating = 4.8,
            reviewCount = 1,
            active = true
        )
        db.productDao().insertProduct(product)
        return Result.success(product)
    }

    fun observeCart(userId: String): Flow<List<Pair<CartItemEntity, ProductEntity>>> =
        combine(db.cartDao().getCartItems(userId), db.productDao().getAllProducts()) { cartItems, products ->
            cartItems.mapNotNull { item ->
                val product = products.find { it.id == item.productId }
                if (product != null) Pair(item, product) else null
            }
        }

    fun observeOrdersForCustomer(customerId: String): Flow<List<OrderEntity>> = db.orderDao().getOrdersByCustomer(customerId)

    suspend fun addToCart(userId: String, productId: String) {
        val existing = db.cartDao().getCartItem(userId, productId)
        if (existing != null) {
            db.cartDao().updateCartItem(existing.copy(quantity = existing.quantity + 1))
        } else {
            db.cartDao().insertCartItem(CartItemEntity(userId = userId, productId = productId, quantity = 1))
        }
    }

    suspend fun updateCartQuantity(cartItemId: Long, quantity: Int) {
        if (quantity <= 0) {
            db.cartDao().deleteCartItem(cartItemId)
        } else {
            val allItems = db.cartDao().getCartItems(_currentUser.value?.id ?: "").first()
            val item = allItems.find { it.id == cartItemId } ?: return
            db.cartDao().updateCartItem(item.copy(quantity = quantity))
        }
    }

    suspend fun removeFromCart(cartItemId: Long) {
        db.cartDao().deleteCartItem(cartItemId)
    }

    suspend fun checkoutCart(userId: String, shippingAddress: String): Result<OrderEntity> {
        val cartPairs = observeCart(userId).first()
        if (cartPairs.isEmpty()) return Result.failure(Exception("Your shopping cart is empty."))

        val totalAmount = cartPairs.sumOf { it.first.quantity * it.second.price }
        val wallet = db.walletDao().getWalletByUserId(userId) ?: return Result.failure(Exception("Wallet not found."))

        if (wallet.availableBalance < totalAmount) {
            return Result.failure(Exception("Insufficient wallet balance ($${"%.2f".format(wallet.availableBalance)}). Required: $${"%.2f".format(totalAmount)}."))
        }

        // Deduct from wallet
        val updatedWallet = wallet.copy(
            availableBalance = wallet.availableBalance - totalAmount,
            updatedAt = System.currentTimeMillis()
        )
        db.walletDao().updateWallet(updatedWallet)

        val orderId = "ORD-SVX-${System.currentTimeMillis() % 1000000}"
        val order = OrderEntity(
            id = orderId,
            customerId = userId,
            totalAmount = totalAmount,
            status = "PAID",
            paymentStatus = "PAID",
            shippingAddress = shippingAddress.trim()
        )
        db.orderDao().insertOrder(order)

        val orderItems = cartPairs.map { (cartItem, product) ->
            OrderItemEntity(
                orderId = orderId,
                productId = product.id,
                productName = product.name,
                productPrice = product.price,
                quantity = cartItem.quantity
            )
        }
        db.orderDao().insertOrderItems(orderItems)

        // Clear cart
        db.cartDao().clearCart(userId)

        // Log transaction
        val user = db.userDao().getUserById(userId)
        val defGw = db.merchantGatewayDao().getDefaultMerchantGateway()
        db.walletDao().insertTransaction(
            WalletTransactionEntity(
                id = "TXN-ORD-${orderId.substring(8)}",
                walletId = wallet.id,
                userId = userId,
                type = "SERVICE_PAYMENT",
                grossAmount = totalAmount,
                fee = 0.0,
                netAmount = totalAmount,
                status = "COMPLETED",
                referenceId = orderId,
                note = "Physical Marketplace Order #$orderId",
                customerName = user?.name ?: "Customer #$userId",
                customerEmail = user?.email ?: "",
                customerPhone = user?.phone ?: "",
                customerAddress = shippingAddress,
                merchantGatewayName = defGw?.name ?: "Servexa Direct Merchant",
                merchantAccountId = defGw?.merchantAccountId ?: "acct_servexa_primary",
                merchantCaptureRef = "ch_store_${orderId.substring(8)}",
                captureStatus = "CAPTURED",
                createdAt = System.currentTimeMillis()
            )
        )

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_ord_$orderId",
                userId = userId,
                type = "PAYMENT",
                title = "Order Confirmed: #$orderId",
                message = "Your order of $${"%.2f".format(totalAmount)} has been placed and is being prepared for shipping.",
                referenceId = orderId
            )
        )

        return Result.success(order)
    }

    // ==========================================
    // REAL-TIME GPS & LIVE LOCATION TRACKING
    // ==========================================

    fun observeBookingLiveLocation(bookingId: String): Flow<LocationLogEntity?> =
        db.locationLogDao().observeLatestLocation(bookingId)

    suspend fun updateBookingLiveLocation(
        bookingId: String,
        userId: String,
        userRole: String,
        latitude: Double,
        longitude: Double,
        speedKmh: Double = 28.0,
        heading: Float = 45f,
        distanceRemainingKm: Double = 1.2,
        etaMinutes: Int = 5,
        streetName: String = "Market Street & 4th Ave",
        customerLat: Double = 37.7749,
        customerLng: Double = -122.4194,
        providerLat: Double = 37.7833,
        providerLng: Double = -122.4167
    ) {
        val log = LocationLogEntity(
            bookingId = bookingId,
            userId = userId,
            role = userRole,
            latitude = latitude,
            longitude = longitude,
            distanceKm = distanceRemainingKm,
            etaMinutes = etaMinutes,
            speedKmh = speedKmh,
            heading = heading,
            streetName = streetName,
            customerLat = customerLat,
            customerLng = customerLng,
            providerLat = providerLat,
            providerLng = providerLng,
            timestamp = System.currentTimeMillis()
        )
        db.locationLogDao().insertLocationLog(log)
    }

    // ==========================================
    // DISPUTES & COMPLAINT RESOLUTION SYSTEM
    // ==========================================

    fun observeDisputesForUser(userId: String): Flow<List<DisputeEntity>> = db.disputeDao().getDisputesForUser(userId)
    fun observeAllDisputesAdmin(): Flow<List<DisputeEntity>> = db.disputeDao().getAllDisputes()

    suspend fun submitComplaint(
        userId: String,
        reason: String,
        description: String,
        bookingId: String = "GENERAL_COMPLAINT"
    ): Result<DisputeEntity> {
        val user = db.userDao().getUserById(userId) ?: return Result.failure(Exception("User not found."))
        val disputeId = "CMP-${System.currentTimeMillis() % 100000}"
        
        val dispute = DisputeEntity(
            id = disputeId,
            bookingId = bookingId,
            createdByUserId = userId,
            createdByName = user.name,
            role = user.role,
            reason = reason.trim(),
            description = description.trim(),
            status = "OPEN"
        )
        db.disputeDao().insertDispute(dispute)

        // If linked to an active booking, update booking status to DISPUTED
        if (bookingId != "GENERAL_COMPLAINT" && bookingId.isNotBlank()) {
            val booking = db.bookingDao().getBookingById(bookingId)
            if (booking != null) {
                db.bookingDao().updateBooking(booking.copy(status = "DISPUTED"))
            }
        }

        // Notify user & Admin
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_cmp_$disputeId",
                userId = userId,
                type = "ADMIN_ALERT",
                title = "Complaint Submitted: #$disputeId",
                message = "Your grievance has been lodged directly with the Admin Support Team.",
                referenceId = disputeId
            )
        )

        return Result.success(dispute)
    }

    suspend fun openDispute(
        bookingId: String,
        userId: String,
        reason: String,
        description: String
    ): Result<DisputeEntity> {
        return submitComplaint(userId, reason, description, bookingId)
    }

    suspend fun adminResolveDispute(
        disputeId: String,
        adminId: String,
        resolutionType: String, // "REFUND_CUSTOMER", "PAYOUT_PROVIDER", "DISMISSED"
        adminNotes: String
    ): Result<Unit> {
        val allDisputes = db.disputeDao().getAllDisputes().first()
        val dispute = allDisputes.find { it.id == disputeId } ?: return Result.failure(Exception("Dispute not found."))
        val booking = db.bookingDao().getBookingById(dispute.bookingId) ?: return Result.failure(Exception("Booking not found."))

        if (resolutionType == "REFUND_CUSTOMER") {
            val custWallet = db.walletDao().getWalletByUserId(booking.customerId)
            if (custWallet != null) {
                db.walletDao().updateWallet(custWallet.copy(availableBalance = custWallet.availableBalance + booking.price))
                db.walletDao().insertTransaction(
                    WalletTransactionEntity(
                        id = "TXN-DSP-REF-${disputeId}",
                        walletId = custWallet.id,
                        userId = booking.customerId,
                        type = "REFUND",
                        grossAmount = booking.price,
                        fee = 0.0,
                        netAmount = booking.price,
                        status = "COMPLETED",
                        referenceId = dispute.bookingId,
                        note = "Admin Dispute Resolution Refund ($disputeId)"
                    )
                )
            }
            db.bookingDao().updateBooking(booking.copy(status = "REFUNDED"))
        } else if (resolutionType == "PAYOUT_PROVIDER") {
            var provWallet = db.walletDao().getWalletByUserId(booking.providerId)
            if (provWallet == null) {
                provWallet = WalletEntity(id = "wall_${booking.providerId}", userId = booking.providerId)
                db.walletDao().insertWallet(provWallet)
            }
            db.walletDao().updateWallet(provWallet.copy(availableBalance = provWallet.availableBalance + booking.providerNetAmount))
            db.bookingDao().updateBooking(booking.copy(status = "COMPLETED"))
        }

        val updatedDispute = dispute.copy(
            status = "RESOLVED",
            adminNotes = adminNotes,
            resolution = resolutionType,
            updatedAt = System.currentTimeMillis()
        )
        db.disputeDao().updateDispute(updatedDispute)

        recordAuditLog(adminId, "Admin", "ADMIN", "RESOLVE_DISPUTE", "DISPUTE", disputeId, "Resolved with: $resolutionType")
        return Result.success(Unit)
    }

    // ==========================================
    // NOTIFICATIONS & AUDIT
    // ==========================================

    fun observeNotificationsForUser(userId: String): Flow<List<NotificationEntity>> = db.notificationDao().getNotificationsForUser(userId)
    suspend fun markNotificationRead(id: String) = db.notificationDao().markAsRead(id)
    suspend fun markAllNotificationsRead(userId: String) = db.notificationDao().markAllAsRead(userId)

    fun observeRecentAuditLogs(): Flow<List<AuditLogEntity>> = db.auditLogDao().getRecentAuditLogs()

    suspend fun recordAuditLog(
        actorId: String,
        actorName: String,
        actorRole: String,
        action: String,
        entity: String,
        entityId: String,
        metadata: String
    ) {
        db.auditLogDao().insertAuditLog(
            AuditLogEntity(
                actorId = actorId,
                actorName = actorName,
                actorRole = actorRole,
                action = action,
                entity = entity,
                entityId = entityId,
                metadata = metadata
            )
        )
    }

    // ==========================================
    // ADMIN USER & TAXONOMY MANAGEMENT
    // ==========================================

    fun observeAllUsersAdmin(): Flow<List<UserEntity>> = db.userDao().getAllUsers()

    suspend fun adminVerifyProvider(userId: String, adminId: String): Result<Unit> {
        db.userDao().updateUserVerification(userId, "VERIFIED")
        val profile = db.providerProfileDao().getProfileByUserId(userId)
        if (profile != null) {
            db.providerProfileDao().updateProfile(profile.copy(verificationStatus = "VERIFIED"))
        }
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_ver_$userId",
                userId = userId,
                type = "APPROVAL",
                title = "Provider Verified!",
                message = "Congratulations! Your provider profile has been verified and is now live on the marketplace.",
                referenceId = userId
            )
        )
        recordAuditLog(adminId, "Admin", "ADMIN", "VERIFY_PROVIDER", "USER", userId, "Verified provider profile")
        return Result.success(Unit)
    }

    suspend fun adminToggleUserStatus(userId: String, currentStatus: String, adminId: String): Result<Unit> {
        val newStatus = if (currentStatus == "ACTIVE") "SUSPENDED" else "ACTIVE"
        db.userDao().updateUserStatus(userId, newStatus)
        recordAuditLog(adminId, "Admin", "ADMIN", "TOGGLE_USER_STATUS", "USER", userId, "Changed status to $newStatus")
        return Result.success(Unit)
    }

    suspend fun adminCreateCategory(name: String, description: String, iconName: String): Result<CategoryEntity> {
        val id = "cat_${name.lowercase().replace(" ", "_").replace("&", "and")}_${System.currentTimeMillis() % 1000}"
        val category = CategoryEntity(
            id = id,
            name = name.trim(),
            slug = name.lowercase().replace(" ", "-"),
            description = description.trim(),
            iconName = iconName.ifBlank { "Build" },
            active = true
        )
        db.categoryDao().insertCategory(category)
        return Result.success(category)
    }

    suspend fun adminToggleCategoryActive(categoryId: String, currentActive: Boolean): Result<Unit> {
        val cat = db.categoryDao().getCategoryById(categoryId) ?: return Result.failure(Exception("Category not found."))
        db.categoryDao().updateCategory(cat.copy(active = !currentActive))
        return Result.success(Unit)
    }

    // Platform Settings
    fun observePlatformSettings(): Flow<List<PlatformSettingEntity>> = db.platformSettingDao().getAllSettings()
    suspend fun updatePlatformSetting(key: String, value: String, description: String) {
        db.platformSettingDao().insertSetting(PlatformSettingEntity(key, value, description))
    }

    // Provider profile edit
    suspend fun updateProviderProfile(
        userId: String,
        title: String,
        bio: String,
        locationName: String,
        workingHours: String,
        facebookUrl: String,
        websiteUrl: String,
        emergencyAvailable: Boolean
    ): Result<Unit> {
        val profile = db.providerProfileDao().getProfileByUserId(userId) ?: return Result.failure(Exception("Profile not found."))
        db.providerProfileDao().updateProfile(
            profile.copy(
                title = title.trim(),
                bio = bio.trim(),
                locationName = locationName.trim(),
                workingHours = workingHours.trim(),
                facebookUrl = facebookUrl.trim(),
                websiteUrl = websiteUrl.trim(),
                emergencyAvailable = emergencyAvailable
            )
        )
        return Result.success(Unit)
    }

    suspend fun addServiceForProvider(
        providerId: String,
        categoryId: String,
        title: String,
        description: String,
        price: Double,
        durationMinutes: Int
    ): Result<ServiceEntity> {
        val serviceId = "srv_prov_${System.currentTimeMillis()}"
        val srv = ServiceEntity(
            id = serviceId,
            providerId = providerId,
            categoryId = categoryId,
            title = title.trim(),
            description = description.trim(),
            price = price,
            durationMinutes = durationMinutes,
            active = true
        )
        db.serviceDao().insertService(srv)
        return Result.success(srv)
    }

    // Admin Reject Provider
    suspend fun adminRejectProvider(userId: String, adminId: String, reason: String): Result<Unit> {
        db.userDao().updateUserVerification(userId, "REJECTED")
        val profile = db.providerProfileDao().getProfileByUserId(userId)
        if (profile != null) {
            db.providerProfileDao().updateProfile(profile.copy(verificationStatus = "REJECTED"))
        }
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_rej_prov_$userId",
                userId = userId,
                type = "APPROVAL",
                title = "Provider Application Update",
                message = "Your provider registration was not approved: $reason. Please update your details and resubmit.",
                referenceId = userId
            )
        )
        recordAuditLog(adminId, "Admin", "ADMIN", "REJECT_PROVIDER", "USER", userId, "Rejected provider: $reason")
        return Result.success(Unit)
    }

    // ==========================================
    // IN-APP CHAT & TEXT MESSAGING ENGINE
    // ==========================================

    fun observeMessagesBetweenUsers(user1Id: String, user2Id: String): Flow<List<ChatMessageEntity>> =
        db.chatMessageDao().getMessagesBetweenUsers(user1Id, user2Id)

    fun observeMessagesByBooking(bookingId: String): Flow<List<ChatMessageEntity>> =
        db.chatMessageDao().getMessagesByBooking(bookingId)

    fun observeAllUserMessages(userId: String): Flow<List<ChatMessageEntity>> =
        db.chatMessageDao().getAllMessagesForUser(userId)

    suspend fun sendChatMessage(
        senderId: String,
        senderName: String,
        senderRole: String,
        receiverId: String,
        receiverName: String,
        text: String,
        bookingId: String = "",
        mediaType: String = "NONE",
        mediaUrl: String = "",
        mediaCaption: String = "",
        videoDurationSec: Int = 0,
        locationLat: Double = 0.0,
        locationLng: Double = 0.0,
        locationAddress: String = ""
    ): Result<ChatMessageEntity> {
        val effectiveText = if (text.isBlank()) {
            when (mediaType) {
                "PHOTO" -> if (mediaCaption.isNotBlank()) mediaCaption else "📷 [Photo Attachment]"
                "VIDEO" -> if (mediaCaption.isNotBlank()) mediaCaption else "🎥 [Video Clip ($videoDurationSec s)]"
                "LOCATION" -> "📍 Live Location: $locationAddress"
                else -> return Result.failure(Exception("Message cannot be empty."))
            }
        } else text.trim()

        val msg = ChatMessageEntity(
            id = "MSG-${System.currentTimeMillis()}-${(100..999).random()}",
            senderId = senderId,
            senderName = senderName,
            senderRole = senderRole,
            receiverId = receiverId,
            receiverName = receiverName,
            text = effectiveText,
            bookingId = bookingId,
            mediaType = mediaType,
            mediaUrl = mediaUrl,
            mediaCaption = mediaCaption,
            videoDurationSec = videoDurationSec,
            locationLat = locationLat,
            locationLng = locationLng,
            locationAddress = locationAddress,
            timestamp = System.currentTimeMillis(),
            read = false
        )
        db.chatMessageDao().insertMessage(msg)
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_msg_${msg.id}",
                userId = receiverId,
                type = "MESSAGE",
                title = "New message from $senderName",
                message = when (mediaType) {
                    "PHOTO" -> "📷 Sent you a photo: $effectiveText"
                    "VIDEO" -> "🎥 Sent you a video: $effectiveText"
                    "LOCATION" -> "📍 Shared live location with you"
                    else -> effectiveText.take(60)
                },
                referenceId = msg.id
            )
        )
        return Result.success(msg)
    }

    suspend fun markChatRead(receiverId: String, senderId: String) {
        db.chatMessageDao().markMessagesAsRead(receiverId, senderId)
    }

    // ==========================================
    // PROVIDER STORE SUBDOMAIN & WEB STOREFRONT ($5/month)
    // ==========================================
    fun observeProviderStore(providerId: String): Flow<ProviderStoreEntity?> =
        db.providerStoreDao().observeStoreByProviderId(providerId)

    fun observeStoreBySubdomain(subdomain: String): Flow<ProviderStoreEntity?> =
        db.providerStoreDao().observeStoreBySubdomain(subdomain.lowercase().trim())

    fun observeAllPublicStores(): Flow<List<ProviderStoreEntity>> =
        db.providerStoreDao().getAllActiveStores()

    suspend fun getStoreBySubdomain(subdomain: String): ProviderStoreEntity? =
        db.providerStoreDao().getStoreBySubdomain(subdomain.lowercase().trim())

    suspend fun getStoreByProviderId(providerId: String): ProviderStoreEntity? =
        db.providerStoreDao().getStoreByProviderId(providerId)

    suspend fun incrementStoreVisitor(subdomain: String) {
        db.providerStoreDao().incrementVisitorCount(subdomain.lowercase().trim())
    }

    suspend fun toggleStoreActive(providerId: String, isActive: Boolean): Result<Unit> {
        db.providerStoreDao().updateStoreActiveState(providerId, isActive, System.currentTimeMillis())
        return Result.success(Unit)
    }

    suspend fun cancelStoreSubdomain(providerId: String): Result<Unit> {
        db.providerStoreDao().updateSubscriptionStatus(providerId, "CANCELLED", false, System.currentTimeMillis())
        return Result.success(Unit)
    }

    suspend fun createOrRenewProviderSubdomain(
        providerId: String,
        providerName: String,
        rawSubdomain: String,
        storeTitle: String,
        tagline: String,
        aboutBio: String,
        category: String,
        contactPhone: String,
        contactEmail: String,
        whatsappNumber: String,
        businessAddress: String,
        operatingHours: String,
        announcement: String
    ): Result<ProviderStoreEntity> {
        val cleanSlug = rawSubdomain.trim().lowercase()
            .replace(Regex("[^a-z0-9-]"), "")
            .trim('-')

        if (cleanSlug.length < 3 || cleanSlug.length > 30) {
            return Result.failure(Exception("Subdomain must be between 3 and 30 characters (letters, numbers, hyphens)."))
        }

        val reserved = setOf("admin", "api", "www", "mail", "portal", "servexa", "auth", "billing", "app", "root")
        if (cleanSlug in reserved) {
            return Result.failure(Exception("Subdomain '$cleanSlug' is a reserved system address. Please choose another."))
        }

        val existingForSubdomain = db.providerStoreDao().getStoreBySubdomain(cleanSlug)
        if (existingForSubdomain != null && existingForSubdomain.providerId != providerId) {
            return Result.failure(Exception("Subdomain '$cleanSlug.servexa.com' is already registered by another provider."))
        }

        val wallet = db.walletDao().getWalletByUserId(providerId)
            ?: return Result.failure(Exception("Provider wallet not initialized."))

        val monthlyFee = 5.0
        val existingStore = db.providerStoreDao().getStoreByProviderId(providerId)
        val isFirstTimeSubscription = existingStore == null || existingStore.subscriptionStatus != "ACTIVE"

        if (isFirstTimeSubscription) {
            if (wallet.availableBalance < monthlyFee) {
                return Result.failure(Exception("Insufficient wallet balance ($${"%.2f".format(wallet.availableBalance)}). Subdomain setup requires $5.00/month. Please top up your wallet."))
            }

            // Deduct $5.00 from provider wallet
            val newBalance = wallet.availableBalance - monthlyFee
            db.walletDao().updateWallet(wallet.copy(availableBalance = newBalance, updatedAt = System.currentTimeMillis()))

            val txnId = "TXN-SUB-${System.currentTimeMillis() % 1000000}"
            val txn = WalletTransactionEntity(
                id = txnId,
                walletId = wallet.id,
                userId = providerId,
                type = "SUBSCRIPTION",
                paymentChannel = "WALLET",
                grossAmount = monthlyFee,
                fee = 0.0,
                netAmount = monthlyFee,
                status = "COMPLETED",
                note = "Monthly Store Subdomain Fee: $cleanSlug.servexa.com ($5.00/mo)",
                referenceId = cleanSlug,
                createdAt = System.currentTimeMillis()
            )
            db.walletDao().insertTransaction(txn)
        }

        val storeId = existingStore?.id ?: "store_$providerId"
        val now = System.currentTimeMillis()
        val nextBilling = if (existingStore != null && existingStore.subscriptionStatus == "ACTIVE") {
            existingStore.nextBillingDate
        } else {
            now + (30L * 24 * 60 * 60 * 1000)
        }

        val store = ProviderStoreEntity(
            id = storeId,
            providerId = providerId,
            providerName = providerName.ifBlank { "Certified Service Provider" },
            subdomain = cleanSlug,
            storeTitle = storeTitle.ifBlank { "$providerName Service Store" },
            tagline = tagline.ifBlank { "Professional Certified & Insured Services" },
            aboutBio = aboutBio,
            category = category.ifBlank { "All Services" },
            themeColorHex = "#1D4ED8", // Royal Blue
            bannerImageUrl = "",
            logoUrl = "",
            contactPhone = contactPhone,
            contactEmail = contactEmail,
            whatsappNumber = whatsappNumber,
            businessAddress = businessAddress,
            operatingHours = operatingHours.ifBlank { "Mon - Sat: 8:00 AM - 7:00 PM" },
            announcement = announcement.ifBlank { "Welcome to our official web storefront! Book verified services online." },
            monthlyFee = monthlyFee,
            isActive = true,
            autoRenew = true,
            subscriptionStatus = "ACTIVE",
            subscribedAt = existingStore?.subscribedAt ?: now,
            nextBillingDate = nextBilling,
            totalVisitors = existingStore?.totalVisitors ?: 1,
            totalOrdersFromSubdomain = existingStore?.totalOrdersFromSubdomain ?: 0,
            updatedAt = now
        )

        db.providerStoreDao().insertOrUpdateStore(store)

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_sub_${System.currentTimeMillis() % 100000}",
                userId = providerId,
                type = "APPROVAL",
                title = "🌐 Store Subdomain Active: $cleanSlug.servexa.com",
                message = "Your official web store is live at https://$cleanSlug.servexa.com. Monthly subscription ($5.00/mo) active.",
                referenceId = cleanSlug,
                read = false
            )
        )

        recordAuditLog(
            actorId = providerId,
            actorName = providerName,
            actorRole = "PROVIDER",
            action = "CREATE_SUBDOMAIN",
            entity = "STORE_SUBDOMAIN",
            entityId = storeId,
            metadata = "Activated subdomain $cleanSlug.servexa.com ($5.00/mo charged from wallet)"
        )

        return Result.success(store)
    }
}

// Data holder classes
data class ProviderSearchResult(
    val user: UserEntity,
    val profile: ProviderProfileEntity,
    val services: List<ServiceEntity>,
    val categoryName: String,
    val startingPrice: Double,
    val estimatedDistanceKm: Double
)

data class ActiveCallSession(
    val callId: String,
    val bookingId: String,
    val callerId: String,
    val callerName: String,
    val callerRole: String,
    val receiverId: String,
    val receiverName: String,
    val receiverRole: String,
    val state: String, // "CONNECTING", "RINGING", "CONNECTED", "ENDED"
    val startTime: Long,
    val answerTime: Long? = null
)
