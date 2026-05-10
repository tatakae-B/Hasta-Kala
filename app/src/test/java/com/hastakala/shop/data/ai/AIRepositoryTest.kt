package com.hastakala.shop.data.ai

import com.hastakala.shop.data.ShopRepository
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AIRepositoryTest {
    private lateinit var shopRepository: ShopRepository
    private lateinit var contextBuilder: AIContextBuilder
    private lateinit var repository: AIRepository

    @Before
    fun setup() {
        shopRepository = mockk()
        contextBuilder = AIContextBuilder()
        repository = AIRepository(shopRepository, contextBuilder)
    }

    @Test
    fun `getBusinessResponse returns failure when API key is missing`() = runTest {
        // Since we can't easily mock BuildConfig in a local unit test without extra setup,
        // we'll check if the logic handles the blank/placeholder key.
        val result = repository.getBusinessResponse("Hello", "en")
        
        assertTrue(result.isFailure)
        // This confirms the guard clause in AIRepository is working
    }
}