package demo.android.ss.com.floatbubbleapplication

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast

class TestFloatView : RelativeLayout{
     constructor(context: Context) : this(context, null)
     constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
     constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        val rootView = View.inflate(context, R.layout.test_float_view, this)

        val button = rootView.findViewById<Button>(R.id.float_button)
        button.setOnClickListener {
            Toast.makeText(context, "float button clicked ", Toast.LENGTH_LONG).show()
        }
    }


}