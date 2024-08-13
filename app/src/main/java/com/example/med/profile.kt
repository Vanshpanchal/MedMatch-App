package com.example.med

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.med.databinding.CustomprogressBinding
import com.example.med.databinding.FragmentProfileBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.json.JSONObject
import java.util.Locale


class profile : Fragment() {
    lateinit var fs: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    lateinit var sr: StorageReference
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    lateinit var binding: FragmentProfileBinding
    private var address = ""
    private val locationRequestCode = 1000
    private var P_longitude = 0.0
    private var P_latitude = 0.0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var galleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
//                    galleryprofile()
                val dialog = Dialog(requireContext())
                val layout = CustomprogressBinding.inflate(layoutInflater)
                dialog.setContentView(layout.root)
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()
//                imageUri = result?.data!!.data!!
                binding.profilePic.setImageURI(result.data!!.data)
                sr = FirebaseStorage.getInstance().getReference("Profile")
                    .child(auth.currentUser?.uid!!)
                sr.putFile(result.data?.data!!).addOnSuccessListener {
                    Log.d("D_CHECK", "Product Image Uploaded ")
                    dialog.dismiss()

                }.addOnFailureListener {
                    Log.d("D_CHECK", "Product Image Not Uploaded ")
                    dialog.dismiss()
                }

//                imageUri = result.data?.data
//                LoadImg(add_dailog, result.data?.data!!)


//                product_img?.setImageURI(result.data?.data)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fs = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        sharedPreferences =
            requireContext().getSharedPreferences("USERDATA", AppCompatActivity.MODE_PRIVATE)
        editor = sharedPreferences.edit()
        loadData()
        var shopname = ""
        var email = ""
        var username = ""
        fs.collection("Users").document(auth.currentUser?.uid!!).get().addOnSuccessListener {
            shopname = it.get("Shop-Name") as String
            email = it.get("Email") as String
            username = it.get("Uname") as String
            binding.email.text = email
            binding.shopname.text = "Shopname: " + shopname
            binding.username.text = username
            address = it.get("Address") as String

        }

        binding.profilePic.setOnClickListener {
            requestpermission()
        }

        binding.locateCard.setOnClickListener {
            checkLocationPermission()
//            addressApi(address,auth.currentUser?.uid!!,shopname)
//            custom_snackbar("Location Added Successfully")
        }
        binding.logoutCard.setOnClickListener {
            MaterialAlertDialogBuilder(
                requireContext()
            ).setTitle("Log Out").setIcon(R.drawable.baseline_logout_24)
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes") { dialog, which ->
                    editor.clear()
                    editor.commit()
                    auth.signOut()
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()

                }.setNegativeButton("No") { dialog, which ->
                    dialog.dismiss()
                }.show();
        }
        binding.resetCard.setOnClickListener {
            if (binding.email.text.toString().isNotEmpty()) {
                auth.sendPasswordResetEmail(binding.email.text.toString())
                    .addOnSuccessListener {
                        Log.d("hello", "Email sent.")
                        custom_snackbar("Reset Mail Sent")
                    }.addOnFailureListener {
                        Log.d("hello", "Failed")
                        custom_snackbar("Failed to send mail. Try Again Later!")
                    }
            }
        }
    }

    fun loadData() {
        sr = FirebaseStorage.getInstance().getReference("Profile").child(auth.currentUser?.uid!!)
        sr.downloadUrl.addOnSuccessListener {
            Glide.with(requireContext()).load(it).into(binding.profilePic)
        }
    }

    private fun checkpermissionRead() = ActivityCompat.checkSelfPermission(
        requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkpermissionReadImages() = ActivityCompat.checkSelfPermission(
        requireContext(), android.Manifest.permission.READ_MEDIA_IMAGES
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestpermission() {
        val permissiontoRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= 33) {
            if (!checkpermissionReadImages()) {
                permissiontoRequest.add(android.Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                val galleryIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galleryLauncher.launch(galleryIntent)
            }

            if (permissiontoRequest.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    requireContext() as Activity, permissiontoRequest.toTypedArray(), 0
                )
            }
        } else {
            if (!checkpermissionRead()) {
                permissiontoRequest.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                val galleryIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galleryLauncher.launch(galleryIntent)
////                profileImage()
//                profile()
            }

            if (permissiontoRequest.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    requireContext() as Activity, permissiontoRequest.toTypedArray(), 0
                )
            }
        }

    }


