package com.hastakala.shop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hastakala.shop.data.CategorySalesTotal
import com.hastakala.shop.data.ColorSalesTotal
import com.hastakala.shop.data.Product
import com.hastakala.shop.data.ProductSalesTotal
import com.hastakala.shop.data.SaleRecord
import com.hastakala.shop.data.ShopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TimeFilter { TODAY, WEEK, MONTH, ALL, CUSTOM }

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val repository: ShopRepository
) : ViewModel() {

    val products: StateFlow<List<Product>> = repository.observeProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    fun setCategoryFilter(category: String?) {
        _selectedCategory.value = category
        refreshAnalytics()
    }

    val sales: StateFlow<List<SaleRecord>> = repository.observeSales()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedFilter = MutableStateFlow(TimeFilter.WEEK)
    val selectedFilter = _selectedFilter.asStateFlow()

    private val _topProducts = MutableStateFlow<List<ProductSalesTotal>>(emptyList())
    val topProducts = _topProducts.asStateFlow()

    private val _colorBreakdown = MutableStateFlow<List<ColorSalesTotal>>(emptyList())
    val colorBreakdown = _colorBreakdown.asStateFlow()

    private val _categoryBreakdown = MutableStateFlow<List<CategorySalesTotal>>(emptyList())
    val categoryBreakdown = _categoryBreakdown.asStateFlow()

    private val _revenue = MutableStateFlow(0.0)
    val revenue = _revenue.asStateFlow()

    private val _profit = MutableStateFlow(0.0)
    val profit = _profit.asStateFlow()

    private val _filteredSales = MutableStateFlow<List<SaleRecord>>(emptyList())
    val filteredSales = _filteredSales.asStateFlow()

    private val _customStartMillis = MutableStateFlow(System.currentTimeMillis() - (7 * DAY_MILLIS))
    val customStartMillis = _customStartMillis.asStateFlow()

    private val _customEndMillis = MutableStateFlow(System.currentTimeMillis())
    val customEndMillis = _customEndMillis.asStateFlow()

    private val _exportMessage = MutableSharedFlow<String>()
    val exportMessage = _exportMessage

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        viewModelScope.launch {
            refreshData(forceClear = true)
        }
        viewModelScope.launch {
            sales.collect {
                refreshAnalytics()
            }
        }
    }

    fun refreshData(forceClear: Boolean = false) {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.syncFromFirestore(forceClear)
            _isRefreshing.value = false
        }
    }

    fun setFilter(filter: TimeFilter) {
        _selectedFilter.value = filter
        refreshAnalytics()
    }

    fun setCustomDateRange(startMillis: Long, endMillis: Long) {
        if (startMillis > endMillis) return
        _customStartMillis.value = startMillis
        _customEndMillis.value = endMillis
        _selectedFilter.value = TimeFilter.CUSTOM
        refreshAnalytics()
    }

    fun addProduct(name: String, category: String, color: String, costPriceText: String, sellingPriceText: String, stockText: String, thresholdText: String) {
        val costPrice = costPriceText.toDoubleOrNull() ?: 0.0
        val sellingPrice = sellingPriceText.toDoubleOrNull() ?: return
        val stock = stockText.toIntOrNull() ?: return
        val threshold = thresholdText.toIntOrNull() ?: return
        if (name.isBlank() || category.isBlank() || color.isBlank()) return

        viewModelScope.launch {
            repository.addProduct(
                name = name,
                category = category,
                color = color,
                costPrice = costPrice,
                sellingPrice = sellingPrice,
                stock = stock,
                lowStockThreshold = threshold
            )
            refreshAnalytics()
        }
    }

    fun updateProductDetails(product: Product, costPriceText: String, sellingPriceText: String, stockText: String) {
        val newCost = costPriceText.toDoubleOrNull() ?: product.costPrice
        val newSelling = sellingPriceText.toDoubleOrNull() ?: product.sellingPrice
        val newStock = stockText.toIntOrNull() ?: product.stock
        viewModelScope.launch {
            repository.updateProduct(product.copy(costPrice = newCost, sellingPrice = newSelling, stock = newStock.coerceAtLeast(0)))
            refreshAnalytics()
        }
    }

    fun recordSale(product: Product, quantityText: String) {
        val qty = quantityText.toIntOrNull() ?: return
        if (qty <= 0 || product.stock <= 0) return
        viewModelScope.launch {
            repository.recordSale(product, qty.coerceAtMost(product.stock))
            refreshAnalytics()
        }
    }

    fun deleteProduct(productId: Int) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
            refreshAnalytics()
        }
    }

    private fun refreshAnalytics() {
        viewModelScope.launch {
            val fromTime = filterToEpoch(_selectedFilter.value)
            val currentCat = _selectedCategory.value
            
            val filtered = sales.value.filter { sale ->
                (sale.timestamp in fromTime..currentEndTime()) &&
                (currentCat == null || sale.category == currentCat)
            }
            _filteredSales.value = filtered

            _revenue.value = filtered.sumOf { it.subtotal }
            _profit.value = filtered.sumOf { it.subtotal - (it.quantity * it.costPrice) }
            _topProducts.value = filtered
                .groupBy { it.productName }
                .map { ProductSalesTotal(productName = it.key, totalQty = it.value.sumOf { row -> row.quantity }) }
                .sortedByDescending { it.totalQty }
            _colorBreakdown.value = filtered
                .groupBy { it.color }
                .map { ColorSalesTotal(color = it.key, totalQty = it.value.sumOf { row -> row.quantity }) }
                .sortedByDescending { it.totalQty }

            _categoryBreakdown.value = filtered
                .groupBy { it.category }
                .map { CategorySalesTotal(category = it.key, revenue = it.value.sumOf { row -> row.subtotal }) }
                .sortedByDescending { it.revenue }
        }
    }

    fun createCsv(): String {
        val header = "Date,Product,Category,Color,Quantity,UnitPrice,Subtotal"
        val rows = _filteredSales.value.map { sale ->
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(sale.timestamp))
            "$date,${sale.productName},${sale.category},${sale.color},${sale.quantity},${sale.unitPrice},${sale.subtotal}"
        }
        return (listOf(header) + rows).joinToString("\n")
    }

    fun notifyExported(path: String) {
        viewModelScope.launch {
            _exportMessage.emit(path)
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.clearLocalData()
            onComplete()
        }
    }

    private fun filterToEpoch(filter: TimeFilter): Long {
        val now = System.currentTimeMillis()
        return when (filter) {
            TimeFilter.TODAY -> now - DAY_MILLIS
            TimeFilter.WEEK -> now - (7 * DAY_MILLIS)
            TimeFilter.MONTH -> now - (30 * DAY_MILLIS)
            TimeFilter.ALL -> 0L
            TimeFilter.CUSTOM -> _customStartMillis.value
        }
    }

    private fun currentEndTime(): Long {
        return if (_selectedFilter.value == TimeFilter.CUSTOM) _customEndMillis.value else System.currentTimeMillis()
    }

    companion object {
        private const val DAY_MILLIS = 24 * 60 * 60 * 1000L
    }
}
