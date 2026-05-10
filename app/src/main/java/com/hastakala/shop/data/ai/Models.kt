package com.hastakala.shop.data.ai

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class BusinessContext(
    val topSellingProducts: List<String>,
    val lowStockProducts: List<String>,
    val colorTrends: List<String>,
    val totalRevenue: Double,
    val totalProfit: Double,
    val slowMovingProducts: List<String>,
    val appLanguage: String,
    val quickActions: List<String> = emptyList()
)