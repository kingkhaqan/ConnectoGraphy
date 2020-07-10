package com.example.cgprototype

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_home.*


class HomeActivity : AppCompatActivity() {

    lateinit var loginClient: LoginClient
    companion object{
        var clickedHalfStoryId: String? = null
    }

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)





        val anushka = BitmapFactory.decodeResource(resources, R.drawable.anushka)

        val permissionsManager = PermissionsManager(this)
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
//            startActivity(Intent(this, DrawingActivity::class.java))
//            Toast.makeText(this, getDateString(System.currentTimeMillis()).toString(), Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, EditingActivity::class.java))

//            if (isFabOptionsAreVisible)
//                hideOptionFab()
//            else
//                showOptionFab()


        }
//        fab_image.setOnClickListener {
//            if (permissionsManager.isPermissionGranted(PermissionsManager.PERM_READ_EXTERNAL_STORAGE)){
//                initiateImageUpload()
//            }
//            else{
//                permissionsManager.askForPermissions(arrayOf(
//                    PermissionsManager.PERM_READ_EXTERNAL_STORAGE,
//                    PermissionsManager.PERM_WRITE_EXTERNAL_STORAGE
//                ), PermissionsManager.CODE_STORAGE_PERM)
//            }
//
//        }
//        fab_drawing.setOnClickListener {
//            if (PermissionsManager(this).isPermissionGranted(PermissionsManager.PERM_READ_EXTERNAL_STORAGE)) {
//                clickedHalfStoryId = null
//                startActivity(Intent(this, EditingActivity::class.java))
//            }
//            else{
//                permissionsManager.askForPermissions(arrayOf(
//                    PermissionsManager.PERM_READ_EXTERNAL_STORAGE,
//                    PermissionsManager.PERM_WRITE_EXTERNAL_STORAGE
//                ), PermissionsManager.CODE_STORAGE_PERM)
//            }
//        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)





//        navController.addOnDestinationChangedListener{_, destination, _ ->
//            if (destination.id == R.id.nav_home)
//                tablayout.visibility = View.VISIBLE
//            else
//                tablayout.visibility = View.GONE
//        }




        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_my_half, R.id.nav_my_completed, R.id.nav_my_loc

            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)




        updateHeaderLayout(navView)

        loginClient = object : LoginClient(this){
            override fun updateUI(user: FirebaseUser?) {
                updateHeaderLayout(navView)
//                if (user!=null){
//                    val pm = PermissionsManager(this@HomeActivity)
//                    if (pm.isPermissionGranted(PermissionsManager.PERM_EXCESS_FINE_LOCATION))
//                        getCurrentLocation()
//                    else
//                        pm.askForPermissions(arrayOf(PermissionsManager.PERM_EXCESS_FINE_LOCATION), PermissionsManager.CODE_FINE_LOCATION)
//
//                }
            }
        }


    }

    fun getCurrentLocation(){


        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val locationClient = object : LocationClient(this){
            override fun isLocationProvidedCallBack(location: LatLng?) {

                if (location == null)
                    getCurrentLocation()

            }

            override fun currentLocationCallBack(location: LatLng?) {

                if (location==null){
                    AlertDialog.Builder(this@HomeActivity).apply {
                        setView(TextView(this@HomeActivity).apply { text = "Please make sure that you've turned on device location" })
                        setPositiveButton("try again", {di, id-> getCurrentLocation()})
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

            }

        }

        locationClient.checkIfLocationIsProvided(uid)
    }

    private fun updateHeaderLayout(navView: NavigationView) {
        val headerLayout = navView.getHeaderView(0)
        val nametv = headerLayout.findViewById<TextView?>(R.id.textview_name)
        val emailtv = headerLayout.findViewById<TextView?>(R.id.textview_email)
        val user = FirebaseAuth.getInstance().currentUser
        nametv?.text = user?.displayName
        emailtv?.text = user?.email
        val iv = headerLayout.findViewById<ImageView>(R.id.imageView)
        object: UrlImageDownloader("${user?.photoUrl}?height=100"){
            override fun downloadCompletedCallBack(bitmap: Bitmap?) {
                iv.setImageBitmap(bitmap)

            }
        }

    }

    private fun initiateImageUpload() {
        clickedHalfStoryId = null
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(this);

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        val user = FirebaseAuth.getInstance().currentUser
        if (user==null)
            menuInflater.inflate(R.menu.menu_login, menu)
        else
            menuInflater.inflate(R.menu.menu_logout, menu)

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {

        menu?.clear()
        val user = FirebaseAuth.getInstance().currentUser
        if (user==null)
            menuInflater.inflate(R.menu.menu_login, menu)
        else
            menuInflater.inflate(R.menu.menu_logout, menu)
        return super.onPrepareOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {


        when(item.itemId){
            R.id.action_logout->{
                val auth = FirebaseAuth.getInstance()
                auth.signOut()
                updateHeaderLayout(nav_view)

            }
            R.id.action_login->{
                loginClient.initLogin()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)

        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

            loginClient.onResultReceived(requestCode, resultCode, data)

        if (data!=null && resultCode== Activity.RESULT_OK)
            when(requestCode){
                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE->{

                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.getUri()
                    val intnt = Intent(this, UploadActivity::class.java).apply {
                        if (clickedHalfStoryId!=null){
                            object : ScalingBitmap(resultUri.path, TEMP_SCALED) {
                                override fun scalingCompleted(scaledImage: String) {

                                    putExtra(EXTRA_IMAGE_PATH, scaledImage)

                                }
                            }
                        }
                        else {
//                        putExtra(HALF_STORY_DATA_KEY, clickedHalfStoryId)
                            object : ScalingBitmap(resultUri.path, TEMP_SCALED_HALF) {
                                override fun scalingCompleted(scaledImage: String) {

                                    putExtra(EXTRA_IMAGE_PATH, scaledImage)

                                }
                            }
                        }
                    }
                    startActivity(intnt)

                }

                PermissionsManager.CODE_FINE_LOCATION->{

                    getCurrentLocation()

                }



            }
    }

    override fun onResume() {
        super.onResume()
//        hideOptionFab()

        updateHeaderLayout(nav_view)
    }
//    var isFabOptionsAreVisible = false
//    fun showOptionFab(){
//        val fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
//
//        val rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
//
//
////        fab_drawing.visibility = View.VISIBLE
////        fab_image.visibility = View.VISIBLE
//        fab_drawing.startAnimation(fab_open)
//        fab_image.startAnimation(fab_open)
//        fab.startAnimation(rotate_forward)
//        isFabOptionsAreVisible = true
//    }
//
//    fun hideOptionFab(){
//        val fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
//        val rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);
//
////        fab_drawing.visibility = View.GONE
////        fab_image.visibility = View.GONE
//        fab_drawing.startAnimation(fab_close)
//        fab_image.startAnimation(fab_close)
//        fab.startAnimation(rotate_backward)
//        isFabOptionsAreVisible = false
//    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PermissionsManager.CODE_STORAGE_PERM -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }


            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }
}
