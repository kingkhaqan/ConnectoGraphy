package com.example.cgprototype

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat.animate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jsibbold.zoomage.ZoomageView
import com.ortiz.touchview.TouchImageView
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditorView
import ja.burhanrashid52.photoeditor.PhotoFilter
import kotlinx.android.synthetic.main.activity_editing.*
import kotlinx.android.synthetic.main.view_color_selector.*
import ozaydin.serkan.com.image_zoom_view.ImageViewZoom
import java.io.File


class EditingActivity : AppCompatActivity() {


    var isShown = false
    lateinit var shapeImageView: ImageView
    val DEFAULT_SHAPE_SIZE = 60

    private var imageBitmap: Bitmap? = null


    val DEFAULT_BRUSH_SIZE = 10f
    val DEFAULT_BRUSH_OPACITY = 80
    val DEFAULT_ERASER_SIZE = 20f

    var isSelectorShowing = false
    lateinit var mPhotoEditorView: PhotoEditorView
    lateinit var mPhotoEditor: PhotoEditor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editing)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        val bmpData = intent.getByteArrayExtra(EXTRA_IMAGE_BYTE_ARRAY)



        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)

        val anushka = BitmapFactory.decodeResource(resources, R.drawable.anushka)
        val shape = BitmapFactory.decodeResource(resources, R.drawable._shape)



        mPhotoEditorView = findViewById<PhotoEditorView>(R.id.photoEditorView)
        if (bmpData!=null){

            image_button.visibility = View.GONE
            imageview_croping.visibility = View.GONE

             imageBitmap = BitmapFactory.decodeByteArray(bmpData,0 , bmpData.size)

            if (imageBitmap!=null){
                val bmpWidth = imageBitmap!!.width; val bmpHeight = imageBitmap!!.height

                val bmp = Bitmap.createBitmap(bmpWidth,  bmpHeight, Bitmap.Config.ARGB_8888)

                val newBitmap = Bitmap.createBitmap(bmpWidth*2, bmpHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(newBitmap).apply {
                    drawColor(Color.WHITE)
                    drawBitmap(imageBitmap!!, 0f , 0f,  null)
                    drawBitmap(bmp, bmpWidth.toFloat(), 0f, null)
                }



                mPhotoEditorView.source.setImageBitmap(newBitmap)



                }

        }
        else{

            image_button.visibility = View.VISIBLE
            imageview_croping.visibility = View.VISIBLE

            val mBitmap = Bitmap.createBitmap(BITMAP_IMAGE_WIDTH, BITMAP_IMAGE_HEIGHT, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(mBitmap).apply {
                drawColor(Color.WHITE)
            }

            mPhotoEditorView.source.setImageBitmap(mBitmap)



        }


        // PIECE OF CODE TO ADD A CROPPING SHAPE OPTIONS
//        imageview_croping.visibility = View.GONE

//        val shapeTouchView = TouchImageView(this).apply {
////            setImageBitmap(anushka)
//            setImageResource(R.drawable._shape)
//            layoutParams = RelativeLayout.LayoutParams(500, 500)
//
//        }
//        var ddX = 0f
//        var ddY = 0f
//        shapeTouchView.setOnTouchListener { view, event ->
//
//            when (event.action) {
//
//                MotionEvent.ACTION_DOWN -> {
//                    ddX = view.x - event.rawX
//                    ddY = view.y - event.rawY
//                }
//                MotionEvent.ACTION_MOVE -> view.animate()
//                    .x(event.rawX + ddX)
//                    .y(event.rawY + ddY)
//                    .setDuration(0)
//                    .start()
//                else -> false
//            }
//            true
//        }
//
//        mPhotoEditorView.addView(shapeTouchView)


        val CROP_SHAPE_MAX_SCALE = 300
        var mScaleFactor = 1f

        val scaleListener = object: ScaleGestureDetector.SimpleOnScaleGestureListener(){
            override fun onScale(detector: ScaleGestureDetector?): Boolean {
                mScaleFactor *= detector!!.scaleFactor

                // Don't let the object get too small or too large.
                mScaleFactor = Math.max(1f, Math.min(mScaleFactor, 3.0f))

//                shapeImageView.scaleX = shapeImageView.scaleX+mScaleFactor
//                shapeImageView.scaleY = shapeImageView.scaleY+mScaleFactor
                shapeImageView.layoutParams = RelativeLayout.LayoutParams((mScaleFactor*CROP_SHAPE_MAX_SCALE).toInt(), (mScaleFactor*CROP_SHAPE_MAX_SCALE).toInt())
//                Toast.makeText(this@EditingActivity, mScaleFactor.toString(), Toast.LENGTH_SHORT).show()

                return true
            }
        }

        val mScaleDetector = ScaleGestureDetector(this, scaleListener)

        var dX = 0f
        var dY = 0f
        shapeImageView = ImageView(this)
//        shapeImageView.layoutParams = RelativeLayout.LayoutParams(DEFAULT_SHAPE_SIZE, DEFAULT_SHAPE_SIZE)



        shapeImageView.layoutParams = RelativeLayout.LayoutParams((mScaleFactor*CROP_SHAPE_MAX_SCALE).toInt(), (mScaleFactor*CROP_SHAPE_MAX_SCALE).toInt())
        shapeImageView.setOnTouchListener { view, event ->

            mScaleDetector.onTouchEvent(event)

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

        mPhotoEditorView.addView(shapeImageView)



        var shapeNo = 0
        imageview_croping.setOnClickListener {
            animateTools(TOOLS_CROPING)
        }
        btn_add_shape.setOnClickListener {
            if(isShown){
                shapeImageView.setImageResource(android.R.color.transparent)
                isShown = false
                btn_add_shape.text = "add shape"
            }
            else {
                if(shapeNo==0) {
                    shapeImageView.setImageResource(R.drawable._shape)

                }
                else if(shapeNo==1) {
                    shapeImageView.setImageResource(R.drawable._shape4)

                }
                else if(shapeNo==2) {
                    shapeImageView.setImageResource(R.drawable._shape3)

                }
//                shapeImageView.setImageBitmap(shape)
                shapeNo = (shapeNo+1) % 3
//                Toast.makeText(this, shapeNo.toString(), Toast.LENGTH_SHORT).show()
                isShown = true
                btn_add_shape.text = "remove shape"
            }
        }
        seekbar_shape_size.visibility = View.GONE
        seekbar_shape_size.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                shapeImageView.layoutParams = RelativeLayout.LayoutParams(SHAPE_SCALE_FACTOR*progress, SHAPE_SCALE_FACTOR*progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // END OF CROPPING SHAPE



        val permissionsManager = PermissionsManager(this)

        image_button.setOnClickListener {



            if (permissionsManager.isPermissionGranted(PermissionsManager.PERM_READ_EXTERNAL_STORAGE)){
                initiateImageUpload()
            }
            else{
                permissionsManager.askForPermissions(arrayOf(
                    PermissionsManager.PERM_READ_EXTERNAL_STORAGE,
                    PermissionsManager.PERM_WRITE_EXTERNAL_STORAGE
                ), PermissionsManager.CODE_STORAGE_PERM)
            }
        }


         mPhotoEditor = PhotoEditor.Builder(this, mPhotoEditorView)
            .setPinchTextScalable(true)
            .build()

        mPhotoEditor.setBrushEraserSize(DEFAULT_ERASER_SIZE)
        mPhotoEditor.setBrushDrawingMode(true)
        mPhotoEditor.brushSize = DEFAULT_BRUSH_SIZE
        mPhotoEditor.setOpacity(DEFAULT_BRUSH_OPACITY)


        recyclerview_filters.apply {
            layoutManager = LinearLayoutManager(this@EditingActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = object: RecyclerView.Adapter<PhotoFilterViewHolder>(){
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): PhotoFilterViewHolder {
                    val itemView = LayoutInflater.from(context).inflate(R.layout.view_filter_item, parent, false)

                    return PhotoFilterViewHolder(itemView)
                }

                override fun onBindViewHolder(holder: PhotoFilterViewHolder, position: Int) {
                    holder.bind(filterItems[position])
                    holder.imageView.setOnClickListener {
                        mPhotoEditor.setFilterEffect(filterItems[position].filter)
                    }

                }

                override fun getItemCount() = filterItems.size


            }
        }

        seekbar_brush_size.progress = DEFAULT_BRUSH_SIZE.toInt()
        seekbar_brush_opacity.progress = DEFAULT_BRUSH_OPACITY
        seekbar_eraser_size.progress = DEFAULT_ERASER_SIZE.toInt()

        animateTools(TOOLS_BRUSH)
        imageview_brush.setOnClickListener {
//            animateBrushTools()
            animateTools(TOOLS_BRUSH)

        }
        imageview_eraser.setOnClickListener {
//            animateEraserTools()
            animateTools(TOOLS_ERASER)

        }
        imageview_text.setOnClickListener {
            animateTools(TOOLS_TEXT)
        }
        imageview_filters.setOnClickListener {
            animateTools(TOOLS_FILTERS)
        }

        button_add_text.setOnClickListener {
            val text = editext_text_tools.text.toString()
            mPhotoEditor.addText(text, Color.parseColor("#ff0000"))
            editext_text_tools.setText("")
        }

        seekbar_eraser_size.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                mPhotoEditor.setBrushEraserSize(progress.toFloat())
                mPhotoEditor.brushEraser()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekbar_brush_size.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {

                mPhotoEditor.brushSize = progress.toFloat()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekbar_brush_opacity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {

                mPhotoEditor.setOpacity(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })




        val recyclerView = selector.findViewById<RecyclerView>(R.id.recyclerview).apply {
            layoutManager = LinearLayoutManager(this@EditingActivity, LinearLayoutManager.HORIZONTAL, true)

            adapter = object : ColorCodeSelectorAdapter(this@EditingActivity, brushColors){
                override fun onClick(color: Int) {
                    mPhotoEditor.brushColor = color

                }
            }


        }


    }


    private fun initiateImageUpload() {
        HomeActivity.clickedHalfStoryId = null
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(this);

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null && resultCode == Activity.RESULT_OK)
            when (requestCode) {
                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {

                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.getUri()

                    val bitmap = BitmapFactory.decodeFile(resultUri.path)
                    val scaled = Bitmap.createScaledBitmap(bitmap, BITMAP_IMAGE_WIDTH, BITMAP_IMAGE_HEIGHT, false)


                    mPhotoEditorView.source.setImageBitmap(scaled)


                }
            }
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PermissionsManager.CODE_STORAGE_PERM -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    initiateImageUpload()
                }
                return
            }

        }
    }

    private fun animateTools(tools: String){

        imageview_brush.background = getRoundedColorBitmap(this, Color.parseColor(UNSELECTED_TOOL_BACKGROUND_COLOR_STR))
        imageview_eraser.background = getRoundedColorBitmap(this, Color.parseColor(UNSELECTED_TOOL_BACKGROUND_COLOR_STR))
        imageview_text.background = getRoundedColorBitmap(this, Color.parseColor(UNSELECTED_TOOL_BACKGROUND_COLOR_STR))
        imageview_filters.background = getRoundedColorBitmap(this, Color.parseColor(UNSELECTED_TOOL_BACKGROUND_COLOR_STR))
        imageview_croping.background = getRoundedColorBitmap(this, Color.parseColor(UNSELECTED_TOOL_BACKGROUND_COLOR_STR))

        ll_eraser_wrapper.visibility = View.GONE
        ll_brush_tools_wrapper.visibility = View.GONE
        ll_text_tools_wrapper.visibility = View.GONE
        ll_filter_tools.visibility = View.GONE
        ll_croping_tools.visibility = View.GONE

        when(tools){
            TOOLS_CROPING->{
                ll_croping_tools.visibility = View.VISIBLE
                imageview_croping.background = getRoundedColorBitmap(this, Color.parseColor(SELECTED_TOOL_BACKGROUND_COLOR_STR))
            }
            TOOLS_BRUSH->{
                mPhotoEditor.setBrushDrawingMode(true)
                ll_brush_tools_wrapper.visibility = View.VISIBLE
                imageview_brush.background = getRoundedColorBitmap(this, Color.parseColor(SELECTED_TOOL_BACKGROUND_COLOR_STR))
            }
            TOOLS_ERASER->{
                mPhotoEditor.brushEraser()
                ll_eraser_wrapper.visibility = View.VISIBLE
                imageview_eraser.background = getRoundedColorBitmap(this, Color.parseColor(SELECTED_TOOL_BACKGROUND_COLOR_STR))
            }
            TOOLS_TEXT->{
                ll_text_tools_wrapper.visibility = View.VISIBLE
                imageview_text.background = getRoundedColorBitmap(this, Color.parseColor(SELECTED_TOOL_BACKGROUND_COLOR_STR))
            }
            TOOLS_FILTERS->{
                ll_filter_tools.visibility = View.VISIBLE
                imageview_filters.background = getRoundedColorBitmap(this, Color.parseColor(SELECTED_TOOL_BACKGROUND_COLOR_STR))
            }
        }
    }

