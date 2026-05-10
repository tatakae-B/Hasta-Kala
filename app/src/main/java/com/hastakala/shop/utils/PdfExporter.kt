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
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {
    fun exportSalesToPdf(
        context: Context, 
        sales: List<SaleRecord>, 
        totalRevenue: Double,
        userProfile: UserProfile? = null
    ): String? {
        val pdfDocument = PdfDocument()
        
        // Colors from Artisan Theme
        val terracottaColor = 0xFFB35A44.toInt()
        val goldColor = 0xFFC5A059.toInt()
        val artisanWarmBlack = 0xFF1C1917.toInt()
        val slateColor = 0xFF4A5568.toInt()
        val lightCream = 0xFFF9F6F0.toInt() // Artisan Cream

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
        
        // Header with Logo Placeholder
        val logoPaint = Paint().apply { 
            color = terracottaColor
            isAntiAlias = true 
        }
        canvas.drawCircle(70f, 65f, 25f, logoPaint)
        
        val logoTextPaint = Paint().apply { 
            color = Color.WHITE
            textSize = 28f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true 
        }
        canvas.drawText("H", 61f, 75f, logoTextPaint)
        
        canvas.drawText("HASTA KALA", 110f, 75f, titlePaint)
        
        y += 50f
        // Divider
        canvas.drawRect(50f, y, 545f, y + 2f, Paint().apply { color = goldColor })
        
        y += 40f
        // Artisan Info
        canvas.drawText("ARTISAN REPORT", 50f, y, headerPaint)
        y += 20f
        canvas.drawText("Shop: ${userProfile?.shopName ?: "Hasta-Kala Artisan Shop"}", 50f, y, bodyPaint)
        y += 18f
        canvas.drawText("Artisan: ${userProfile?.fullName ?: "Artisan"}", 50f, y, bodyPaint)
        y += 18f
        canvas.drawText("Date: ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())}", 50f, y, bodyPaint)
        
        y += 40f
        // Summary Card
        val summaryPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
        }
        canvas.drawRoundRect(50f, y, 545f, y + 60f, 12f, 12f, summaryPaint)
        canvas.drawText("TOTAL REVENUE", 70f, y + 25f, labelPaint)
        canvas.drawText("Rs. ${String.format("%.2f", totalRevenue)}", 70f, y + 50f, titlePaint)
        
        canvas.drawText("TOTAL SALES", 300f, y + 25f, labelPaint)
        canvas.drawText("${sales.size}", 300f, y + 50f, titlePaint)
        
        y += 100f
        // Table Header
        canvas.drawText("DATE", 50f, y, headerPaint)
        canvas.drawText("PRODUCT", 150f, y, headerPaint)
        canvas.drawText("QTY", 400f, y, headerPaint)
        canvas.drawText("SUBTOTAL", 480f, y, headerPaint)
        
        y += 10f
        canvas.drawRect(50f, y, 545f, y + 1f, Paint().apply { color = (slateColor and 0x00FFFFFF) or (0x4D shl 24) })
        
        y += 25f
        
        val rowPaint = Paint().apply {
            color = artisanWarmBlack
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        sales.take(20).forEach { sale ->
            if (y > 780f) return@forEach // Basic page overflow handling
            
            val dateStr = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(sale.timestamp))
            canvas.drawText(dateStr, 50f, y, rowPaint)
            
            val productName = if (sale.productName.length > 30) sale.productName.substring(0, 27) + "..." else sale.productName
            canvas.drawText(productName, 150f, y, rowPaint)
            
            canvas.drawText(sale.quantity.toString(), 400f, y, rowPaint)
            canvas.drawText("Rs. ${sale.subtotal.toInt()}", 480f, y, rowPaint)
            
            y += 20f
        }

        if (sales.size > 20) {
            canvas.drawText("... and ${sales.size - 20} more items", 50f, y, footerPaint)
        }

        // Footer
        canvas.drawText("Crafted with ❤️ for Artisans by Hasta-Kala Shop", 297f, 810f, footerPaint)

        pdfDocument.finishPage(page)

        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "HastaKala/Exports"
        )
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "HastaKala_Report_${System.currentTimeMillis()}.pdf")

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
}
