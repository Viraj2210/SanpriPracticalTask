package com.sanpri.sanpripracticaltask.base

import java.io.File
import java.io.FileInputStream
import java.io.IOException


object FileLoader {
    private const val BUFFER_SIZE = 4096 // Adjust the buffer size as needed
    fun loadFileInChunks(file: File?, callback: FileLoadingCallback) {
        try {
            FileInputStream(file).use { inputStream ->
                val buffer = ByteArray(BUFFER_SIZE)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    // Process the current chunk of data
                    callback.onChunkLoaded(buffer, bytesRead)
                }

                // Notify callback that the file has been completely loaded
                callback.onLoadComplete()
            }
        } catch (e: IOException) {
            // Handle the exception
            callback.onLoadError(e)
        }
    }

    interface FileLoadingCallback {
        fun onChunkLoaded(chunk: ByteArray?, bytesRead: Int)
        fun onLoadComplete()
        fun onLoadError(e: Exception?)
    }
}
