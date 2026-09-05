package com.example

import com.example.data.local.entity.ProviderStoreEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SubdomainAndStorefrontTest {

    @Test
    fun `test subdomain formatting sanitizes string correctly`() {
        val rawInput = "  Marcus Vance & Co. #1 Power!  "
        val sanitized = rawInput.trim().lowercase()
            .replace(" ", "-")
            .replace(Regex("[^a-z0-9-]"), "")
            .replace(Regex("-+"), "-")
            .trim('-')

        assertEquals("marcus-vance-co-1-power", sanitized)
    }

    @Test
    fun `test store monthly fee is five dollars per month`() {
        val store = ProviderStoreEntity(
            id = "store_test_01",
            providerId = "prov_test_01",
            providerName = "Test Electrician",
            subdomain = "test-electric",
            storeTitle = "Test Electric Official Store",
            monthlyFee = 5.0
        )

        assertEquals(5.0, store.monthlyFee, 0.001)
        assertTrue(store.isActive)
        assertEquals("test-electric", store.subdomain)
        val expectedWebUrl = "https://${store.subdomain}.servexa.com"
        assertEquals("https://test-electric.servexa.com", expectedWebUrl)
    }

    @Test
    fun `test royal blue theme default hex in store entity`() {
        val store = ProviderStoreEntity(
            id = "store_test_02",
            providerId = "prov_test_02",
            providerName = "Elena Clean",
            subdomain = "elena-clean",
            storeTitle = "Elena Cleaning Store"
        )

        assertEquals("#1D4ED8", store.themeColorHex)
    }
}
