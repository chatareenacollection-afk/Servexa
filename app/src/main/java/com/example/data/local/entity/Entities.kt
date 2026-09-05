package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val role: String, // "CUSTOMER", "PROVIDER", "ADMIN"
    val name: String,
    val email: String,
    val phone: String,
    val passwordHash: String,
    val profileImage: String,
    val status: String = "ACTIVE", // "ACTIVE", "SUSPENDED"
    val verificationStatus: String = "NONE", // "NONE", "PENDING", "VERIFIED", "REJECTED"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "provider_profiles")
data class ProviderProfileEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val bio: String,
    val locationName: String,
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194,
    val serviceArea: String = "Metro Area (25 km radius)",
    val workingHours: String = "Mon - Sat: 8:00 AM - 6:00 PM",
    val emergencyAvailable: Boolean = true,
    val rating: Double = 4.9,
    val reviewCount: Int = 0,
    val completedJobs: Int = 0,
    val facebookUrl: String = "",
    val instagramUrl: String = "",
    val websiteUrl: String = "",
    val verificationStatus: String = "VERIFIED", // "PENDING", "VERIFIED", "REJECTED"
    val verificationDocuments: String = "Certified Professional License"
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val slug: String,
    val description: String,
    val iconName: String,
    val bannerUrl: String = "",
    val parentId: String? = null,
    val active: Boolean = true,
    val orderIndex: Int = 0
)

@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey val id: String,
    val providerId: String,
    val categoryId: String,
    val subcategoryName: String = "",
    val title: String,
    val description: String,
    val price: Double,
    val durationMinutes: Int = 60,
    val active: Boolean = true
)

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String, // e.g. "SVX-2026-000101"
    val customerId: String,
    val providerId: String,
    val serviceId: String,
    val serviceTitle: String,
    val address: String,
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194,
    val scheduledAt: Long = System.currentTimeMillis(),
    val status: String = "REQUESTED", // "REQUESTED", "ACCEPTED", "REJECTED", "CANCELLED", "PROVIDER_ON_THE_WAY", "ARRIVED", "IN_PROGRESS", "COMPLETED", "DISPUTED", "REFUNDED"
    val price: Double,
    val platformCommission: Double = 0.0, // 6% of price
    val providerNetAmount: Double = 0.0,  // 94% of price
    val problemDescription: String = "",
    val specialInstructions: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "booking_status_history")
data class BookingStatusHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookingId: String,
    val previousStatus: String,
    val newStatus: String,
    val actorId: String,
    val actorRole: String,
    val timestamp: Long = System.currentTimeMillis(),
    val reason: String = ""
)

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val availableBalance: Double = 0.0,
    val pendingBalance: Double = 0.0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey val id: String, // e.g. "TXN-100293"
    val walletId: String,
    val userId: String,
    val type: String, // "TOP_UP", "SERVICE_PAYMENT", "SERVICE_EARNING", "WITHDRAWAL", "COMMISSION_DEDUCTION", "REFUND", "ADJUSTMENT", "POS_CREDIT_ALLOTMENT"
    val grossAmount: Double,
    val fee: Double = 0.0, // 5% on top-up, 6% on service commission
    val netAmount: Double,
    val status: String = "COMPLETED", // "PENDING", "COMPLETED", "REJECTED"
    val referenceId: String = "",
    val note: String = "",
    val customerName: String = "",
    val customerEmail: String = "",
    val customerPhone: String = "",
    val customerAddress: String = "",
    val merchantGatewayName: String = "Stripe Merchant Gateway",
    val merchantAccountId: String = "acct_servexa_primary",
    val merchantCaptureRef: String = "",
    val captureStatus: String = "CAPTURED", // "CAPTURED", "ESCROW_HELD", "PENDING_VERIFICATION", "SETTLED"
    val paymentChannel: String = "GATEWAY", // "GATEWAY", "POS_TERMINAL", "BANK_TRANSFER", "WALLET", "CREDIT_ALLOTMENT"
    val posTerminalId: String = "",
    val posLocation: String = "",
    val posAgentName: String = "",
    val posAuthCode: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_kyc_documents")
