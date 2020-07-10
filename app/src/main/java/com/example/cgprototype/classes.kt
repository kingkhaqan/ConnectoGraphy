package com.example.cgprototype

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Location
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.marginLeft
import androidx.core.view.marginTop
import androidx.recyclerview.widget.RecyclerView
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.internal.GoogleApiAvailabilityCache
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import ja.burhanrashid52.photoeditor.PhotoFilter
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.net.URL
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*


val STORY_TYPE_HALF = "half_story"
val STORY_TYPE_COMPLETED = "completed_story"

val CLUSTER_ICON_WIDTH = 140
val CLUSTER_ICON_HEIGHT = 180
val MARKER_ICON_WIDTH = 100
val MARKER_ICON_HEIGHT = 140

val STORAGE_REF_IMAGES = "images"
val STORAGE_REF_HALF = "half_images"
val STORAGE_REF_COMPLETED = "completed_images"

val DATA_KEY_UID = "uid"

val COLLECTION_PROFILE = "profile"
val COLLECTION_STORY = "stories"

val COLLECTION_HALF = "half_stories"
val COLLECTION_COMPLETED = "completed_stories"

val PROFILE_DATA_NAME = "name"
val PROFILE_DATA_EMAIL = "email"
val PROFILE_DATA_PHOTO_URL = "photoUrl"
val PROFILE_DATA_LATITUDE = "lat"
val PROFILE_DATA_LONGITUDE = "lng"

val STORY_DATA_IMAGE = "imageUrl"
val STORY_DATA_LINER = "oneLiner"
val STORY_DATA_TIME = "timeStamp"
val STORY_DATA_KEY = "storyId"

val HALF_STORY_DATA_KEY = "halfStoryId"
val COMPLETED_STORY_DATA_KEY = "completedStoryId"

val TEMP_SCALED_HALF = "temp_scaled_half"
val TEMP_SCALED = "temp_scaled"

val ROUNDED_BITMAP_DIAMETER = 80
val SELECTED_TOOL_BACKGROUND_COLOR_STR = "#D81B60"
val UNSELECTED_TOOL_BACKGROUND_COLOR_STR = "#646363"

val brushColors = arrayOf(Color.BLUE, Color.YELLOW, Color.RED, Color.GREEN, Color.MAGENTA, Color.DKGRAY, Color.CYAN, Color.LTGRAY, Color.BLACK)


fun getDateString(time: Long): String {
//    val stamp = Timestamp(time)
//    val date = Date(stamp.time)
//    return date

    val sdf = SimpleDateFormat("MM-dd-yyyy")
    val netDate = Date(time)
    return sdf.format(netDate)
}

abstract class LocationClient(val activity: Activity){

    private var fusedLocationClient: FusedLocationProviderClient



    init {


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
    }

    abstract fun isLocationProvidedCallBack(location: LatLng?)
    abstract fun currentLocationCallBack(location: LatLng?)
    fun checkIfLocationIsProvided(uid: String?){

        object : FirestoreDataClient(){
            override fun queryResult(data: MutableMap<String, DocumentSnapshot>) {

                if (data.isEmpty())
                    isLocationProvidedCallBack(null)

                data.forEach {
                    val profileId = it.key
                    val profile = it.value.toObject(Profile::class.java)
                    val location = profile?.getLocation()
                    isLocationProvidedCallBack(location)

                }



            }
        }.query(COLLECTION_PROFILE, DATA_KEY_UID, listOf(uid))

    }


    fun getCurrentLocation(){

//        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->


                if (location!=null) {

                    currentLocationCallBack(LatLng(location.latitude, location.longitude))
                }
                else{
                    currentLocationCallBack(null)
//                    Toast.makeText(requireContext(), "Please Enable location and Pin your location!", Toast.LENGTH_SHORT).show()
                }



            }
            .addOnFailureListener {
                Toast.makeText(activity.applicationContext, it.message, Toast.LENGTH_SHORT).show()
            }

    }




}

abstract class LoginClient(val activity: Activity){


    abstract fun updateUI(user: FirebaseUser?)
     var googleSignInClient: GoogleSignInClient
     var callbackManager: CallbackManager
     var auth: FirebaseAuth
    var alertDialog: AlertDialog? = null
    var progressBar: ProgressBar? = null
    val RC_SIGN_IN = 1001
    val GOOGLE_SIGNIN = 1548
    val FACEBOOK_SIGNIN = 5684

    init {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(activity, gso)

        callbackManager = CallbackManager.Factory.create()
        auth = FirebaseAuth.getInstance()





    }

