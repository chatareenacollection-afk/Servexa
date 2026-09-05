package com.example.data.local.db

import com.example.data.local.entity.*

object SeedDataHelper {
    suspend fun seedInitialData(db: ServexaDatabase) {
        ensureSeedData(db)
    }

    suspend fun ensureSeedData(db: ServexaDatabase) {
        val adminPasswordHash = ServexaDatabase.hashPassword("Piratesworld123$$")

        // 1. Special Admin Account (Admin logs in via standard sign-in)
        val existingAdmin = db.userDao().getUserById("admin_root_1")
        if (existingAdmin == null) {
            val admin = UserEntity(
                id = "admin_root_1",
                role = "ADMIN",
                name = "Mr-Pirate",
                email = "admin@servexa.com",
                phone = "+1 (800) 555-0199",
                passwordHash = adminPasswordHash,
                profileImage = "",
                status = "ACTIVE",
                verificationStatus = "VERIFIED"
            )
            db.userDao().insertUser(admin)
        }

        // 2. Comprehensive Service & Marketplace Categories
        val categories = listOf(
            CategoryEntity(id = "cat_restaurant", name = "Restaurants & Food", slug = "restaurants", description = "Table bookings, chef dine-in, gourmet meals & food delivery", iconName = "Restaurant", orderIndex = 1),
            CategoryEntity(id = "cat_hotels", name = "Hotels & Stays", slug = "hotels", description = "Luxury suites, boutique hotel stays & room reservations", iconName = "Hotel", orderIndex = 2),
            CategoryEntity(id = "cat_taxis", name = "Taxis & Cabs", slug = "taxis", description = "City cab rides, airport transfer & point-to-point car rides", iconName = "LocalTaxi", orderIndex = 3),
            CategoryEntity(id = "cat_bikes", name = "Bike & Moto Rides", slug = "bikes", description = "Fast 2-wheeler moto rides, scooters & express user transit", iconName = "TwoWheeler", orderIndex = 4),
            CategoryEntity(id = "cat_userride", name = "User Ride Hailing", slug = "user-rides", description = "On-demand peer-to-peer rides, carpools & city trips", iconName = "DirectionsCar", orderIndex = 5),
            CategoryEntity(id = "cat_buysell", name = "Buy & Sell Marketplace", slug = "buy-sell", description = "Buy & sell vehicles, gadgets, furniture, phones & goods", iconName = "Storefront", orderIndex = 6),
            CategoryEntity(id = "cat_itspecialist", name = "IT Specialists", slug = "it-specialist", description = "Software devs, cybersecurity, network setup & PC repair", iconName = "Computer", orderIndex = 7),
            CategoryEntity(id = "cat_doctors", name = "Doctors & Healthcare", slug = "doctors", description = "Certified physicians, medical consults, clinics & telehealth", iconName = "MedicalServices", orderIndex = 8),
            CategoryEntity(id = "cat_veterinary", name = "Veterinary Doctors", slug = "veterinary", description = "Pet veterinarians, vaccinations, animal health & surgery", iconName = "Pets", orderIndex = 9),
            CategoryEntity(id = "cat_elec", name = "Electrical", slug = "electrical", description = "Electrician, wiring, panel, fixtures", iconName = "Bolt", orderIndex = 10),
            CategoryEntity(id = "cat_plumb", name = "Plumbing", slug = "plumbing", description = "Leaks, pipes, drainage, heaters", iconName = "WaterDrop", orderIndex = 11),
            CategoryEntity(id = "cat_clean", name = "Cleaning", slug = "cleaning", description = "Deep home cleaning, sanitation", iconName = "CleaningServices", orderIndex = 12),
            CategoryEntity(id = "cat_hvac", name = "HVAC & AC", slug = "hvac", description = "Air conditioning, heating, duct cleaning", iconName = "AcUnit", orderIndex = 13),
            CategoryEntity(id = "cat_carp", name = "Carpentry", slug = "carpentry", description = "Woodwork, doors, cabinets, repairs", iconName = "Handyman", orderIndex = 14),
            CategoryEntity(id = "cat_paint", name = "Painting", slug = "painting", description = "Interior & exterior wall painting", iconName = "FormatPaint", orderIndex = 15),
            CategoryEntity(id = "cat_appliance", name = "Appliances", slug = "appliances", description = "Refrigerators, washers, ovens fix", iconName = "HomeRepairService", orderIndex = 16),
            CategoryEntity(id = "cat_auto", name = "Auto Mobile", slug = "auto", description = "Mobile mechanic, oil, diagnostics", iconName = "CarRepair", orderIndex = 17),
            CategoryEntity(id = "cat_garden", name = "Gardening", slug = "gardening", description = "Lawn care, landscaping, trimming", iconName = "Yard", orderIndex = 18),
            CategoryEntity(id = "cat_beauty", name = "Beauty & Salon", slug = "beauty", description = "Hair styling, makeup, manicure, facials & spa", iconName = "Face", orderIndex = 19),
            CategoryEntity(id = "cat_moving", name = "Movers & Logistics", slug = "moving", description = "Home moving, furniture hauling & courier delivery", iconName = "LocalShipping", orderIndex = 20),
            CategoryEntity(id = "cat_roofing", name = "Roofing & Siding", slug = "roofing", description = "Roof repairs, shingles, guttering & waterproofing", iconName = "Roofing", orderIndex = 21),
            CategoryEntity(id = "cat_locksmith", name = "Locksmith & Security", slug = "locksmith", description = "Emergency lockout, smart locks, key duplication", iconName = "Lock", orderIndex = 22),
            CategoryEntity(id = "cat_pest", name = "Pest Control", slug = "pest-control", description = "Termite, rodent, bug extermination & fumigation", iconName = "BugReport", orderIndex = 23),
            CategoryEntity(id = "cat_laundry", name = "Laundry & Dry Clean", slug = "laundry", description = "Wash, fold, iron & dry cleaning doorstep service", iconName = "LocalLaundryService", orderIndex = 24),
            CategoryEntity(id = "cat_tutoring", name = "Tutoring & Lessons", slug = "tutoring", description = "Academic tutoring, language lessons & music coaching", iconName = "School", orderIndex = 25),
            CategoryEntity(id = "cat_photo", name = "Photography & Video", slug = "photography", description = "Portrait, wedding, event & commercial media shooting", iconName = "PhotoCamera", orderIndex = 26),
            CategoryEntity(id = "cat_event", name = "Events & Party", slug = "events", description = "Event coordinators, DJ setup, decoration & party planning", iconName = "Celebration", orderIndex = 27),
            CategoryEntity(id = "cat_legal", name = "Legal & Consulting", slug = "legal", description = "Contract review, business consulting & notary services", iconName = "Gavel", orderIndex = 28),
            CategoryEntity(id = "cat_solar", name = "Solar & Energy", slug = "solar", description = "Solar panel installation, battery backup & energy audit", iconName = "WbSunny", orderIndex = 29),
            CategoryEntity(id = "cat_childcare", name = "Childcare & Babysitting", slug = "childcare", description = "Certified babysitters, nanny care & child supervision", iconName = "ChildCare", orderIndex = 30)
        )
        db.categoryDao().insertCategories(categories)

        // Clean up any previously seeded mock data providers, mock services, and mock products
        val mockProviderIds = listOf(
            "prov_rest_1", "prov_hotel_1", "prov_taxi_1", "prov_bike_1",
            "prov_it_1", "prov_doc_1", "prov_vet_1", "prov_elec_1"
        )
        for (id in mockProviderIds) {
            db.userDao().deleteUser(id)
            val services = listOf(
                "srv_rest_1", "srv_rest_2", "srv_rest_3",
                "srv_hotel_1", "srv_hotel_2", "srv_hotel_3",
                "srv_taxi_1", "srv_taxi_2", "srv_taxi_3",
                "srv_bike_1", "srv_bike_2", "srv_bike_3",
                "srv_it_1", "srv_it_2", "srv_it_3",
                "srv_doc_1", "srv_doc_2", "srv_doc_3",
                "srv_vet_1", "srv_vet_2", "srv_vet_3",
                "srv_elec_1", "srv_elec_2", "srv_elec_3"
            )
            for (srvId in services) {
                db.serviceDao().deleteService(srvId)
            }
        }
        val mockProductIds = listOf("prod_1", "prod_2", "prod_3", "prod_4", "prod_5", "prod_6", "prod_7", "prod_8")
        for (pId in mockProductIds) {
            db.productDao().deleteProduct(pId)
        }

        // 3. Platform Configuration Settings
        val settings = listOf(
            PlatformSettingEntity("topup_fee_percent", "5.0", "Wallet Top-up Fee % deducted from customer deposit"),
            PlatformSettingEntity("service_commission_percent", "6.0", "Platform commission % deducted upon completed service"),
            PlatformSettingEntity("call_rate_per_min", "0.0", "Secure platform voice call rate per minute"),
            PlatformSettingEntity("currency", "$", "Platform currency symbol"),
            PlatformSettingEntity("emergency_service_enabled", "true", "Enable emergency provider dispatch feature"),
            PlatformSettingEntity("withdrawal_window_hours", "48", "Withdrawal processing window in hours")
        )
        db.platformSettingDao().insertSettings(settings)

        // 4. Default Top-Up & Payment Methods (Can be dynamically edited/added by Admin)
        val defaultPaymentMethods = listOf(
            PaymentMethodEntity(
                id = "pm_bank_transfer",
                name = "Direct Bank Wire / ACH Transfer",
                type = "BANK_TRANSFER",
                accountTitle = "Servexa Marketplace Escrow LLC",
                accountNumber = "US89 3704 0044 0532 0130 00",
                bankOrProviderName = "JPMorgan Chase Bank, N.A.",
                routingOrSwift = "Routing: 021000021 | SWIFT: CHASUS33",
                instructions = "Transfer funds to the official bank account above. Include your User ID or Email in the transfer memo for automated verification.",
                minAmount = 20.0,
                maxAmount = 25000.0,
                feePercent = 5.0,
                active = true,
                orderIndex = 1
            ),
            PaymentMethodEntity(
                id = "pm_card_gateway",
                name = "Credit & Debit Cards (Visa / MC / Amex)",
                type = "CARD",
                accountTitle = "Servexa Secure Gateway",
                accountNumber = "Instant Card Processing",
                bankOrProviderName = "Stripe & Visa Merchant Network",
                routingOrSwift = "3D Secure Encrypted",
                instructions = "Instant card authorization. Funds are credited immediately after 3D Secure verification.",
                minAmount = 10.0,
                maxAmount = 5000.0,
                feePercent = 5.0,
                active = true,
                orderIndex = 2
            ),
            PaymentMethodEntity(
                id = "pm_crypto_usdt",
                name = "Crypto USDT (TRC20 / ERC20)",
                type = "CRYPTO",
                accountTitle = "Servexa Treasury Wallet",
                accountNumber = "TYD1e3BqZ9QJzW5o6k2V48R7X1mKpLmNv8",
                bankOrProviderName = "Tether Escrow Smart Contract",
                routingOrSwift = "Network: TRON (TRC20) / Ethereum (ERC20)",
                instructions = "Send only USDT TRC20 to this address. Enter your transaction hash (TXID) below for admin confirmation.",
                minAmount = 25.0,
                maxAmount = 50000.0,
                feePercent = 5.0,
                active = true,
                orderIndex = 3
            ),
            PaymentMethodEntity(
                id = "pm_paypal_digital",
                name = "PayPal & Digital Wallet",
                type = "PAYPAL",
                accountTitle = "Servexa Global Holdings",
                accountNumber = "payments@servexa.com",
                bankOrProviderName = "PayPal Business Services",
                routingOrSwift = "Instant Settlement",
                instructions = "Send payment to payments@servexa.com. Keep your PayPal transaction reference ID ready.",
                minAmount = 15.0,
                maxAmount = 3000.0,
                feePercent = 5.0,
                active = true,
                orderIndex = 4
            ),
            PaymentMethodEntity(
                id = "pm_cash_agent",
                name = "Cash & Authorized Agent Deposit",
                type = "CASH",
                accountTitle = "Servexa Local Partner Agents",
                accountNumber = "Agent ID: SVX-AGENT-0091",
                bankOrProviderName = "Servexa City Hub Deposit Counter",
                routingOrSwift = "PIN: 4892",
                instructions = "Deposit physical cash with your verified agent and present your digital receipt voucher.",
                minAmount = 10.0,
                maxAmount = 2000.0,
                feePercent = 5.0,
                active = true,
                orderIndex = 5
            )
        )
        db.paymentMethodDao().insertPaymentMethods(defaultPaymentMethods)

        // 5. Merchant Gateway Accounts for Admin Panel (Configurable in Admin Console)
        val defaultGateways = listOf(
            MerchantGatewayAccountEntity(
                id = "gw_stripe_primary",
                name = "Stripe Merchant Gateway",
                gatewayType = "STRIPE",
                merchantAccountId = "acct_1Nz828xServexaMerc",
                publicKeyOrClientId = "pk_live_51Mv9K2eServexaMerchant992",
                secretKeyOrApiKey = "sk_live_51Mv9K2eServexaSecretKey_PROD_SECURE",
                webhookSecret = "whsec_984f882194c391290bb8a12c40",
                isLiveMode = true,
                autoCapture = true,
                captureCustomerDetails = true,
                settlementCurrency = "USD",
                platformFeePercent = 5.0,
                payoutDelayDays = 2,
                isActive = true,
                isDefault = true,
                webhookEndpointUrl = "https://api.servexa.com/v1/payments/webhooks/stripe"
            ),
            MerchantGatewayAccountEntity(
                id = "gw_paypal_biz",
                name = "PayPal Commerce & Business",
                gatewayType = "PAYPAL",
                merchantAccountId = "merchant-vault@servexapay.com",
                publicKeyOrClientId = "Aec91K_Client_PayPal_ServexaLive_ID",
                secretKeyOrApiKey = "ELp4_Secret_PayPal_ServexaLive_Token",
                webhookSecret = "WH-908129038-PYPL",
                isLiveMode = true,
                autoCapture = true,
                captureCustomerDetails = true,
                settlementCurrency = "USD",
                platformFeePercent = 5.0,
                payoutDelayDays = 2,
                isActive = true,
                isDefault = false,
                webhookEndpointUrl = "https://api.servexa.com/v1/payments/webhooks/paypal"
            ),
            MerchantGatewayAccountEntity(
                id = "gw_razorpay_live",
                name = "Razorpay Merchant Network",
                gatewayType = "RAZORPAY",
                merchantAccountId = "acc_RZPServexaGlobal",
                publicKeyOrClientId = "rzp_live_K38J91kLm09",
                secretKeyOrApiKey = "rzp_sec_991823901kLxMbP",
                webhookSecret = "rzp_whsec_8849102",
                isLiveMode = true,
                autoCapture = true,
                captureCustomerDetails = true,
                settlementCurrency = "USD",
                platformFeePercent = 5.0,
                payoutDelayDays = 2,
                isActive = true,
                isDefault = false,
                webhookEndpointUrl = "https://api.servexa.com/v1/payments/webhooks/razorpay"
            ),
            MerchantGatewayAccountEntity(
                id = "gw_escrow_chase",
                name = "Servexa Escrow Treasury Bank",
                gatewayType = "BANK_ESCROW",
                merchantAccountId = "US89370400440532013000",
                publicKeyOrClientId = "CHASUS33-ROUTING-021000021",
                secretKeyOrApiKey = "CORP-TREASURY-AUTH-9092",
                webhookSecret = "swift_sec_chase_99",
                isLiveMode = true,
                autoCapture = true,
                captureCustomerDetails = true,
                settlementCurrency = "USD",
                platformFeePercent = 5.0,
                payoutDelayDays = 2,
                isActive = true,
                isDefault = false,
                webhookEndpointUrl = "https://api.servexa.com/v1/payments/webhooks/bank_wire"
            )
        )
        db.merchantGatewayDao().insertMerchantGateways(defaultGateways)

        // 6. Default Customer User (Alex Johnson) & Customer Wallet
        val existingCust = db.userDao().getUserById("usr_customer_01")
        if (existingCust == null) {
            val customer = UserEntity(
                id = "usr_customer_01",
                role = "CUSTOMER",
                name = "Alex Johnson",
                email = "alex@customer.com",
                phone = "+1 (555) 234-5678",
                passwordHash = ServexaDatabase.hashPassword("customer123"),
                profileImage = "",
                status = "ACTIVE",
                verificationStatus = "PENDING"
            )
            db.userDao().insertUser(customer)

            val customerWallet = WalletEntity(
                id = "wall_usr_customer_01",
                userId = "usr_customer_01",
                availableBalance = 350.0,
                pendingBalance = 0.0
            )
            db.walletDao().insertWallet(customerWallet)

            // Seed initial transactions with full gateway & POS metadata
            db.walletDao().insertTransaction(
                WalletTransactionEntity(
                    id = "TXN-POS-89210",
                    walletId = "wall_usr_customer_01",
                    userId = "usr_customer_01",
                    type = "TOP_UP",
                    grossAmount = 200.0,
                    fee = 10.0,
                    netAmount = 190.0,
                    status = "COMPLETED",
                    referenceId = "POS-V400M-REF-9921",
                    note = "In-Store POS Terminal Top-Up Deposit",
                    customerName = "Alex Johnson",
                    customerEmail = "alex@customer.com",
                    customerPhone = "+1 (555) 234-5678",
                    customerAddress = "Market Street & 4th Ave, San Francisco, CA",
                    merchantGatewayName = "Square POS Terminal Station",
                    merchantAccountId = "pos_terminal_sf_hub_01",
                    merchantCaptureRef = "AUTH_POS_983192",
                    captureStatus = "CAPTURED",
                    paymentChannel = "POS_TERMINAL",
                    posTerminalId = "POS-TERM-SF-01",
                    posLocation = "Servexa Downtown Hub, San Francisco",
                    posAgentName = "David Vance (Certified Agent)",
                    posAuthCode = "APPR-983192",
                    createdAt = System.currentTimeMillis() - 86400000L
                )
            )

            db.walletDao().insertTransaction(
                WalletTransactionEntity(
                    id = "TXN-STRIPE-40291",
                    walletId = "wall_usr_customer_01",
                    userId = "usr_customer_01",
                    type = "TOP_UP",
                    grossAmount = 150.0,
                    fee = 7.50,
                    netAmount = 142.50,
                    status = "COMPLETED",
                    referenceId = "ch_3N8291048291039",
                    note = "Online Visa Card Payment Gateway",
                    customerName = "Alex Johnson",
                    customerEmail = "alex@customer.com",
                    customerPhone = "+1 (555) 234-5678",
                    customerAddress = "Market Street & 4th Ave, San Francisco, CA",
                    merchantGatewayName = "Stripe Merchant Gateway",
                    merchantAccountId = "acct_1Nz828xServexaMerc",
                    merchantCaptureRef = "pi_3N8291048291039_secret_9921",
                    captureStatus = "CAPTURED",
                    paymentChannel = "GATEWAY",
                    createdAt = System.currentTimeMillis() - 172800000L
                )
            )
        }

        // 7. Seed Sample KYC Documents for Admin Review
        val existingKyc = db.userKycDocumentDao().getKycDocumentById("KYC-88201")
        if (existingKyc == null) {
            val kyc1 = UserKycDocumentEntity(
                id = "KYC-88201",
                userId = "usr_customer_01",
                userName = "Alex Johnson",
                userEmail = "alex@customer.com",
                userPhone = "+1 (555) 234-5678",
                documentType = "DRIVING_LICENSE",
                documentNumber = "DL-CA-9938472-X",
                issuingCountry = "United States",
                issuingStateOrProvince = "California",
                expiryDate = "2029-08-20",
                dateOfBirth = "1993-04-12",
                residentialAddress = "Market Street & 4th Ave, San Francisco, CA",
                documentFrontImage = "dl_front_preview",
                documentBackImage = "dl_back_preview",
                selfieImage = "selfie_liveness_verified",
                verificationStatus = "PENDING",
                rejectionReason = "",
                adminNotes = "High resolution photo submitted. Address matches GPS location.",
                submittedAt = System.currentTimeMillis() - 3600000L
            )
            db.userKycDocumentDao().insertKycDocument(kyc1)

            val kyc2 = UserKycDocumentEntity(
                id = "KYC-88202",
                userId = "usr_cust_marcus",
                userName = "Marcus Vance",
                userEmail = "marcus.vance@example.com",
                userPhone = "+1 (555) 778-9901",
                documentType = "PASSPORT",
                documentNumber = "PASS-USA-89401928",
                issuingCountry = "United States",
                issuingStateOrProvince = "New York",
                expiryDate = "2032-11-15",
                dateOfBirth = "1988-09-24",
                residentialAddress = "Midtown East, New York, NY",
                documentFrontImage = "passport_front_preview",
                documentBackImage = "",
                selfieImage = "selfie_marcus_vance",
                verificationStatus = "VERIFIED",
                rejectionReason = "",
                adminNotes = "Approved by Admin Mr-Pirate on 2026-08-20",
                reviewedByAdminId = "admin_root_1",
                reviewedAt = System.currentTimeMillis() - 86400000L,
                submittedAt = System.currentTimeMillis() - 172800000L
            )
            db.userKycDocumentDao().insertKycDocument(kyc2)
        }

        // 8. Seed Verified Providers & Active Store Subdomains ($5/mo)
        val provUser = db.userDao().getUserById("prov_marcus_1")
        if (provUser == null) {
            val marcus = UserEntity(
                id = "prov_marcus_1",
                role = "PROVIDER",
                name = "Marcus Vance",
                email = "marcus@electric.com",
                phone = "+1 (555) 345-6789",
                passwordHash = ServexaDatabase.hashPassword("provider123"),
                profileImage = "",
                status = "ACTIVE",
                verificationStatus = "VERIFIED"
            )
            db.userDao().insertUser(marcus)

            val marcusProfile = ProviderProfileEntity(
                id = "prof_prov_marcus_1",
                userId = "prov_marcus_1",
                title = "Master Certified Electrician & Smart Panel Specialist",
                bio = "Licensed Electrical Contractor with 12+ years expertise in commercial & residential power, EV chargers & breaker panels.",
                locationName = "San Francisco & Bay Area, CA",
                serviceArea = "30 km radius",
                workingHours = "Mon - Sat: 7:00 AM - 8:00 PM",
                emergencyAvailable = true,
                rating = 4.96,
                reviewCount = 58,
                completedJobs = 142,
                verificationStatus = "VERIFIED",
                verificationDocuments = "State Contractor License C-10 #984210, General Liability $2M"
            )
            db.providerProfileDao().insertProfile(marcusProfile)

            val marcusWallet = WalletEntity(
                id = "wall_prov_marcus_1",
                userId = "prov_marcus_1",
                availableBalance = 620.0,
                pendingBalance = 85.0
            )
            db.walletDao().insertWallet(marcusWallet)

            val srv1 = ServiceEntity(
                id = "srv_marcus_1",
                providerId = "prov_marcus_1",
                categoryId = "cat_elec",
                subcategoryName = "Emergency",
                title = "Emergency Electrical Diagnostic & Power Restoration",
                description = "Complete diagnostic inspection of circuit breaker trips, burnt outlets, wiring faults & immediate safe power recovery.",
                price = 120.0,
                durationMinutes = 60,
                active = true
            )
            val srv2 = ServiceEntity(
                id = "srv_marcus_2",
                providerId = "prov_marcus_1",
                categoryId = "cat_elec",
                subcategoryName = "EV Chargers",
                title = "Level 2 EV Fast Charger Station Installation",
                description = "Dedicated 240V 50A breaker circuit run, conduit, NEMA 14-50 outlet or hardwired Tesla / Universal charger mounting.",
                price = 380.0,
                durationMinutes = 180,
                active = true
            )
            db.serviceDao().insertService(srv1)
            db.serviceDao().insertService(srv2)

            // Seed active subdomain store ($5/month)
            val marcusStore = ProviderStoreEntity(
                id = "store_prov_marcus_1",
                providerId = "prov_marcus_1",
                providerName = "Marcus Vance",
                subdomain = "marcus-electric",
                storeTitle = "Vance Electric & Power Systems",
                tagline = "Licensed Master Electrical Contractor • EV & Smart Panels",
                aboutBio = "Fast, code-compliant residential and commercial electrical solutions across the Bay Area.",
                category = "Electrical & Power",
                themeColorHex = "#1D4ED8",
                contactPhone = "+1 (555) 345-6789",
                contactEmail = "marcus@electric.com",
                operatingHours = "Mon - Sat: 7:00 AM - 8:00 PM (24/7 Emergency Dispatch)",
                monthlyFee = 5.0,
                subscribedAt = System.currentTimeMillis() - 86400000L * 10,
                nextBillingDate = System.currentTimeMillis() + 86400000L * 20,
                isActive = true,
                totalOrdersFromSubdomain = 34,
                totalVisitors = 412
            )
            db.providerStoreDao().insertOrUpdateStore(marcusStore)
        }

        val elenaUser = db.userDao().getUserById("prov_clean_1")
        if (elenaUser == null) {
            val elena = UserEntity(
                id = "prov_clean_1",
                role = "PROVIDER",
                name = "Elena Rostova",
                email = "elena@cleaning.com",
                phone = "+1 (555) 890-1234",
                passwordHash = ServexaDatabase.hashPassword("provider123"),
                profileImage = "",
                status = "ACTIVE",
                verificationStatus = "VERIFIED"
            )
            db.userDao().insertUser(elena)

            val elenaProfile = ProviderProfileEntity(
                id = "prof_prov_clean_1",
                userId = "prov_clean_1",
                title = "Eco-Friendly Deep House & Office Cleaning Pro",
                bio = "Professional eco-friendly sanitation expert with 8+ years trusted experience.",
                locationName = "San Francisco, CA",
                serviceArea = "25 km radius",
                workingHours = "Mon - Fri: 8:00 AM - 6:00 PM",
                emergencyAvailable = false,
                rating = 4.98,
                reviewCount = 82,
                completedJobs = 195,
                verificationStatus = "VERIFIED",
                verificationDocuments = "Bonded & Insured Cleaners Union #4401"
            )
            db.providerProfileDao().insertProfile(elenaProfile)

            val elenaWallet = WalletEntity(
                id = "wall_prov_clean_1",
                userId = "prov_clean_1",
                availableBalance = 410.0,
                pendingBalance = 0.0
            )
            db.walletDao().insertWallet(elenaWallet)

            val srv3 = ServiceEntity(
                id = "srv_clean_1",
                providerId = "prov_clean_1",
                categoryId = "cat_clean",
                subcategoryName = "Deep Clean",
                title = "Deep Home Eco-Sanitization & Move-Out Clean",
                description = "Complete 360-degree deep cleaning of kitchens, appliances, bathrooms, baseboards & hardwood floor polishing.",
                price = 160.0,
                durationMinutes = 150,
                active = true
            )
            db.serviceDao().insertService(srv3)

            val elenaStore = ProviderStoreEntity(
                id = "store_prov_clean_1",
                providerId = "prov_clean_1",
                providerName = "Elena Rostova",
                subdomain = "elena-cleaning",
                storeTitle = "Elena's Eco Cleaning Studio",
                tagline = "Spotless, Non-Toxic Home & Commercial Sanitizing",
                aboutBio = "Providing pristine, child-safe & pet-safe cleanliness with 100% satisfaction guarantee.",
                category = "Cleaning & Maid",
                themeColorHex = "#0D9488",
                contactPhone = "+1 (555) 890-1234",
                contactEmail = "elena@cleaning.com",
                operatingHours = "Mon - Fri: 8:00 AM - 6:00 PM",
                monthlyFee = 5.0,
                subscribedAt = System.currentTimeMillis() - 86400000L * 5,
                nextBillingDate = System.currentTimeMillis() + 86400000L * 25,
                isActive = true,
                totalOrdersFromSubdomain = 28,
                totalVisitors = 389
            )
            db.providerStoreDao().insertOrUpdateStore(elenaStore)
        }
    }
}
