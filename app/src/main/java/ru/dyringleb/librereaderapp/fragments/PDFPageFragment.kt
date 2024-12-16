import android.graphics.Bitmap
import android.os.Bundle
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import ru.dyringleb.librereaderapp.R

class PDFPageFragment : Fragment() {
    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bitmap = it.getParcelable(ARG_BITMAP)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.item_pdf_page, container, false)
        val imageView: ImageView = view.findViewById(R.id.imageView)
        imageView.setImageBitmap(bitmap)
        return view
    }

    companion object {
        private const val ARG_BITMAP = "bitmap"

        fun newInstance(bitmap: Bitmap): PDFPageFragment {
            val fragment = PDFPageFragment()
            val args = Bundle()
            args.putParcelable(ARG_BITMAP, bitmap)
            fragment.arguments = args
            return fragment
        }
    }
}

