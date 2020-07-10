package com.example.cgprototype


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

/**
 * A simple [Fragment] subclass.
 */
class MapCompletedFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mClusterManager: ClusterManager<MyClusterItem>
    private lateinit var mClusterRenderer: DefaultClusterRenderer<MyClusterItem>
    private lateinit var map: GoogleMap

    override fun onMapReady(p0: GoogleMap) {
        map = p0
        setUpCluster()

    }

    private fun setUpCluster() {

        mClusterManager = ClusterManager(requireContext(), map)
        mClusterRenderer = CustomClusterRenderer(requireContext(), map, mClusterManager)
        map.setInfoWindowAdapter(CustomInfoWindowAadapter(requireContext()))
        map.setOnCameraIdleListener(mClusterManager)
        mClusterManager.renderer = mClusterRenderer

        saturateCluster()


        map.setOnInfoWindowClickListener {
            val intent = Intent(requireContext(), DrawingActivity::class.java).apply {
                putExtra(EXTRA_USER_ID, it.snippet)
            }
            startActivity(intent)
        }

    }

    private fun saturateCluster() {

        val items = mutableListOf<MyClusterItem>()
        val db = FirebaseFirestore.getInstance()
        db.collection(COLLECTION_PROFILE)
            .get()
            .addOnSuccessListener {
                it.documents.forEach {data->
                    val lat = data["lat"] as Double?
                    val lng = data["lng"] as Double?
                    if (lat!=null && lng!=null)
                        items.add(MyClusterItem(LatLng(lat, lng), data["name"] as String, data.id))
                }
                mClusterManager.addItems(items)
                mClusterManager.cluster()
            }

        val clusterItems = items


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root =  inflater.inflate(R.layout.fragment_map_completed, container, false)
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_completed) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return root
    }


}