    fun initLogin(){
        val itemView = LayoutInflater.from(activity).inflate(R.layout.view_signin_layout, null, false)
        val google = itemView.findViewById<SignInButton>(R.id.google)
        val facebook = itemView.findViewById<LoginButton>(R.id.facebook)
        progressBar = itemView.findViewById(R.id.progress_bar)

        google.setOnClickListener {

            updateProgressBar()

            val signInIntent = googleSignInClient.getSignInIntent();
            activity.startActivityForResult(signInIntent, RC_SIGN_IN);
        }



        facebook.setReadPermissions("email", "public_profile")
        facebook.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {

//                Toast.makeText(this@LoginActivity, "something else" , Toast.LENGTH_SHORT).show()
                handleFacebookAccessToken(loginResult.accessToken)





            }

            override fun onCancel() {

            }

            override fun onError(error: FacebookException) {
                Toast.makeText(activity, error.message , Toast.LENGTH_LONG).show()

            }
        })

        val builder = AlertDialog.Builder(activity).apply {
            setView(itemView)
        }

        alertDialog = builder.create()
        alertDialog?.show()
    }

    private fun updateProgressBar() {
        if (progressBar?.visibility==View.GONE)
            progressBar?.visibility = View.VISIBLE
        else
            progressBar?.visibility = View.GONE


    }


    fun onResultReceived(requestCode: Int, resultCode: Int, data: Intent?){

//        Toast.makeText(activity, requestCode.toString()+" "+resultCode, Toast.LENGTH_SHORT).show()

        if (requestCode == RC_SIGN_IN) {

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {

                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {

                Toast.makeText(activity.applicationContext, e.toString(), Toast.LENGTH_SHORT).show()

            }
        }
        else
            callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {


        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {

                    val user = auth.currentUser
                    loginResult(user)

                } else {



                    loginResult(null)

                }


            }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
//        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
//                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser

                    loginResult(user)

                } else {
                    // If sign in fails, display a message to the user.
//                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(activity.baseContext, token.dataAccessExpirationTime.toString(),
                        Toast.LENGTH_SHORT).show()
                    loginResult(null)
                }

                // ...
            }
    }

    private fun loginResult(user: FirebaseUser?) {
        if (user!=null)
            alertDialog?.dismiss()
        else
            progressBar?.visibility = View.GONE
        updateUI(user)

        if(user!=null)
        object : FirestoreDataClient(){
            override fun queryResult(data: MutableMap<String, DocumentSnapshot>) {

                if (data.isEmpty()){

                    val profile = Profile(user.uid, user.displayName, user.email, user.photoUrl.toString())
                    FirebaseFirestore.getInstance().collection(COLLECTION_PROFILE).document(user.uid)
                        .set(profile)

                }



            }
        }.query(COLLECTION_PROFILE, DATA_KEY_UID, listOf(user.uid))
    }

}


class ShortStoryViewHolder(v: View): RecyclerView.ViewHolder(v){
    var imageView: ImageView
    var bitmap: Bitmap? = null
    init {
        imageView = v.findViewById(R.id.imageview)
    }

    fun bind(bitmap: Bitmap?){
        this.bitmap = bitmap
        imageView.setImageBitmap(bitmap)
    }
}

class PhotoFilterViewHolder(val v: View): RecyclerView.ViewHolder(v){
    var imageView: ImageView
    var textView: TextView
    init {
        imageView = v.findViewById(R.id.imageview)
        textView = v.findViewById(R.id.textview)
    }
    fun bind(filter: FilterItem){
        imageView.setImageResource(filter.imageResId)
        textView.text = filter.filterName
    }
}
data class FilterItem(var imageResId: Int, var filter: PhotoFilter? = null, var filterName: String? = null)

fun getRoundedColorBitmap(context: Context, color: Int): RoundedBitmapDrawable {

    return RoundedBitmapDrawableFactory.create(context.resources, Bitmap.createBitmap(
        ROUNDED_BITMAP_DIAMETER, ROUNDED_BITMAP_DIAMETER, Bitmap.Config.ARGB_8888).also {
        Canvas(it).apply {
            drawColor(color)
        }
    }).apply {
        cornerRadius = 50.0f
        setAntiAlias(true)
    }
}

abstract class ScalingBitmap(val filePath: String?, val action: String){
    abstract fun scalingCompleted(scaledImage: String)

    init {
        val bitmap = BitmapFactory.decodeFile(filePath)

        if (bitmap!=null) {
            val tempFile = File.createTempFile(System.currentTimeMillis().toString(), ".png")
            val fos = FileOutputStream(tempFile)
            val scaledBitmap = getScaledBitmap(bitmap)
            when (action) {
                TEMP_SCALED -> {
                    scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)

                    scalingCompleted(tempFile.path)
                    fos.flush()
                }
                TEMP_SCALED_HALF ->  {
                    val halfBitmap = getHalfBitmap(scaledBitmap)
                    halfBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    scalingCompleted(tempFile.path)
                    fos.flush()
                }
            }

        }
    }
}

