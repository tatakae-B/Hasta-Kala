package com.hastakala.shop.utils

import android.content.Context
import com.hastakala.shop.data.AppDatabase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupManager {
    private const val BACKUP_DIR_NAME = "backups"
    private const val DB_NAME = "hasta_kala_db"

    fun backupDatabase(context: Context): Boolean {
        return try {
            val dbFile = context.getDatabasePath(DB_NAME)
            if (!dbFile.exists()) return false

            val backupDir = File(context.getExternalFilesDir(null), BACKUP_DIR_NAME)
            if (!backupDir.exists()) backupDir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFile = File(backupDir, "${DB_NAME}_backup_$timestamp.db")

            FileInputStream(dbFile).use { input ->
                FileOutputStream(backupFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            // Store last backup time in SharedPreferences
            val prefs = context.getSharedPreferences("hastakala_prefs", Context.MODE_PRIVATE)
            prefs.edit().putLong("last_backup_time", System.currentTimeMillis()).apply()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getLastBackupTime(context: Context): String {
        val prefs = context.getSharedPreferences("hastakala_prefs", Context.MODE_PRIVATE)
        val time = prefs.getLong("last_backup_time", 0L)
        return if (time == 0L) "Never" 
               else SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(time))
    }

    fun restoreDatabase(context: Context, backupFile: File): Boolean {
        // Warning: This requires closing the DB first or restarting the app
        return try {
            val dbFile = context.getDatabasePath(DB_NAME)
            AppDatabase.getInstance(context).close()

            FileInputStream(backupFile).use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
