package com.hastakala.shop.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hastakala.shop.R
import com.hastakala.shop.data.*
import com.hastakala.shop.viewmodel.ShopViewModel
import com.hastakala.shop.viewmodel.TimeFilter
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import com.hastakala.shop.ui.theme.*

data class ArtisanCategory(val nameRes: Int, val shortNameRes: Int, val items: List<String>)

fun getArtisanData() = listOf(
    ArtisanCategory(R.string.cat_pottery, R.string.cat_pottery_short, listOf("Clay pots", "Terracotta diyas", "Ceramic bowls", "Plates", "Decorative vases", "Clay idols")),
    ArtisanCategory(R.string.cat_textile, R.string.cat_textile_short, listOf("Handloom sarees", "Shawls", "Scarves", "Cotton fabrics", "Embroidered items", "Dress materials")),
    ArtisanCategory(R.string.cat_jewellery, R.string.cat_jewellery_short, listOf("Beaded necklaces", "Earrings", "Bangles", "Terracotta jewellery", "Thread jewellery")),
    ArtisanCategory(R.string.cat_wood, R.string.cat_wood_short, listOf("Wooden toys", "Carved sculptures", "Bamboo baskets", "Wooden decor items", "Small furniture")),
    ArtisanCategory(R.string.cat_art, R.string.cat_art_short, listOf("Canvas paintings", "Madhubani art", "Warli art", "Portrait sketches", "Wall art")),
    ArtisanCategory(R.string.cat_home, R.string.cat_home_short, listOf("Handmade candles", "Lamps", "Lanterns", "Wall hangings", "Decorative mirrors", "Handmade clocks")),
    ArtisanCategory(R.string.cat_eco, R.string.cat_eco_short, listOf("Handmade soaps", "Jute bags", "Paper crafts", "Bamboo items", "Eco-friendly products")),
    ArtisanCategory(R.string.cat_misc, R.string.cat_misc_short, listOf("Handmade dolls", "Soft toys", "Festival items", "Gift items", "Custom crafts"))
)

