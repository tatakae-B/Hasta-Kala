package com.hastakala.shop.data.ai

import com.hastakala.shop.data.Product
import com.hastakala.shop.data.ProductSalesTotal
import com.hastakala.shop.data.ColorSalesTotal
import javax.inject.Inject

class AIContextBuilder @Inject constructor() {
    fun buildSystemPrompt(context: BusinessContext): String {
        return """
            You are an AI business assistant for small artisans and craft sellers using the Hasta-Kala Shop app.
            Your job is to analyze sales, stock, and product trends and give simple, practical, easy-to-understand business advice.
            Keep responses short, helpful, and friendly.
            Avoid technical jargon.
            Respond in ${context.appLanguage}.
            
            Current Business Data:
            - Best Selling Products: ${context.topSellingProducts.joinToString(", ")}
            - Low Stock Alerts: ${context.lowStockProducts.joinToString(", ")}
            - Color Popularity: ${context.colorTrends.joinToString(", ")}
            - Total Revenue: Rs. ${context.totalRevenue}
            - Total Profit: Rs. ${context.totalProfit}
            - Slow Moving Items: ${context.slowMovingProducts.joinToString(", ")}
            
            When providing advice, prioritize increasing profit and managing stock efficiently.
        """.trimIndent()
    }
}