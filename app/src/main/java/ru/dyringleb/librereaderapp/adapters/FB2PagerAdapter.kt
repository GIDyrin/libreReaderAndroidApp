package ru.dyringleb.librereaderapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.dyringleb.librereaderapp.R

class FB2PagerAdapter(
    private var pages: MutableList<String>,
    private val textSize: Float // Передача размера текста в адаптер
) : RecyclerView.Adapter<FB2PagerAdapter.PageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        // Создание view для страницы с использованием XML разметки
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_page, parent, false)
        return PageViewHolder(view, textSize)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.bind(pages[position])
    }

    override fun getItemCount(): Int {
        return pages.size
    }

    // Метод для добавления новой страницы
    fun addPage(page: String) {
        pages.add(page)
        notifyItemInserted(pages.size - 1) // Уведомляем адаптер, что добавлена новая страница
    }

    // Метод для добавления нескольких страниц
    fun addPages(newPages: List<String>) {
        val startPosition = pages.size
        pages.addAll(newPages)
        notifyItemRangeInserted(startPosition, newPages.size) // Уведомляем адаптер о добавлении нескольких страниц
    }

    // Метод для обновления страницы
    fun updatePage(position: Int, newPage: String) {
        if (position in pages.indices) {
            pages[position] = newPage
            notifyItemChanged(position) // Уведомляем адаптер о том, что определенная страница обновлена
        }
    }

    class PageViewHolder(private val view: View, private val textSize: Float) : RecyclerView.ViewHolder(view) {
        fun bind(pageText: String) {
            val textView = view.findViewById<TextView>(R.id.page_text)
            textView.text = pageText
            textView.textSize = textSize // Установка размера текста
            textView.setLineSpacing(4f, 1.2f) // Установка межстрочного интервала
        }
    }
}
