package com.gdm.alphageeksales.view.ui.module.common.help

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gdm.alphageeksales.databinding.FragmentHelpAndSupportBinding

class HelpAndSupportFragment : Fragment() {
    private lateinit var binding: FragmentHelpAndSupportBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHelpAndSupportBinding.inflate(layoutInflater)

        binding.pdfViewer
            .fromAsset("user_manual_sales.pdf")
            .defaultPage(0)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .load()

        return binding.root
    }
}