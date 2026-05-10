package com.hastakala.shop.data.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.RequestOptions
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
    init {
        android.util.Log.d("GeminiDebug", "AIRepository initialized")
    }

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = "" // TODO: Add your Gemini API Key here
    )

    suspend fun getBusinessResponse(userMessage: String, language: String): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = "" // TODO: Use BuildConfig or secure way for production
        if (apiKey.isBlank()) {
            // For now, return a placeholder or handle the error gracefully
            return@withContext Result.failure(Exception("Gemini API Key is not configured. Please add it to BuildConfig or local.properties."))
        }
        
        try {
            android.util.Log.d("GeminiDebug", "Preparing Gemini request...")
            val products = shopRepository.observeProducts().first()
            val topProducts = shopRepository.topProducts(0) 
            val colors = shopRepository.colorBreakdown(0)
            val revenue = shopRepository.revenueFrom(0)
            val profit = shopRepository.profitFrom(0)

            val businessContext = BusinessContext(
                topSellingProducts = topProducts.map { "${it.productName} (${it.totalQty} sold)" },
                lowStockProducts = products.filter { it.stock <= it.lowStockThreshold }.map { "${it.name} (${it.stock} left)" },
                colorTrends = colors.map { "${it.color} (${it.totalQty})" },
                totalRevenue = revenue,
                totalProfit = profit,
                slowMovingProducts = products.filter { p -> topProducts.none { it.productName == p.name } }.map { it.name },
                appLanguage = language
            )

            val systemPrompt = contextBuilder.buildSystemPrompt(businessContext)

            android.util.Log.d("GeminiDebug", "Sending request to Gemini model...")
            val response = generativeModel.generateContent(
                content {
                    text(systemPrompt)
                    text("User question: $userMessage")
                }
            )
            
            val responseText = response.text
            android.util.Log.d("GeminiDebug", "Response received successfully")
            
            if (responseText.isNullOrBlank()) {
                android.util.Log.w("GeminiDebug", "Empty response text")
                Result.failure(Exception("Gemini service returned no response text"))
            } else {
                Result.success(responseText)
            }
        } catch (e: Exception) {
            val errorMessage = e.message ?: "Unknown error"
            val stackTrace = e.stackTraceToString()
            android.util.Log.e("GeminiError", "Exception during Gemini request: $errorMessage")
            android.util.Log.e("GeminiError", "Stacktrace: $stackTrace")
            
            val customException = when {
                errorMessage.contains("401") || errorMessage.contains("403") || errorMessage.contains("API_KEY_INVALID") -> 
                    Exception("Invalid Gemini API Key")
                errorMessage.contains("429") || errorMessage.contains("quota") -> 
                    Exception("Quota exceeded")
                errorMessage.contains("500") || errorMessage.contains("503") || errorMessage.contains("unavailable") -> 
                    Exception("Gemini service unavailable")
                errorMessage.contains("timeout") || errorMessage.contains("Timed out") -> 
                    Exception("Connection timeout")
                errorMessage.contains("No address associated with hostname") || errorMessage.contains("Unable to resolve host") ->
                    Exception("No internet connection")
                else -> e
            }
            Result.failure(customException)
        }
    }
}