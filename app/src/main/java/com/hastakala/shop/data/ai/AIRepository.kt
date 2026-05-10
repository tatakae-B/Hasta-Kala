package com.hastakala.shop.data.ai

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.hastakala.shop.BuildConfig
import com.hastakala.shop.data.ShopRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIRepository @Inject constructor(
    private val shopRepository: ShopRepository,
    private val contextBuilder: AIContextBuilder
) {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun getBusinessContext(language: String): BusinessContext = withContext(Dispatchers.IO) {
        val products = shopRepository.observeProducts().first()
        val topProducts = shopRepository.topProducts(0)
        val colors = shopRepository.colorBreakdown(0)
        val revenue = shopRepository.revenueFrom(0)
        val profit = shopRepository.profitFrom(0)

        val lowStockItems = products.filter { it.stock <= it.lowStockThreshold }

        val quickActions = mutableListOf<String>()
        if (lowStockItems.isNotEmpty()) {
            quickActions.add("What should I restock?")
        }
        if (topProducts.isNotEmpty()) {
            quickActions.add("Best selling product?")
        }
        quickActions.add("How is my profit?")
        if (colors.isNotEmpty()) {
            quickActions.add("Popular colors?")
        }

        BusinessContext(
            topSellingProducts = topProducts.map { "${it.productName} (${it.totalQty} sold)" },
            lowStockProducts = lowStockItems.map { "${it.name} (${it.stock} left)" },
            colorTrends = colors.map { "${it.color} (${it.totalQty})" },
            totalRevenue = revenue,
            totalProfit = profit,
            slowMovingProducts = products.filter { p -> topProducts.none { it.productName == p.name } }.map { it.name },
            appLanguage = language,
            quickActions = quickActions
        )
    }

    suspend fun getBusinessResponse(userMessage: String, language: String): Result<String> = withContext(Dispatchers.IO) {
        if (BuildConfig.GEMINI_API_KEY.isBlank() || BuildConfig.GEMINI_API_KEY == "\"\"") {
            return@withContext Result.failure(Exception("API_KEY_MISSING"))
        }

        try {
            val businessContext = getBusinessContext(language)
            val systemPrompt = contextBuilder.buildSystemPrompt(businessContext)

            val response = generativeModel.generateContent(
                content {
                    text(systemPrompt)
                    text("User question: $userMessage")
                }
            )

            val responseText = response.text
            if (responseText.isNullOrBlank()) {
                Result.failure(Exception("EMPTY_RESPONSE"))
            } else {
                Result.success(responseText)
            }
        } catch (e: Exception) {
            Log.e("AIRepository", "Gemini API Error: ${e.message}", e)
            Result.failure(e)
        }
    }
}