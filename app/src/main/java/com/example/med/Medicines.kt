package com.example.med

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.med.databinding.FragmentMedicinesBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Medicines : Fragment() {
    lateinit var binding: FragmentMedicinesBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentMedicinesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()
        binding.addMedicine.setOnClickListener {
            val view = LayoutInflater.from(requireContext())
                .inflate(R.layout.medicine_add, null, false)
            val dialog = MaterialAlertDialogBuilder(
                requireContext()
            )
                .setView(view)
                .create()
            view.findViewById<Button>(R.id.submit_btn).setOnClickListener {

                if (view.findViewById<TextInputEditText>(R.id.medicine_name).text.toString()
                        .isNotEmpty() && view.findViewById<TextInputEditText>(R.id.priceper_unit).text.toString()
                        .isNotEmpty() && view.findViewById<TextInputEditText>(R.id.stock).text.toString()
                        .isNotEmpty() && view.findViewById<TextInputEditText>(R.id.category).text.toString()
                        .isNotEmpty() && view.findViewById<TextInputEditText>(R.id.description).text.toString()
                        .isNotEmpty()
                ) {


                    val inventoryInfo = hashMapOf(
                        "Medicine" to view.findViewById<TextInputEditText>(R.id.medicine_name).text.toString(),
                        "Description" to view.findViewById<TextInputEditText>(R.id.description).text.toString(),
                        "Stock" to view.findViewById<TextInputEditText>(R.id.stock).text.toString(),
                        "PricePerUnit" to view.findViewById<TextInputEditText>(R.id.priceper_unit).text.toString(),
                        "Category" to view.findViewById<TextInputEditText>(R.id.category).text.toString(),
                        "CreatedAt" to Timestamp.now().toDate(),
                        "UserID" to auth.currentUser?.uid!!

                    )
                    fs.collection("Medicines").document(auth.currentUser?.uid!!)
                        .collection("MyMedicines").document().set(inventoryInfo)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                custom_snackbar("Medicine Added")
//                                checkUser()
//                                getInventory()

                            } else {
                                custom_snackbar("Error")
                            }
                        }
                    Log.d("hello", "itemClickListener: $} ")
                    dialog.dismiss()
                } else {
                    custom_snackbar("Enter Proper Detail")
                }
            }
            dialog.show()
        }
        }

    private fun custom_snackbar(message: String) {
        val bar = Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
        bar.setBackgroundTint(resources.getColor(R.color.blue))
        bar.setAction("OK") {
            bar.dismiss()
        }
        bar.setActionTextColor(resources.getColor(R.color.blue))
        bar.show()
    }
    }
