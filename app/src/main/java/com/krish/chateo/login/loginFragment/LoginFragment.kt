package com.krish.chateo.login.loginFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.krish.chateo.R
import com.krish.chateo.databinding.FragmentLoginBinding
import com.krish.chateo.util.showToast

const val TAG = "LoginFragment"
class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var phoneNumber: String
    private lateinit var countryCode: String
    private lateinit var navController: NavController
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        val navHostFragment = activity?.supportFragmentManager?.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        initViews()

        return binding.root
    }

    private fun initViews(){
        binding.btnSendVerification.setOnClickListener {
            verifyNumber()
        }

        binding.loginToolBar.topAppBar.setNavigationOnClickListener {
            navController.popBackStack(R.id.loginFragment,true)
            activity?.finish()
        }
    }

    private fun verifyNumber() {
        countryCode = binding.ccp.selectedCountryCodeWithPlus
        phoneNumber = countryCode + binding.etPhoneNumber.text.toString()
        if (phoneNumber.isEmpty() || binding.etPhoneNumber.text.toString().length != 10) {
            Toast.makeText(requireContext(), "Invalid Number", Toast.LENGTH_SHORT).show()
        } else {
            notifyUser()
        }
    }

    private fun notifyUser() {
        MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme).apply {
            setMessage("We will be verifying the phone number: $phoneNumber\nIs this OK, or would you like to edit the number")
            setPositiveButton("Ok") { _, _ ->
                showOtpFragment()
            }
            setNegativeButton("Edit") { dialog, _ ->
                dialog.dismiss()
            }
            setCancelable(false)
            create()
            show()
        }
    }

    private fun showOtpFragment() {
        clearText()
        val action = LoginFragmentDirections.actionLoginFragmentToOtpFragment(phoneNumber = phoneNumber)
        findNavController().navigate(action)
    }

    private fun clearText() {
        binding.etPhoneNumber.text?.clear()
    }


}