//    private fun animateColorSelector() {
//        if (isSelectorShowing)
//            hideSelector()
//        else
//            showSelector()
//
//    }
//
//    private fun translateSelector(v: Int){
//        animate(selector).apply { translationY(v*(selector.height.toFloat())) }
//    }
//    private fun showSelector() {
//        translateSelector(-1)
//        isSelectorShowing = true
//    }
//
//    private fun hideSelector() {
//        translateSelector(1)
//        isSelectorShowing = false
//    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){

            R.id.tool_undo->{
                mPhotoEditor.undo()
            }
            R.id.tool_redo->{
                mPhotoEditor.redo()
            }


            R.id.tool_send->{





                val tv = TextView(this)
                tv.text = "Are you sure to save the paint?"
                AlertDialog.Builder(this).apply {
                    setView(tv)
                    setPositiveButton("ok"){di, id->
                        val file =   File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");

                        mPhotoEditor.saveAsFile(file.absolutePath, object: PhotoEditor.OnSaveListener{
                            override fun onSuccess(imagePath: String) {

//                                if (isShown){
//                                    val image = BitmapFactory.decodeFile(imagePath)
//                                    val bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
//                                    val canvas = Canvas(bitmap)
//                                    canvas.drawBitmap(image, 0f, 0f, null)
//
//                                    AlertDialog.Builder(this@EditingActivity).apply {
//                                        setView(ImageView(this@EditingActivity).apply {
//                                            setImageBitmap(image)
//                                        })
//                                        show()
//                                    }
//
//
//                                }


                                val intnt = Intent(this@EditingActivity, UploadActivity::class.java).apply {
                                    //                        putExtra(HALF_STORY_DATA_KEY, clickedHalfStoryId)
                                    if (HomeActivity.clickedHalfStoryId!=null) {
                                        object : ScalingBitmap(imagePath, TEMP_SCALED){
                                            override fun scalingCompleted(scaledImage: String) {
                                                putExtra(EXTRA_IMAGE_PATH, scaledImage)

                                            }
                                        }
                                    }
                                    else{
                                        object : ScalingBitmap(imagePath, TEMP_SCALED_HALF){
                                            override fun scalingCompleted(scaledImage: String) {
                                                putExtra(EXTRA_IMAGE_PATH, scaledImage)

                                            }
                                        }
                                    }
                                }
                                startActivity(intnt)

                                finish()


                            }

                            override fun onFailure(exception: Exception) {}
                        })
                    }
                    setNegativeButton("cancel"){di, id->}
                    show()
                }



            }
            android.R.id.home->{
                val tv = TextView(this).apply {
                    text = "You're leaving the Paint..."
                    textSize = 20f
                    setTextColor(Color.BLACK)
                }

                AlertDialog.Builder(this).apply {
                    setView(tv)
                    show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.drawing, menu)
        return super.onCreateOptionsMenu(menu)


    }




     abstract class ColorCodeSelectorAdapter(val context: Context, val colors: Array<Int>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
         abstract fun onClick(color: Int)
         override fun onCreateViewHolder(
             parent: ViewGroup,
             viewType: Int
         ) = object : RecyclerView.ViewHolder(View(context)){}
         override fun getItemCount() = colors.size
         override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

             holder.itemView.apply {
                 layoutParams = ViewGroup.LayoutParams(ROUNDED_BITMAP_DIAMETER, ROUNDED_BITMAP_DIAMETER)
                 background = getRoundedColorBitmap(context, colors[position])
                 setOnClickListener {
                     onClick(colors[position])
                 }
             }

         }

     }







}
