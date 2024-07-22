package com.example.med

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.med.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var bind: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)
//        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance()
        bind.signIn.setOnClickListener {
            val intent = Intent(this, Signin::class.java)
            startActivity(intent)
            finish()
        }

        bind.singUp.setOnClickListener {
            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
            finish()
        }

        bind.explore.setOnClickListener{
            val intent = Intent(this, User::class.java)
            startActivity(intent)
            finish()
        }

        auth = FirebaseAuth.getInstance()

        sharedPreferences = getSharedPreferences("USERDATA", MODE_PRIVATE)
        editor = sharedPreferences.edit()

        if (sharedPreferences.contains("Email") && sharedPreferences.contains("Pass")) {
            authenticateUser(
                sharedPreferences.getString("Email", "").toString(),
                sharedPreferences.getString("Pass", "").toString()
            )
        }

    }

    private fun authenticateUser(emailAddress: String, pass: String) {
        auth.signInWithEmailAndPassword(emailAddress, pass)
            .addOnCompleteListener { it ->
                val user = auth.currentUser
                if (it.isSuccessful) {
                    if (user != null && user.isEmailVerified) {

                        val intent = Intent(this, Home::class.java)
                        startActivity(intent)

                    } else {
                        val bar = Snackbar.make(
                            bind.root,
                            "Verify Email",
                            Snackbar.LENGTH_SHORT
                        )
                        bar.setBackgroundTint(getColor(R.color.blue))
                        bar.setActionTextColor(getColor(R.color.blue))
                        bar.setAction("OK") {
                            bar.dismiss()
                        }
                        bar.show()
                    }
                } else {
                    Log.d("hello", "onCreate: ${it.exception?.message} ")
                }
            }.addOnFailureListener {
                val bar = Snackbar.make(
                    bind.root,
                    "Please Check Your Entered Credentials",
                    Snackbar.LENGTH_SHORT
                )
                bar.setAction("OK") {
                    bar.dismiss()
                }
                bar.setBackgroundTint(getColor(R.color.blue))
                bar.setActionTextColor(getColor(R.color.blue))
                bar.show()
                Log.d("hello", "onCreate: ${it.message} ")
            }
    }
}