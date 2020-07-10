package com.example.cgprototype

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.facebook.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_upload.*
import kotlinx.android.synthetic.main.activity_upload.progress_bar

class UploadActivity : AppCompatActivity() {


    lateinit var loginClient: LoginClient

    var imagePath: String? = null
    var imageBtmp: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FacebookSdk.sdkInitialize(applicationContext);

        setContentView(R.layout.activity_upload)

        actionBar?.setDisplayHomeAsUpEnabled(true)



        imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH)

        if (imagePath!=null) {
            imageview.setImageURI(Uri.parse(imagePath))
//            imageBtmp = BitmapFactory.decodeFile(imagePath)
//            val shape = BitmapFactory.decodeResource(resources, R.drawable.hexagon)
//            val scaled = Bitmap.createScaledBitmap(shape, 120, 120, false)
//
//            if (scaled!=null)
//                cropimagedraw.init(imageBtmp, scaled)

        }

//        print_button.setOnClickListener {
//            imageBtmp = cropimagedraw.getBitmap()
//        }

    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_upload, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.upload->{

                val currentUser = FirebaseAuth.getInstance().currentUser
                progress_bar.visibility = View.VISIBLE
                if (currentUser!=null) {

                    checkLocationStatus(currentUser.uid)
//                        uploadContent()
                }

                else{
                    loginClient = object : LoginClient(this){
                        override fun updateUI(user: FirebaseUser?) {
                            if (user!=null){
                               checkLocationStatus(user.uid)
//                                uploadContent()
                            }

                        }
                    }

                    loginClient.initLogin()

                }

            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun checkLocationStatus(uid: String) {
        val pm = PermissionsManager(this@UploadActivity)
        if (pm.isPermissionGranted(PermissionsManager.PERM_EXCESS_FINE_LOCATION))
            getCurrentLocation()
        else
            pm.askForPermissions(arrayOf(PermissionsManager.PERM_EXCESS_FINE_LOCATION), PermissionsManager.CODE_FINE_LOCATION)
    }

    fun getCurrentLocation(){


        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val locationClient = object : LocationClient(this){
            override fun isLocationProvidedCallBack(location: LatLng?) {

                if (location == null)
                    getCurrentLocation()
                else
                    uploadContent()

            }

            override fun currentLocationCallBack(location: LatLng?) {

                if (location==null){
                    android.app.AlertDialog.Builder(this@UploadActivity).apply {
                        setView(TextView(this@UploadActivity).apply { text = "Please make sure that you've turned on device location" })
                        setPositiveButton("ok", {di, id-> })
                        setNegativeButton("cancel", {di, id-> di.dismiss()})
                        show()
                    }


                }
                else{
                    if (uid!=null)
                        FirebaseFirestore.getInstance().collection(COLLECTION_PROFILE).document(uid)
                            .update(
                                mapOf(
                                    "lat" to location.latitude,
                                    "lng" to location.longitude
                                )
                            )
                            .addOnSuccessListener {
                                Toast.makeText(
                                    activity
                                    , "location added", Toast.LENGTH_SHORT
                                ).show()
                            }
                }

                uploadContent()

            }

        }

        locationClient.checkIfLocationIsProvided(uid)
    }

    fun uploadContent(){

        val storageRef = getStorageRef()
        val oneLiner = editext.text.toString()

        object : ImageUploader(this){
            override fun imageUploadCompleted(imageUrl: String) {
                if (HomeActivity.clickedHalfStoryId!=null)
                    uploadCompletedStory(oneLiner, imageUrl, HomeActivity.clickedHalfStoryId)
                else
                    uploadHalfStory(oneLiner, imageUrl)

            }

            override fun storyUploadCompleted(storyId: String, type: String) {

                if (type== STORAGE_REF_COMPLETED)
                    startActivity(Intent(this@UploadActivity, CompletedStoryPreviewActivity::class.java).apply {
                        putExtra(EXTRA_IMAGE_PATH, imagePath)
                        putExtra(EXTRA_TEXT, oneLiner)
                    })
                progress_bar.visibility = View.GONE
                finish()

            }
        }.apply {

//            if (imageBtmp!=null){
//                val bos = ByteArrayOutputStream()
//                imageBtmp?.compress(Bitmap.CompressFormat.PNG, 100, bos)
//                val file = File.createTempFile(System.currentTimeMillis().toString(), "PNG")
//                file.writeBytes(bos.toByteArray())
//                bos.flush()
//                uploadImageToFirebase(storageRef, Uri.fromFile(file).path)
//            }
//            else
                uploadImageToFirebase(storageRef, imagePath)
        }




//    }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        loginClient.onResultReceived(requestCode, resultCode, data)


        if (data!=null && resultCode== Activity.RESULT_OK)
            when(requestCode) {
                PermissionsManager.CODE_FINE_LOCATION->{

                    getCurrentLocation()

                }

            }

    }




}
