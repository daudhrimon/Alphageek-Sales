package com.gdm.alphageeksales.view.ui.module.common.inbox

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gdm.alphageeksales.databinding.FragmentInboxDetailsBinding
import com.gdm.alphageeksales.utils.Utils
import com.gdm.alphageeksales.utils.showErrorToast

class InboxDetailsFragment : Fragment() {
    private lateinit var binding: FragmentInboxDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInboxDetailsBinding.inflate(layoutInflater)


        binding.title.text   = Utils.currentBrief.title
        binding.details.text = Utils.currentBrief.description


        binding.downloadBtn.setOnClickListener {
            if (!Utils.currentBrief.file.isNullOrEmpty()) {
                val url = Utils.currentBrief.file
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            } else {
                showErrorToast(requireContext(),"No file found")
            }
        }


        return binding.root
    }
}