@Composable
fun CategoryLegendRow(
    category: String,
    revenue: Double,
    percentage: Float,
    color: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = category,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            
            Text(
                text = "Rs. ${"%.0f".format(revenue)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.1f),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DashboardScreen(
    totalSales: Double,
    totalProducts: Int,
    lowStockItems: Int,
    profit: Double,
    topProducts: List<ProductSalesTotal>,
    categoryBreakdown: List<CategorySalesTotal>,
    colorBreakdown: List<ColorSalesTotal>,
    slowMovingProducts: List<SlowMovingProduct>,
    filter: TimeFilter,
    selectedCategory: String?,
    isDarkMode: Boolean = false,
    isRefreshing: Boolean = false,
    onFilterChange: (TimeFilter) -> Unit,
    onCategoryChange: (String?) -> Unit,
    onSalesClick: () -> Unit,
    onProfitClick: () -> Unit,
    onTotalProductsClick: () -> Unit,
    onLowStockClick: () -> Unit,
    onRefresh: () -> Unit,
    onExportCsv: () -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = onRefresh
    )

    Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Time Filter
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()).padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimeFilter.entries.take(4).forEach { tFilter ->
                    val labelId = when(tFilter) {
                        TimeFilter.TODAY -> R.string.filter_today
                        TimeFilter.WEEK -> R.string.filter_week
                        TimeFilter.MONTH -> R.string.filter_month
                        TimeFilter.ALL -> R.string.filter_all
                        else -> R.string.label_all
                    }
                    val isSelected = filter == tFilter
                    Surface(
                        modifier = Modifier.clickable { onFilterChange(tFilter) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)) else null
                    ) {
                        Text(
                            text = stringResource(labelId),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Stats Grid
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = stringResource(R.string.stat_total_sales),
                        value = "Rs. ${"%.0f".format(totalSales)}",
                        icon = Icons.Default.Payments,
                        accentColor = TerracottaPrimary,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSalesClick() }
                    )
                    StatCard(
                        title = stringResource(R.string.stat_profit),
                        value = "Rs. ${"%.0f".format(profit)}",
                        icon = Icons.Default.TrendingUp,
                        accentColor = SuccessGreen,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onProfitClick() }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = stringResource(R.string.stat_total_products),
                        value = "$totalProducts",
                        icon = Icons.Default.Inventory,
                        accentColor = SageGreen,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onTotalProductsClick() }
                    )
                    StatCard(
                        title = stringResource(R.string.stat_low_stock),
                        value = "$lowStockItems",
                        icon = Icons.Default.Warning,
                        accentColor = if (lowStockItems > 0) ErrorRed else SuccessGreen,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onLowStockClick() }
                    )
                }
            }

            // Category Chips
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.label_categories),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        CategoryChip(
                            selected = selectedCategory == null,
                            onClick = { onCategoryChange(null) },
                            label = stringResource(R.string.label_all)
                        )
                    }
                    items(getArtisanData()) { category ->
                        val categoryName = stringResource(category.nameRes)
                        CategoryChip(
                            selected = selectedCategory == categoryName,
                            onClick = { onCategoryChange(categoryName) },
                            label = stringResource(category.shortNameRes)
                        )
                    }
                }
            }

            // Performance Overview
            if (categoryBreakdown.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(R.string.label_performance_overview),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            CategoryPieChart(
                                data = categoryBreakdown,
                                totalRevenue = totalSales,
                                isDarkMode = isDarkMode,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            val palette = listOf(TerracottaPrimary, GoldAccent, SageGreen, EarthySlate, MutedTerracotta, MutedGold, MutedGreen)

                            categoryBreakdown.forEachIndexed { index, item ->
                                val percentage = if (totalSales > 0) (item.revenue / totalSales).toFloat() else 0f
                                CategoryLegendRow(
                                    category = item.category,
                                    revenue = item.revenue,
                                    percentage = percentage,
                                    color = palette[index % palette.size]
                                )
                                if (index < categoryBreakdown.size - 1) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Top Products
            if (topProducts.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(R.string.label_top_products),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            topProducts.take(5).forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.width(24.dp)
                                        )
                                        Text(
                                            item.productName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Text(
                                        stringResource(R.string.label_sold_count, item.totalQty),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                if (index < topProducts.take(5).size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Export Action
            Button(
                onClick = onExportCsv,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    Icons.Default.Share, 
                    contentDescription = null, 
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.btn_export_csv), style = MaterialTheme.typography.labelLarge)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            contentColor = MaterialTheme.colorScheme.primary,
            backgroundColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun SalesAnalyticsScreen(
    sales: List<SaleRecord>,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val totalRevenue = sales.sumOf { it.subtotal }
        
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TOTAL REVENUE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text("Rs. ${"%.0f".format(totalRevenue)}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("${sales.size} Total Sales Recorded", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("RECENT TRANSACTIONS", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            items(sales.reversed()) { sale ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(sale.productName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            val date = java.text.SimpleDateFormat("MMM dd, yyyy • HH:mm", java.util.Locale.getDefault()).format(java.util.Date(sale.timestamp))
                            Text(date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Rs. ${sale.subtotal.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Qty: ${sale.quantity}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfitInsightsScreen(
    products: List<Product>,
    sales: List<SaleRecord>,
    totalProfit: Double,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ESTIMATED TOTAL PROFIT", style = MaterialTheme.typography.labelMedium, color = SuccessGreen)
                        Text("Rs. ${"%.0f".format(totalProfit)}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = SuccessGreen)
                        
                        val totalRevenue = sales.sumOf { it.subtotal }
                        val margin = if (totalRevenue > 0) (totalProfit / totalRevenue) * 100 else 0.0
                        Text("Overall Margin: ${"%.1f".format(margin)}%", style = MaterialTheme.typography.bodySmall, color = SuccessGreen.copy(alpha = 0.8f))
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("PROFIT BY CATEGORY", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    
                    val categoryProfit = sales.groupBy { it.category }
                        .mapValues { entry -> entry.value.sumOf { it.subtotal - (it.quantity * it.costPrice) } }
                        .toList()
                        .sortedByDescending { it.second }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            categoryProfit.forEachIndexed { index, (category, profit) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(category, style = MaterialTheme.typography.bodyMedium)
                                    Text("Rs. ${profit.toInt()}", fontWeight = FontWeight.Bold, color = SuccessGreen)
                                }
                                if (index < categoryProfit.size - 1) {
                                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("SLOW MOVING STOCK VALUE", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    
                    val stockValue = products.sumOf { it.stock * it.costPrice }
                    val potentialRevenue = products.sumOf { it.stock * it.sellingPrice }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Stock Cost", style = MaterialTheme.typography.labelSmall)
                                Text("Rs. ${stockValue.toInt()}", fontWeight = FontWeight.Bold)
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Potential Rev.", style = MaterialTheme.typography.labelSmall)
                                Text("Rs. ${potentialRevenue.toInt()}", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color = TerracottaPrimary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun LowStockScreen(
    products: List<Product>,
    onNavigateBack: () -> Unit,
    onEditProduct: (Product) -> Unit
) {
    val lowStockProducts = products.filter { it.stock <= it.lowStockThreshold }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (lowStockProducts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = SuccessGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "All items are well stocked!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Your inventory is in good shape.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "The following items are running low and may need restocking soon.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                items(lowStockProducts) { product ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEditProduct(product) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ErrorRed.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Warning, 
                                    contentDescription = null, 
                                    tint = ErrorRed,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    product.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "${product.category} • ${product.color}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "${product.stock}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = ErrorRed,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "LEFT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ErrorRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(selected: Boolean, onClick: () -> Unit, label: String) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProductListScreen(
    products: List<Product>,
    selectedCategory: String?,
    onCategoryFilterChange: (String?) -> Unit,
    onDelete: (Int) -> Unit,
    onUpdate: (Product, String, String, String) -> Unit,
    onAddProductInCategory: (String) -> Unit
) {
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    if (editingProduct != null) {
        EditProductDialog(
            product = editingProduct!!,
            onDismiss = { editingProduct = null },
            onConfirm = { cost, sell, stock ->
                onUpdate(editingProduct!!, cost, sell, stock)
                editingProduct = null
            }
        )
    }

    val displayProducts = if (selectedCategory == null) products else products.filter { it.category == selectedCategory }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Categories
        LazyRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                CategoryChip(
                    selected = selectedCategory == null,
                    onClick = { onCategoryFilterChange(null) },
                    label = stringResource(R.string.label_all)
                )
            }
            items(getArtisanData()) { category ->
                val categoryName = stringResource(category.nameRes)
                CategoryChip(
                    selected = selectedCategory == categoryName,
                    onClick = { onCategoryFilterChange(categoryName) },
                    label = stringResource(category.shortNameRes)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            val grouped = displayProducts.groupBy { it.category }
            
            grouped.forEach { (cat, items) ->
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { onAddProductInCategory(cat) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                items(items) { product ->
                    ProductItemCard(product, onEdit = { editingProduct = it }, onDelete = onDelete)
                }
            }
        }
    }
}

@Composable
fun ProductItemCard(product: Product, onEdit: (Product) -> Unit, onDelete: (Int) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onEdit(product) },
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = product.name.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${product.color} • ${stringResource(R.string.label_stock_qty, product.stock)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (product.stock <= product.lowStockThreshold) ErrorRed else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Rs. ${product.sellingPrice.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(
                    onClick = { onDelete(product.id) },
                    modifier = Modifier.size(20.dp).offset(x = 8.dp)
                ) {
                    Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
fun EditProductDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var cost by remember { mutableStateOf(product.costPrice.toString()) }
    var sell by remember { mutableStateOf(product.sellingPrice.toString()) }
    var stock by remember { mutableStateOf(product.stock.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.title_edit_product, product.name), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                OutlinedTextField(
                    value = cost,
                    onValueChange = { cost = it },
                    label = { Text(stringResource(R.string.hint_cost_price)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = sell,
                    onValueChange = { sell = it },
                    label = { Text(stringResource(R.string.hint_selling_price)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text(stringResource(R.string.hint_initial_stock)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(cost, sell, stock) }, shape = RoundedCornerShape(12.dp)) { 
                Text(stringResource(R.string.btn_update)) 
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductAddScreen(
    initialCategoryName: String? = null,
    onAdd: (String, String, String, String, String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var name by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var sell by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var threshold by remember { mutableStateOf("5") }
    val artisanData = getArtisanData()
    var selectedCategory by remember { 
        mutableStateOf(artisanData.find { context.getString(it.nameRes) == initialCategoryName }) 
    }

    val defaultLabel = stringResource(R.string.label_default)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Category Selection
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.step_select_category), style = MaterialTheme.typography.titleSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(artisanData) { category ->
                    CategoryChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = stringResource(category.shortNameRes)
                    )
                }
            }
        }

        if (selectedCategory != null) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.step_quick_pick), style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedCategory!!.items.forEach { item ->
                        SuggestionChip(
                            onClick = { name = item },
                            label = { Text(item) },
                            shape = RoundedCornerShape(8.dp),
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if(name == item) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                        )
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.hint_product_name)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = color,
                onValueChange = { color = it },
                label = { Text(stringResource(R.string.hint_color_variant)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            
            ColorPickerSection(
                selectedColor = color,
                onColorSelected = { color = it }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = cost,
                    onValueChange = { cost = it },
                    label = { Text(stringResource(R.string.hint_cost_price)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = sell,
                    onValueChange = { sell = it },
                    label = { Text(stringResource(R.string.hint_selling_price)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text(stringResource(R.string.hint_initial_stock)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = threshold,
                    onValueChange = { threshold = it },
                    label = { Text(stringResource(R.string.hint_low_stock_alert)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = { 
                onAdd(name, context.getString(selectedCategory!!.nameRes), color.ifBlank { defaultLabel }, cost, sell, quantity, threshold) 
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = name.isNotBlank() && sell.isNotBlank() && quantity.isNotBlank() && selectedCategory != null,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(stringResource(R.string.btn_save_product), style = MaterialTheme.typography.labelLarge)
        }
        
        TextButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.btn_cancel), color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun ColorPickerSection(
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    val colorItems = listOf(
        stringResource(R.string.color_natural) to Color(0xFF8D6E63),
        stringResource(R.string.color_red) to Color(0xFFEF5350),
        stringResource(R.string.color_blue) to Color(0xFF42A5F5),
        stringResource(R.string.color_green) to Color(0xFF66BB6A),
        stringResource(R.string.color_yellow) to Color(0xFFFFEE58),
        stringResource(R.string.color_black) to Color(0xFF263238),
        stringResource(R.string.color_white) to Color(0xFFF5F5F5)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.label_quick_color_select),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(colorItems) { (name, color) ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color)
                        .border(
                            width = if (selectedColor == name) 3.dp else 1.dp,
                            color = if (selectedColor == name) TerracottaPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onColorSelected(name) },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedColor == name) {
                        Icon(Icons.Default.Check, null, tint = if (name == "White") Color.Black else Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesEntryScreen(
    products: List<Product>,
    onBill: (Product, String) -> Unit,
    vm: ShopViewModel
) {
    var selectedCategoryName by remember { mutableStateOf<String?>(null) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var quantity by remember { mutableStateOf("1") }
    val artisanData = getArtisanData()

    LaunchedEffect(Unit) {
        vm.saleSuccess.collect { success ->
            if (success) {
                selectedProduct = null
                quantity = "1"
            }
        }
    }

    val filteredProducts = if (selectedCategoryName == null) products else products.filter { it.category == selectedCategoryName }
    val totalPrice = (selectedProduct?.sellingPrice ?: 0.0) * (quantity.toDoubleOrNull() ?: 0.0)

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                CategoryChip(
                    selected = selectedCategoryName == null,
                    onClick = { selectedCategoryName = null },
                    label = stringResource(R.string.label_all)
                )
            }
            items(artisanData) { category ->
                val categoryName = stringResource(category.nameRes)
                CategoryChip(
                    selected = selectedCategoryName == categoryName,
                    onClick = { selectedCategoryName = categoryName },
                    label = stringResource(category.shortNameRes)
                )
            }
        }

        Text(stringResource(R.string.label_select_product_title), style = MaterialTheme.typography.titleSmall)
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredProducts) { product ->
                val isSelected = selectedProduct?.id == product.id
                Card(
                    onClick = { selectedProduct = product },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Inventory2, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Rs. ${product.sellingPrice.toInt()}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        if (selectedProduct != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(selectedProduct!!.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Available: ${selectedProduct!!.stock}", style = MaterialTheme.typography.bodySmall)
                        }
                        
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { 
                                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                    quantity = it
                                }
                            },
                            modifier = Modifier.width(80.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    
                    val qtyInt = quantity.toIntOrNull() ?: 0
                    if (qtyInt > selectedProduct!!.stock) {
                        Text(
                            text = stringResource(R.string.error_insufficient_stock, selectedProduct!!.stock),
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(stringResource(R.string.label_total_amount_title), style = MaterialTheme.typography.labelLarge)
                        Text(
                            "Rs. ${"%.0f".format(totalPrice)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Button(
            onClick = { selectedProduct?.let { onBill(it, quantity) } },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = selectedProduct != null && (quantity.toIntOrNull() ?: 0) > 0 && (quantity.toIntOrNull() ?: 0) <= (selectedProduct?.stock ?: 0),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(stringResource(R.string.btn_submit_sale), style = MaterialTheme.typography.labelLarge)
        }
    }
}
