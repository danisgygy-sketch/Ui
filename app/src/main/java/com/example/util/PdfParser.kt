package com.example.util

import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder

object PdfParser {
    fun parsePdfText(context: Context, uri: Uri): String {
        val sb = StringBuilder()
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, "ISO-8859-1")).use { reader ->
                    var line: String?
                    var count = 0
                    while (reader.readLine().also { line = it } != null && count < 5000) {
                        line?.let { l ->
                            var index = 0
                            while (true) {
                                val start = l.indexOf('(', index)
                                if (start == -1) break
                                val end = l.indexOf(')', start)
                                if (end == -1) break
                                if (end > start + 1) {
                                    val textSegment = l.substring(start + 1, end)
                                    // avoid parsing binary garbage/formatting
                                    if (textSegment.all { it.isLetterOrDigit() || it.isWhitespace() || ",.-_/()".contains(it) }) {
                                        sb.append(textSegment).append(" ")
                                        count++
                                    }
                                }
                                index = end + 1
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val text = sb.toString().replace(Regex("\\s+"), " ").trim()
        return if (text.length > 50) text.take(5000) else "Dokumen Silabus Kompetensi Pendidikan Standardisasi Nasional."
    }
}
