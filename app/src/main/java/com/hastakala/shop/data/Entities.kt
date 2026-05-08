package com.hastakala.shop.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val color: String,
    val costPrice: Double,
    val sellingPrice: Double,
    val stock: Int,
    val lowStockThreshold: Int
)

@Entity(tableName = "sales")
data class SaleRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Int,
    val productName: String,
    val category: String,
    val color: String,
    val quantity: Int,
    val costPrice: Double,
    val unitPrice: Double,
    val subtotal: Double,
    val timestamp: Long = System.currentTimeMillis()
)

data class ProductSalesTotal(
    val productName: String,
    val totalQty: Int
)

data class ColorSalesTotal(
    val color: String,
    val totalQty: Int
)

data class CategorySalesTotal(
    val category: String,
    val revenue: Double
)
