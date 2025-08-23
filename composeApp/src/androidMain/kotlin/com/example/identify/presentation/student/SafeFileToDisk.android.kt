package com.example.identify.presentation.student

import com.example.identify.app.App
import java.io.File

actual fun saveFileToDisk(byteArray: ByteArray, filename: String): String {
    val file = File(
        android.os.Environment.getExternalStoragePublicDirectory(
            android.os.Environment.DIRECTORY_DOWNLOADS
        ),
        filename
    )
    file.writeBytes(byteArray)
    return file.absolutePath
}