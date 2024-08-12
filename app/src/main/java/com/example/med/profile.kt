package com.example.med

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.med.databinding.CustomprogressBinding
import com.example.med.databinding.FragmentProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.json.JSONObject


class profile : Fragment() {
    lateinit var fs:    FirebaseFirestore
    lateinit var auth: FirebaseAuth
    lateinit var sr: StorageReference
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    lateinit var binding: FragmentProfileBinding
    private var address = ""
    private var P_longitude = 0.0
    private var P_latitude = 0.0
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
        fs.collection("Users").document(auth.currentUser?.uid!!).get().addOnSuccessListener {
            shopname = it.get("Shop-Name") as String
            email = it.get("Email") as String
            binding.email.text = "Email: "+email
            binding.shopname.text = "Shopname: "+shopname
            address = it.get("Address") as String

        }

        binding.profilePic.setOnClickListener {
            requestpermission()
        }

        binding.locateCard.setOnClickListener{
            addressApi(address,auth.currentUser?.uid!!)
        }
        binding.logoutCard.setOnClickListener{
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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && grantResults.isNotEmpty()) {
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("hello", "onRequestPermissionsResult: Done")

                }
            }
        } else {
//            checkLocationPermission()
//            getLastKnownLocation()
        }
    }

    private fun custom_snackbar(message: String) {
        val bar = Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
        bar.setBackgroundTint(resources.getColor(R.color.blue))
        bar.setAction("OK") {
            bar.dismiss()
        }
        bar.setActionTextColor(resources.getColor(R.color.white))
        bar.show()
    }

    private fun addressApi(Address: String, Uid: String) {
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
        Volley.newRequestQueue(requireContext()).add(stringRequest)


    }
}