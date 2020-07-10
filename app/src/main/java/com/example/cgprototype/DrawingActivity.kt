package com.example.cgprototype

import android.content.Context
import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.scale
import kotlinx.android.synthetic.main.activity_drawing.*

class DrawingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawing)

        actionBar?.setDisplayHomeAsUpEnabled(true)

        val uid = intent.getStringExtra(EXTRA_USER_ID)

        var anushka = BitmapFactory.decodeResource(resources, R.drawable.anushka)
//        var shape = Bitmap.createBitmap(160, 160, Bitmap.Config.ARGB_8888)

        val shape = BitmapFactory.decodeResource(resources, R.drawable._shape)
        val scaled = Bitmap.createScaledBitmap(shape, 120, 120, false)
//        Canvas(shape).apply {
//            drawColor(Color.GREEN)
//        }

//        AlertDialog.Builder(this).apply {
//            setView(ImageView(this@DrawingActivity).apply {
//                setImageBitmap(shape)
//            })
//            show()
//        }

        if (shape!=null)
            cropimagedraw.init(anushka, scaled)


        print_button.setOnClickListener {
            cropimagedraw.getBitmap()
        }
//        cropimagedraw.createImageView(shape)

//         var photoFilter = PhotoFilter(GLSurfaceView(this), object: OnProcessingCompletionListener {
//            override fun onProcessingComplete(bitmap: Bitmap) {
//                // Do anything with the bitmap save it or add another effect to it
//            }
//        })
//
//        photoFilter?.applyEffect(anushka, AutoFix())
//
//        val metrics = DisplayMetrics()
//        windowManager.defaultDisplay.getMetrics(metrics)
////        if (uid==null)
////            myDraw.init(metrics)
////        else
//            myDraw.init(metrics, anushka)
//        myDraw.setBrushSize(5f)
//        myDraw.setBrushColor(Color.RED)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.drawing, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
//            R.id.tool_color->{
//                val grid = LayoutInflater.from(this).inflate(R.layout.view_color_selector, null, false)
//                AlertDialog.Builder(this).apply {
//                    setView(grid)
//                    show()
//                }
//            }
//            R.id.tool_undo->{
//                myDraw.undo()
//            }
//            R.id.tool_brush->{
//
//            }
            R.id.tool_send->{
//                val obitmap = myDraw.getOrignalBitmap()
//                val bitmap = myDraw.getBitmap()
//
//
                val iv = ImageView(this).apply {
                    setImageBitmap(cropimagedraw.getBitmap())
                }
//
                AlertDialog.Builder(this).apply {
//                    if (obitmap!=null)
//                        iv.setImageBitmap(bitmap)
//                    else
//                        iv.setImageBitmap(getLaftHalf(bitmap!!))
                    setView(iv)
                    show()
                }
            }
        }
        val tv = TextView(this).apply {
            text = "Action here"
        }

        return super.onOptionsItemSelected(item)
    }



}

class CropImageDraw(context: Context, attrs: AttributeSet) : View(context, attrs){
    private var mPaint = Paint()
     var mBitmap: Bitmap? = null
    private var mCanvas: Canvas? = null
     val imageView = View(context)
     var shape: Bitmap? = null

    init {
        mPaint.apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 8f
            isAntiAlias = true
            setBackgroundColor(Color.WHITE)
        }
    }

    fun init(bitmap: Bitmap?, sh: Bitmap){

        shape = sh
//        mBitmap = bitmap?.copy(Bitmap.Config.ARGB_8888, true)

        if(bitmap!=null){
            mBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            mCanvas = Canvas(mBitmap!!)
            mCanvas?.drawBitmap(bitmap, 0f, 0f, mPaint)
//            mCanvas?.drawBitmap(sh, 0f,0f, mPaint)
        if (shape!=null)
            createImageView()


        }



    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (mBitmap!=null)
            canvas?.drawBitmap(mBitmap!!, 0f, 0f, mPaint)



//        if (shape!=null)
//            createImageView()
    }

    fun createImageView() {

        imageView.apply {
            background = shape?.toDrawable(resources)
            layoutParams = ViewGroup.LayoutParams(shape!!.width, shape!!.height)
        }
//        val imageView = ImageButton(context).apply {
//            setImageBitmap(shape)
//        }
//        val layoutParams = LinearLayout.LayoutParams(shape!!.width, shape!!.height)
//        layoutParams.leftMargin = 50
//        layoutParams.topMargin = 50
//
//
//        imageView.layoutParams = layoutParams
//        imageView.setImageBitmap(shape)
        var dX = 0f
        var dY = 0f
        imageView.setOnTouchListener { view, event ->

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> view.animate()
                    .x(event.rawX + dX)
                    .y(event.rawY + dY)
                    .setDuration(0)
                    .start()
                else -> false
            }
            true
        }




        val root = super.getRootView().findViewById<ViewGroup>(R.id.root)
        root.addView(imageView)
//        Toast.makeText(context, imageView.toString(), Toast.LENGTH_SHORT).show()

    }


    fun getBitmap(): Bitmap? {
//        mCanvas?.save()
//        mCanvas?.drawBitmap(mBitmap!!, 0f, 0f, mPaint)
        val dim = IntArray(2)
        imageView.getLocationOnScreen(dim)
//        dim[0] = imageView.left
        dim[1] -= shape!!.height+60
        if (shape!=null)
            mCanvas?.drawBitmap(shape!!, dim[0].toFloat(), dim[1].toFloat(), mPaint)
//        Toast.makeText(context, dim[0].toString(), Toast.LENGTH_SHORT).show()

        return mBitmap

    }

}
