package com.example.med

import android.location.Address
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.med.databinding.ActivityHomeBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject

class Home : AppCompatActivity() {
    private lateinit var bind: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore
    private var address = ""
    private var P_longitude = 0.0
    private var P_latitude = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(bind.root)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.Frame, Medicines())
                .commit()
        }
        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()

//        fs.collection("Medical-Store").document(auth.currentUser?.uid!!).collection("My-Store")
//            .whereEqualTo("Uid",auth.currentUser?.uid!!).get().addOnSuccessListener { documents ->
//            for (document in documents) {
//                // Assuming the field you want to retrieve is called "fieldName"
//                val fieldValue = document.getString("Address")
//                if (fieldValue != null) {
//                    address = fieldValue.toString()
//
//                    Log.d("hello", "Field value: $fieldValue")
//                    // You can use the field value as needed
//                } else {
//                    println("Field not found in document: ${document.id}")
//                }
//            }
//        }
//            .addOnFailureListener { exception ->
//                println("Error getting documents: $exception")
//            }


        bind.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.item_1 -> {
                    replacefragement(Medicines(), "Medicines")
                    Log.d("hello", "Field value: $address")

//                    loadFragment(dashboard())
                    true
                }

                R.id.item_2 -> {
                    replacefragement(invoices(), "inventory")
//                    loadFragment(inventory())

                    true
                }

                R.id.item_3 -> {
//                    loadFragment(product())
//                    replacefragement(product(), "product")
                    true
                }

                else -> false
            }
        }
    }

    fun replacefragement(fragment: Fragment, tag: String) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        FragmentTransaction.TRANSIT_ENTER_MASK
        fragmentTransaction.replace(R.id.Frame, fragment)
        fragmentTransaction.commit()


    }

    private fun addressApi(Address: String, Uid: String) {
        var cordinates = listOf<Double>()
        val url =
            "https://api.geoapify.com/v1/geocode/search?text=$Address&apiKey=a4df04f3e2154cafbf08d57831558743"
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->

                var longitude = 0.0
                var latitude = 0.0
                val jsonObject = JSONObject(response)
                val featuresArray = jsonObject.getJSONArray("features")
                val feature = featuresArray.getJSONObject(0)
                val geometry = feature.getJSONObject("geometry")
                val coordinates = geometry.getJSONArray("coordinates")
                if (coordinates.length() >= 2) {
                    longitude = coordinates.getDouble(0)
                    latitude = coordinates.getDouble(1)
                    P_longitude = longitude
                    P_latitude = latitude
//                    }
                }
                Log.d("D_CHECK", "addressApi: ${longitude}  $latitude}")
                fs = FirebaseFirestore.getInstance()
                val cordinates = hashMapOf(
                    "Longitude" to P_longitude,
                    "Latitude" to P_latitude,
                    "Address" to Address,
                    "CreatedAt" to Timestamp.now().toDate(),
                    "UserID" to Uid
                )
                fs.collection("Cordinates").document(auth.currentUser?.uid!!)
                    .collection("MyCordinates").document().set(
                        cordinates
                    ).addOnSuccessListener {
                        Log.d(
                            "D_CHECK",
                            "Successfully added to firestore addressApi: ${cordinates}"
                        )
                    }

            },
            { error ->
            })
        Volley.newRequestQueue(this).add(stringRequest)


    }
}