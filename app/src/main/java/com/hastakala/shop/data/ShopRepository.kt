package com.hastakala.shop.data

import kotlinx.coroutines.flow.Flow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ShopRepository(private val dao: AppDao) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun getUserId(): String? = auth.currentUser?.uid

    fun observeProducts(): Flow<List<Product>> = dao.observeProducts()
    fun observeSales(): Flow<List<SaleRecord>> = dao.observeSales()

    suspend fun syncFromFirestore(forceClear: Boolean = false) {
        val uid = getUserId() ?: return
        
        try {
            if (forceClear) {
                dao.clearAllData()
            }
            
            // Sync Products
            val productsSnapshot = firestore.collection("users").document(uid).collection("products").get().await()
            val fProducts = productsSnapshot.toObjects(FirestoreProduct::class.java)
            fProducts.forEach { fp ->
                dao.insertProduct(Product(
                    id = fp.id.toIntOrNull() ?: 0,
                    name = fp.name,
                    category = fp.category,
                    color = fp.color,
                    costPrice = fp.costPrice,
                    sellingPrice = fp.sellingPrice,
                    stock = fp.stock,
                    lowStockThreshold = fp.lowStockThreshold
                ))
            }

            // Sync Sales
            val salesSnapshot = firestore.collection("users").document(uid).collection("sales").get().await()
            val fSales = salesSnapshot.toObjects(FirestoreSaleRecord::class.java)
            fSales.forEach { fs ->
                dao.insertSale(SaleRecord(
                    id = fs.id.toLongOrNull() ?: 0L,
                    productId = fs.productId.toIntOrNull() ?: 0,
                    productName = fs.productName,
                    category = fs.category,
                    color = fs.color,
                    quantity = fs.quantity,
                    costPrice = fs.costPrice,
                    unitPrice = fs.unitPrice,
                    subtotal = fs.subtotal,
                    timestamp = fs.timestamp
                ))
            }
        } catch (e: Exception) {
            // Log or handle sync error
        }
    }

    suspend fun addProduct(
        name: String,
        category: String,
        color: String,
        costPrice: Double,
        sellingPrice: Double,
        stock: Int,
        lowStockThreshold: Int
    ) {
        val product = Product(
            name = name.trim(),
            category = category.trim(),
            color = color.trim(),
            costPrice = costPrice,
            sellingPrice = sellingPrice,
            stock = stock,
            lowStockThreshold = lowStockThreshold
        )
        val rowIdLong = dao.insertProduct(product)
        val rowId = rowIdLong.toInt()
        
        // Sync to Firestore
        getUserId()?.let { uid ->
            val firestoreProduct = FirestoreProduct(
                id = rowId.toString(),
                name = product.name,
                category = product.category,
                color = product.color,
                costPrice = product.costPrice,
                sellingPrice = product.sellingPrice,
                stock = product.stock,
                lowStockThreshold = product.lowStockThreshold
            )
            firestore.collection("users").document(uid)
                .collection("products").document(rowId.toString())
                .set(firestoreProduct)
        }
    }

    suspend fun updateProduct(product: Product) {
        dao.updateProduct(product)
        getUserId()?.let { uid ->
            val firestoreProduct = FirestoreProduct(
                id = product.id.toString(),
                name = product.name,
                category = product.category,
                color = product.color,
                costPrice = product.costPrice,
                sellingPrice = product.sellingPrice,
                stock = product.stock,
                lowStockThreshold = product.lowStockThreshold
            )
            firestore.collection("users").document(uid)
                .collection("products").document(product.id.toString())
                .set(firestoreProduct)
        }
    }

    suspend fun deleteProduct(productId: Int) {
        dao.deleteProduct(productId)
        getUserId()?.let { uid ->
            firestore.collection("users").document(uid)
                .collection("products").document(productId.toString())
                .delete()
        }
    }

    suspend fun recordSale(product: Product, quantity: Int) {
        val finalQty = quantity.coerceAtMost(product.stock)
        dao.recordSaleAndUpdateStock(product, finalQty)
        
        // Sync Sale to Firestore
        getUserId()?.let { uid ->
            // We need a way to get the last inserted sale ID or just use timestamp as ID if unique enough
            // For now, let's just use timestamp as part of the document ID
            val timestamp = System.currentTimeMillis()
            val firestoreSale = FirestoreSaleRecord(
                id = timestamp.toString(),
                productId = product.id.toString(),
                productName = product.name,
                category = product.category,
                color = product.color,
                quantity = finalQty,
                costPrice = product.costPrice,
                unitPrice = product.sellingPrice,
                subtotal = product.sellingPrice * finalQty,
                timestamp = timestamp
            )
            
            firestore.collection("users").document(uid)
                .collection("sales").document(timestamp.toString())
                .set(firestoreSale)

            // Also update product stock in Firestore
            updateProduct(product.copy(stock = (product.stock - finalQty).coerceAtLeast(0)))
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        firestore.collection("users").document(profile.uid).set(profile).await()
    }

    suspend fun getUserProfile(): UserProfile? {
        val uid = getUserId() ?: return null
        return try {
            firestore.collection("users").document(uid).get().await().toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun clearLocalData() = dao.clearAllData()

    suspend fun topProducts(fromTime: Long) = dao.getTopProducts(fromTime)
    suspend fun colorBreakdown(fromTime: Long) = dao.getColorBreakdown(fromTime)
    suspend fun revenueFrom(fromTime: Long) = dao.getRevenueFrom(fromTime)
    suspend fun profitFrom(fromTime: Long) = dao.getProfitFrom(fromTime)
}
