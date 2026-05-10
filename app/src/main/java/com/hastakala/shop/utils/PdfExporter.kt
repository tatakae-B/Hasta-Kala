package com.hastakala.shop.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.hastakala.shop.data.SaleRecord
import com.hastakala.shop.data.UserProfile
import com.hastakala.shop.data.Expense
import com.hastakala.shop.data.Product
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {
    fun exportReport(
        context: Context,
        type: ExportType,
        timeRange: ExportTimeRange,
        isDetailed: Boolean,
        sales: List<SaleRecord>,
        products: List<Product>,
        expenses: List<Expense>,
        startDate: Long,
        endDate: Long,
        userProfile: UserProfile? = null
    ): String? {
        val pdfDocument = PdfDocument()
        
        // Colors from Artisan Theme
        val terracottaColor = 0xFFB35A44.toInt()
        val goldColor = 0xFFC5A059.toInt()
        val artisanWarmBlack = 0xFF1C1917.toInt()
        val slateColor = 0xFF4A5568.toInt()
        val lightCream = 0xFFF9F6F0.toInt()

        val titlePaint = Paint().apply {
            color = terracottaColor
            textSize = 28f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = artisanWarmBlack
            textSize = 14f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = artisanWarmBlack
            textSize = 12f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val labelPaint = Paint().apply {
            color = slateColor
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val footerPaint = Paint().apply {
            color = slateColor
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        canvas.drawColor(lightCream)

        var y = 60f
        
        // Header
        val logoPaint = Paint().apply { color = terracottaColor; isAntiAlias = true }
        canvas.drawCircle(70f, 65f, 25f, logoPaint)
        val logoTextPaint = Paint().apply { color = Color.WHITE; textSize = 28f; typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD); isAntiAlias = true }
        canvas.drawText("H", 61f, 75f, logoTextPaint)
        canvas.drawText("HASTA KALA", 110f, 75f, titlePaint)
        
        y += 50f
        canvas.drawRect(50f, y, 545f, y + 2f, Paint().apply { color = goldColor })
        
        y += 40f
        val reportTitle = "${type.name} REPORT (${timeRange.name})"
        canvas.drawText(reportTitle, 50f, y, headerPaint)
        
        y += 20f
        canvas.drawText("Shop: ${userProfile?.shopName ?: "Hasta-Kala Artisan Shop"}", 50f, y, bodyPaint)
        y += 18f
        canvas.drawText("Artisan: ${userProfile?.fullName ?: "Artisan"}", 50f, y, bodyPaint)
        y += 18f
        val dateRangeStr = "${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(startDate))} - ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(endDate))}"
        canvas.drawText("Period: $dateRangeStr", 50f, y, bodyPaint)
        
        y += 40f
        // Metrics summary card
        val summaryPaint = Paint().apply { color = Color.WHITE; isAntiAlias = true }
        canvas.drawRoundRect(50f, y, 545f, y + 80f, 12f, 12f, summaryPaint)
        
        val totalRevenue = sales.sumOf { it.subtotal }
        val totalExpenses = expenses.sumOf { it.amount }
        val netProfit = totalRevenue - sales.sumOf { it.quantity * it.costPrice } - totalExpenses

        canvas.drawText("TOTAL REVENUE", 70f, y + 25f, labelPaint)
        canvas.drawText("Rs. ${String.format(Locale.getDefault(), "%.2f", totalRevenue)}", 70f, y + 50f, titlePaint)
        
        canvas.drawText("NET PROFIT", 300f, y + 25f, labelPaint)
        val profitPaint = Paint(titlePaint).apply { color = if (netProfit >= 0) 0xFF2E7D32.toInt() else Color.RED }
        canvas.drawText("Rs. ${String.format(Locale.getDefault(), "%.2f", netProfit)}", 300f, y + 50f, profitPaint)

        y += 120f
        
        if (isDetailed) {
            drawDetailedTable(canvas, y, type, sales, expenses, products, headerPaint, bodyPaint, slateColor, artisanWarmBlack)
        } else {
            drawSummaryAnalytics(canvas, y, type, sales, expenses, headerPaint, bodyPaint, goldColor, terracottaColor)
        }

        // Footer
        canvas.drawText("Generated by Hasta-Kala Shop App", 297f, 810f, footerPaint)
        pdfDocument.finishPage(page)

        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "HastaKala/Exports")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "HastaKala_${type.name}_${System.currentTimeMillis()}.pdf")

        return try {
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
            fos.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun drawDetailedTable(
        canvas: Canvas,
        startY: Float,
        type: ExportType,
        sales: List<SaleRecord>,
        expenses: List<Expense>,
        products: List<Product>,
        headerPaint: Paint,
        bodyPaint: Paint,
        slateColor: Int,
        artisanWarmBlack: Int
    ) {
        var y = startY
        val col1: String
        val col2: String
        val col3: String
        val col4: String

        when (type) {
            ExportType.CUSTOMERS -> {
                col1 = "CUSTOMER NAME"
                col2 = "VISITS"
                col3 = "METRIC"
                col4 = "TOTAL SPENT"
            }
            ExportType.ARTISAN -> {
                col1 = "CATEGORY"
                col2 = "ITEMS SOLD"
                col3 = "METRIC"
                col4 = "REVENUE"
            }
            ExportType.INVENTORY -> {
                col1 = "CATEGORY"
                col2 = "PRODUCT NAME"
                col3 = "STOCK"
                col4 = "VALUE"
            }
            else -> {
                col1 = "DATE"
                col2 = "DESCRIPTION"
                col3 = "QTY/CAT"
                col4 = "TOTAL"
            }
        }

        canvas.drawText(col1, 50f, y, headerPaint)
        canvas.drawText(col2, 150f, y, headerPaint)
        canvas.drawText(col3, 400f, y, headerPaint)
        canvas.drawText(col4, 480f, y, headerPaint)
        
        y += 10f
        canvas.drawRect(50f, y, 545f, y + 1f, Paint().apply { color = (slateColor and 0x00FFFFFF) or (0x4D shl 24) })
        y += 25f

        val rowPaint = Paint(bodyPaint).apply { textSize = 10f }
        
        when (type) {
            ExportType.SALES, ExportType.ORDERS -> {
                sales.take(25).forEach { sale ->
                    if (y > 780f) return@forEach
                    val dateStr = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(sale.timestamp))
                    canvas.drawText(dateStr, 50f, y, rowPaint)
                    canvas.drawText(sale.productName.take(30), 150f, y, rowPaint)
                    canvas.drawText(sale.quantity.toString(), 400f, y, rowPaint)
                    canvas.drawText(sale.subtotal.toInt().toString(), 480f, y, rowPaint)
                    y += 20f
                }
            }
            ExportType.EXPENSES -> {
                expenses.take(25).forEach { exp ->
                    if (y > 780f) return@forEach
                    val dateStr = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(exp.timestamp))
                    canvas.drawText(dateStr, 50f, y, rowPaint)
                    canvas.drawText(exp.notes.take(30).ifBlank { exp.category }, 150f, y, rowPaint)
                    canvas.drawText(exp.category.take(10), 400f, y, rowPaint)
                    canvas.drawText(exp.amount.toInt().toString(), 480f, y, rowPaint)
                    y += 20f
                }
            }
            ExportType.INVENTORY -> {
                products.take(25).forEach { p ->
                    if (y > 780f) return@forEach
                    canvas.drawText(p.category.take(12), 50f, y, rowPaint)
                    canvas.drawText(p.name.take(30), 150f, y, rowPaint)
                    canvas.drawText(p.stock.toString(), 400f, y, rowPaint)
                    canvas.drawText((p.stock * p.sellingPrice).toInt().toString(), 480f, y, rowPaint)
                    y += 20f
                }
            }
            ExportType.CUSTOMERS -> {
                val customers = sales.filter { it.customerName.isNotBlank() }
                    .groupBy { it.customerName }
                    .map { (name, s) ->
                        Triple(name, s.size, s.sumOf { it.subtotal })
                    }
                    .sortedByDescending { it.third }
                    .take(25)

                customers.forEach { (name, visits, total) ->
                    if (y > 780f) return@forEach
                    canvas.drawText(name.take(25), 50f, y, rowPaint)
                    canvas.drawText("$visits visits", 150f, y, rowPaint)
                    canvas.drawText("Total Spend:", 400f, y, rowPaint)
                    canvas.drawText(total.toInt().toString(), 480f, y, rowPaint)
                    y += 20f
                }
            }
            ExportType.ARTISAN -> {
                val artisanSales = sales.groupBy { it.category }
                artisanSales.forEach { (category, items) ->
                    if (y > 780f) return@forEach
                    val total = items.sumOf { it.subtotal }
                    val qty = items.sumOf { it.quantity }
                    canvas.drawText(category.take(20), 50f, y, rowPaint)
                    canvas.drawText("$qty items sold", 150f, y, rowPaint)
                    canvas.drawText("Revenue:", 400f, y, rowPaint)
                    canvas.drawText(total.toInt().toString(), 480f, y, rowPaint)
                    y += 20f
                }
            }
            else -> {}
        }
    }

    private fun drawSummaryAnalytics(
        canvas: Canvas,
        startY: Float,
        type: ExportType,
        sales: List<SaleRecord>,
        expenses: List<Expense>,
        headerPaint: Paint,
        bodyPaint: Paint,
        goldColor: Int,
        terracottaColor: Int
    ) {
        var y = startY
        canvas.drawText("PERFORMANCE INSIGHTS", 50f, y, headerPaint)
        y += 30f

        // Category Breakdown (Mini Chart Simulation)
        val categorySales = sales.groupBy { it.category }.mapValues { it.value.sumOf { s -> s.subtotal } }
        canvas.drawText("Sales by Category", 50f, y, bodyPaint)
        y += 20f
        
        val maxVal = categorySales.values.maxOrNull() ?: 1.0
        categorySales.entries.take(5).forEach { (cat, total) ->
            val barWidth = (total / maxVal * 300).toFloat()
            canvas.drawRect(150f, y - 10f, 150f + barWidth, y, Paint().apply { color = goldColor })
            canvas.drawText(cat.take(12), 50f, y, bodyPaint)
            canvas.drawText("Rs. ${total.toInt()}", 160f + barWidth, y, bodyPaint)
            y += 25f
        }
        
        y += 30f
        canvas.drawText("Key Trends", 50f, y, bodyPaint)
        y += 20f
        val avgSale = if (sales.isNotEmpty()) sales.sumOf { it.subtotal } / sales.size else 0.0
        canvas.drawText("• Average Transaction Value: Rs. ${String.format(Locale.getDefault(), "%.2f", avgSale)}", 60f, y, bodyPaint)
        y += 20f
        val topCustomer = sales.filter { it.customerName.isNotBlank() }.groupBy { it.customerName }.maxByOrNull { it.value.sumOf { s -> s.subtotal } }?.key ?: "N/A"
        canvas.drawText("• Top Customer: $topCustomer", 60f, y, bodyPaint)
        y += 20f
        val topProduct = sales.groupBy { it.productName }.maxByOrNull { it.value.sumOf { s -> s.quantity } }?.key ?: "N/A"
        canvas.drawText("• Best Selling Item: $topProduct", 60f, y, bodyPaint)
    }
}
