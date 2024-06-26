package com.fromryan.projectfoodapp.presentation.profile

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.fromryan.projectfoodapp.R
import com.fromryan.projectfoodapp.databinding.FragmentProfileBinding
import com.fromryan.projectfoodapp.presentation.login.LoginActivity
import com.fromryan.projectfoodapp.presentation.main.MainActivity
import com.fromryan.projectfoodapp.presentation.main.SharedPreferenceMainManager
import com.fromryan.projectfoodapp.utils.proceedWhen
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding

    private val profileViewModel: ProfileViewModel by viewModel()

    var count = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        checkIfUserLogin()
        setClickListener()
        getProfileData()
        changeEditMode()
        setSwitchMode()
    }

    private fun getProfileData() {
        profileViewModel.getCurrentUser()?.let {
            binding.etNameTextProfile.setText(it.fullName)
            binding.etEmailTextProfile.setText(it.email)
        }
    }

    private fun checkIfUserLogin() {
        if (profileViewModel.isUserLoggedIn()) {
        } else {
            navigateToLogin()
        }
    }

    private fun setSwitchMode() {
        val themeTitleList = arrayOf("Light", "Dark", "Auto (System Default)")
        val sharedPreferencesManager = SharedPreferenceMainManager(requireContext())
        var checkedTheme = sharedPreferencesManager.theme
        val themeDialog =
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Theme")
                .setPositiveButton("ok") { _, _ ->
                    sharedPreferencesManager.theme = checkedTheme
                    AppCompatDelegate.setDefaultNightMode(sharedPreferencesManager.themeFlag[checkedTheme])
                }
                .setSingleChoiceItems(themeTitleList, checkedTheme) { _, which ->
                    checkedTheme = which
                }
                .setCancelable(false)

        binding.switchMode.setOnClickListener {
            themeDialog.show()
        }
    }

    private fun setClickListener() {
        binding.btnEditProfile.setOnClickListener {
            if (profileViewModel.isUserLoggedIn()) {
                count += 1
                profileViewModel.changeEditMode()
                if (count % 2 == 0) {
                    val name = binding.etNameTextProfile.text.toString().trim()
                    binding.btnEditProfile.setText(getString(R.string.text_edit_profile))
                    changeProfileName(name)
                } else {
                    binding.btnEditProfile.setText(getString(R.string.text_save))
                }
            } else {
                navigateToLogin()
            }
        }

        binding.logoutProfile.setOnClickListener {
            if (profileViewModel.isUserLoggedIn()) {
                logoutUser()
            } else {
                navigateToLogin()
            }
        }

        binding.btnChangePw.setOnClickListener {
            if (profileViewModel.isUserLoggedIn()) {
                changePasswordUser()
            } else {
                navigateToLogin()
            }
        }
    }

    private fun changeEditMode() {
        profileViewModel.isEditMode.observe(viewLifecycleOwner) {
            binding.etNameTextProfile.isEnabled = it
            binding.etNomorTextProfile.isEnabled = it
        }
    }

    private fun changePasswordUser() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.layout_dialog_change_password)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        profileViewModel.changePassword()
        val backBtn: Button = dialog.findViewById(R.id.btn_back)
        backBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun changeProfileName(fullName: String) {
        profileViewModel.changeProfile(fullName).observe(viewLifecycleOwner) {
            it.proceedWhen(
                doOnSuccess = {
                    Toast.makeText(requireContext(), getString(R.string.text_link_edit_profile_success), Toast.LENGTH_SHORT).show()
                    profileViewModel.changeEditMode()
                },
                doOnError = {
                    Toast.makeText(requireContext(), getString(R.string.text_link_edit_profile_failed), Toast.LENGTH_SHORT).show()
                },
            )
        }
    }

    private fun logoutUser() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.layout_dialog_logout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel_dialog)
        val btnLogout = dialog.findViewById<Button>(R.id.btn_logout_dialog)
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        btnLogout.setOnClickListener {
            dialog.dismiss()
            profileViewModel.doLogout()
            navigateToHome()
        }
        dialog.show()
    }

    private fun navigateToLogin() {
        startActivity(Intent(requireContext(), LoginActivity::class.java))
    }

    private fun navigateToHome() {
        startActivity(
            Intent(requireContext(), MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            },
        )
    }
}
