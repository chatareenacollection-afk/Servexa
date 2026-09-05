package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET status = :status WHERE id = :id")
    suspend fun updateUserStatus(id: String, status: String)

    @Query("UPDATE users SET verificationStatus = :status WHERE id = :id")
    suspend fun updateUserVerification(id: String, status: String)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: String)
}

@Dao
interface ProviderProfileDao {
    @Query("SELECT * FROM provider_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getProfileByUserId(userId: String): ProviderProfileEntity?

    @Query("SELECT * FROM provider_profiles WHERE userId = :userId LIMIT 1")
    fun observeProfileByUserId(userId: String): Flow<ProviderProfileEntity?>

    @Query("SELECT * FROM provider_profiles")
    fun getAllProfiles(): Flow<List<ProviderProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProviderProfileEntity)

    @Update
    suspend fun updateProfile(profile: ProviderProfileEntity)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE active = 1 ORDER BY orderIndex ASC")
    fun getActiveCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY orderIndex ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategory(id: String)
}

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services WHERE active = 1")
    fun getAllActiveServices(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE providerId = :providerId")
    fun getServicesByProvider(providerId: String): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE categoryId = :categoryId AND active = 1")
    fun getServicesByCategory(categoryId: String): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE id = :id LIMIT 1")
    suspend fun getServiceById(id: String): ServiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(services: List<ServiceEntity>)

    @Update
    suspend fun updateService(service: ServiceEntity)

    @Query("DELETE FROM services WHERE id = :id")
    suspend fun deleteService(id: String)
}

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getBookingsByCustomer(customerId: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE providerId = :providerId ORDER BY createdAt DESC")
    fun getBookingsByProvider(providerId: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings ORDER BY createdAt DESC")
    fun getAllBookings(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    suspend fun getBookingById(id: String): BookingEntity?

    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    fun observeBookingById(id: String): Flow<BookingEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity)

    @Update
    suspend fun updateBooking(booking: BookingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatusHistory(history: BookingStatusHistoryEntity)

    @Query("SELECT * FROM booking_status_history WHERE bookingId = :bookingId ORDER BY timestamp ASC")
    fun getStatusHistory(bookingId: String): Flow<List<BookingStatusHistoryEntity>>
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets WHERE userId = :userId LIMIT 1")
    suspend fun getWalletByUserId(userId: String): WalletEntity?

    @Query("SELECT * FROM wallets WHERE userId = :userId LIMIT 1")
    fun observeWalletByUserId(userId: String): Flow<WalletEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity)

    @Update
    suspend fun updateWallet(wallet: WalletEntity)

    @Query("SELECT * FROM wallet_transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun getTransactionsByUserId(userId: String): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<WalletTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: WalletTransactionEntity)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE active = 1")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE sellerId = :sellerId AND active = 1")
    fun getProductsBySeller(sellerId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE categoryId = :categoryId AND active = 1")
    fun getProductsByCategory(categoryId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: String)
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    fun getCartItems(userId: String): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE userId = :userId AND productId = :productId LIMIT 1")
    suspend fun getCartItem(userId: String, productId: String): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Update
    suspend fun updateCartItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItem(id: Long)

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearCart(userId: String)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getOrdersByCustomer(customerId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItems(orderId: String): List<OrderItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Update
    suspend fun updateOrder(order: OrderEntity)
}

@Dao
interface WorkVideoDao {
    @Query("SELECT * FROM work_videos WHERE status = 'ACTIVE' ORDER BY createdAt DESC")
    fun getActiveVideos(): Flow<List<WorkVideoEntity>>

    @Query("SELECT * FROM work_videos WHERE providerId = :providerId ORDER BY createdAt DESC")
    fun getVideosByProvider(providerId: String): Flow<List<WorkVideoEntity>>

    @Query("SELECT * FROM work_videos ORDER BY createdAt DESC")
    fun getAllVideos(): Flow<List<WorkVideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: WorkVideoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<WorkVideoEntity>)

    @Update
    suspend fun updateVideo(video: WorkVideoEntity)

    @Query("DELETE FROM work_videos WHERE id = :id")
    suspend fun deleteVideo(id: String)

    @Query("SELECT * FROM video_likes WHERE videoId = :videoId AND userId = :userId LIMIT 1")
    suspend fun getLike(videoId: String, userId: String): VideoLikeEntity?

    @Query("SELECT * FROM video_likes WHERE userId = :userId")
    fun getUserLikes(userId: String): Flow<List<VideoLikeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: VideoLikeEntity)

    @Query("DELETE FROM video_likes WHERE videoId = :videoId AND userId = :userId")
    suspend fun deleteLike(videoId: String, userId: String)

    @Query("SELECT * FROM video_comments WHERE videoId = :videoId AND status = 'ACTIVE' ORDER BY createdAt ASC")
    fun getCommentsForVideo(videoId: String): Flow<List<VideoCommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: VideoCommentEntity)

    @Query("DELETE FROM video_comments WHERE id = :id")
    suspend fun deleteComment(id: String)
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE providerId = :providerId AND status = 'PUBLISHED' ORDER BY createdAt DESC")
    fun getReviewsForProvider(providerId: String): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE bookingId = :bookingId LIMIT 1")
    suspend fun getReviewForBooking(bookingId: String): ReviewEntity?

    @Query("SELECT * FROM reviews ORDER BY createdAt DESC")
    fun getAllReviews(): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<ReviewEntity>)

    @Update
    suspend fun updateReview(review: ReviewEntity)

    @Query("DELETE FROM reviews WHERE id = :id")
    suspend fun deleteReview(id: String)
}

