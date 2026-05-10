package com.hastakala.shop.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProduct(productId: Int)

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun observeProducts(): Flow<List<Product>>

    @Insert
    suspend fun insertSale(saleRecord: SaleRecord)

    @Insert
    suspend fun insertExpense(expense: Expense)

    @Query("SELECT * FROM expenses WHERE timestamp >= :fromTime ORDER BY timestamp DESC")
    fun observeExpenses(fromTime: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    suspend fun getExpensesInRange(startTime: Long, endTime: Long): List<Expense>

    @Query("SELECT * FROM sales WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    suspend fun getSalesInRange(startTime: Long, endTime: Long): List<SaleRecord>

    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun observeSales(): Flow<List<SaleRecord>>

    @Query(
        """
        SELECT productName, SUM(quantity) AS totalQty
        FROM sales
        WHERE timestamp >= :fromTime
        GROUP BY productName
        ORDER BY totalQty DESC
        """
    )
    suspend fun getTopProducts(fromTime: Long): List<ProductSalesTotal>

    @Query(
        """
        SELECT color, SUM(quantity) AS totalQty
        FROM sales
        WHERE timestamp >= :fromTime
        GROUP BY color
        ORDER BY totalQty DESC
        """
    )
    suspend fun getColorBreakdown(fromTime: Long): List<ColorSalesTotal>

    @Query("SELECT COALESCE(SUM(subtotal), 0.0) FROM sales WHERE timestamp >= :fromTime")
    suspend fun getRevenueFrom(fromTime: Long): Double

    @Query("SELECT COALESCE(SUM(subtotal - (quantity * costPrice)), 0.0) FROM sales WHERE timestamp >= :fromTime")
    suspend fun getProfitFrom(fromTime: Long): Double

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()

    @Query("DELETE FROM sales")
    suspend fun deleteAllSales()

    @Transaction
    suspend fun clearAllData() {
        deleteAllProducts()
        deleteAllSales()
    }

    @Update
    suspend fun updateSale(saleRecord: SaleRecord)

    @Query("DELETE FROM sales WHERE id = :saleId")
    suspend fun deleteSale(saleId: Long)

    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: Int): Product?

    @Query("SELECT * FROM sales WHERE id = :saleId")
    suspend fun getSaleById(saleId: Long): SaleRecord?

    @Transaction
    suspend fun updateSaleAndUpdateStock(updatedSale: SaleRecord) {
        val oldSale = getSaleById(updatedSale.id) ?: return
        val product = getProductById(updatedSale.productId) ?: return
        
        // Restore old stock, then apply new quantity
        val restoredStock = product.stock + oldSale.quantity
        val finalStock = (restoredStock - updatedSale.quantity).coerceAtLeast(0)
        
        updateProduct(product.copy(stock = finalStock))
        updateSale(updatedSale)
    }

    @Transaction
    suspend fun deleteSaleAndRestoreStock(saleId: Long) {
        val sale = getSaleById(saleId) ?: return
        val product = getProductById(sale.productId) ?: return
        
        updateProduct(product.copy(stock = product.stock + sale.quantity))
        deleteSale(saleId)
    }

    @Transaction
    suspend fun recordSaleAndUpdateStock(product: Product, quantity: Int, paymentMethod: String = "Cash", customerName: String = "", notes: String = "") {
        val finalQty = quantity.coerceAtLeast(1)
        val subtotal = product.sellingPrice * finalQty
        insertSale(
            SaleRecord(
                productId = product.id,
                productName = product.name,
                category = product.category,
                color = product.color,
                quantity = finalQty,
                costPrice = product.costPrice,
                unitPrice = product.sellingPrice,
                subtotal = subtotal,
                paymentMethod = paymentMethod,
                customerName = customerName,
                notes = notes
            )
        )
        updateProduct(product.copy(stock = (product.stock - finalQty).coerceAtLeast(0)))
    }
}