data class UserKycDocumentEntity(
    @PrimaryKey val id: String, // e.g. "KYC-88201"
    val userId: String,
    val userName: String,
    val userEmail: String,
    val userPhone: String,
    val documentType: String, // "DRIVING_LICENSE", "NATIONAL_ID", "PASSPORT"
    val documentNumber: String, // e.g. "DL-9938472-CA", "ID-88291024", "PASSPORT-A9928172"
    val issuingCountry: String = "United States",
    val issuingStateOrProvince: String = "California",
    val expiryDate: String = "2029-12-31",
    val dateOfBirth: String = "1994-05-18",
    val residentialAddress: String = "Market Street & 4th Ave, San Francisco, CA",
    val documentFrontImage: String = "",
    val documentBackImage: String = "",
    val selfieImage: String = "",
    val verificationStatus: String = "PENDING", // "PENDING", "VERIFIED", "REJECTED"
    val rejectionReason: String = "",
    val adminNotes: String = "",
    val reviewedByAdminId: String? = null,
    val reviewedAt: Long? = null,
    val submittedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val sellerId: String = "system",
    val categoryId: String,
    val name: String,
    val description: String,
    val price: Double,
    val inventory: Int = 50,
    val imageUrl: String = "",
    val rating: Double = 4.8,
    val reviewCount: Int = 12,
    val active: Boolean = true
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val productId: String,
    val quantity: Int = 1,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String, // "ORD-99201"
    val customerId: String,
    val totalAmount: Double,
    val status: String = "PAID", // "PENDING", "PAID", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED"
    val paymentStatus: String = "PAID",
    val shippingAddress: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: String,
    val productId: String,
    val productName: String,
    val productPrice: Double,
    val quantity: Int
)

@Entity(tableName = "work_videos")
data class WorkVideoEntity(
    @PrimaryKey val id: String,
    val providerId: String,
    val providerName: String,
    val providerAvatar: String = "",
    val title: String,
    val description: String,
    val category: String,
    val videoUrl: String = "",
    val thumbnailUrl: String = "",
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val viewsCount: Int = 0,
    val status: String = "ACTIVE",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "video_likes")
data class VideoLikeEntity(
    @PrimaryKey val id: String, // "${videoId}_${userId}"
    val videoId: String,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "video_comments")
data class VideoCommentEntity(
    @PrimaryKey val id: String,
    val videoId: String,
    val userId: String,
    val userName: String,
    val userAvatar: String = "",
    val comment: String,
    val status: String = "ACTIVE",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String,
    val bookingId: String,
    val customerId: String,
    val customerName: String,
    val customerAvatar: String = "",
    val providerId: String,
    val rating: Double,
    val reviewText: String,
    val providerReply: String? = null,
    val status: String = "PUBLISHED",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey val id: String, // "CALL-44021"
    val bookingId: String = "",
    val callerId: String,
    val callerName: String,
    val callerRole: String,
    val receiverId: String,
    val receiverName: String,
    val receiverRole: String,
    val startTime: Long,
    val answerTime: Long? = null,
    val endTime: Long? = null,
    val durationSeconds: Long = 0,
    val status: String = "COMPLETED", // "COMPLETED", "MISSED", "DECLINED", "CANCELLED"
    val direction: String = "OUTGOING",
    val recordingAvailable: Boolean = true,
    val recordingRef: String = "SVX_SECURE_REC_ENC_01"
)

@Entity(tableName = "location_logs")
data class LocationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookingId: String,
    val userId: String,
    val role: String,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double = 1.8,
    val etaMinutes: Int = 6,
    val speedKmh: Double = 32.0,
    val heading: Float = 45f,
    val streetName: String = "Market Street & 4th Ave",
    val customerLat: Double = 37.7749,
    val customerLng: Double = -122.4194,
    val providerLat: Double = 37.7833,
    val providerLng: Double = -122.4167,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val type: String, // "BOOKING_UPDATE", "PAYMENT", "CALL", "COMMENT", "ADMIN_ALERT", "APPROVAL"
    val title: String,
    val message: String,
    val referenceId: String = "",
    val read: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "disputes")
data class DisputeEntity(
    @PrimaryKey val id: String, // "DSP-9102"
    val bookingId: String,
    val createdByUserId: String,
    val createdByName: String,
    val role: String,
    val reason: String,
    val description: String,
    val evidence: String = "",
    val status: String = "OPEN", // "OPEN", "UNDER_REVIEW", "WAITING_FOR_USER", "WAITING_FOR_PROVIDER", "RESOLVED", "REJECTED"
    val adminNotes: String = "",
    val resolution: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actorId: String,
    val actorName: String,
    val actorRole: String,
    val action: String,
    val entity: String,
    val entityId: String,
    val metadata: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "platform_settings")