@Dao
interface CallLogDao {
    @Query("SELECT * FROM call_logs WHERE callerId = :userId OR receiverId = :userId ORDER BY startTime DESC")
    fun getCallsForUser(userId: String): Flow<List<CallLogEntity>>

    @Query("SELECT * FROM call_logs ORDER BY startTime DESC")
    fun getAllCalls(): Flow<List<CallLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: CallLogEntity)

    @Update
    suspend fun updateCall(call: CallLogEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET read = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET read = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)
}

@Dao
interface DisputeDao {
    @Query("SELECT * FROM disputes WHERE createdByUserId = :userId ORDER BY createdAt DESC")
    fun getDisputesForUser(userId: String): Flow<List<DisputeEntity>>

    @Query("SELECT * FROM disputes ORDER BY createdAt DESC")
    fun getAllDisputes(): Flow<List<DisputeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDispute(dispute: DisputeEntity)

    @Update
    suspend fun updateDispute(dispute: DisputeEntity)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 200")
    fun getRecentAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)
}

@Dao
interface PlatformSettingDao {
    @Query("SELECT * FROM platform_settings")
    fun getAllSettings(): Flow<List<PlatformSettingEntity>>

    @Query("SELECT * FROM platform_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): PlatformSettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: PlatformSettingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: List<PlatformSettingEntity>)
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites WHERE userId = :userId")
    fun getFavorites(userId: String): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE userId = :userId AND targetType = :targetType AND targetId = :targetId LIMIT 1")
    suspend fun getFavorite(userId: String, targetType: String, targetId: String): FavoriteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE userId = :userId AND targetType = :targetType AND targetId = :targetId")
    suspend fun deleteFavorite(userId: String, targetType: String, targetId: String)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE (senderId = :user1Id AND receiverId = :user2Id) OR (senderId = :user2Id AND receiverId = :user1Id) ORDER BY timestamp ASC")
    fun getMessagesBetweenUsers(user1Id: String, user2Id: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE bookingId = :bookingId ORDER BY timestamp ASC")
    fun getMessagesByBooking(bookingId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE senderId = :userId OR receiverId = :userId ORDER BY timestamp DESC")
    fun getAllMessagesForUser(userId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("UPDATE chat_messages SET read = 1 WHERE receiverId = :receiverId AND senderId = :senderId")
    suspend fun markMessagesAsRead(receiverId: String, senderId: String)
}

@Dao
interface PaymentMethodDao {
    @Query("SELECT * FROM payment_methods WHERE active = 1 ORDER BY orderIndex ASC")
    fun getActivePaymentMethods(): Flow<List<PaymentMethodEntity>>

    @Query("SELECT * FROM payment_methods ORDER BY orderIndex ASC")
    fun getAllPaymentMethods(): Flow<List<PaymentMethodEntity>>

    @Query("SELECT * FROM payment_methods WHERE id = :id LIMIT 1")
    suspend fun getPaymentMethodById(id: String): PaymentMethodEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethod(method: PaymentMethodEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethods(methods: List<PaymentMethodEntity>)

    @Update
    suspend fun updatePaymentMethod(method: PaymentMethodEntity)

    @Query("DELETE FROM payment_methods WHERE id = :id")
    suspend fun deletePaymentMethod(id: String)
}

@Dao
interface UserPayoutAccountDao {
    @Query("SELECT * FROM user_payout_accounts WHERE userId = :userId LIMIT 1")
    fun getPayoutAccountByUserId(userId: String): Flow<UserPayoutAccountEntity?>

    @Query("SELECT * FROM user_payout_accounts WHERE userId = :userId LIMIT 1")
    suspend fun getPayoutAccountDirect(userId: String): UserPayoutAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayoutAccount(account: UserPayoutAccountEntity)

    @Query("DELETE FROM user_payout_accounts WHERE userId = :userId")
    suspend fun deletePayoutAccount(userId: String)
}

@Dao
interface LocationLogDao {
    @Query("SELECT * FROM location_logs WHERE bookingId = :bookingId ORDER BY timestamp DESC LIMIT 1")
    fun observeLatestLocation(bookingId: String): Flow<LocationLogEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationLog(log: LocationLogEntity)
}

@Dao
interface MerchantGatewayDao {
    @Query("SELECT * FROM merchant_gateways ORDER BY updatedAt DESC")
    fun getAllMerchantGateways(): Flow<List<MerchantGatewayAccountEntity>>

    @Query("SELECT * FROM merchant_gateways WHERE isActive = 1 ORDER BY isDefault DESC, updatedAt DESC")
    fun getActiveMerchantGateways(): Flow<List<MerchantGatewayAccountEntity>>

    @Query("SELECT * FROM merchant_gateways WHERE id = :id LIMIT 1")
    suspend fun getMerchantGatewayById(id: String): MerchantGatewayAccountEntity?

    @Query("SELECT * FROM merchant_gateways WHERE isActive = 1 AND isDefault = 1 LIMIT 1")
    suspend fun getDefaultMerchantGateway(): MerchantGatewayAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMerchantGateway(gateway: MerchantGatewayAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMerchantGateways(gateways: List<MerchantGatewayAccountEntity>)

    @Update
    suspend fun updateMerchantGateway(gateway: MerchantGatewayAccountEntity)

    @Query("DELETE FROM merchant_gateways WHERE id = :id")
    suspend fun deleteMerchantGateway(id: String)
}

@Dao
interface UserKycDocumentDao {
    @Query("SELECT * FROM user_kyc_documents ORDER BY submittedAt DESC")
    fun getAllKycDocuments(): Flow<List<UserKycDocumentEntity>>

    @Query("SELECT * FROM user_kyc_documents WHERE userId = :userId ORDER BY submittedAt DESC LIMIT 1")
    fun observeKycDocumentByUserId(userId: String): Flow<UserKycDocumentEntity?>

    @Query("SELECT * FROM user_kyc_documents WHERE userId = :userId ORDER BY submittedAt DESC LIMIT 1")
    suspend fun getKycDocumentByUserId(userId: String): UserKycDocumentEntity?

    @Query("SELECT * FROM user_kyc_documents WHERE id = :id LIMIT 1")
    suspend fun getKycDocumentById(id: String): UserKycDocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKycDocument(doc: UserKycDocumentEntity)

    @Update
    suspend fun updateKycDocument(doc: UserKycDocumentEntity)

    @Query("UPDATE user_kyc_documents SET verificationStatus = :status, rejectionReason = :rejectionReason, adminNotes = :adminNotes, reviewedByAdminId = :adminId, reviewedAt = :timestamp, updatedAt = :timestamp WHERE id = :id")
    suspend fun reviewKycDocument(id: String, status: String, rejectionReason: String, adminNotes: String, adminId: String, timestamp: Long)

    @Query("DELETE FROM user_kyc_documents WHERE id = :id")
    suspend fun deleteKycDocument(id: String)
}

@Dao
interface ProviderStoreDao {
    @Query("SELECT * FROM provider_stores WHERE providerId = :providerId LIMIT 1")
    fun observeStoreByProviderId(providerId: String): Flow<ProviderStoreEntity?>

    @Query("SELECT * FROM provider_stores WHERE providerId = :providerId LIMIT 1")
    suspend fun getStoreByProviderId(providerId: String): ProviderStoreEntity?

    @Query("SELECT * FROM provider_stores WHERE LOWER(subdomain) = LOWER(:subdomain) LIMIT 1")
    suspend fun getStoreBySubdomain(subdomain: String): ProviderStoreEntity?

    @Query("SELECT * FROM provider_stores WHERE LOWER(subdomain) = LOWER(:subdomain) LIMIT 1")
    fun observeStoreBySubdomain(subdomain: String): Flow<ProviderStoreEntity?>

    @Query("SELECT * FROM provider_stores WHERE isActive = 1 ORDER BY totalVisitors DESC")
    fun getAllActiveStores(): Flow<List<ProviderStoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStore(store: ProviderStoreEntity)

    @Update
    suspend fun updateStore(store: ProviderStoreEntity)

    @Query("UPDATE provider_stores SET totalVisitors = totalVisitors + 1 WHERE LOWER(subdomain) = LOWER(:subdomain)")
    suspend fun incrementVisitorCount(subdomain: String)

    @Query("UPDATE provider_stores SET isActive = :isActive, updatedAt = :timestamp WHERE providerId = :providerId")
    suspend fun updateStoreActiveState(providerId: String, isActive: Boolean, timestamp: Long)

    @Query("UPDATE provider_stores SET subscriptionStatus = :status, isActive = :isActive, updatedAt = :timestamp WHERE providerId = :providerId")
    suspend fun updateSubscriptionStatus(providerId: String, status: String, isActive: Boolean, timestamp: Long)

    @Query("DELETE FROM provider_stores WHERE providerId = :providerId")
    suspend fun deleteStore(providerId: String)
}

