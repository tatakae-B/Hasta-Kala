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

    @Transaction
    suspend fun recordSaleAndUpdateStock(product: Product, quantity: Int) {
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
                subtotal = subtotal
            )
        )
        updateProduct(product.copy(stock = (product.stock - finalQty).coerceAtLeast(0)))
    }
}
