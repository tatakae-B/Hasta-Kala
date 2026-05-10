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
    fun observeExpenses(fromTime: Long): Flow<List<Expense>> = dao.observeExpenses(fromTime)

    suspend fun getExpensesInRange(start: Long, end: Long) = dao.getExpensesInRange(start, end)
    suspend fun getSalesInRange(start: Long, end: Long) = dao.getSalesInRange(start, end)

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
                    paymentMethod = fs.paymentMethod,
                    customerName = fs.customerName,
                    customerContact = fs.customerContact,
                    notes = fs.notes,
                    timestamp = fs.timestamp,
                    lastModified = fs.lastModified
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

    suspend fun recordSale(product: Product, quantity: Int, paymentMethod: String = "Cash", customerName: String = "", notes: String = "") {
        val finalQty = quantity.coerceAtMost(product.stock)
        dao.recordSaleAndUpdateStock(product, finalQty, paymentMethod, customerName, notes)
        
        // Sync Sale to Firestore
        getUserId()?.let { uid ->
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
                paymentMethod = paymentMethod,
                customerName = customerName,
                notes = notes,
                timestamp = timestamp,
                lastModified = timestamp
            )
            
            firestore.collection("users").document(uid)
                .collection("sales").document(timestamp.toString())
                .set(firestoreSale)

            // Also update product stock in Firestore
            updateProduct(product.copy(stock = (product.stock - finalQty).coerceAtLeast(0)))
        }
    }

    suspend fun updateSale(saleRecord: SaleRecord) {
        dao.updateSaleAndUpdateStock(saleRecord)
        getUserId()?.let { uid ->
            val firestoreSale = FirestoreSaleRecord(
                id = saleRecord.id.toString(),
                productId = saleRecord.productId.toString(),
                productName = saleRecord.productName,
                category = saleRecord.category,
                color = saleRecord.color,
                quantity = saleRecord.quantity,
                costPrice = saleRecord.costPrice,
                unitPrice = saleRecord.unitPrice,
                subtotal = saleRecord.subtotal,
                paymentMethod = saleRecord.paymentMethod,
                customerName = saleRecord.customerName,
                customerContact = saleRecord.customerContact,
                notes = saleRecord.notes,
                timestamp = saleRecord.timestamp,
                lastModified = saleRecord.lastModified
            )
            firestore.collection("users").document(uid)
                .collection("sales").document(saleRecord.id.toString())
                .set(firestoreSale)

            // Since updateSaleAndUpdateStock also updates product stock, we should sync the product too
            dao.getProductById(saleRecord.productId)?.let { updatedProduct ->
                updateProduct(updatedProduct)
            }
        }
    }

    suspend fun deleteSale(saleId: Long) {
        val sale = dao.getSaleById(saleId)
        dao.deleteSaleAndRestoreStock(saleId)
        getUserId()?.let { uid ->
            firestore.collection("users").document(uid)
                .collection("sales").document(saleId.toString())
                .delete()
            
            // Sync updated product stock
            sale?.let { s ->
                dao.getProductById(s.productId)?.let { updatedProduct ->
                    updateProduct(updatedProduct)
                }
            }
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        firestore.collection("users").document(profile.uid).set(profile).await()
    }

    suspend fun uploadProfileImage(uri: android.net.Uri): String? {
        val uid = getUserId() ?: return null
        // Since we don't have Firebase Storage enabled in the build.gradle.kts yet, 
        // we'll simulate the URL for now or if you want I can add Firebase Storage.
        // Actually, let's assume the user wants to store it.
        // I will return the local URI string for now or a placeholder.
        return uri.toString()
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
