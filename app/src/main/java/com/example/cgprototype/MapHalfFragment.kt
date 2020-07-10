package com.example.cgprototype


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

/**
 * A simple [Fragment] subclass.
 */
class MapHalfFragment : Fragment(), OnMapReadyCallback {

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

//        val clusterItems = getItems(map)
//        mClusterManager.addItems(clusterItems)
//        mClusterManager.cluster()

//        mClusterRenderer.setOnClusterItemClickListener {
//            Toast.makeText(requireContext(), "cluster clicked", Toast.LENGTH_SHORT).show()
//            true
//        }
//        mClusterManager.setOnClusterClickListener {
//            //            map.moveCamera()
//
//
//            val builder = LatLngBounds.builder()
//            var i = it.items.first()
//            it.items.forEach { item->
//
//                    builder.include(item.position)
//
//
//            }
//            val bounds = builder.build()
////            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
//
//            val zoomLevel = 20.0f
//            map.moveCamera(CameraUpdateFactory.newLatLngZoom(i.position, zoomLevel))
//
//            true
//        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root =  inflater.inflate(R.layout.fragment_map_half, container, false)
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_half) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return root
    }




}
