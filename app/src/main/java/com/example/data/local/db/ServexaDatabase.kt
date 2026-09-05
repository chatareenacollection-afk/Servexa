package com.example.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest

@Database(
    entities = [
        UserEntity::class,
        ProviderProfileEntity::class,
        CategoryEntity::class,
        ServiceEntity::class,
        BookingEntity::class,
        BookingStatusHistoryEntity::class,
        WalletEntity::class,
        WalletTransactionEntity::class,
        ProductEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        WorkVideoEntity::class,
        VideoLikeEntity::class,
        VideoCommentEntity::class,
        ReviewEntity::class,
        CallLogEntity::class,
        LocationLogEntity::class,
        NotificationEntity::class,
        DisputeEntity::class,
        AuditLogEntity::class,
        PlatformSettingEntity::class,
        FavoriteEntity::class,
        ChatMessageEntity::class,
        PaymentMethodEntity::class,
        UserPayoutAccountEntity::class,
        MerchantGatewayAccountEntity::class,
        UserKycDocumentEntity::class,
        ProviderStoreEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class ServexaDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun providerProfileDao(): ProviderProfileDao
    abstract fun categoryDao(): CategoryDao
    abstract fun serviceDao(): ServiceDao
    abstract fun bookingDao(): BookingDao
    abstract fun walletDao(): WalletDao
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun workVideoDao(): WorkVideoDao
    abstract fun reviewDao(): ReviewDao
    abstract fun callLogDao(): CallLogDao
    abstract fun notificationDao(): NotificationDao
    abstract fun disputeDao(): DisputeDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun platformSettingDao(): PlatformSettingDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun userPayoutAccountDao(): UserPayoutAccountDao
    abstract fun locationLogDao(): LocationLogDao
    abstract fun merchantGatewayDao(): MerchantGatewayDao
    abstract fun userKycDocumentDao(): UserKycDocumentDao
    abstract fun providerStoreDao(): ProviderStoreDao

    companion object {
        @Volatile
        private var INSTANCE: ServexaDatabase? = null

        fun getInstance(context: Context): ServexaDatabase = getDatabase(context)

        fun getDatabase(context: Context): ServexaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ServexaDatabase::class.java,
                    "servexa_marketplace.db"
                )
                .fallbackToDestructiveMigration(true)
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        fun hashPassword(password: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    SeedDataHelper.seedInitialData(database)
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    SeedDataHelper.ensureSeedData(database)
                }
            }
        }
    }
}
