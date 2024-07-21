package com.example.med

import com.google.firebase.Timestamp

data class medicine(
    val Medicine: String? = null,
    val Description: String? = null,
    val PricePerUnit: String? = null,
    val MedicineId: String? = null,
    val Stock: String? = null,
    val CreatedAt: Timestamp? = null,
    val LowStock: String? = null,
    val UserID: String? = null,
    val Category : String? = null,
    val ImageUri : String? = null
)