fun getScaledBitmap(bitmap: Bitmap) = Bitmap.createScaledBitmap(bitmap, BITMAP_IMAGE_WIDTH, BITMAP_IMAGE_HEIGHT, true)
fun getHalfBitmap(bitmap: Bitmap) = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width/2, bitmap.height)

fun sizedImageUrl(url: String, height: Int): String{
    return "$url?height=$height"
}

class PermissionsManager(val activity: Activity){
    companion object{
        val PERM_READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
        val PERM_WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
        val PERM_EXCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
        val CODE_READ_STORAGE_PERM = 1001
        val CODE_WRITE_STORAGE_PERM = 1002
        val CODE_EXCESS_STORAGE_PERM = 1003
        val CODE_STORAGE_PERM = 1004
        val CODE_FINE_LOCATION = 1593

    }

    fun isPermissionGranted(permission: String) = (ContextCompat.checkSelfPermission(activity.applicationContext, permission) == PackageManager.PERMISSION_GRANTED)

    fun askForPermissions(permissions: Array<String>, requestCode: Int){
        ActivityCompat.requestPermissions(activity,
            permissions,
            requestCode)
    }
}

abstract class GetHalfImage(context: Context, imagePath: String?){
    abstract fun halfImageCallBack(uri: String?)

    init {
        if (imagePath!=null) {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, Uri.fromFile(File(imagePath)));
            val halfbmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width / 2, bitmap.height)

            val file =   File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "half_image_" + System.currentTimeMillis() + ".png");

            val fos = FileOutputStream(file)
            halfbmp.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()

            halfImageCallBack(file.absolutePath)
        }

    }
}

abstract class FirestoreDataClient{
    abstract fun queryResult(data: MutableMap<String, DocumentSnapshot>)

    fun queryAll(collection: String){
        val db = FirebaseFirestore.getInstance()

        db.collection(collection)
            .get()
            .addOnSuccessListener {
                val data = mutableMapOf<String, Map<String, Any>?>()
                val temp = mutableMapOf<String, DocumentSnapshot>()
                it.documents.forEach {doc->
                    temp[doc.id] = doc
                    data[doc.id] = doc.data
                }
                queryResult(temp)
            }
    }
    fun query(collection: String, where: String, equals: List<Any?>){

        val db = FirebaseFirestore.getInstance()
        db.collection(collection)
            .whereIn(where, equals)
            .get()
            .addOnSuccessListener {


                val data = mutableMapOf<String, Map<String, Any>?>()
                val temp = mutableMapOf<String, DocumentSnapshot>()
                it.documents.forEach {doc->
                    temp[doc.id] = doc
                    val key = doc.id
                    data[key] = doc.data

                }
                queryResult(temp)
            }
    }
}


abstract class UrlImageDownloader(url: String?){
    abstract fun downloadCompletedCallBack(bitmap: Bitmap?)

    init {
        startDownload(url)
    }

    fun startDownload(url: String?){
        object: AsyncTask<String, Unit, Bitmap?>(){
            override fun doInBackground(vararg params: String?): Bitmap? {
                val urlString = params[0]
                var bitmap: Bitmap? = null
                if (urlString!=null){
                    val photoUrl = urlString
                    try {
                        bitmap = BitmapFactory.decodeStream(URL(photoUrl).openConnection().getInputStream())

                    }
                    catch (e: Exception){}

                }
                return bitmap
            }

            override fun onPostExecute(result: Bitmap?) {
                super.onPostExecute(result)
                downloadCompletedCallBack(result)
//         if (result!=null)
//             imageView.setImageBitmap(result)
            }
        }.execute(url)
    }
}



abstract class ImageUploader(val context: Context){

//    abstract fun uploadCompletedCallBack()
    abstract fun imageUploadCompleted(imageUrl: String)
    abstract fun storyUploadCompleted(storyId: String, type: String)
//    abstract fun halfStoryUploadCompleted(halfStoryId: String)
//    abstract fun completeStoryUploadCompleted(completedStoryId: String )
//    abstract fun storyAddedToCollection(halfStoryId: String)

    fun uploadImageToFirebase(
        storageRef: StorageReference,
        imagePath: String?
    ) {

        storageRef.putFile(Uri.fromFile(File(imagePath)))
            .addOnSuccessListener {


            }
            .addOnCompleteListener {task->
                storageRef.downloadUrl.addOnSuccessListener {
                    imageUploadCompleted(it.toString())
                }

            }




    }

