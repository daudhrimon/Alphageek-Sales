package com.gdm.alphageeksales.view.ui.module.common.inbox

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdm.alphageeksales.databinding.FragmentInboxBinding
import com.gdm.alphageeksales.utils.isGONE
import com.gdm.alphageeksales.utils.isVISIBLE
import com.gdm.alphageeksales.view.adapter.BriefAdapter
import com.gdm.alphageeksales.viewmodels.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InboxFragment : Fragment() {
    private lateinit var binding: FragmentInboxBinding
    private val dashboardViewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInboxBinding.inflate(layoutInflater)

        // get value from db
        dashboardViewModel.getBriefList()
        dashboardViewModel.briefList.observe(viewLifecycleOwner){
            if (it != null){
                // set up the information
                if (it.isNotEmpty()){
                    binding.emptyViewLayout.isGONE()
                    binding.recyclerview.isVISIBLE()
                    binding.recyclerview.layoutManager = LinearLayoutManager(requireActivity())
                    binding.recyclerview.adapter = BriefAdapter(it)
                }else{
                    binding.emptyViewLayout.isVISIBLE()
                    binding.recyclerview.isGONE()
                }
            }
        }

        return binding.root
    }
}