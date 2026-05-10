package com.hastakala.shop.viewmodel.ai

import com.hastakala.shop.data.ai.AIRepository
import com.hastakala.shop.data.ai.BusinessContext
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AIViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var aiRepository: AIRepository
    private lateinit var viewModel: AIViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        aiRepository = mockk()
        viewModel = AIViewModel(aiRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadBusinessContext updates state when repository returns data`() = runTest {
        val mockContext = BusinessContext(
            topSellingProducts = emptyList(),
            lowStockProducts = emptyList(),
            colorTrends = emptyList(),
            totalRevenue = 1000.0,
            totalProfit = 200.0,
            slowMovingProducts = emptyList(),
            appLanguage = "en",
            quickActions = listOf("Action 1")
        )
        coEvery { aiRepository.getBusinessContext("en") } returns mockContext

        viewModel.loadBusinessContext("en")
        advanceUntilIdle()

        assertNotNull(viewModel.businessContext.value)
        assertEquals(1000.0, viewModel.businessContext.value?.totalRevenue)
        assertEquals(listOf("Action 1"), viewModel.businessContext.value?.quickActions)
    }

    @Test
    fun `sendMessage adds user message immediately`() = runTest {
        coEvery { aiRepository.getBusinessResponse(any(), any()) } returns Result.success("AI Response")

        viewModel.sendMessage("Hello", "en")
        
        assertEquals(1, viewModel.chatMessages.value.size)
        assertEquals("Hello", viewModel.chatMessages.value[0].text)
        assertEquals(true, viewModel.chatMessages.value[0].isUser)
    }

    @Test
    fun `sendMessage adds AI response after loading`() = runTest {
        coEvery { aiRepository.getBusinessResponse("Hello", "en") } returns Result.success("AI Response")

        viewModel.sendMessage("Hello", "en")
        advanceUntilIdle()

        assertEquals(2, viewModel.chatMessages.value.size)
        assertEquals("AI Response", viewModel.chatMessages.value[1].text)
        assertEquals(false, viewModel.chatMessages.value[1].isUser)
    }
}