    fun uploadHalfStory(
        oneLiner: String?,
        imageUrl: String?
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()
        val halfStory = HalfStory(currentUser?.uid, imageUrl, oneLiner, System.currentTimeMillis())

//        val s = mapOf(
//            DATA_KEY_UID to currentUser?.uid,
//            STORY_DATA_IMAGE to imageUrl,
//            STORY_DATA_LINER to oneLiner,
//            STORY_DATA_TIME to System.currentTimeMillis()
//        )

        db.collection(COLLECTION_HALF)
            .add(halfStory)

            .addOnSuccessListener {

                storyUploadCompleted(it.id, STORAGE_REF_HALF)
            }
    }

    fun uploadCompletedStory(
        oneLiner: String?,
        imageUrl: String?,
        halfStoryId: String?
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()
        val completedStory = CompletedStory(currentUser?.uid, imageUrl, oneLiner, System.currentTimeMillis(), halfStoryId)
//        val s = mapOf(
//            DATA_KEY_UID to currentUser?.uid,
//            STORY_DATA_IMAGE to imageUrl,
//            STORY_DATA_LINER to oneLiner,
//            STORY_DATA_TIME to System.currentTimeMillis(),
//            HALF_STORY_DATA_KEY to halfStoryId
//        )

        db.collection(COLLECTION_COMPLETED)
            .add(completedStory)

            .addOnSuccessListener {

                storyUploadCompleted(it.id, STORAGE_REF_COMPLETED)
            }
    }



}

fun getStorageRef(): StorageReference {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val timeStamp = System.currentTimeMillis()
    return FirebaseStorage.getInstance().reference.child(STORAGE_REF_IMAGES)
        .child(currentUser?.uid!!).child("$timeStamp.png")
}


open class Story(val uid: String? = null, val imageUrl: String? = null, val oneLiner: String? = null, val timeStamp: Long? = null)
class HalfStory( UID: String? = null,  imageUrl: String? = null,  oneLiner: String? = null,  timeStamp: Long? = null): Story(UID, imageUrl, oneLiner, timeStamp)
class CompletedStory( UID: String? = null,  imageUrl: String? = null,  oneLiner: String? = null,  timeStamp: Long? = null, val halfStoryId: String? = null): Story(UID, imageUrl, oneLiner, timeStamp)

data class Profile(val uid: String? = null, val name: String? = null, val email: String? = null, val photoUrl: String? = null, val lat: Double? = null, val lng: Double? = null){

    fun getLocation(): LatLng? {

        if (lat!=null && lng!=null)
            return LatLng(lat, lng)
        return null
    }
}



@RequiresApi(Build.VERSION_CODES.N)

val polyLineColors = arrayListOf(Color.BLUE, Color.CYAN, Color.GRAY, Color.GREEN, Color.MAGENTA, Color.RED, Color.YELLOW)

class MyFileProvider: FileProvider()

 fun joinBitmaps(bitmap1: Bitmap, bitmap2: Bitmap): Bitmap {
    val mBitmapPaint = Paint(Paint.DITHER_FLAG)
    val width = bitmap1.width.plus(bitmap2.width)
    val height = bitmap1.height
    val newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(newBitmap)
    canvas.drawBitmap(bitmap1, 0f, 0f, mBitmapPaint)
    canvas.drawBitmap(bitmap2, width / 2f, 0f, mBitmapPaint)
    return newBitmap
}

class MyClusterItem(var mposition: LatLng, var mtitle: String, var msnippest: String, var imageUrl: String? = null): ClusterItem {
    override fun getSnippet() = msnippest
    override fun getTitle() = mtitle
    override fun getPosition() = mposition
}

fun getLaftHalf(bitmap: Bitmap): Bitmap? {

    return Bitmap.createBitmap(bitmap, 0,0, bitmap.width/2, bitmap.height)
}
fun getRightHalf(bitmap: Bitmap): Bitmap? {
    return Bitmap.createBitmap(bitmap, 0,0, bitmap.width/2, bitmap.height)
}

