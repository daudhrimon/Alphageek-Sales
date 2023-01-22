package com.gdm.alphageeksales.view.ui.module.common.outlet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.databinding.FragmentOutletListBinding
import com.gdm.alphageeksales.utils.isGONE
import com.gdm.alphageeksales.utils.isVISIBLE
import com.gdm.alphageeksales.view.adapter.OutletListAdapter
import com.gdm.alphageeksales.viewmodels.OutletViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OutletListFragment : Fragment() {
    private lateinit var binding: FragmentOutletListBinding
    private val outletViewModel:OutletViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOutletListBinding.inflate(layoutInflater)

        // get value from db
        outletViewModel.getAllOutlet()
        // observe schedule data
        outletViewModel.outletList.observe(viewLifecycleOwner) {
            if (it != null){
                // set up the information
                if (it.isNotEmpty()){
                    binding.emptyViewLayout.isGONE()
                    binding.recyclerview.isVISIBLE()
                    binding.recyclerview.layoutManager = LinearLayoutManager(requireActivity())
                    binding.recyclerview.adapter = OutletListAdapter(it,requireActivity())
                }else{
                    binding.emptyViewLayout.isVISIBLE()
                    binding.recyclerview.isGONE()
                }
            }
        }


        binding.createOutlet.setOnClickListener{
            findNavController().navigate(R.id.createOutletFragment)
        }


        return binding.root
    }

}