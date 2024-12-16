package ru.dyringleb.librereaderapp

import android.content.Context
import android.util.Log
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

class FB2Parser(private val context: Context) {

    fun loadFile(file: File): String {
        // Загружаем XML и извлекаем текст
        val doc = loadXml(file)
        return extractText(doc)
    }

    // Загрузка XML файла
    private fun loadXml(file: File): Document {
        return try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder: DocumentBuilder = factory.newDocumentBuilder()
            builder.parse(file).apply { normalizeDocument() } // Нормализация документа
        } catch (e: Exception) {
            Log.e("FB2Parser", "Ошибка загрузки XML: ${e.message}")
            // Возвращаем пустой документ в случае ошибки
            DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        }
    }


    private fun extractText(doc: Document): String {
        val texts = mutableListOf<String>()
        val body = doc.getElementsByTagName("body").item(0) as? Element
        if (body != null) {
            val paragraphs = body.getElementsByTagName("p")
            for (i in 0 until paragraphs.length) {
                val paragraphText = paragraphs.item(i).textContent.trim()
                if (paragraphText.isNotEmpty()) {
                    texts.add(paragraphText)
                }
            }
        }
        return texts.joinToString("\n   ") // Объединение параграфов в одну строку
    }
}