//fun getItems(googleMap: GoogleMap): MutableList<MyClusterItem> {
//
//
//    var items = mutableListOf<MyClusterItem>()
//    val sydney = LatLng(-34.0, 151.0)
//    val otherLoc = LatLng(-35.0, 151.0)
//
//    val locations = listOf(
//        LatLng	(   24.8607998, 67.0103989)
//        ,LatLng	(	31.5580006, 74.350708)
//        ,LatLng	(	31.4155407, 73.0896912)
//        ,LatLng	(	33.6007004, 73.0679016)
//        ,LatLng	(	30.1967907, 71.478241)
//        ,LatLng	(	25.3924198, 68.3736572)
//        ,LatLng	(	32.1556702, 74.1870499)
//        ,LatLng	(	34.0079994, 71.5784912)
//        ,LatLng	(	30.1841393, 67.0014114)
//        ,LatLng	(	34.3700218, 73.4708176)
//    )
//
////    val location0 =  LatLng	(   24.8607998, 67.0103989)
////    val location1 =  LatLng	(	31.5580006, 74.350708)
////    val location2 =  LatLng	(	31.4155407, 73.0896912)
////    val location3 =  LatLng	(	33.6007004, 73.0679016)
////    val location4 =  LatLng	(	30.1967907, 71.478241)
////    val location5 =  LatLng	(	25.3924198, 68.3736572)
////    val location6 =  LatLng	(	32.1556702, 74.1870499)
////    val location7 =  LatLng	(	34.0079994, 71.5784912)
////    val location8 =  LatLng	(	30.1841393, 67.0014114)
////    val location9 =  LatLng	(	34.3700218, 73.4708176)
////    val location1 =     LatLng	(	34.6771889, 73.0232925)
////    val location1 =       LatLng	(	33.5183601, 73.9021988)
////    val location1 =    LatLng	(	33.7214813, 73.0432892)
////    val location1 =     LatLng	(	29.3977909, 71.6752014)
////    val location1 =      LatLng	(	32.0858612, 72.6741791)
////    val location1 =     LatLng	(	32.4926796, 74.5313416)
////    val location1 =     LatLng	(	27.7032299, 68.8588867)
//
////        for(i in 1..10){
////            items.add(MyClusterItem(LatLng(-34.0+1, 151.0+i), "location "+i, "snippest "+i))
////        }
//
//
//    for (i in 0..locations.size-1){
//        items.add(MyClusterItem(locations[i], "Location-$i", "Link-$i"))
//    }
//
//
////    items.add(MyClusterItem(sydney, "Sydney", "https://java.sogeti.nl/JavaBlog/wp-content/uploads/2009/04/android_icon_256.png"))
////    items.add(MyClusterItem(otherLoc, "other location", "other snippest"))
//
//
//    var polyline1 = googleMap.addPolyline(
//        PolylineOptions()
//            .clickable(true)
//            .add(locations[0], locations[6])
//            .color(polyLineColors.shuffled().first())
//    )
//    var polyline2 = googleMap.addPolyline(
//        PolylineOptions()
//            .clickable(true)
//            .add(locations[4], locations[3])
//            .color(polyLineColors.shuffled().first())
//    )
//    var polyline3 = googleMap.addPolyline(
//        PolylineOptions()
//            .clickable(true)
//            .add(locations[2], locations[9])
//            .color(polyLineColors.shuffled().first())
//    )
//    var polyline4 = googleMap.addPolyline(
//        PolylineOptions()
//            .clickable(true)
//            .add(locations[6], locations[1])
//            .color(polyLineColors.shuffled().first())
//    )
//    var polyline5 = googleMap.addPolyline(
//        PolylineOptions()
//            .clickable(true)
//            .add(locations[1], locations[4])
//            .color(polyLineColors.shuffled().first())
//    )
//    var polyline6 = googleMap.addPolyline(
//        PolylineOptions()
//            .clickable(true)
//            .add(locations[8], locations[5])
//            .color(polyLineColors.shuffled().first())
//    )
//
//
//    return items
//}

private fun createClusterIcon(
    person1: Bitmap,
    person2: Bitmap,
    markerOptions: MarkerOptions?,
    size: String?,
    context: Context
) {

    val width = CLUSTER_ICON_WIDTH
    val height = CLUSTER_ICON_HEIGHT
    var scaledBmp1 = Bitmap.createScaledBitmap(person1, width/2, height, false)
    var scaledBmp2 = Bitmap.createScaledBitmap(person2, width/2, height, false)

    // crop half images
    var leftHalf = Bitmap.createBitmap(scaledBmp1, scaledBmp1.width/2,0, scaledBmp1.width/2, scaledBmp1.height)
    var rightHalf = Bitmap.createBitmap(scaledBmp2, 0,0, scaledBmp2.width/2, scaledBmp2.height)

    var newBmp = Bitmap.createBitmap(width, height, person1.config)
    var canvas = Canvas(newBmp)


    canvas.drawBitmap(scaledBmp1, 0f, 0f, null)
    canvas.drawBitmap(scaledBmp2, width/2f, 0f, null)

    var iconGenerator = IconGenerator(context.applicationContext)
    iconGenerator.setBackground(newBmp.toDrawable(context.resources))

    val iconview = LayoutInflater.from(context).inflate(R.layout.cluster_icon, null, false)
    iconview.findViewById<ImageView>(R.id.image).setImageBitmap(newBmp)
    iconview.findViewById<TextView>(R.id.text).text = size
    iconGenerator.setContentView(iconview)

//    var icon = iconGenerator.makeIcon(size)


    val icon = iconGenerator.makeIcon()


    markerOptions?.icon(BitmapDescriptorFactory.fromBitmap(icon))


}

