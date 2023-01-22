package com.gdm.alphageeksales.view.ui.welcome_screen

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import androidx.lifecycle.MutableLiveData
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.remote.profile.ProfileData
import com.gdm.alphageeksales.databinding.DialogProfileBinding
import com.gdm.alphageeksales.utils.SharedPref
import com.gdm.alphageeksales.view.ui.update_profile.ProfileUpdateActivity
import com.google.gson.Gson
import com.squareup.picasso.Picasso

class ProfileDialog(context: Context, private val upSyncForLogout: MutableLiveData<Boolean>): Dialog(context) {
    private lateinit var binding: DialogProfileBinding
    private var profileData: ProfileData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogProfileBinding.bind(layoutInflater.inflate(R.layout.dialog_profile,null))
        setContentView(binding.root)
        setCancelable(false)
        SharedPref.init(context)

        val width = context.resources.displayMetrics.widthPixels
        window?.setLayout((6 * width) / 7, WindowManager.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        profileData = Gson().fromJson(SharedPref.read("PROFILE","")?:"", ProfileData::class.java)

        profileData?.reg_info?.name?.let { binding.userNameTv.text = it.replace("  "," ") }
        profileData?.details?.let { details->
            details.image?.let { image->
                when {image.isNotEmpty()-> Picasso.get().load(image).error(R.drawable.ic_user).into(binding.profileImg)}
            }
            details.email?.let { when{it.isNotEmpty()-> binding.emailTv.text = it} }
            details.phone?.let { when{it.isNotEmpty()-> binding.phoneTV.text = it} }
            details.gender?.let { when{it.isNotEmpty()-> binding.genderTv.text = it} }
            details.education?.let { when{it.isNotEmpty()-> binding.educationTv.text = it} }
            details.team_id?.let { when{it.isNotEmpty()-> binding.teamTv.text = it} }
            details.nin?.let { when{it.isNotEmpty()-> binding.ninTv.text = it} }
            details.lassra?.let { when{it.isNotEmpty()-> binding.lasraTv.text = it} }
            details.lga?.let { when{it.isNotEmpty()-> binding.lgaTv.text = it} }
            details.address?.let { when{it.isNotEmpty()-> binding.addressTv.text = it} }
            details.state_id?.let { when{it.isNotEmpty()-> binding.stateTv.text = it} }
            details.country_id?.let { when{it.isNotEmpty()-> binding.countryTv.text = it} }
            details.created_at?.let { when{it.isNotEmpty()-> binding.createTv.text = it} }
        }

        binding.closeBtn.setOnClickListener { dismiss() }

        binding.editBtn.setOnClickListener {
            AlertDialog.Builder(context,R.style.Calender_dialog_theme)
                .setTitle("Update Profile")
                .setMessage("Are you sure to update your profile ?")
                .setNegativeButton("No", DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() })
                .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, _ ->
                    SharedPref.write("UP_PROFILE","YES")
                    context.startActivity(Intent(context, ProfileUpdateActivity::class.java))
                })
                .setCancelable(false)
                .show()
        }

        binding.logOutBtn.setOnClickListener {
            val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        upSyncForLogout.postValue(true)
                        dialog.dismiss()
                        dismiss()
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {
                        dialog.dismiss()
                    }
                }
            }
            AlertDialog.Builder(context,R.style.Calender_dialog_theme)
                .setTitle(context.getString(R.string.are_you_sure_to_logout))
                .setCancelable(false)
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show()
        }
    }
}
