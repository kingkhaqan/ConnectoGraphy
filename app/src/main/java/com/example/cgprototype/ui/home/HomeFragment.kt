package com.example.cgprototype.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cgprototype.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.ByteArrayOutputStream

class HomeFragment : Fragment(), OnMapReadyCallback {



    private  var halfStoriesIndexing: MutableMap<String, MutableSet<String>?> = mutableMapOf()
    private  var completedStoriesIndexing: MutableMap<String, MutableSet<String>?> = mutableMapOf()
    private  var profilesData: MutableMap<String, Profile?> = mutableMapOf()
    private  var  halfStoriesData: MutableMap<String, HalfStory?> = mutableMapOf()
    private  var  completedStoriesData: MutableMap<String, CompletedStory?> = mutableMapOf()


    private  var uids: MutableSet<String> = mutableSetOf()
    private  var locations: MutableMap<String, LatLng> = mutableMapOf()
    private lateinit var clusterItems: MutableList<MyClusterItem>

    private lateinit var homeViewModel: HomeViewModel

    private lateinit var map: GoogleMap
    private lateinit var mClusterManager: ClusterManager<MyClusterItem>
    private lateinit var mClusterRenderer: DefaultClusterRenderer<MyClusterItem>
    override fun onMapReady(p0: GoogleMap) {
        map = p0
        setUpCluster()


        map.animateCamera(CameraUpdateFactory.newLatLngZoom(PAKISTAN_LOCATION, PAKISTAN_lOCATION_ZOOM_LEVEL))

        map.mapType = SELECTED_MAP_TYPE
    }