class CustomClusterRenderer(val context: Context, val map: GoogleMap, manager: ClusterManager<MyClusterItem>): DefaultClusterRenderer<MyClusterItem>(context, map, manager  ) {
    override fun onBeforeClusterItemRendered(
        item: MyClusterItem?,
        markerOptions: MarkerOptions?
    ) {
        super.onBeforeClusterItemRendered(item, markerOptions)



        val photoUrl = item?.imageUrl
        if (photoUrl!=null){
            object : UrlImageDownloader(photoUrl){
                override fun downloadCompletedCallBack(bitmap: Bitmap?) {
                    markerOptions?.icon(BitmapDescriptorFactory.fromBitmap(bitmap))

                }
            }

        }




//
//        when (item?.mtitle) {
//            "Location-0" -> iconGenerator.setBackground(
//                ContextCompat.getDrawable(
//                    context,
//                    R.drawable.person0
//                )
//            )
//            "Location-1" -> iconGenerator.setBackground(
//                ContextCompat.getDrawable(
//                    context,
//                    R.drawable.person1
//                )
//            )
//            "Location-2" -> iconGenerator.setBackground(
//                ContextCompat.getDrawable(
//                    context,
//                    R.drawable.person2
//                )
//            )
//            "Location-3" -> iconGenerator.setBackground(
//                ContextCompat.getDrawable(
//                    context,
//                    R.drawable.person3
//                )
//            )
//            "Location-4" -> iconGenerator.setBackground(
//                ContextCompat.getDrawable(
//                    context,
//                    R.drawable.person4
//                )
//            )
//            "Location-5" -> iconGenerator.setBackground(
//                ContextCompat.getDrawable(
//                    context,
//                    R.drawable.person5
//                )
//            )
//            "Location-6" -> iconGenerator.setBackground(
//                ContextCompat.getDrawable(
//                    context,
//                    R.drawable.person6
//                )
//            )
//            "Location-7" -> iconGenerator.setBackground(
//                ContextCompat.getDrawable(
//                    context,
//                    R.drawable.person7
//                )
//            )
//            "Location-8" -> iconGenerator.setBackground(
//                ContextCompat.getDrawable(
//                    context,
//                    R.drawable.person8
//                )
//            )
//            "Location-9" -> iconGenerator.setBackground(
//                ContextCompat.getDrawable(
//                    context,
//                    R.drawable.person9
//                )
//            )
//
//
//        }
//        var iconGenerator = IconGenerator(context.applicationContext)
//        iconGenerator.setBackground(ContextCompat.getDrawable(context, R.drawable.color_selector_tool_bg))
//        var icon = iconGenerator.makeIcon()
//        var scaledIcon = Bitmap.createScaledBitmap(icon, MARKER_ICON_WIDTH, MARKER_ICON_HEIGHT, false)
//        markerOptions?.icon(BitmapDescriptorFactory.fromBitmap(icon))

//        Toast.makeText(context, item?.snippet, Toast.LENGTH_SHORT).show()

//        val url = "https://lh3.googleusercontent.com/a-/AAuE7mDWT7d3Kc-8HVYcZ8kaYNjjzdUgM14VJWMyyO0xHA=s96-c"
//
//            LoadIconFromUrlTask(markerOptions, context).execute(url)

    }


    override fun onBeforeClusterRendered(
        cluster: Cluster<MyClusterItem>?,
        markerOptions: MarkerOptions?
    ) {
        super.onBeforeClusterRendered(cluster, markerOptions)
//            var iconGenerator = IconGenerator(context.applicationContext)
//            iconGenerator.setBackground(ContextCompat.getDrawable(context, R.drawable.person2))
//            var icon = iconGenerator.makeIcon(cluster?.size?.toString())
//            var scaledIcon = Bitmap.createScaledBitmap(icon, 100, 120, false)


//        val iconGenerator = IconGenerator(context)
//        val clustorIconView = LayoutInflater.from(context).inflate(R.layout.view_cluster_icon, null, false)
//        val tvIV = clustorIconView.findViewById<TextView>(R.id.textview)
//        tvIV.text = cluster?.size?.toString()
//
//        iconGenerator.setContentView(clustorIconView)
//        markerOptions?.icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))

//        var person1 = BitmapFactory.decodeResource(context.resources, R.drawable.person1)
//        var person2 = BitmapFactory.decodeResource(context.resources, R.drawable.person2)
//
//
//
//        createClusterIcon(person1, person2, markerOptions, cluster?.size?.toString(), context)


    }
}

