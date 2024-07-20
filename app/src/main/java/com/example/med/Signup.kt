package com.example.med

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.med.databinding.ActivitySignupBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

class Signup : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()

        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()

        binding.SignupBtn.setOnClickListener{
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
//                        val intent = Intent(this, Signin::class.java)
//                        startActivity(intent)
//                        finish()
                    }
                    bar.setActionTextColor(getColor(R.color.blue))
                    bar.show()
                    val randomInt = Random.nextInt(0, 100)
                    val userData = hashMapOf(
                        "Email" to auth.currentUser?.email,
                        "Uid" to auth.currentUser?.uid,
                        "Uname" to binding.username.text.toString() + "firestore-$randomInt",
                        "Shop-Name" to shopname
                    )

                    fs.collection("Users")
                        .document(auth.currentUser?.uid.toString())
                        .set(userData)

                } else {
                    Log.d("D_CHECK", "sendEmailVerification: $task.exception?.message")
                }
            }?.addOnFailureListener {
                val bar = Snackbar.make(binding.root, "An Error Occurred", Snackbar.LENGTH_SHORT)
                bar.setBackgroundTint(getColor(R.color.blue))
                bar.setAction("OK") {
                    bar.dismiss()
                }
                bar.setActionTextColor(getColor(R.color.blue))
                bar.show()
            }
    }
}