    private fun setUpCluster() {



        mClusterManager = ClusterManager(requireContext(), map)
//        mClusterRenderer = CustomClusterRenderer(requireContext(), map, mClusterManager)
        mClusterRenderer = DefaultClusterRenderer(requireContext(), map, mClusterManager)
        mClusterRenderer.minClusterSize = 1

        map.setInfoWindowAdapter(CustomInfoWindowAadapter(requireContext()))
//        map.setInfoWindowAdapter()
        map.setOnCameraIdleListener(mClusterManager)
        mClusterManager.renderer = mClusterRenderer


//-------------------------------------------------------
        uids = mutableSetOf()
        locations = mutableMapOf()
        clusterItems = mutableListOf()


        saturateCluster()

        map.setOnMarkerClickListener {


            val currentZoom = map.cameraPosition.zoom
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(it.position, currentZoom+ZOOM_ON_CLUSTER_TOUCH))

            val uid = it.snippet

            if (uid!=null)
                popupStoriesList(uid)

            true
        }






    }

    private fun popupStoriesList(
        uid: String
    ) {

        val halfStoryIds = halfStoriesIndexing[uid]
        val completedStoryIds = completedStoriesIndexing[uid]
        var mutableStoryIds = mutableListOf<String>()
        var storyType = ""
        var halfStoriesCount = 0
        if (halfStoryIds!=null) {
            halfStoriesCount += halfStoryIds.size
            mutableStoryIds.addAll(halfStoryIds)
        }
        if (completedStoryIds!=null)
            mutableStoryIds.addAll(completedStoryIds)

        val storyIds = mutableStoryIds.toTypedArray()
        val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.view_stories_popup, null, false)
        val recyclerview = itemView.findViewById<RecyclerView>(R.id.recyclerview)
        val imageDown = itemView.findViewById<ImageView>(R.id.imageview_down)
        recyclerview.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerview.adapter = object : RecyclerView.Adapter<ShortStoryViewHolder>(){
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ) = ShortStoryViewHolder(LayoutInflater.from(requireContext()).inflate(R.layout.view_item_short_story, parent, false))

            override fun getItemCount() = if (storyIds==null) 0 else storyIds.size

            override fun onBindViewHolder(holder: ShortStoryViewHolder, position: Int) {
                val storyId = storyIds[position]
                val storyType = if (position<halfStoriesCount) STORY_TYPE_HALF else STORY_TYPE_COMPLETED
//                val url = if (storyType== STORY_TYPE_HALF) halfStoriesData[storyId]?.imageUrl else completedStoriesData[storyId]?.imageUrl
                val url = if (storyType== STORY_TYPE_HALF) halfStoriesData[storyId]?.imageUrl else completedStoriesData[storyId]?.imageUrl
//                Toast.makeText(requireContext(), url, Toast.LENGTH_SHORT).show()

                val profile = profilesData[uid]
                itemView.findViewById<TextView>(R.id.textview_one).text = profile?.name
                itemView.findViewById<TextView>(R.id.textview_two).text = "Half stories: $halfStoriesCount, Total stories: ${mutableStoryIds.size}"
                itemView.findViewById<TextView>(R.id.textview_three).text = profile?.getLocation()?.toString()


                holder.itemView.setOnClickListener {
//                    showStoryAlertDialogue(storyId, storyType)


                    if (storyType== STORY_TYPE_HALF){

                            HomeActivity.clickedHalfStoryId = storyId

                            val bos = ByteArrayOutputStream()
                            holder.bitmap?.compress(Bitmap.CompressFormat.PNG, 100, bos)
                            val bmpData = bos.toByteArray()
                            startActivity(Intent(requireContext(), EditingActivity::class.java).apply {

                                putExtra(EXTRA_IMAGE_BYTE_ARRAY, bmpData)
                            })
                    }
                    else
                        showStoryAlertDialogue(storyId, storyType, holder.bitmap)


                }
                object : UrlImageDownloader(url){
                    override fun downloadCompletedCallBack(bitmap: Bitmap?) {
                        holder.bind(bitmap)

                    }
                }

            }
        }



        val popUp = PopupWindow(itemView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true).apply {
            showAtLocation(this.contentView, Gravity.BOTTOM, 0, 0)
            update(0, 0, this.contentView.width-10, this.contentView.height-10)

        }

        imageDown.setOnClickListener {
            Toast.makeText(requireContext(), "must be down", Toast.LENGTH_SHORT).show()

            popUp.dismiss()
        }


    }

    private fun showStoryAlertDialogue(
        storyId: String?,
        storyType: String,
        bitmap: Bitmap?
    ) {

        var url: String? = null
        var oneliner: String?  = null
        var initialName: String? = null
        var completedName: String? = null
        var fromDate: Long? = null
        var toDate: Long? = null
        var completedOneLiner: String? = null
        var fromLocation: LatLng? = null
        var toLocation: LatLng? = null
        when(storyType){
            STORY_TYPE_HALF-> {
                url = halfStoriesData[storyId]?.imageUrl
                oneliner = halfStoriesData[storyId]?.oneLiner
            }
            STORY_TYPE_COMPLETED-> {
                url = completedStoriesData[storyId]?.imageUrl

                val completedStory = completedStoriesData[storyId]
                val halfStory = halfStoriesData[completedStory?.halfStoryId]
                val firstUid = halfStory?.uid
                val secondUid = completedStory?.uid
                 initialName = profilesData[firstUid]?.name
                 completedName = profilesData[secondUid]?.name
                 fromDate = halfStory?.timeStamp
                 toDate = completedStory?.timeStamp
                oneliner = halfStory?.oneLiner
                completedOneLiner = completedStoriesData[storyId]?.oneLiner

                fromLocation = profilesData[firstUid]?.getLocation()
                toLocation = profilesData[secondUid]?.getLocation()

            }
        }

        val storyPrivew = LayoutInflater.from(requireContext()).inflate(R.layout.view_story_preview, null, false)
        storyPrivew?.findViewById<TextView>(R.id.textview_oneliner)?.apply {
            text = oneliner
        }
        storyPrivew?.findViewById<TextView>(R.id.textview_one)?.apply {
            text = "$initialName"
        }
        storyPrivew?.findViewById<TextView>(R.id.textview_two)?.apply {
            if (fromDate!=null)
                text = "Created on: ${getDateString(fromDate)}"
        }
        storyPrivew?.findViewById<TextView>(R.id.textview_three)?.apply {
            text = "Completed by: $completedName"
        }
        storyPrivew?.findViewById<TextView>(R.id.textview_four)?.apply {
            text = completedOneLiner
        }
        storyPrivew?.findViewById<TextView>(R.id.textview_five)?.apply {
            if (toDate!=null)
                text = "On Date: ${getDateString(toDate)}"
        }
        storyPrivew?.findViewById<TextView>(R.id.textview_six)?.apply {

                text = "${fromLocation}"
        }
        storyPrivew?.findViewById<TextView>(R.id.textview_seven)?.apply {

                text = "${toLocation}"
        }



        val iv = storyPrivew.findViewById<ImageView>(R.id.iamgeview).apply { setImageBitmap(bitmap) }
//        val downloadButton = storyPrivew.findViewById<Button>(R.id.download_button)
//        storyPrivew.findViewById<Button>(R.id.button).apply {
//            if (storyType== STORY_TYPE_COMPLETED)
//                visibility = View.GONE
//            setOnClickListener {
//
//                HomeActivity.clickedHalfStoryId = storyId
//
//                CropImage.activity()
//                    .setGuidelines(CropImageView.Guidelines.ON)
//                    .start(requireActivity());
//            }
//        }
        /*val pb = storyPrivew.findViewById<ProgressBar>(R.id.progress_bar)
        pb.visibility = View.VISIBLE

        object: UrlImageDownloader(url){
            override fun downloadCompletedCallBack(bitmap: Bitmap?) {
                iv.setImageBitmap(bitmap)
                pb.visibility = View.GONE

                val permissionsManager = PermissionsManager(requireActivity())
//                downloadButton.setOnClickListener {
//
//                    if (permissionsManager.isPermissionGranted(PermissionsManager.PERM_WRITE_EXTERNAL_STORAGE))
//                        saveImageToDirectory(bitmap, System.currentTimeMillis().toString())
//                    else
//                        permissionsManager.askForPermissions(arrayOf(PermissionsManager.PERM_READ_EXTERNAL_STORAGE, PermissionsManager.PERM_WRITE_EXTERNAL_STORAGE), PermissionsManager.CODE_STORAGE_PERM)
//
//                }

                if (storyType== STORY_TYPE_HALF)
                    iv.setOnClickListener {

                        HomeActivity.clickedHalfStoryId = storyId

                        val bos = ByteArrayOutputStream()
                        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, bos)
                        val bmpData = bos.toByteArray()
                        startActivity(Intent(requireContext(), EditingActivity::class.java).apply {

                            putExtra(EXTRA_IMAGE_BYTE_ARRAY, bmpData)
                        })
                    }

            }
        }*/

        AlertDialog.Builder(requireContext()).apply {
            setView(storyPrivew)
            setPositiveButton("ok"){di, id->di.dismiss()}
            show()
        }

    }

    private fun saveImageToDirectory(bitmap: Bitmap?, name: String) {

        val imagePath = MediaStore.Images.Media.insertImage(
            requireContext().contentResolver,
            bitmap,
            name,
            System.currentTimeMillis().toString()
        )

        val uri = Uri.parse(imagePath)
        Toast.makeText(requireContext(), uri.path, Toast.LENGTH_SHORT).show()
    }

    private fun saturateCluster() {

        fetchHalfStories()




    }

    private fun fetchHalfStories() {
        object : FirestoreDataClient(){
            override fun queryResult(halfStories: MutableMap<String, DocumentSnapshot>) {

                halfStories.forEach {
                    val halfStoryId = it.key
                    val halfStory = it.value.toObject(HalfStory::class.java)
                    halfStoriesData[it.key] = halfStory
                    val uid = halfStory?.uid
                    if (uid!=null)
                        uids.add(uid)



                    if (halfStoriesIndexing[uid]==null)
                        halfStoriesIndexing[uid!!] = mutableSetOf(halfStoryId)
                    else
                        halfStoriesIndexing[uid]?.add(halfStoryId)


                }

                fetchCompletedStories()



            }

        }.queryAll(COLLECTION_HALF)

    }

    private fun fetchCompletedStories() {

        object : FirestoreDataClient(){
            override fun queryResult(completedStories: MutableMap<String, DocumentSnapshot>) {

                completedStories.forEach {
                    val completedStory = it.value.toObject(CompletedStory::class.java)
                    completedStoriesData[it.key] = completedStory
                    val participentUID = completedStory?.uid
                    if (participentUID!=null)
                        uids.add(participentUID)


                    if (completedStoriesIndexing[participentUID]==null)
                        completedStoriesIndexing[participentUID!!] = mutableSetOf(it.key)
                    else
                        completedStoriesIndexing[participentUID]?.add(it.key)

                }


                fetchProfilesFromUids()

            }
        }.queryAll(COLLECTION_COMPLETED)

    }

    private fun profileQuery(list: Set<String>){
        if (list.size>0)
        object : FirestoreDataClient(){
                override fun queryResult(data: MutableMap<String, DocumentSnapshot>) {

                    data.forEach {
                        val profile = it.value.toObject(Profile::class.java)
                        val uid = profile?.uid
                        profilesData[uid!!] = profile
                        val location = profile.getLocation()

                    }

                    addMarkersAndPolylines()

                }

            }.query(COLLECTION_PROFILE, DATA_KEY_UID, list.toList())




    }

    private fun fetchProfilesInSegmants(uids: MutableSet<String>){
        val list = uids.toList()
        if(list.size>10){
            val first = list.subList(0, 10)
            val second = list.subList(10, list.lastIndex+1)
            profileQuery(first.toHashSet())
            fetchProfilesInSegmants(second.toMutableSet())

        }
        else {
            profileQuery(list.toHashSet())

        }
    }

    private fun fetchProfilesFromUids() {

        fetchProfilesInSegmants(uids)


    }

    private fun addMarkersAndPolylines() {

        profilesData.forEach {
            val profile = it.value
            val location = profile?.getLocation()
            val uid = it.key

            if (location!=null && !locations.containsKey(uid)) {
                locations[uid] = location
                clusterItems.add(MyClusterItem(location, profile.name!!, uid, profile.photoUrl))
                mClusterManager.addItems(clusterItems)
                mClusterManager.cluster()

            }
        }

        completedStoriesData.forEach {
            val completedStoryId = it.key
            val completedStory = it.value
            val halfStoryId = completedStory?.halfStoryId
            val halfStory = halfStoriesData[halfStoryId]

            val uid1 = completedStory?.uid
            val uid2 = halfStory?.uid

            val locationOne = profilesData[uid1]?.getLocation()
            val locationTwo = profilesData[uid2]?.getLocation()

            createPolyLine(locationOne, locationTwo)

        }



    }


    private fun createPolyLine(locationOne: LatLng?, locationTwo: LatLng?){

        if (locationOne!=null && locationTwo!=null){
            map.addPolyline(PolylineOptions().apply {
                add(locationOne, locationTwo)
                color(polyLineColors.shuffled().first())
                width(4f)
            })
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)



        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_main) as SupportMapFragment
        mapFragment.getMapAsync(this)



        return root
    }

    data class Frag(var frag: Fragment, var title: String)




}