class LoadIconFromUrlTask(val markerOptions: MarkerOptions?, val context: Context): AsyncTask<String?, Unit, Bitmap?>() {
    override fun doInBackground(vararg params: String?): Bitmap? {
        val urlString = params[0]
//            Toast.makeText(context, urlString, Toast.LENGTH_SHORT).show()
        var bitmap: Bitmap? = null
        if (urlString!=null){
            val photoUrl = urlString
            try {
                bitmap = BitmapFactory.decodeStream(URL(photoUrl).openConnection().getInputStream())

            }
            catch (e: Exception){}

        }
        return bitmap
    }

    override fun onPostExecute(result: Bitmap?) {
        super.onPostExecute(result)

        if (result!=null) {
            markerOptions?.icon(BitmapDescriptorFactory.fromBitmap(result))
//                val iv = ImageView(context).apply {
//                    setImageBitmap(result)
//                }
//                AlertDialog.Builder(context).apply {
//                    setView(iv)
//                    show()
//                }
//                val iconGenerator = IconGenerator(context)

//                Toast.makeText(context, markerOptions?.snippet, Toast.LENGTH_SHORT).show()
//                iconGenerator.setBackground(result.toDrawable(context.resources))
//                markerOptions?.icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
        }
    }
}

class CustomInfoWindowAadapter(private val context: Context): GoogleMap.InfoWindowAdapter{
    override fun getInfoContents(p0: Marker?): View? {


        if (p0==null || p0.title==null)
            return null

        val inflater = LayoutInflater.from(context)
        val itemView = inflater.inflate(R.layout.marker_info_window, null, false)

        var anushka = BitmapFactory.decodeResource(context.resources, R.drawable.anushka)
        val url = p0.snippet

        val iv = itemView.findViewById<ImageView>(R.id.image)
        val tv = itemView.findViewById<TextView>(R.id.title)


        tv.text = p0.title



        return itemView
    }



    override fun getInfoWindow(p0: Marker?) = null
}

class MyDraw(context: Context, attrs: AttributeSet) : View(context, attrs) {

    val imageView = ImageView(context)

    private var paths = mutableListOf<Path>()
    private var undoPaths = mutableListOf<Path>()

    private var thumbBitmap: Bitmap? = null
    private var thumbCanvas: Canvas? = null
    private var oBitmap: Bitmap? = null

    private var mWidth = 0
    private var mHeight = 0

    private var thumbWidth = 0
    private var thumbHeight = 0

    private var mBitmap: Bitmap? = null
    private var mCanvas: Canvas? = null
    private val mBitmapPaint = Paint(Paint.DITHER_FLAG)

    private var mPaint = Paint()
    private var mPath = Path()

    private var mCurX = 0f
    private var mCurY = 0f
    private var mStartX = 0f
    private var mStartY = 0f

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


    fun setBrushSize(size: Float) {
        mPaint.strokeWidth = size
    }

    fun setBrushColor(color: Int) {
        mPaint.color = color
    }

    fun init(metrix: DisplayMetrics, bitmap: Bitmap? = null) {

        oBitmap = bitmap
        mWidth = metrix.widthPixels
        mHeight = metrix.heightPixels


        redrawCanvas()

        if (oBitmap!=null)
            createThumb(metrix)





        redrawCanvas()

    }