//    override fun onRequestPermissionsResult(
//        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == 0 && grantResults.isNotEmpty()) {
//            for (i in grantResults.indices) {
//                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
//                    Log.d("hello", "onRequestPermissionsResult: Done")
//
//                }
//            }
//        } else if (requestCode == locationRequestCode && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//            getLocationAndUpdateFirestore()
//        }
////            checkLocationPermission()
////            getLastKnownLocation()
//
//    }

    private fun custom_snackbar(message: String) {
        val bar = Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
        bar.setBackgroundTint(resources.getColor(R.color.blue))
        bar.setAction("OK") {
            bar.dismiss()
        }
        bar.setActionTextColor(resources.getColor(R.color.white))
        bar.show()
    }

    private fun addressApi(Address: String, Uid: String, shopname: String) {
        Log.d("D_CHECK", "addressApi: ${Address}")
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
                    "UserID" to Uid,
                    "Shopname" to shopname
                )

                fs.collection("Cordinates").document(auth.currentUser?.uid!!)
                    .collection("MyCordinates").document("data").set(
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
        Volley.newRequestQueue(requireContext()).add(stringRequest)


    }


//    fun checkLocationPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            // For Android Q and above
//            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED ||
//                ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED ||
//                ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//
//                ActivityCompat.requestPermissions(
//                    requireContext() as Activity,
//                    arrayOf(
//                        android.Manifest.permission.ACCESS_FINE_LOCATION,
//                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
//                        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
//                    ),
//                    locationRequestCode
//                )
//            } else {
//                getLocation1()
//            }
//        } else {
//            // For Android P and below
//            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED ||
//                ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//
//                ActivityCompat.requestPermissions(
//                    requireContext() as Activity,
//                    arrayOf(
//                        android.Manifest.permission.ACCESS_FINE_LOCATION,
//                       android.Manifest.permission.ACCESS_COARSE_LOCATION
//                    ),
//                    locationRequestCode
//                )
//            } else {
//                getLocation1()
//            }
//        }
//    }
//
//    //    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
////        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
////        if (requestCode == locationRequestCode && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
////            getLocation()
////        } else {
////            // Permission denied
////        }
////    }
//
//    fun getLocation1() {
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
//
//        if (ActivityCompat.checkSelfPermission(
//                requireContext(),
//                android.Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                requireContext(),
//                android.Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED &&
//            ActivityCompat.checkSelfPermission(
//                requireContext(),
//                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // Request the permission if not already granted
//            checkLocationPermission()
//            return
//        }
//
//        fusedLocationClient.lastLocation
//            .addOnSuccessListener { location: Location? ->
//                if (location != null) {
//                    val latitude = location.latitude
//                    val longitude = location.longitude
//                    // Use the latitude and longitude as needed
//                    Log.d("Location", "Latitude: $latitude, Longitude: $longitude")
//                    fs = FirebaseFirestore.getInstance()
//                    val address = getAddressFromLocation(latitude, longitude)
//                    val cordinates = mapOf(
//                        "Longitude" to longitude,
//                        "Latitude" to latitude,
//                        "Address" to address
//                    )
//                    val medicalStore = mapOf(
//                        "Address" to address
//                    )
//                    fs.collection("Cordinates").document(auth.currentUser?.uid!!)
//                        .collection("MyCordinates").document("data").update(
//                            cordinates
//                        ).addOnSuccessListener {
//                            Log.d(
//                                "D_CHECK",
//                                "Successfully added to firestore addressApi: ${cordinates}"
//                            )
//                        }
//                    fs.collection("Medical-Store").document(auth.currentUser?.uid!!).update(
//                        medicalStore
//                    ).addOnSuccessListener {
//                        Log.d(
//                            "D_CHECK",
//                            "Successfully added to firestore addressApi: ${medicalStore}"
//                        )
//                    }
//                    fs.collection("Users").document(auth.currentUser?.uid!!).update(
//                        medicalStore
//                    ).addOnSuccessListener {
//                        Log.d(
//                            "D_CHECK",
//                            "Successfully added to firestore addressApi: ${medicalStore}"
//                        )
//                    }
//                } else {
//                    // Handle the case when location is null
//                    Log.d("Location", "Location is null")
//                }
//            }
//            .addOnFailureListener {
//                // Handle the failure scenario
//                Log.e("Location", "Failed to get location", it)
//            }
//    }
//
//    private fun getAddressFromLocation(latitude: Double, longitude: Double): String {
//        val geocoder = Geocoder(requireContext(), Locale.getDefault())
//        try {
//            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
//            if (addresses?.isNotEmpty()!!) {
//                val address = addresses?.get(0)
//                val addressText = address?.getAddressLine(0) // Full address
//                val city = address?.locality // City
//                val state = address?.adminArea // State
//                val country = address?.countryName // Country
//
//                Log.d(
//                    "Address",
//                    "Address: $addressText, City: $city, State: $state, Country: $country"
//                )
//                return addressText.toString()
//            } else {
//                Log.d("Address", "No address found for the location.")
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Log.e("Geocoder", "Failed to get address", e)
//        }
//        return "Null"
//    }
fun checkLocationPermission() {
    val fineLocationPermission = android.Manifest.permission.ACCESS_FINE_LOCATION
    val coarseLocationPermission = android.Manifest.permission.ACCESS_COARSE_LOCATION
    val backgroundLocationPermission = android.Manifest.permission.ACCESS_BACKGROUND_LOCATION

    // For Android Q and above (API level 29+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        if (ContextCompat.checkSelfPermission(requireContext(), fineLocationPermission) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), coarseLocationPermission) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), backgroundLocationPermission) != PackageManager.PERMISSION_GRANTED) {

            // Request all necessary permissions
            ActivityCompat.requestPermissions(
                requireContext() as Activity,
                arrayOf(fineLocationPermission, coarseLocationPermission, backgroundLocationPermission),
                locationRequestCode
            )
        } else {
            getLocationAndUpdateFirestore()
        }
    } else {
        // For Android P and below (API level < 29)
        if (ContextCompat.checkSelfPermission(requireContext(), fineLocationPermission) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), coarseLocationPermission) != PackageManager.PERMISSION_GRANTED) {

            // Request necessary permissions
            ActivityCompat.requestPermissions(
                requireContext() as Activity,
                arrayOf(fineLocationPermission, coarseLocationPermission),
                locationRequestCode
            )
        } else {
            getLocationAndUpdateFirestore()
        }
    }
}

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndUpdateFirestore()
            }else if (requestCode == 0 && grantResults.isNotEmpty()) {
                for (i in grantResults.indices) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.d("hello", "onRequestPermissionsResult: Done")

                    }
                }
            }
            else {
                Log.e("Permissions", "Location permission denied.")
                custom_snackbar("Location permission is required to access this feature.")
            }
        }
    }

    private fun getLocationAndUpdateFirestore() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val address = getAddressFromLocation(latitude, longitude)
                    updateFirestoreWithLocationData(latitude, longitude, address)
                } else {
                    Log.d("Location", "Location is null")
                }
            }.addOnFailureListener { e ->
                Log.e("Location", "Failed to get location", e)
            }
        } else {
            Log.e("Permissions", "Location permission not granted.")
        }
    }

    private fun updateFirestoreWithLocationData(latitude: Double, longitude: Double, address: String) {
        val cordinates = mapOf(
            "Longitude" to longitude,
            "Latitude" to latitude,
            "Address" to address
        )
        val medicalStore = mapOf(
            "Address" to address
        )

        val uid = auth.currentUser?.uid ?: return
        fs.collection("Cordinates").document(uid)
            .collection("MyCordinates").document("data")
            .update(cordinates)
            .addOnSuccessListener {
                Log.d("Firestore", "Successfully updated Cordinates: $cordinates")
            }.addOnFailureListener { e ->
                Log.e("Firestore", "Failed to update Cordinates", e)
            }

        fs.collection("Medical-Store").document(uid)
            .update(medicalStore)
            .addOnSuccessListener {
                Log.d("Firestore", "Successfully updated Medical-Store: $medicalStore")
            }.addOnFailureListener { e ->
                Log.e("Firestore", "Failed to update Medical-Store", e)
            }

        fs.collection("Users").document(uid)
            .update(medicalStore)
            .addOnSuccessListener {
                Log.d("Firestore", "Successfully updated Users: $medicalStore")
            }.addOnFailureListener { e ->
                Log.e("Firestore", "Failed to update Users", e)
            }
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses?.isNotEmpty()!!) {
                val address = addresses[0]
                val addressText = address.getAddressLine(0) ?: "No Address Found"
                Log.d("Address", "Address: $addressText")
                addressText
            } else {
                Log.d("Address", "No address found for the location.")
                "No Address Found"
            }
        } catch (e: Exception) {
            Log.e("Geocoder", "Failed to get address", e)
            "No Address Found"
        }
    }

}