package com.example.secondhandtrade.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.secondhandtrade.R
import com.example.secondhandtrade.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var binding: FragmentHomeBinding? = null
    private lateinit var itemAdapter: ItemAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentHomeBinding = FragmentHomeBinding.bind(view)
        binding = fragmentHomeBinding

        itemAdapter = ItemAdapter()

        fragmentHomeBinding.itemRecyclerView.layoutManager = LinearLayoutManager(context)
        fragmentHomeBinding.itemRecyclerView.adapter = itemAdapter
    }
}