    private fun redrawCanvas() {

        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap!!)
    }

    private fun createThumb(metrix: DisplayMetrics) {


        thumbWidth = metrix.widthPixels / 2
        thumbHeight = metrix.heightPixels / 6


        val scaledLeftHalf =
            Bitmap.createScaledBitmap(oBitmap!!, thumbWidth / 2, thumbHeight, false)
        val scaledRightHalf =
            Bitmap.createScaledBitmap(mBitmap!!, thumbWidth / 2, thumbHeight, false)

        thumbBitmap = joinBitmaps(scaledLeftHalf, scaledRightHalf)
        thumbCanvas = Canvas(thumbBitmap!!)

//        thumbCanvas!!.drawPaint(Paint().apply {
//            setBackgroundColor(Color.WHITE)
//        })

//        thumbBitmap = Bitmap.createBitmap(thumbWidth, thumbHeight, Bitmap.Config.ARGB_8888)
//        thumbCanvas = Canvas(thumbBitmap!!)
//        thumbCanvas?.drawBitmap(scaledLeftHalf, 0f, 0f, mBitmapPaint)
//        thumbCanvas?.drawBitmap(scaledRightHalf, thumbWidth/2f, 0f, mBitmapPaint)

        createImageView()


    }

    private fun joinBitmaps(bitmap1: Bitmap, bitmap2: Bitmap): Bitmap {
        val width = bitmap1.width.plus(bitmap2.width)
        val height = bitmap1.height
        val newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(newBitmap)
        canvas.drawBitmap(bitmap1, 0f, 0f, mBitmapPaint)
        canvas.drawBitmap(bitmap2, width / 2f, 0f, mBitmapPaint)
        return newBitmap
    }

    private fun createImageView() {


        val layoutParams = RelativeLayout.LayoutParams(thumbWidth, thumbHeight)
        layoutParams.leftMargin = 50
        layoutParams.topMargin = 50

        imageView.setImageBitmap(thumbBitmap)
        imageView.layoutParams = layoutParams
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



        super.getRootView().findViewById<ViewGroup>(R.id.root).addView(imageView)

    }

    fun getBitmap(): Bitmap? {
        mCanvas?.save()
        val arr = IntArray(2)
        imageView.getLocationOnScreen(arr)
        mCanvas?.drawBitmap(thumbBitmap!!, arr[0].toFloat(), arr[1].toFloat(), mBitmapPaint)

        if (oBitmap==null)
            return mBitmap
        val scaledBitmap = Bitmap.createScaledBitmap(mBitmap!!, oBitmap!!.width, oBitmap!!.height, false)
        return joinBitmaps(oBitmap!!, scaledBitmap)
    }

    fun getOrignalBitmap() = oBitmap

    fun undo(){
        undoPaths.add(paths.removeAt(paths.lastIndex))


        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

//        paths.forEach {
//            mCanvas?.drawPath(it, mPaint)
//        }
        mCanvas?.drawPath(mPath, mPaint)


        canvas.drawBitmap(mBitmap!!, 0f, 0f, mBitmapPaint)

        if (oBitmap!=null) {

            val scaledRightHalf =
                Bitmap.createScaledBitmap(mBitmap!!, thumbWidth / 2, thumbHeight, false)

            thumbCanvas?.drawBitmap(scaledRightHalf, thumbWidth / 2f, 0f, mBitmapPaint)
        }

    }


    private fun actionDown(x: Float, y: Float) {
        mPath.moveTo(x, y)
        mCurX = x
        mCurY = y

        paths.add(mPath)
    }

    private fun actionMove(x: Float, y: Float) {
        mPath.quadTo(mCurX, mCurY, (x + mCurX) / 2, (y + mCurY) / 2)
        mCurX = x
        mCurY = y

        paths.add(mPath)
    }

    private fun actionUp() {
        mPath.lineTo(mCurX, mCurY)

        // draw a dot on click
        if (mStartX == mCurX && mStartY == mCurY) {
            mPath.lineTo(mCurX, mCurY + 2)
            mPath.lineTo(mCurX + 1, mCurY + 2)
            mPath.lineTo(mCurX + 1, mCurY)
        }

        paths.add(mPath)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mStartX = x
                mStartY = y
                actionDown(x, y)
            }
            MotionEvent.ACTION_MOVE -> actionMove(x, y)
            MotionEvent.ACTION_UP -> actionUp()
        }
        invalidate()
        return true
    }


}


class ImageCard(var image: Bitmap?, var title: String? = "", var subtitle: String?="", var extra: String?="")
class ImageCardVH( v: View): RecyclerView.ViewHolder(v){
    var imageview: ImageView = v.findViewById<ImageView>(R.id.imageview)
    var tv_title = v.findViewById<TextView>(R.id.textview_title)
    var tv_subtitle = v.findViewById<TextView>(R.id.textview_subtitle)
    var tv_extra = v.findViewById<TextView>(R.id.textview_extra)

    fun bind(card: ImageCard){
        imageview.setImageBitmap(card.image)
        tv_title.text = card.title
        tv_subtitle.text = card.subtitle
        tv_extra.text = card.extra
    }
}
abstract class RecyclerViewAdapter(val context: Context, val dataset: MutableList<ImageCard>): RecyclerView.Adapter<ImageCardVH>() {
    abstract fun onClickCallBack()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageCardVH {
        return ImageCardVH(LayoutInflater.from(context).inflate(R.layout.image_card, parent, false))
    }
    override fun getItemCount() = dataset.size
    override fun onBindViewHolder(holder: ImageCardVH, position: Int) {
        holder.bind(dataset[position])
        holder.itemView.setOnClickListener { onClickCallBack() }
    }
}