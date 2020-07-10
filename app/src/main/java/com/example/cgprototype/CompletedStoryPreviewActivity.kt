package com.example.cgprototype

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_completed_story_preview.*
import java.io.File
import java.net.URI

class CompletedStoryPreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_completed_story_preview)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val imagepath = intent.getStringExtra(EXTRA_IMAGE_PATH)
        val oneLiner = intent.getStringExtra(EXTRA_TEXT)
        if (imagepath!=null) {
            val uri = Uri.fromFile(File(imagepath))
            imageview.setImageURI(uri)
            share_btn.setOnClickListener {
                val photoURI = FileProvider.getUriForFile(this, this.baseContext.packageName + ".provider", File(uri.path!!))

                val share =  Intent(Intent.ACTION_SEND).apply {
                    setType("image/*")
                    if (oneLiner!=null)
                        putExtra(Intent.EXTRA_TEXT, oneLiner)
                    if (Build.VERSION.SDK_INT >=24)
                        putExtra(Intent.EXTRA_STREAM, photoURI)
                    else
                        putExtra(Intent.EXTRA_STREAM, uri)
                }


                startActivity(Intent.createChooser(share, "Share to"));
            }
        }


    }
}
