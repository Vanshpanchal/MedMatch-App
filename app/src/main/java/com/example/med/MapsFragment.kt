package com.example.med

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MapsFragment : Fragment() {
    private lateinit var fs: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var cordinates: ArrayList<Cordinate>

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
//        val sydney = LatLng(-34.0, 151.0)
//        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        AdminfetchCoordinatesAndAddMarkers(googleMap)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fs = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        cordinates = arrayListOf()
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun AdminfetchCoordinatesAndAddMarkers(googleMap: GoogleMap) {
        cordinates.clear()
        fs.collection("Users").get().addOnSuccessListener {
            for (document in it) {
                Log.d("D_CHECK", "helloMap:: ${it.documents}")

                fs.collection("Cordinates").document(document.data["Uid"].toString())
                    .collection("MyCordinates").get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            Log.d("D_CHECK", "helloMap ${documents}")
                            val lat = document.getDouble("Latitude") ?: 0.0
                            val lng = document.getDouble("Longitude") ?: 0.0
                            val position = LatLng(lat, lng)

                            val marker = document.data["Shopname"] as String +", "+ document.data["Address"] as String
                            val r = document.toObject(Cordinate::class.java)
                            cordinates.add(r)
                            if (cordinates.size > 0) {
                                googleMap.addMarker(
                                    MarkerOptions().position(position).title(marker)
                                )
                            }
                        }
                        // Optionally move the camera to the last marker
                        if (!documents.isEmpty) {
                            val lastPosition = LatLng(
                                documents.last().getDouble("Latitude") ?: 0.0,
                                documents.last().getDouble("Longitude") ?: 0.0
                            )
                            googleMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    lastPosition,
                                    1f
                                )
                            )
                        }
                        Log.d("D_CHECK", "Map ${cordinates}")
                        if (cordinates.size > 0) {
                            cordinates.sortBy { it.CreatedAt }
                            val lastPosition = LatLng(
                                cordinates.last().Latitude ?: 0.0,
                                cordinates.last().Longitude ?: 0.0
                            )
                            googleMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    lastPosition,
                                    8f
                                )
                            )
                        }
                    }

                    .addOnFailureListener { exception ->
                        Log.w("MapsFragment", "Error getting documents: ", exception)
                    }


            }

        }.addOnFailureListener {
            Log.d("D_CHECK", "helloMap ${it}")

        }
    }
}