import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.petsapp26.R

class CustomArrayAdapter(context: Context, items: List<String>) : ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, items) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun isEnabled(position: Int): Boolean {
        // Disable the first item from Spinner
        // First item will be used as hint
        return position != 0
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {

        // Inflate the custom layout
        val view = inflater.inflate(R.layout.spinner_item_dropdown, parent, false) as TextView
        view.text = getItem(position)
        if (position == 0) {
            view.setTextColor(Color.GRAY)
        } else {
            view.setTextColor(Color.BLACK)
        }
        // Now each item has the background with a divider
        return view
    }
//    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
//        val view = super.getDropDownView(position, convertView, parent)
//        val textView = view as TextView
//        if (position == 0) {
//            // Set the hint text color grey
//            textView.setTextColor(Color.GRAY)
//        } else {
//            // Set the other items text color to black
//            textView.setTextColor(Color.BLACK)
//        }
//        return view
//    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent) as TextView
        if (position == 0) {
            // Set the hint text color grey
            view.setTextColor(Color.GRAY)
        } else {
            // Set the other items text color to black
            view.setTextColor(Color.BLACK)
        }
        return view
    }
}
