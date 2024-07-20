package com.example.med

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.med.databinding.ActivityHomeBinding
import com.example.med.databinding.FragmentMedicinesBinding

class Home : AppCompatActivity() {
    private lateinit var bind: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(bind.root)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.Frame, Medicines())
                .commit()
        }
        bind.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.item_1 -> {
                    replacefragement(Medicines(), "Medicines")
//                    loadFragment(dashboard())
                    true
                }

                R.id.item_2 -> {
//                    replacefragement(MapsFragment(), "inventory")
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
}