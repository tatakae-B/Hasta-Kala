package com.hastakala.shop.utils

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {
    fun export(context: Context, csvContent: String): String {
        val dir = File(context.getExternalFilesDir(null), "exports")
        if (!dir.exists()) dir.mkdirs()
        val fileName = "income_log_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
        val file = File(dir, fileName)
        file.writeText(csvContent)
        return file.absolutePath
    }
}
