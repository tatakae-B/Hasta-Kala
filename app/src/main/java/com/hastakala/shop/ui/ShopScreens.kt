package com.hastakala.shop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.hastakala.shop.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hastakala.shop.data.Product
import com.hastakala.shop.viewmodel.TimeFilter
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState

data class ArtisanCategory(val name: String, val shortName: String, val items: List<String>)

val artisanData = listOf(
    ArtisanCategory("🏺 Pottery & Clay", "Clay items", listOf("Clay pots", "Terracotta diyas", "Ceramic bowls", "Plates", "Decorative vases", "Clay idols")),
    ArtisanCategory("🧵 Textile & Handloom", "Sarees & fabrics", listOf("Handloom sarees", "Shawls", "Scarves", "Cotton fabrics", "Embroidered items", "Dress materials")),
    ArtisanCategory("💍 Jewellery", "Jewellery", listOf("Beaded necklaces", "Earrings", "Bangles", "Terracotta jewellery", "Thread jewellery")),
    ArtisanCategory("🪵 Wood & Bamboo", "Wood & bamboo items", listOf("Wooden toys", "Carved sculptures", "Bamboo baskets", "Wooden decor items", "Small furniture")),
    ArtisanCategory("🖼️ Art & Paintings", "Paintings", listOf("Canvas paintings", "Madhubani art", "Warli art", "Portrait sketches", "Wall art")),
    ArtisanCategory("🕯️ Home Decor", "Home decor", listOf("Handmade candles", "Lamps", "Lanterns", "Wall hangings", "Decorative mirrors", "Handmade clocks")),
    ArtisanCategory("🍃 Eco-Friendly Products", "Eco products", listOf("Handmade soaps", "Jute bags", "Paper crafts", "Bamboo items", "Eco-friendly products")),
    ArtisanCategory("🧸 Miscellaneous:-", "Toys & gifts", listOf("Handmade dolls", "Soft toys", "Festival items", "Gift items", "Custom crafts"))
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
            // Colored Indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Category Name (including emoji if present in string)
            Text(
                text = category,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            
            // Percentage
            Text(
                text = "${(percentage * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            // Revenue
            Text(
                text = "Rs. ${"%.0f".format(revenue)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Progress Bar
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
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
    topProducts: List<com.hastakala.shop.data.ProductSalesTotal>,
    categoryBreakdown: List<com.hastakala.shop.data.CategorySalesTotal>,
    colorBreakdown: List<com.hastakala.shop.data.ColorSalesTotal>,
    filter: TimeFilter,
    selectedCategory: String?,
    isDarkMode: Boolean = false,
    isRefreshing: Boolean = false,
    onFilterChange: (TimeFilter) -> Unit,
    onCategoryChange: (String?) -> Unit,
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category Filter Section
            Text(stringResource(R.string.label_categories), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { onCategoryChange(null) },
                        label = { Text(stringResource(R.string.label_all)) }
                    )
                }
                items(artisanData) { category ->
                    FilterChip(
                        selected = selectedCategory == category.name,
                        onClick = { onCategoryChange(category.name) },
                        label = { Text(category.shortName) }
                    )
                }
            }

            // Stats Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp)
            ) {
                item {
                    StatCard(stringResource(R.string.stat_total_sales), "Rs. ${"%.0f".format(totalSales)}", Icons.Default.Payments, MaterialTheme.colorScheme.primary)
                }
                item {
                    StatCard(stringResource(R.string.stat_total_products), "$totalProducts", Icons.Default.Inventory, MaterialTheme.colorScheme.primary)
                }
                item {
                    StatCard(stringResource(R.string.stat_low_stock), "$lowStockItems", Icons.Default.Warning, Color(0xFFD32F2F))
                }
                item {
                    StatCard(stringResource(R.string.stat_profit), "Rs. ${"%.0f".format(profit)}", Icons.Default.TrendingUp, Color(0xFF388E3C))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.label_sales_summary), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TimeFilter.entries.take(4).forEach { tFilter ->
                    val labelId = when(tFilter) {
                        TimeFilter.TODAY -> R.string.filter_today
                        TimeFilter.WEEK -> R.string.filter_week
                        TimeFilter.MONTH -> R.string.filter_month
                        TimeFilter.ALL -> R.string.filter_all
                        else -> R.string.label_all
                    }
                    FilterChip(
                        selected = filter == tFilter,
                        onClick = { onFilterChange(tFilter) },
                        label = { Text(stringResource(labelId)) }
                    )
                }
            }

            if (topProducts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.label_top_products), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        topProducts.take(3).forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.productName)
                                Text(stringResource(R.string.label_sold_count, item.totalQty), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            if (categoryBreakdown.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    stringResource(R.string.label_performance_overview),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Modern Donut Chart
                        CategoryPieChart(
                            data = categoryBreakdown,
                            totalRevenue = totalSales,
                            isDarkMode = isDarkMode,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Detailed Legend Section
                        val modernColors = listOf(
                            Color(0xFF673AB7), // Deep Purple
                            Color(0xFF3F51B5), // Indigo
                            Color(0xFF2196F3), // Blue
                            Color(0xFF00BCD4), // Cyan
                            Color(0xFF009688), // Teal
                            Color(0xFF4CAF50), // Green
                            Color(0xFFFFC107), // Amber
                            Color(0xFFFF5722)  // Deep Orange
                        )

                        categoryBreakdown.forEachIndexed { index, item ->
                            val percentage = if (totalSales > 0) (item.revenue / totalSales).toFloat() else 0f
                            val color = modernColors[index % modernColors.size]
                            
                            CategoryLegendRow(
                                category = item.category,
                                revenue = item.revenue,
                                percentage = percentage,
                                color = color
                            )
                            
                            if (index < categoryBreakdown.size - 1) {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onExportCsv,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.btn_export_csv))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, iconColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
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

    Column(modifier = Modifier.fillMaxSize()) {
        // Category Filter Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategoryFilterChange(null) },
                    label = { Text(stringResource(R.string.label_all)) }
                )
            }
            items(artisanData) { category ->
                FilterChip(
                    selected = selectedCategory == category.name,
                    onClick = { onCategoryFilterChange(category.name) },
                    label = { Text(category.shortName) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Group products by category for a structured view
            val grouped = displayProducts.groupBy { it.category }
            
            // If filtering, we show the category header once and then its products.
            if (selectedCategory != null) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedCategory,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { onAddProductInCategory(selectedCategory) }) {
                            Icon(Icons.Default.Add, contentDescription = "Add to $selectedCategory", modifier = Modifier.size(20.dp))
                        }
                    }
                }
                items(displayProducts) { product ->
                    ProductItemCard(product, onEdit = { editingProduct = it }, onDelete = onDelete)
                }
            } else {
                grouped.forEach { (categoryName, categoryProducts) ->
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = categoryName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(onClick = { onAddProductInCategory(categoryName) }) {
                                Icon(Icons.Default.Add, contentDescription = "Add to $categoryName", modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                    items(categoryProducts) { product ->
                        ProductItemCard(product, onEdit = { editingProduct = it }, onDelete = onDelete)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductItemCard(product: Product, onEdit: (Product) -> Unit, onDelete: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${product.category} • ${product.color}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text("Sell: Rs. ${product.sellingPrice}", style = MaterialTheme.typography.bodyMedium)
                Text("Stock: ${product.stock}", style = MaterialTheme.typography.bodyMedium, color = if(product.stock <= product.lowStockThreshold) Color.Red else Color.Unspecified)
            }
            Row {
                IconButton(onClick = { onEdit(product) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { onDelete(product.id) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFD32F2F))
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
        title = { Text(stringResource(R.string.title_edit_product, product.name)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text(stringResource(R.string.hint_cost_price)) })
                OutlinedTextField(value = sell, onValueChange = { sell = it }, label = { Text(stringResource(R.string.hint_selling_price)) })
                OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text(stringResource(R.string.hint_initial_stock)) })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(cost, sell, stock) }) { Text(stringResource(R.string.btn_update)) }
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
    var name by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var sell by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var threshold by remember { mutableStateOf("5") }
    var selectedCategory by remember { 
        mutableStateOf(artisanData.find { it.name == initialCategoryName }) 
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(stringResource(R.string.title_add_product), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // Category Selection
        Text(stringResource(R.string.step_select_category), style = MaterialTheme.typography.titleSmall, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            items(artisanData) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category.shortName) }
                )
            }
        }

        if (selectedCategory != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.step_quick_pick), style = MaterialTheme.typography.titleSmall, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedCategory!!.items.forEach { item ->
                    SuggestionChip(
                        onClick = { name = item },
                        label = { Text(item) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if(name == item) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.hint_product_name)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = color,
            onValueChange = { color = it },
            label = { Text(stringResource(R.string.hint_color_variant)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = cost,
                onValueChange = { cost = it },
                label = { Text(stringResource(R.string.hint_cost_price)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            )
            OutlinedTextField(
                value = sell,
                onValueChange = { sell = it },
                label = { Text(stringResource(R.string.hint_selling_price)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text(stringResource(R.string.hint_initial_stock)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            )
            OutlinedTextField(
                value = threshold,
                onValueChange = { threshold = it },
                label = { Text(stringResource(R.string.hint_low_stock_alert)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { 
                onAdd(name, selectedCategory?.name ?: "Other", color.ifBlank { "Default" }, cost, sell, quantity, threshold) 
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = name.isNotBlank() && sell.isNotBlank() && quantity.isNotBlank() && selectedCategory != null,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(stringResource(R.string.btn_save_product))
        }
        TextButton(onClick = onCancel) {
            Text(stringResource(R.string.btn_cancel), color = Color.Gray)
        }
    }
}

@Composable
fun SalesEntryScreen(
    products: List<Product>,
    onBill: (Product, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedCategoryName by remember { mutableStateOf<String?>(null) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var quantity by remember { mutableStateOf("1") }

    val filteredProducts = if (selectedCategoryName == null) products else products.filter { it.category == selectedCategoryName }
    val totalPrice = (selectedProduct?.sellingPrice ?: 0.0) * (quantity.toDoubleOrNull() ?: 0.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.title_add_sale), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        
        // Category Selector for Sales
        Text(stringResource(R.string.label_filter_category), style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.Start))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(selected = selectedCategoryName == null, onClick = { selectedCategoryName = null }, label = { Text(stringResource(R.string.label_all)) })
            }
            items(artisanData) { category ->
                FilterChip(selected = selectedCategoryName == category.name, onClick = { selectedCategoryName = category.name }, label = { Text(category.shortName) })
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(selectedProduct?.name ?: stringResource(R.string.hint_select_product), color = if(selectedProduct == null) Color.Gray else Color.Black)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Black)
                }
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.fillMaxWidth(0.85f)) {
                if (filteredProducts.isEmpty()) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.msg_no_products)) }, onClick = { expanded = false })
                }
                filteredProducts.forEach { product ->
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.label_product_stock_hint, product.name, product.stock)) },
                        onClick = {
                            selectedProduct = product
                            expanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = quantity,
            onValueChange = { if (it.all { char -> char.isDigit() }) quantity = it },
            label = { Text(stringResource(R.string.hint_quantity_sold)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            isError = selectedProduct != null && (quantity.toIntOrNull() ?: 0) > (selectedProduct?.stock ?: 0)
        )

        if (selectedProduct != null) {
            val qtyInt = quantity.toIntOrNull() ?: 0
            val isOutOfStock = qtyInt > selectedProduct!!.stock
            
            if (isOutOfStock) {
                Text(
                    text = stringResource(R.string.error_insufficient_stock, selectedProduct!!.stock),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.label_total_price), style = MaterialTheme.typography.bodyMedium)
                    Text("Rs. ${"%.2f".format(totalPrice)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { selectedProduct?.let { onBill(it, quantity) } },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = selectedProduct != null && (quantity.toIntOrNull() ?: 0) > 0 && (quantity.toIntOrNull() ?: 0) <= (selectedProduct?.stock ?: 0),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(stringResource(R.string.btn_submit_sale), fontWeight = FontWeight.Bold)
        }
    }
}
