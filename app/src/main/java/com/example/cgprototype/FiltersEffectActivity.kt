package com.example.cgprototype

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.ArrayAdapter
import android.widget.Toast
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoFilter
import kotlinx.android.synthetic.main.activity_filters_effect.*
import java.io.File

class FiltersEffectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filters_effect)

        val gcu_filter = BitmapFactory.decodeResource(resources, R.drawable.gcu_filter)
        photoEditorView.source.setImageResource(R.drawable.gcu_filter)
        val mPhotoEditor = PhotoEditor.Builder(this, photoEditorView)
            .setPinchTextScalable(true)
            .build()


        val filtersArray = arrayOf(
//            PhotoFilter.NONE,
//        PhotoFilter.AUTO_FIX,
//        PhotoFilter.BLACK_WHITE,
//        PhotoFilter.CONTRAST,
//        PhotoFilter.CROSS_PROCESS,
//        PhotoFilter.DOCUMENTARY,
//        PhotoFilter.DUE_TONE,
//        PhotoFilter.FILL_LIGHT,
//        PhotoFilter.FISH_EYE,
//        PhotoFilter.GRAY_SCALE,
//        PhotoFilter.NEGATIVE,
//        PhotoFilter.GRAIN,
//        PhotoFilter.LOMISH,
//        PhotoFilter.POSTERIZE,
//        PhotoFilter.SATURATE,
//        PhotoFilter.SEPIA,
//        PhotoFilter.SHARPEN,
        PhotoFilter.TEMPERATURE,
        PhotoFilter.TINT,
        PhotoFilter.VIGNETTE,
        PhotoFilter.BRIGHTNESS
        )

        val filterNames = arrayOf(
//            "None",
//            "Auto Fix",
//            "Black White",
//            "Contrast",
//            "Cross Process",
//            "Documentary",
//            "Due Tone",
//            "Fill Light",
//            "Fish Eye",
//            "Gray Scale",
//            "Negative",
//            "Grain",
//            "Lomish",
//            "Posterize",
//            "Saturate",
//            "Sepia",
//            "Sharpen",
            "Temprature",
            "Tint",
            "Vignette",
            "Brightness"
        )

        listview.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, filterNames)
        listview.setOnItemClickListener { parent, view, position, id ->
            val filterName = filterNames[position]
            val filter = filtersArray[position]
            mPhotoEditor.setFilterEffect(filter)
            save_button.setOnClickListener {
                val file =   File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "filter_$filterName" + ".png");

                mPhotoEditor.saveAsFile(file.absolutePath, object: PhotoEditor.OnSaveListener{
                    override fun onSuccess(imagePath: String) {
                        Toast.makeText(this@FiltersEffectActivity, imagePath, Toast.LENGTH_SHORT).show()
                        photoEditorView.source.setImageResource(R.drawable.gcu_filter)

                    }

                    override fun onFailure(exception: Exception) {}
                })
            }
        }


    }
}
