package com.hastakala.shop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hastakala.shop.data.CategorySalesTotal
import com.hastakala.shop.data.ColorSalesTotal
import com.hastakala.shop.data.Product
import com.hastakala.shop.data.ProductSalesTotal
import com.hastakala.shop.data.SaleRecord
import com.hastakala.shop.data.ShopRepository
import androidx.datastore.preferences.core.Preferences
import com.hastakala.shop.data.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TimeFilter { TODAY, WEEK, MONTH, ALL, CUSTOM }

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val repository: ShopRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    val darkMode = preferenceManager.darkModeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val themeColor = preferenceManager.themeColorFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Brown")
    val fontSize = preferenceManager.fontSizeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Medium")
    val autoSaveBills = preferenceManager.autoSaveBillsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val saleConfirmation = preferenceManager.saleConfirmationFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val defaultQty = preferenceManager.defaultQtyFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)
    val currency = preferenceManager.currencyFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "₹")
    val lowStockAlerts = preferenceManager.lowStockAlertsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val minStockThreshold = preferenceManager.minStockThresholdFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5f)
    val stockNotifications = preferenceManager.stockNotificationsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val autoBackup = preferenceManager.autoBackupFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val exportFormat = preferenceManager.exportFormatFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "PDF")
    val weeklySummary = preferenceManager.weeklySummaryFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val backupReminders = preferenceManager.backupRemindersFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val language = preferenceManager.languageFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "English")
    val appLock = preferenceManager.appLockFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val unlockMethod = preferenceManager.unlockMethodFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Biometric")
    val appPin = preferenceManager.appPinFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val lastBackupTime = preferenceManager.lastBackupTimeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun <T> updatePreference(key: Preferences.Key<T>, value: T) {
        viewModelScope.launch {
            preferenceManager.updatePreference(key, value)
        }
    }

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

    private val _slowMovingProducts = MutableStateFlow<List<com.hastakala.shop.data.SlowMovingProduct>>(emptyList())
    val slowMovingProducts = _slowMovingProducts.asStateFlow()

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

    private val _saleSuccess = MutableSharedFlow<Boolean>()
    val saleSuccess = _saleSuccess.asSharedFlow()

    private val _userProfile = MutableStateFlow<com.hastakala.shop.data.UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked = _isAppLocked.asStateFlow()

    fun setAppLocked(locked: Boolean) {
        _isAppLocked.value = locked
    }

    fun setAppPin(pin: String) {
        viewModelScope.launch {
            preferenceManager.updatePreference(PreferenceManager.APP_PIN, pin)
        }
    }

    init {
        viewModelScope.launch {
            refreshData(forceClear = true)
            _userProfile.value = repository.getUserProfile()
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
            _saleSuccess.emit(true)
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

            // Calculate Slow Moving Products
            val productSalesMap = filtered.groupBy { it.productId }
            val allProducts = products.value
            
            _slowMovingProducts.value = allProducts.map { prod ->
                val salesForProd = productSalesMap[prod.id] ?: emptyList()
                com.hastakala.shop.data.SlowMovingProduct(
                    product = prod,
                    totalSold = salesForProd.sumOf { it.quantity },
                    lastSoldTimestamp = salesForProd.maxByOrNull { it.timestamp }?.timestamp,
                    revenue = salesForProd.sumOf { it.subtotal }
                )
            }.filter { it.product.stock > 0 } // Only consider products currently in stock
             .sortedWith(compareBy({ it.totalSold }, { it.revenue }))
             .take(10) // Focus on the top 10 bottlenecks
        }
    }

    fun createCsv(): String {
        val header = "Date,Product,Category,Color,Quantity,UnitPrice,Subtotal"
        val rows = _filteredSales.value.map { sale ->
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(sale.timestamp))
            "$date,${sale.productName},${sale.category},${sale.color},${sale.quantity},${sale.unitPrice},${sale.subtotal}"
        }
        val credit = "\n\nGenerated by Hasta-Kala Shop - Crafted by bdriii"
        return (listOf(header) + rows).joinToString("\n") + credit
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

    fun updateUserProfile(fullName: String, shopName: String, contact: String, location: String) {
        val uid = repository.getUserId() ?: return
        val newProfile = com.hastakala.shop.data.UserProfile(
            uid = uid,
            fullName = fullName,
            shopName = shopName,
            contact = contact,
            location = location
        )
        viewModelScope.launch {
            repository.saveUserProfile(newProfile)
            _userProfile.value = newProfile
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
