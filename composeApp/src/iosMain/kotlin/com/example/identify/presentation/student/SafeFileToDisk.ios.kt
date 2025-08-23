@file:OptIn(BetaInteropApi::class)

package com.example.identify.presentation.student

//import platform.Foundation.NSData
//import platform.Foundation.NSDocumentDirectory
//import platform.Foundation.NSSearchPathForDirectoriesInDomains
//import platform.Foundation.NSUserDomainMask
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.*


@OptIn(ExperimentalForeignApi::class)
actual fun saveFileToDisk(byteArray: ByteArray, filename: String): String {
//    val paths = NSSearchPathForDirectoriesInDomains(
//        NSDocumentDirectory,
//        NSUserDomainMask,
//        true
//    )
//    val documentsDirectory = paths.first() as String
//    val filePath = "$documentsDirectory/$filename"
//
//    val data = NSData.create(bytes = byteArray, length = byteArray.size.toULong())
//    data?.writeToFile(filePath, atomically = true)
//
//    return filePath
}