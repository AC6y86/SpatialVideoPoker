package com.gallery.artbrowser.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Simple file logger that writes log messages to a file
 */
object FileLogger {
    private const val TAG = "FileLogger"
    private const val LOG_FILE_NAME = "gallery_app_debug.log"
    private const val MAX_FILE_SIZE = 5 * 1024 * 1024 // 5MB
    private const val MAX_BACKUP_FILES = 3
    
    private var logFile: File? = null
    private var writer: PrintWriter? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    
    fun initialize(context: Context) {
        executor.execute {
            try {
                val logDir = File(context.cacheDir, "logs")
                if (!logDir.exists()) {
                    logDir.mkdirs()
                }
                
                logFile = File(logDir, LOG_FILE_NAME)
                checkAndRotateLogFile()
                
                writer = PrintWriter(FileWriter(logFile, true), true)
                Log.d(TAG, "FileLogger initialized: ${logFile?.absolutePath}")
                
                // Write initialization message
                writeLog("INFO", TAG, "FileLogger initialized")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize FileLogger", e)
            }
        }
    }
    
    fun d(tag: String, message: String) {
        Log.d(tag, message)
        writeLog("DEBUG", tag, message)
    }
    
    fun i(tag: String, message: String) {
        Log.i(tag, message)
        writeLog("INFO", tag, message)
    }
    
    fun w(tag: String, message: String) {
        Log.w(tag, message)
        writeLog("WARN", tag, message)
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
            writeLog("ERROR", tag, message, throwable)
        } else {
            Log.e(tag, message)
            writeLog("ERROR", tag, message)
        }
    }
    
    fun v(tag: String, message: String) {
        Log.v(tag, message)
        writeLog("VERBOSE", tag, message)
    }
    
    /**
     * Get current timestamp in the same format used by the logger
     */
    fun getCurrentTimestamp(): String {
        return dateFormat.format(Date())
    }
    
    private fun writeLog(level: String, tag: String, message: String, throwable: Throwable? = null) {
        executor.execute {
            try {
                writer?.let { w ->
                    val timestamp = dateFormat.format(Date())
                    w.println("$timestamp $level/$tag: $message")
                    
                    throwable?.let {
                        w.println(Log.getStackTraceString(it))
                    }
                    
                    w.flush()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write log", e)
            }
        }
    }
    
    private fun checkAndRotateLogFile() {
        logFile?.let { file ->
            if (file.exists() && file.length() > MAX_FILE_SIZE) {
                // Rotate log files
                for (i in MAX_BACKUP_FILES downTo 1) {
                    val oldFile = File(file.parentFile, "${LOG_FILE_NAME}.$i")
                    val newFile = File(file.parentFile, "${LOG_FILE_NAME}.${i + 1}")
                    if (oldFile.exists()) {
                        if (i == MAX_BACKUP_FILES) {
                            oldFile.delete()
                        } else {
                            oldFile.renameTo(newFile)
                        }
                    }
                }
                
                // Rename current log to .1
                file.renameTo(File(file.parentFile, "${LOG_FILE_NAME}.1"))
            }
        }
    }
    
    fun getLogFiles(): List<File> {
        val logFiles = mutableListOf<File>()
        logFile?.parentFile?.let { dir ->
            dir.listFiles { file -> 
                file.name.startsWith(LOG_FILE_NAME)
            }?.let { files ->
                logFiles.addAll(files)
            }
        }
        return logFiles.sortedBy { it.name }
    }
    
    fun clearLogs() {
        executor.execute {
            try {
                writer?.close()
                getLogFiles().forEach { it.delete() }
                logFile?.let {
                    writer = PrintWriter(FileWriter(it, false), true)
                    writeLog("INFO", TAG, "Logs cleared")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear logs", e)
            }
        }
    }
    
    fun close() {
        executor.execute {
            try {
                writer?.close()
                writer = null
            } catch (e: Exception) {
                Log.e(TAG, "Failed to close FileLogger", e)
            }
        }
        executor.shutdown()
    }
}