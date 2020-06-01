import android.content.Context
import android.view.View
import com.kishcore.sdk.hybrid.api.PrintableData
import android.widget.ListView
import androidx.core.content.ContextCompat
import com.sabzafzar.flutter_hybrid_cp.Util
import android.widget.ImageView
import android.widget.TextView
import android.R.attr.font
import androidx.core.content.res.ResourcesCompat
import android.graphics.Typeface
import android.annotation.SuppressLint
import androidx.core.content.ContextCompat.getSystemService
import android.view.LayoutInflater
import com.ayalma.mobile_pos_plugin.R


public class FactorPrintableData : PrintableData {
    override fun toView(context: Context?): View {
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var root: View = inflater.inflate(R.layout.print_data, null, false);
        root?.requestLayout()
        return root
    }
}