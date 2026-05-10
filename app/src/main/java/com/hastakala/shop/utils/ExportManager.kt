package com.hastakala.shop.utils

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import com.hastakala.shop.data.SaleRecord
import com.hastakala.shop.data.Product
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class ExportType { SALES, ORDERS, REVENUE, INVENTORY, CUSTOMERS, EXPENSES, ARTISAN }
enum class ExportFormat { PDF, CSV }
enum class ExportTimeRange { TODAY, YESTERDAY, THIS_WEEK, THIS_MONTH, LAST_MONTH, THIS_YEAR, CUSTOM }

object ExportManager {

    suspend fun generateExport(
        context: Context,
        type: ExportType,
        format: ExportFormat,
        timeRange: ExportTimeRange,
        sales: List<SaleRecord>,
        products: List<Product>,
        expenses: List<com.hastakala.shop.data.Expense> = emptyList(),
        startDate: Long,
        endDate: Long,
        userProfile: com.hastakala.shop.data.UserProfile? = null
    ): String? = withContext(Dispatchers.IO) {
        val filteredSales = sales.filter { it.timestamp in startDate..endDate }
        val filteredExpenses = expenses.filter { it.timestamp in startDate..endDate }
        
        val isDetailed = timeRange == ExportTimeRange.TODAY || timeRange == ExportTimeRange.YESTERDAY

        val fileName = "HastaKala_${type.name}_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}"
        
        val exportsDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "HastaKala/Exports"
        )
        if (!exportsDir.exists()) exportsDir.mkdirs()

        return@withContext when (format) {
            ExportFormat.PDF -> {
                PdfExporter.exportReport(
                    context = context,
                    type = type,
                    timeRange = timeRange,
                    isDetailed = isDetailed,
                    sales = filteredSales,
                    products = products,
                    expenses = filteredExpenses,
                    startDate = startDate,
                    endDate = endDate,
                    userProfile = userProfile
                )
            }
            ExportFormat.CSV -> {
                val csvContent = generateCsvContent(type, filteredSales, products, filteredExpenses)
                val file = File(exportsDir, "$fileName.csv")
                file.writeText(csvContent)
                file.absolutePath
            }
        }
    }

    private fun generateCsvContent(
        type: ExportType, 
        sales: List<SaleRecord>, 
        products: List<Product>,
        expenses: List<com.hastakala.shop.data.Expense>
    ): String {
        return when (type) {
            ExportType.SALES, ExportType.ORDERS -> {
                val header = "Date,Product,Category,Color,Quantity,UnitPrice,Subtotal,Payment,Customer,Notes\n"
                val rows = sales.joinToString("\n") { sale ->
                    val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(sale.timestamp))
                    "\"$date\",\"${sale.productName}\",\"${sale.category}\",\"${sale.color}\",${sale.quantity},${sale.unitPrice},${sale.subtotal},\"${sale.paymentMethod}\",\"${sale.customerName}\",\"${sale.notes}\""
                }
                header + rows
            }
            ExportType.INVENTORY -> {
                val header = "Product Name,Category,Color,Stock,Cost Price,Selling Price,Stock Value\n"
                val rows = products.joinToString("\n") { p ->
                    "\"${p.name}\",\"${p.category}\",\"${p.color}\",${p.stock},${p.costPrice},${p.sellingPrice},${p.stock * p.sellingPrice}"
                }
                header + rows
            }
            ExportType.EXPENSES -> {
                val header = "Date,Category,Amount,Notes\n"
                val rows = expenses.joinToString("\n") { e ->
                    val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(e.timestamp))
                    "\"$date\",\"${e.category}\",${e.amount},\"${e.notes}\""
                }
                header + rows
            }
            ExportType.CUSTOMERS -> {
                val customers = sales.filter { it.customerName.isNotBlank() }
                    .groupBy { it.customerName }
                    .map { (name, s) ->
                        val contact = s.firstOrNull { it.customerContact.isNotBlank() }?.customerContact ?: ""
                        val totalSpent = s.sumOf { it.subtotal }
                        val lastVisit = s.maxOf { it.timestamp }
                        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(lastVisit))
                        "\"$name\",\"$contact\",$totalSpent,\"$date\""
                    }
                val header = "Customer Name,Contact,Total Spent,Last Visit\n"
                header + customers.joinToString("\n")
            }
            ExportType.REVENUE, ExportType.ARTISAN -> {
                val totalSales = sales.sumOf { it.subtotal }
                val totalCost = sales.sumOf { it.quantity * it.costPrice }
                val totalExpenses = expenses.sumOf { it.amount }
                val profit = totalSales - totalCost - totalExpenses
                "Metric,Value\nTotal Sales,$totalSales\nTotal Cost of Goods,$totalCost\nTotal Expenses,$totalExpenses\nNet Profit,$profit"
            }
        }
    }


    fun shareFile(context: Context, filePath: String) {
        val file = File(filePath)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = if (filePath.endsWith(".pdf")) "application/pdf" else "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Export"))
    }
}