data class PlatformSettingEntity(
    @PrimaryKey val key: String,
    val value: String,
    val description: String
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String, // "${userId}_${targetType}_${targetId}"
    val userId: String,
    val targetType: String, // "PROVIDER", "SERVICE", "PRODUCT"
    val targetId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payment_methods")
data class PaymentMethodEntity(
    @PrimaryKey val id: String, // e.g. "pm_bank", "pm_card", "pm_crypto"
    val name: String, // e.g. "Direct Bank Transfer (ACH / Wire)"
    val type: String, // "BANK_TRANSFER", "CARD", "CRYPTO", "PAYPAL", "CASH"
    val accountTitle: String = "Servexa Marketplace Escrow LLC",
    val accountNumber: String = "US89 3704 0044 0532 0130 00",
    val bankOrProviderName: String = "JPMorgan Chase & Co.",
    val routingOrSwift: String = "CHASUS33 / Routing: 021000021",
    val instructions: String = "Include your registered email or User ID in deposit reference memo.",
    val minAmount: Double = 10.0,
    val maxAmount: Double = 10000.0,
    val feePercent: Double = 5.0,
    val active: Boolean = true,
    val orderIndex: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_payout_accounts")
data class UserPayoutAccountEntity(
    @PrimaryKey val id: String, // e.g. "payout_acc_${userId}"
    val userId: String,
    val accountType: String, // "BANK_ACCOUNT" or "DEBIT_CARD"
    val accountHolderName: String,
    val bankOrIssuerName: String,
    val accountOrCardNumber: String, // IBAN or Card Number (e.g. **** 4242)
    val routingOrIfscOrCvv: String, // Routing / IFSC / Sort code
    val swiftOrBic: String = "",
    val country: String = "United States",
    val isDefault: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String, // "MSG-123456"
    val senderId: String,
    val senderName: String,
    val senderRole: String, // "CUSTOMER", "PROVIDER", "ADMIN"
    val receiverId: String,
    val receiverName: String,
    val text: String,
    val bookingId: String = "",
    val mediaType: String = "NONE", // "NONE", "PHOTO", "VIDEO", "LOCATION"
    val mediaUrl: String = "",
    val mediaCaption: String = "",
    val videoDurationSec: Int = 0,
    val locationLat: Double = 0.0,
    val locationLng: Double = 0.0,
    val locationAddress: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false
)

@Entity(tableName = "merchant_gateways")
data class MerchantGatewayAccountEntity(
    @PrimaryKey val id: String, // e.g. "gw_stripe_primary", "gw_paypal_biz", "gw_razorpay", "gw_bank_escrow"
    val name: String, // "Stripe Connect Merchant", "PayPal Business", "Razorpay Gateway", "Servexa Escrow Bank", "Square Pay"
    val gatewayType: String, // "STRIPE", "PAYPAL", "RAZORPAY", "BANK_ESCROW", "SQUARE", "UPI"
    val merchantAccountId: String, // e.g. "acct_1Nz828xServexaMerc", "business@servexa.com"
    val publicKeyOrClientId: String = "", // e.g. "pk_live_51M..."
    val secretKeyOrApiKey: String = "", // e.g. "sk_live_..."
    val webhookSecret: String = "", // e.g. "whsec_..."
    val isLiveMode: Boolean = true, // Live vs Sandbox / Test
    val autoCapture: Boolean = true, // Immediate capture vs Manual Escrow Hold
    val captureCustomerDetails: Boolean = true, // Stores name, email, phone, billing address
    val settlementCurrency: String = "USD", // "USD", "EUR", "GBP", "CAD", "INR", "AUD"
    val platformFeePercent: Double = 5.0, // 5% Top-up fee / platform fee
    val payoutDelayDays: Int = 2, // 48 hours settlement
    val isActive: Boolean = true,
    val isDefault: Boolean = true,
    val webhookEndpointUrl: String = "https://api.servexa.com/v1/payments/webhooks/gateway",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "provider_stores")
data class ProviderStoreEntity(
    @PrimaryKey val id: String, // e.g. "store_${providerId}"
    val providerId: String,
    val providerName: String,
    val subdomain: String, // e.g. "alex-plumbing" -> URL: https://alex-plumbing.servexa.com
    val storeTitle: String,
    val tagline: String = "Professional Certified Services",
    val aboutBio: String = "",
    val category: String = "All Services",
    val themeColorHex: String = "#1D4ED8", // Royal Blue
    val bannerImageUrl: String = "",
    val logoUrl: String = "",
    val contactPhone: String = "",
    val contactEmail: String = "",
    val whatsappNumber: String = "",
    val businessAddress: String = "",
    val operatingHours: String = "Mon - Sat: 8:00 AM - 7:00 PM",
    val announcement: String = "Welcome to our official web storefront! Book verified services online with direct escrow guarantee.",
    val monthlyFee: Double = 5.0, // $5.00/month
    val isActive: Boolean = true,
    val autoRenew: Boolean = true,
    val subscriptionStatus: String = "ACTIVE", // "ACTIVE", "PAST_DUE", "CANCELLED"
    val subscribedAt: Long = System.currentTimeMillis(),
    val nextBillingDate: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
    val totalVisitors: Int = 1,
    val totalOrdersFromSubdomain: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)
