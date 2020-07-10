package com.example.cgprototype


import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore


/**
 * A simple [Fragment] subclass.
 */
class MyLocFragment : Fragment(), OnMapReadyCallback {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap
    override fun onMapReady(p0: GoogleMap) {
        map = p0

//        Toast.makeText(requireContext(), "getting location", Toast.LENGTH_SHORT).show()

        getLocationFromProfile()

//        requestLocationPermission()


    }

    private fun getLocationFromProfile() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid!=null)
            object : FirestoreDataClient(){
                override fun queryResult(data: MutableMap<String, DocumentSnapshot>) {

                    data.forEach {
                        val profileId = it.key
                        val profile = it.value.toObject(Profile::class.java)
                        val location = profile?.getLocation()
                        if (location!=null) {
                            map.addMarker(MarkerOptions().position(location))
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))

                        }
                        else{
//                            Toast.makeText(requireContext(), "getting permissions started", Toast.LENGTH_SHORT).show()
                            requestLocationPermission()

                        }
                    }



                }
            }.query(COLLECTION_PROFILE, DATA_KEY_UID, listOf(uid))

        else{

            Toast.makeText(requireContext(), "You're not logged in.", Toast.LENGTH_SHORT).show()
//            progressBar.visibility = View.GONE
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())


        val root =  inflater.inflate(R.layout.fragment_my_loc, container, false)
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_my_loc) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return root
    }

    override fun onResume() {
        super.onResume()

    }

    private fun getLastLocation(){
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->

                if (location!=null) {


                    val auth = FirebaseAuth.getInstance()
                    val db = FirebaseFirestore.getInstance()
                    db.collection(COLLECTION_PROFILE).document(auth.currentUser!!.uid)
                        .update(
                            mapOf(
                                "lat" to location.latitude,
                                "lng" to location.longitude
                            )
                        )
                        .addOnSuccessListener {
                            Toast.makeText(
                                this.requireContext()
                                , "location added", Toast.LENGTH_SHORT
                            ).show()
                        }

                    val rb = arrayListOf<RadioButton>()
                    rb.add(RadioButton(requireContext()).apply {
                        text = "Urban"
                        id = 1001
                        isSelected = true
                    })
                    rb.add(RadioButton(requireContext()).apply {
                        text = "Rural"
                        id = 1002
                    })
                    val rg = RadioGroup(requireContext()).apply {
                        orientation = RadioGroup.VERTICAL
                        addView(rb[0])
                        addView(rb[1])

                    }
                }
                else{
                    Toast.makeText(requireContext(), "Please Enable location and Pin your location!", Toast.LENGTH_SHORT).show()
                }

//                AlertDialog.Builder(requireContext()).apply {
//                    setView(rg)
//                    setPositiveButton("add to your location", {di, id-> di.dismiss()})
//                    show()
//                }
//                Toast.makeText(this.requireContext()
//                    , "location ${location}", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun pinMyLocation(){

//        Toast.makeText(requireContext(), "start pinning", Toast.LENGTH_SHORT).show()
        map.isMyLocationEnabled = true

        map.setOnMyLocationButtonClickListener {
            getLastLocation()
            false
        }


        getLastLocation()

    }


    fun requestLocationPermission() {

        val permission = ContextCompat.checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            pinMyLocation()
        } else {
            // callback will be inside the activity's onRequestPermissionsResult(
            requestPermissions(
                requireActivity(),
                arrayOf(ACCESS_FINE_LOCATION),
                REQUEST_PERMISSION_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pinMyLocation()
            }
        }
    }

}
