package com.hastakala.shop.data

import com.google.firebase.Timestamp

data class UserProfile(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val contact: String = "",
    val shopName: String = "",
    val location: String = "",
    val profileImageUrl: String = "",
    val loginMethod: String = "",
    val createdAt: Timestamp = Timestamp.now()
)

data class FirestoreProduct(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val color: String = "",
    val costPrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val stock: Int = 0,
    val lowStockThreshold: Int = 5
)

data class FirestoreSaleRecord(
    val id: String = "",
    val productId: String = "",
    val productName: String = "",
    val category: String = "",
    val color: String = "",
    val quantity: Int = 0,
    val costPrice: Double = 0.0,
    val unitPrice: Double = 0.0,
    val subtotal: Double = 0.0,
    val paymentMethod: String = "Cash",
    val customerName: String = "",
    val customerContact: String = "",
    val notes: String = "",
    val timestamp: Long = 0L,
    val lastModified: Long = 0L
)
