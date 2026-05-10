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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import com.hastakala.shop.R
import com.hastakala.shop.data.*
import com.hastakala.shop.viewmodel.ShopViewModel
import com.hastakala.shop.viewmodel.TimeFilter
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    totalSales: Double,
    totalProducts: Int,
    lowStockItems: Int,
    profit: Double,
    topProducts: List<ProductSalesTotal>,
    categoryBreakdown: List<CategorySalesTotal>,
    colorBreakdown: List<ColorSalesTotal> = emptyList(),
    slowMovingProducts: List<SlowMovingProduct> = emptyList(),
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
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
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
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
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
    }
}

@Composable
fun SalesAnalyticsScreen(
    sales: List<SaleRecord>,
    heatmapData: Map<Long, Double>,
    currentStreak: Int,
    bestStreak: Int,
    activeDays: Int,
    performanceScore: Double,
    onEditSale: (SaleRecord) -> Unit,
    onDeleteSale: (Long) -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    var editingSale by remember { mutableStateOf<SaleRecord?>(null) }

    editingSale?.let { sale ->
        EditSaleDialog(
            sale = sale,
            onDismiss = { editingSale = null },
            onConfirm = { updated ->
                onEditSale(updated)
                editingSale = null
            }
        )
    }

    val totalRevenue = sales.sumOf { it.subtotal }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PremiumStatCard(
                        label = stringResource(R.string.stat_total_sales),
                        value = "Rs. ${"%.0f".format(totalRevenue)}",
                        icon = Icons.Default.Payments,
                        color = TerracottaPrimary,
                        modifier = Modifier.weight(1.5f)
                    )
                    PremiumStatCard(
                        label = stringResource(R.string.label_perf_score),
                        value = "${performanceScore.toInt()}%",
                        icon = Icons.Default.AutoAwesome,
                        color = GoldAccent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Streak & Activity Info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StreakItem(stringResource(R.string.label_current_streak), "$currentStreak", Icons.Default.LocalFireDepartment, Color(0xFFFF5722))
                        VerticalDivider(modifier = Modifier.height(40.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        StreakItem(stringResource(R.string.label_best_streak), "$bestStreak", Icons.Default.EmojiEvents, GoldAccent)
                        VerticalDivider(modifier = Modifier.height(40.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        StreakItem(stringResource(R.string.label_active_days), "$activeDays", Icons.Default.CalendarToday, SageGreen)
                    }
                }
            }

            // GitHub-style Heatmap
            item {
                Text(
                    stringResource(R.string.label_sales_heatmap),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ) {
                    SalesHeatmap(
                        data = heatmapData,
                        sales = sales,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.label_recent_transactions), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            items(sales.reversed().take(20)) { sale ->
                TransactionItem(
                    sale = sale,
                    onEdit = { editingSale = it },
                    onDelete = { onDeleteSale(it) }
                )
            }
        }
    }
}

@Composable
fun PremiumStatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = color.copy(alpha = 0.8f))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun StreakItem(label: String, value: String, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesHeatmap(data: Map<Long, Double>, sales: List<SaleRecord>, modifier: Modifier = Modifier) {
    val today = remember { 
        java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
    }
    val minMonth = remember { 
        java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.JANUARY, 1, 0, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
    }

    var selectedDay by remember { mutableStateOf<Pair<String, Double>?>(null) }
    var currentViewMonth by remember { 
        val initial = if (today.before(minMonth)) minMonth else today
        mutableStateOf(initial.clone() as java.util.Calendar)
    }

    val monthlySales = remember(currentViewMonth, sales) {
        val start = currentViewMonth.timeInMillis
        val end = (currentViewMonth.clone() as java.util.Calendar).apply {
            add(java.util.Calendar.MONTH, 1)
        }.timeInMillis
        sales.filter { it.timestamp in start until end }
    }

    val monthlyTotal = monthlySales.sumOf { it.subtotal }
    val topMonthlyProducts = monthlySales.groupBy { it.productName }
        .mapValues { entry -> entry.value.sumOf { it.quantity } }
        .toList()
        .sortedByDescending { it.second }
        .take(3)
    
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentViewMonth.timeInMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    // DatePicker uses UTC millis; we extract components to compare against local "today" month
                    val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                        timeInMillis = utcTimeMillis
                    }
                    val year = calendar.get(java.util.Calendar.YEAR)
                    val month = calendar.get(java.util.Calendar.MONTH)
                    
                    val nowYear = today.get(java.util.Calendar.YEAR)
                    val nowMonth = today.get(java.util.Calendar.MONTH)
                    
                    if (year < 2026) return false
                    if (year > nowYear) return false
                    if (year == nowYear && month > nowMonth) return false
                    return true
                }

                override fun isSelectableYear(year: Int): Boolean {
                    return year >= 2026 && year <= today.get(java.util.Calendar.YEAR)
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        currentViewMonth = java.util.Calendar.getInstance().apply {
                            timeInMillis = it
                            set(java.util.Calendar.DAY_OF_MONTH, 1)
                            set(java.util.Calendar.HOUR_OF_DAY, 0)
                            set(java.util.Calendar.MINUTE, 0)
                            set(java.util.Calendar.SECOND, 0)
                            set(java.util.Calendar.MILLISECOND, 0)
                        }
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.btn_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.btn_cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(modifier = modifier) {
        // Month Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val canGoBack = currentViewMonth.after(minMonth)
            IconButton(
                onClick = {
                    if (canGoBack) {
                        currentViewMonth = (currentViewMonth.clone() as java.util.Calendar).apply {
                            add(java.util.Calendar.MONTH, -1)
                        }
                    }
                },
                enabled = canGoBack
            ) {
                Icon(
                    Icons.Default.ChevronLeft, 
                    contentDescription = "Previous Month",
                    tint = if (canGoBack) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }

            Surface(
                onClick = { showDatePicker = true },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault()).format(currentViewMonth.time),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Month", modifier = Modifier.size(20.dp))
                }
            }

            val canGoForward = (currentViewMonth.get(java.util.Calendar.YEAR) < today.get(java.util.Calendar.YEAR)) || 
                               (currentViewMonth.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) && 
                                currentViewMonth.get(java.util.Calendar.MONTH) < today.get(java.util.Calendar.MONTH))
            IconButton(
                onClick = {
                    if (canGoForward) {
                        currentViewMonth = (currentViewMonth.clone() as java.util.Calendar).apply {
                            add(java.util.Calendar.MONTH, 1)
                        }
                    }
                },
                enabled = canGoForward
            ) {
                Icon(
                    Icons.Default.ChevronRight, 
                    contentDescription = "Next Month",
                    tint = if (canGoForward) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        }

        // Selected Date Tooltip
        AnimatedVisibility(
            visible = selectedDay != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            selectedDay?.let { (date, amount) ->
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Event, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(date, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                        
                        val daySales = remember(date, sales) {
                            sales.filter { 
                                java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(it.timestamp)) == date
                            }
                        }
                        val orderCount = daySales.sumOf { it.quantity }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Rs. ${amount.toInt()}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(stringResource(R.string.label_items_sold, orderCount), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        MonthGrid(currentViewMonth, data) { date, amount ->
            selectedDay = if (selectedDay?.first == date) null else date to amount
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Heatmap Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Less", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(8.dp))
            HeatmapBox(0.0) {}
            Spacer(modifier = Modifier.width(4.dp))
            HeatmapBox(500.0) {}
            Spacer(modifier = Modifier.width(4.dp))
            HeatmapBox(2000.0) {}
            Spacer(modifier = Modifier.width(4.dp))
            HeatmapBox(4500.0) {}
            Spacer(modifier = Modifier.width(4.dp))
            HeatmapBox(8000.0) {}
            Spacer(modifier = Modifier.width(4.dp))
            HeatmapBox(12000.0) {}
            Spacer(modifier = Modifier.width(8.dp))
            Text("More", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Monthly Summary Card
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.label_monthly_summary, java.text.SimpleDateFormat("MMMM", java.util.Locale.getDefault()).format(currentViewMonth.time)),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Rs. ${"%.0f".format(monthlyTotal)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (topMonthlyProducts.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                    
                    Text(
                        text = stringResource(R.string.label_top_products_all_caps),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    topMonthlyProducts.forEach { (name, qty) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(name, style = MaterialTheme.typography.bodySmall)
                            Text("$qty sold", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Text(
                        text = stringResource(R.string.label_no_sales_month),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MonthGrid(
    monthStart: java.util.Calendar,
    data: Map<Long, Double>,
    onDayClick: (String, Double) -> Unit
) {
    val daysInMonth = monthStart.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = monthStart.get(java.util.Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
    val boxSize = 26.dp
    val spacing = 4.dp
    
    // Localized weekday labels
    val calendar = remember { java.util.Calendar.getInstance() }
    val weekdayFormat = remember { java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault()) }
    val days = remember {
        (1..7).map {
            calendar.set(java.util.Calendar.DAY_OF_WEEK, it)
            weekdayFormat.format(calendar.time)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Weekday Labels Column
        Column(
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            days.forEach { day ->
                Box(modifier = Modifier.height(boxSize), contentAlignment = Alignment.CenterStart) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Days Grid - Always show 6 weeks to fill the area and remove layout jumps
        Row(
            modifier = Modifier.weight(1f).padding(start = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (w in 0 until 6) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(spacing),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (d in 1..7) {
                        val dayOfMonth = w * 7 + d - (firstDayOfWeek - 1)
                        if (dayOfMonth in 1..daysInMonth) {
                            val currentDay = (monthStart.clone() as java.util.Calendar).apply {
                                set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)
                                set(java.util.Calendar.HOUR_OF_DAY, 0)
                                set(java.util.Calendar.MINUTE, 0)
                                set(java.util.Calendar.SECOND, 0)
                                set(java.util.Calendar.MILLISECOND, 0)
                            }
                            val amount = data[currentDay.timeInMillis] ?: 0.0
                            HeatmapBox(amount, boxSize = boxSize) {
                                val dateStr = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(currentDay.time)
                                onDayClick(dateStr, amount)
                            }
                        } else {
                            // Faint placeholder to "fill" the grid area completely
                            HeatmapBox(amount = 0.0, enabled = false, boxSize = boxSize) {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeatmapBox(
    amount: Double, 
    enabled: Boolean = true, 
    boxSize: Dp = 18.dp, 
    onClick: () -> Unit
) {
    val color = when {
        !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f) // Very faint for placeholders
        amount <= 0.0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) // Distinctly visible for 0-sale days
        amount < 1000.0 -> SageGreen.copy(alpha = 0.3f)
        amount < 3000.0 -> SageGreen.copy(alpha = 0.5f)
        amount < 6000.0 -> SageGreen.copy(alpha = 0.7f)
        amount < 10000.0 -> SageGreen.copy(alpha = 0.85f)
        else -> SageGreen
    }
    
    Box(
        modifier = Modifier
            .size(boxSize)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier)
    )
}

@Composable
fun TransactionItem(
    sale: SaleRecord,
    onEdit: (SaleRecord) -> Unit,
    onDelete: (Long) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.dialog_delete_sale_title)) },
            text = { Text(stringResource(R.string.dialog_delete_sale_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(sale.id)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.btn_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ReceiptLong, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
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

            if (sale.customerName.isNotEmpty() || sale.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(modifier = Modifier.alpha(0.1f))
                Spacer(modifier = Modifier.height(8.dp))
                
                if (sale.customerName.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(sale.customerName, style = MaterialTheme.typography.bodySmall)
                        if (sale.customerContact.isNotEmpty()) {
                            Text(" (${sale.customerContact})", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                if (sale.notes.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Notes, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(sale.notes, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { onEdit(sale) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { 
                    showDeleteDialog = true 
                }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun EditSaleDialog(
    sale: SaleRecord,
    onDismiss: () -> Unit,
    onConfirm: (SaleRecord) -> Unit
) {
    var quantity by remember { mutableStateOf(sale.quantity.toString()) }
    var unitPrice by remember { mutableStateOf(sale.unitPrice.toString()) }
    var customerName by remember { mutableStateOf(sale.customerName) }
    var customerContact by remember { mutableStateOf(sale.customerContact) }
    var notes by remember { mutableStateOf(sale.notes) }
    var paymentMethod by remember { mutableStateOf(sale.paymentMethod) }

    val paymentMethods = listOf(
        stringResource(R.string.payment_cash),
        stringResource(R.string.payment_upi),
        stringResource(R.string.payment_card),
        stringResource(R.string.payment_other)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.title_edit_sale), style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text(stringResource(R.string.label_quantity)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = unitPrice,
                    onValueChange = { unitPrice = it },
                    label = { Text(stringResource(R.string.label_unit_price)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(stringResource(R.string.label_payment_method), style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    paymentMethods.forEach { method ->
                        FilterChip(
                            selected = paymentMethod == method,
                            onClick = { paymentMethod = method },
                            label = { Text(method) }
                        )
                    }
                }

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text(stringResource(R.string.hint_customer_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = customerContact,
                    onValueChange = { customerContact = it },
                    label = { Text(stringResource(R.string.hint_customer_contact)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.hint_notes)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val q = quantity.toIntOrNull() ?: sale.quantity
                    val p = unitPrice.toDoubleOrNull() ?: sale.unitPrice
                    onConfirm(
                        sale.copy(
                            quantity = q,
                            unitPrice = p,
                            subtotal = q * p,
                            customerName = customerName,
                            customerContact = customerContact,
                            paymentMethod = paymentMethod,
                            notes = notes,
                            lastModified = System.currentTimeMillis()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreen)
            ) {
                Text(stringResource(R.string.btn_update))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}

@Composable
fun ProfitInsightsScreen(
    products: List<Product>,
    sales: List<SaleRecord>,
    totalProfit: Double,
    onNavigateBack: () -> Unit = {}
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
                        Text(stringResource(R.string.label_estimated_total_profit), style = MaterialTheme.typography.labelMedium, color = SuccessGreen)
                        Text("Rs. ${"%.0f".format(totalProfit)}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = SuccessGreen)
                        
                        val totalRevenue = sales.sumOf { it.subtotal }
                        val margin = if (totalRevenue > 0) (totalProfit / totalRevenue) * 100 else 0.0
                        Text(stringResource(R.string.label_overall_margin, margin), style = MaterialTheme.typography.bodySmall, color = SuccessGreen.copy(alpha = 0.8f))
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.label_profit_by_category), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    
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
                    Text(stringResource(R.string.label_slow_moving_stock_value), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    
                    val stockValue = products.sumOf { it.stock * it.costPrice }
                    val potentialRevenue = products.sumOf { it.stock * it.sellingPrice }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(stringResource(R.string.label_stock_cost), style = MaterialTheme.typography.labelSmall)
                                Text("Rs. ${stockValue.toInt()}", fontWeight = FontWeight.Bold)
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(stringResource(R.string.label_potential_revenue), style = MaterialTheme.typography.labelSmall)
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
    modifier: Modifier = Modifier,
    accentColor: Color = TerracottaPrimary
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
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
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
                        stringResource(R.string.label_all_stocked_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        stringResource(R.string.label_all_stocked_desc),
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
                        text = stringResource(R.string.label_low_stock_desc),
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
                                    stringResource(R.string.label_left_all_caps),
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
    onBill: (Product, String, String, String, String) -> Unit,
    vm: ShopViewModel
) {
    var selectedCategoryName by remember { mutableStateOf<String?>(null) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var quantity by remember { mutableStateOf("1") }
    var customerName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    
    val paymentMethods = listOf(
        stringResource(R.string.payment_cash),
        stringResource(R.string.payment_upi),
        stringResource(R.string.payment_card),
        stringResource(R.string.payment_other)
    )
    var paymentMethod by remember { mutableStateOf(paymentMethods[0]) }
    
    val artisanData = getArtisanData()

    LaunchedEffect(Unit) {
        vm.saleSuccess.collect { success ->
            if (success) {
                selectedProduct = null
                quantity = "1"
                customerName = ""
                notes = ""
                paymentMethod = paymentMethods[0]
                searchQuery = ""
            }
        }
    }

    val filteredProducts = remember(selectedCategoryName, searchQuery, products) {
        products.filter { 
            (selectedCategoryName == null || it.category == selectedCategoryName) &&
            (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true))
        }
    }
    
    val totalPrice = (selectedProduct?.sellingPrice ?: 0.0) * (quantity.toDoubleOrNull() ?: 0.0)

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.label_search_products)) },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, null) } }
            } else null,
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

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
                            overflow = TextOverflow.Ellipsis
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
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(selectedProduct!!.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.label_available, selectedProduct!!.stock), style = MaterialTheme.typography.bodySmall)
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
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
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

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)
                    
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

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)

                    Text(stringResource(R.string.label_optional_details), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.label_payment_method), style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            paymentMethods.forEach { method ->
                                FilterChip(
                                    selected = paymentMethod == method,
                                    onClick = { paymentMethod = method },
                                    label = { Text(method) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text(stringResource(R.string.hint_customer_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(stringResource(R.string.hint_notes)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }
        }

        Button(
            onClick = { selectedProduct?.let { onBill(it, quantity, paymentMethod, customerName, notes) } },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = selectedProduct != null && (quantity.toIntOrNull() ?: 0) > 0 && (quantity.toIntOrNull() ?: 0) <= (selectedProduct?.stock ?: 0),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(stringResource(R.string.btn_submit_sale), style = MaterialTheme.typography.labelLarge)
        }
    }
}
