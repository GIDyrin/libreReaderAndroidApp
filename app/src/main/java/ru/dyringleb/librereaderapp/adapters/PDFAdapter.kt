import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.io.File

class PDFAdapter(fragmentActivity: FragmentActivity, private val pdfFilePath: String) : FragmentStateAdapter(fragmentActivity) {
    private var pdfRenderer: PdfRenderer? = null
    private var pageCount: Int = 0

    init {
        val file = File(pdfFilePath)
        val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        pdfRenderer = PdfRenderer(fileDescriptor)
        pageCount = pdfRenderer!!.pageCount
    }

    override fun getItemCount(): Int = pageCount

    override fun createFragment(position: Int): Fragment {
        val page = pdfRenderer?.openPage(position)
        val bitmap = Bitmap.createBitmap(page!!.width, page.height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
        page.close()
        return PDFPageFragment.newInstance(bitmap)
    }

    fun close() {
        pdfRenderer?.close()
    }
}
