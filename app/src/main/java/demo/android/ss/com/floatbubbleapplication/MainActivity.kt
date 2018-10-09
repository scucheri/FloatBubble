package demo.android.ss.com.floatbubbleapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.MotionEvent
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import com.ss.android.floatbubble.FBConstant
import com.ss.android.floatbubble.FloatBubblePermission
import com.ss.android.floatbubble.FloatBubble
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    var floatBubbles: ArrayList<FloatBubble> = ArrayList()
    var gravityList: ArrayList<Int> = ArrayList()
    var animationList: ArrayList<Int> = ArrayList()
    var dragModeList: ArrayList<Int> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initGravity()
        initAnimationList()
        initDragMode()

        // Example of a call to a native method
        sample_text.text = stringFromJNI()

//        TestMy.addWindow(this,TestFloatView(this))

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            Toast.makeText(this, "点击了", Toast.LENGTH_SHORT).show()
            startActivity(newIntent(this))
        }

        val layerSpinner = findViewById<Spinner>(R.id.layers_spinner)
        val dragSpinner = findViewById<Spinner>(R.id.drag_mode_spinner)
        val gravitySpinner = findViewById<Spinner>(R.id.gravity_spinner)
        val animationSpinner = findViewById<Spinner>(R.id.animations_spinner)
        val makeFloatBubble = findViewById<Button>(R.id.make_float_bubble)

        makeFloatBubble.setOnClickListener {
            var floatBubble : FloatBubble ? = null
            if (layerSpinner.selectedItemPosition == 0) {
                floatBubble = FloatBubble.makeLocalFloat(this, TestFloatView(application))
            } else {
                if(!FloatBubblePermission.isPermissionGranted(this)) {
                    FloatBubblePermission.requestFloatPermission(this)
                }else {
                    floatBubble = FloatBubble.makeGlobalFloat(application, TestFloatView(application))
                    floatBubble?.setOnDismissListener { Toast.makeText(this,"dismiss float bubble",Toast.LENGTH_LONG).show() }
                            ?.setOnShowListener { Toast.makeText(this,"show float bubble",Toast.LENGTH_LONG).show()}
                            ?.setCloseIcon(true)
                }
            }
            var axisX = 0
            var axisY = 0

            floatBubble?.setDragMode(dragModeList.get(dragSpinner.selectedItemPosition))
                    ?.setGravity(gravityList.get(gravitySpinner.selectedItemPosition))
                    ?.setAxis(axisX, axisY)
                    ?.setAnimationMode(animationList.get(animationSpinner.selectedItemPosition))
                    ?.show()
            if(floatBubble != null) {
                floatBubbles.add(floatBubble)
            }
        }

        val clearButton = findViewById<Button>(R.id.clear_float_bubble)
        clearButton.setOnClickListener {
            floatBubbles.forEach {
                if(it.isShown) {
                    it?.dismiss()
                }
            }
            floatBubbles?.clear()
        }
    }


    private fun initDragMode(){
        dragModeList.add(FBConstant.DRAG_MODE.FREE_DRAGGABLE)
        dragModeList.add(FBConstant.DRAG_MODE.HORIAONTAL_DRAGGABLE)
        dragModeList.add(FBConstant.DRAG_MODE.VERTICAL_DRAGGABLE)
        dragModeList.add(FBConstant.DRAG_MODE.NON_DRAGGABLE)
    }

    private fun initAnimationList(){
        animationList.add(FBConstant.ANIMATION.NO_ANIMATION)
        animationList.add(FBConstant.ANIMATION.APPEAL_LEFT_OR_RIGHT)
    }

    private fun initGravity() {
        gravityList.add(Gravity.LEFT or Gravity.TOP)
        gravityList.add(Gravity.RIGHT or Gravity.TOP)
        gravityList.add(Gravity.LEFT or Gravity.BOTTOM)
        gravityList.add(Gravity.RIGHT or Gravity.BOTTOM)
        gravityList.add(Gravity.CENTER)
        gravityList.add(Gravity.CENTER_HORIZONTAL or Gravity.TOP)
        gravityList.add(Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM)
        gravityList.add(Gravity.CENTER_VERTICAL or Gravity.LEFT)
        gravityList.add(Gravity.CENTER_VERTICAL or Gravity.RIGHT)
    }


    override fun onDestroy() {
        super.onDestroy()
//        floatBubbles.forEach {
//            it.dismiss()
//        }
//        floatBubbles.clear()
    }

    fun newIntent(context: Context): Intent {
        val intent = Intent(context, Main2Activity::class.java)
        return intent
    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        println("dispatch activity ${ev.toString()}")
        return super.dispatchTouchEvent(ev)
    }
}
