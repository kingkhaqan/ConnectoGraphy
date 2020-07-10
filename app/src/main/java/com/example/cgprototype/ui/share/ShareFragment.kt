package com.example.cgprototype.ui.share

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.cgprototype.*
import com.google.firebase.auth.FirebaseAuth
import java.io.File

class ShareFragment : Fragment() {

    private lateinit var shareViewModel: ShareViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        shareViewModel =
            ViewModelProviders.of(this).get(ShareViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_share, container, false)
//        val textView: TextView = root.findViewById(R.id.text_share)
//        shareViewModel.text.observe(this, Observer {
//            textView.text = it
//        })

        val listview = root.findViewById<ListView>(R.id.listview)
        val file =   File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!.absolutePath)
//        val imagesPath = getAllShownImagesPath(requireActivity())
//        val mediaPaths = getAllMedia(requireContext())
//        val sms = getSMS(requireContext())
//        val images = getMediaFiles(requireContext(), "image")
        val images = file.list()
        listview.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, images!!)
        listview.setOnItemClickListener { parent, view, position, id ->
            val iv = ImageView(requireContext())
//            iv.layoutParams = ViewGroup.LayoutParams(200, 200)
            val imagePath = file.absolutePath+"/"+images[position]
            val imageUri = imagePath.toUri()
//            iv.setImageURI(imageUri)

            object : ScalingBitmap(imagePath, TEMP_SCALED_HALF){
                override fun scalingCompleted(scaledImage: String) {
                    iv.setImageURI(Uri.parse(scaledImage))

                }
            }

            val name = FirebaseAuth.getInstance().currentUser?.displayName
            AlertDialog.Builder(requireContext()).apply {
                setView(iv)
                setPositiveButton("share"){di, id->

                    val photoURI = FileProvider.getUriForFile(requireContext(), requireContext().applicationContext.packageName + ".provider", File(imagePath))
                    val uri = Uri.fromFile(File(imagePath))
                    val share =  Intent(Intent.ACTION_SEND).apply {
                        setType("image/*")
                        putExtra(Intent.EXTRA_TEXT, "$name, $INSTANT_GRATITUDE_STRING")
                        if (Build.VERSION.SDK_INT >=24)
                            putExtra(Intent.EXTRA_STREAM, photoURI)
                        else
                            putExtra(Intent.EXTRA_STREAM, uri)
                    }


                    startActivity(Intent.createChooser(share, "Share to"));
                }
                show()

            }
        }
        return root
    }
}