package com.example.secondhandtrade.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.secondhandtrade.AddItemActivity
import com.example.secondhandtrade.Constant.ITEM_PATH_STRING
import com.example.secondhandtrade.ItemDetailActivity
import com.example.secondhandtrade.LoginActivity
import com.example.secondhandtrade.R
import com.example.secondhandtrade.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var binding: FragmentHomeBinding? = null
    private val itemList = mutableListOf<ItemModel>()

    private lateinit var itemDB: DatabaseReference
    private lateinit var itemAdapter: ItemAdapter

    private val listener = object : ChildEventListener{
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val itemModel = snapshot.getValue(ItemModel::class.java)
            itemModel ?: return

            itemList.add(itemModel)
            itemAdapter.submitList(itemList)
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onChildRemoved(snapshot: DataSnapshot) {}

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onCancelled(error: DatabaseError) {}

    }

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentHomeBinding = FragmentHomeBinding.bind(view)
        binding = fragmentHomeBinding

        itemList.clear()

        itemDB = Firebase.database.reference.child(ITEM_PATH_STRING)
        itemAdapter = ItemAdapter(onItemClicked = { itemModel ->
            if(auth.currentUser?.uid == null) {
                startActivity(Intent(context, LoginActivity::class.java))
                return@ItemAdapter
            } else {
                val intent = Intent(context, ItemDetailActivity::class.java)

                intent.putExtra("itemModel", itemModel)
                startActivity(Intent(intent))
            }
        })

        fragmentHomeBinding.itemRecyclerView.layoutManager = LinearLayoutManager(context)
        fragmentHomeBinding.itemRecyclerView.adapter = itemAdapter

        fragmentHomeBinding.addItemFloatingButton.setOnClickListener {
            context?.let {
                val intent: Intent = if (auth.currentUser != null) {
                    Intent(it, AddItemActivity::class.java)

                } else {
                    Intent(it, LoginActivity::class.java)
                }

                startActivity(intent)
            }
        }
        itemDB.addChildEventListener(listener)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        itemAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        itemDB.removeEventListener(listener)
    }
}