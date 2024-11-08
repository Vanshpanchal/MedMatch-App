package com.example.med

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.med.databinding.ActivitySignupBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject
import kotlin.random.Random

class Signup : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore
    private var P_longitude = 0.0
    private var P_latitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()

        // final minor changes
        binding.backSignUp.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.SignupBtn.setOnClickListener {
            if (binding.username.text.toString().isNotBlank() && binding.email.text.toString()
                    .isNotEmpty() && binding.password.text.toString()
                    .isNotEmpty() && binding.shopname.text.toString().isNotEmpty()
            ) {
                signUpUser(binding.email.text.toString(), binding.password.text.toString())
            }
        }
    }

    private fun signUpUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    sendEmailVerification(binding.shopname.text.toString())
                } else {
                    when (val exception = task.exception) {
                        is FirebaseAuthInvalidCredentialsException -> {
                            Log.d("hello", "Invalid email or password format: ${exception.message}")

                            val bar =
                                Snackbar.make(binding.root, "Invalid format", Snackbar.LENGTH_SHORT)
                            bar.setBackgroundTint(getColor(R.color.blue))
                            bar.setAction("OK") {
                                bar.dismiss()
                            }
                            bar.setActionTextColor(getColor(R.color.blue))
                            bar.show()
                        }

                        is FirebaseAuthUserCollisionException -> {
                            Log.d("hello", "Email address already in use: ${exception.message}")
                            val bar =
                                Snackbar.make(
                                    binding.root,
                                    "Email Already Exist",
                                    Snackbar.LENGTH_SHORT
                                )
                            bar.setBackgroundTint(getColor(R.color.blue))
                            bar.setAction("OK") {
                                bar.dismiss()
                            }
                            bar.setActionTextColor(getColor(R.color.blue))
                            bar.show()
                        }

                        is FirebaseAuthInvalidUserException -> {
                            Log.d("hello", "Invalid user: ${exception.message}")
                            val bar =
                                Snackbar.make(
                                    binding.root,
                                    "Invalid credential",
                                    Snackbar.LENGTH_SHORT
                                )
                            bar.setBackgroundTint(getColor(R.color.blue))
                            bar.setAction("OK") {
                                bar.dismiss()
                            }
                            bar.setActionTextColor(getColor(R.color.blue))
                            bar.show()
                        }

                        else -> {
                            Log.d("hello", "Sign-up failed: ${exception?.message}")
                            val bar = Snackbar.make(binding.root, "Else ‼️", Snackbar.LENGTH_SHORT)
                            bar.setBackgroundTint(getColor(R.color.blue))
                            bar.setAction("OK") {
                                bar.dismiss()
                            }
                            bar.setActionTextColor(getColor(R.color.blue))
                            bar.show()
                        }
                    }
                }
            }
    }

    private fun sendEmailVerification(shopname: String) {
        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val bar = Snackbar.make(binding.root, "Mail Sent", Snackbar.LENGTH_SHORT)
                    bar.setBackgroundTint(getColor(R.color.blue))
                    bar.setAction("OK") {
                        bar.dismiss()
                        val intent = Intent(this, Signin::class.java)
                        startActivity(intent)
                        finish()
                    }
                    bar.setActionTextColor(getColor(R.color.blue))
                    bar.show()
                    val randomInt = Random.nextInt(0, 100)
                    val userData = hashMapOf(
                        "Email" to auth.currentUser?.email,
                        "Uid" to auth.currentUser?.uid,
                        "Uname" to binding.username.text.toString() + "firestore-$randomInt",
                        "Shop-Name" to shopname,
                        "Address" to binding.address.text.toString()
                    )

                    fs.collection("Users")
                        .document(auth.currentUser?.uid.toString())
                        .set(userData)
                    val medicalStore = hashMapOf(
                        "Uid" to auth.currentUser?.uid,
                        "Uname" to binding.username.text.toString() + "firestore-$randomInt",
                        "ShopName" to shopname,
                        "Address" to binding.address.text.toString(),
                        "MedicineID" to emptyList<String>()
                    )


                    fs.collection("Medical-Store").document(auth.currentUser?.uid!!).set(medicalStore)
                    val address = binding.address.text.toString()
                    addressApi(address, auth.currentUser?.uid!!,shopname) //Just call after changing Api key
                } else {
                    Log.d("D_CHECK", "sendEmailVerification: $task.exception?.message")
                }
            }?.addOnFailureListener {
                val bar = Snackbar.make(binding.root, "An Error Occurred", Snackbar.LENGTH_SHORT)

                bar.setAction("OK") {
                    bar.dismiss()
                }
                bar.setActionTextColor(getColor(R.color.white))
                bar.show()
            }
    }
    private fun addressApi(Address: String, Uid: String,shopname: String) {
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
        Volley.newRequestQueue(this).add(stringRequest)


    }

}