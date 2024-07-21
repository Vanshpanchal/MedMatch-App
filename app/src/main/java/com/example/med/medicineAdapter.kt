package com.example.med

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class medicineAdapter(private val context: Context,private val items: ArrayList<medicine>):  RecyclerView.Adapter<medicineAdapter.med_ViewHolder>()  {

    lateinit var fs: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    lateinit var sr : StorageReference
    lateinit var editlistener: EditClick

    interface EditClick {
        fun onEditClick(position: Int)
    }

    fun onEdit(listener: EditClick) {
        editlistener = listener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): med_ViewHolder {
        fs = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val view = LayoutInflater.from(parent.context).inflate(R.layout.med_rv, parent, false)
        return med_ViewHolder(view,editlistener)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: med_ViewHolder, position: Int) {
        val item = items[position]
        val p_no = position + 1
        holder.medicinename.text = "Name: "+item.Medicine
        holder.category.text = "Category: "+item.Category
        holder.stock.text = "#" + item.Stock.toString()
        holder.sr.text = p_no.toString() + "."
        sr = FirebaseStorage.getInstance()
            .getReference("Medicines/" + auth.currentUser?.uid)
            .child(item.ImageUri.toString())

        sr.downloadUrl.addOnSuccessListener {
            Glide.with(context)
                .load(it)
                .into(holder.image)
        }


    }
    class med_ViewHolder (view: View,   editlistener: EditClick
    ) : RecyclerView.ViewHolder(view){
        val medicinename: TextView = view.findViewById(R.id.m_name)
        val category: TextView = view.findViewById(R.id.category)
        val image: de.hdodenhof.circleimageview.CircleImageView = view.findViewById(R.id.m_image)
        val sr: TextView = view.findViewById(R.id.sr)
        val stock : TextView = view.findViewById(R.id.m_stock)
        val edit: ImageButton = view.findViewById(R.id.edit_btn)

        init {
            edit.setOnClickListener {
                editlistener.onEditClick(adapterPosition)
            }
        }
    }

}