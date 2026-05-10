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
        sales: List<SaleRecord>,
        products: List<Product>,
        startDate: Long,
        endDate: Long,
        userProfile: com.hastakala.shop.data.UserProfile? = null
    ): String? = withContext(Dispatchers.IO) {
        val filteredSales = sales.filter { it.timestamp in startDate..endDate }
        
        if (filteredSales.isEmpty() && type != ExportType.INVENTORY) {
            return@withContext null
        }

        val fileName = "HastaKala_${type.name}_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}"
        
        val exportsDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "HastaKala/Exports"
        )
        if (!exportsDir.exists()) exportsDir.mkdirs()

        return@withContext when (format) {
            ExportFormat.PDF -> {
                val filePath = PdfExporter.exportSalesToPdf(
                    context, 
                    filteredSales, 
                    filteredSales.sumOf { it.subtotal },
                    userProfile
                )
                filePath
            }
            ExportFormat.CSV -> {
                val csvContent = generateCsvContent(type, filteredSales, products)
                val file = File(exportsDir, "$fileName.csv")
                file.writeText(csvContent)
                file.absolutePath
            }
        }
    }

    private fun generateCsvContent(type: ExportType, sales: List<SaleRecord>, products: List<Product>): String {
        return when (type) {
            ExportType.SALES -> {
                val header = "Date,Product,Category,Color,Quantity,UnitPrice,Subtotal\n"
                val rows = sales.joinToString("\n") { sale ->
                    val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(sale.timestamp))
                    "$date,${sale.productName},${sale.category},${sale.color},${sale.quantity},${sale.unitPrice},${sale.subtotal}"
                }
                header + rows
            }
            ExportType.INVENTORY -> {
                val header = "Product Name,Category,Color,Stock,Cost Price,Selling Price\n"
                val rows = products.joinToString("\n") { p ->
                    "${p.name},${p.category},${p.color},${p.stock},${p.costPrice},${p.sellingPrice}"
                }
                header + rows
            }
            else -> "Feature coming soon for ${type.name}"
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
