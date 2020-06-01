import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import com.kishcore.sdk.hybrid.api.PrintableData
import android.view.LayoutInflater
import android.widget.ImageView
import com.ayalma.mobile_pos_plugin.R


public class FactorPrintableData(private  var bytes: ByteArray) : PrintableData {

    override fun toView(context: Context?): View {
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var root: View = inflater.inflate(R.layout.print_data, null, false)

        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val printImage = root.findViewById<ImageView>(R.id.printImage);

        printImage.setImageBitmap(bmp)
        printImage.setImageBitmap(Bitmap.createBitmap(bmp))
        root?.requestLayout()
        return root
    }
}