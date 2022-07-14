package com.krish.chateo.login.otpFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.krish.chateo.R
import com.krish.chateo.databinding.FragmentOtpBinding
import com.krish.chateo.util.dismissDialogBox
import com.krish.chateo.util.showLoadingDialog
import com.krish.chateo.util.showToast
import java.util.concurrent.TimeUnit

private const val TAG = "OtpFragment"

class OtpFragment : Fragment() {

    private lateinit var binding: FragmentOtpBinding
    private val args by navArgs<OtpFragmentArgs>()
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var mVerificationId: String? = null
    private var mResendToken: PhoneAuthProvider.ForceResendingToken? = null
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentOtpBinding.inflate(inflater, container, false)
        val navHostFragment = activity?.supportFragmentManager?.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        initView()
        startVerification()
        return binding.root
    }

    private fun initView() {
        binding.tvPhoneNumberVerify.text = getString(R.string.verification_text, args.phoneNumber)
        binding.otpToolBar.topAppBar.setNavigationOnClickListener {
            navController.navigateUp()
        }

        binding.tvResendCode.setOnClickListener {
            handleResendCode()
        }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted:$credential")
                val smsCode = credential.smsCode
                if (!smsCode.isNullOrEmpty()) {
                    binding.etVerificationCode.setText(smsCode)
                    dismissDialogBox()
                }
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "onVerificationFailed", e)
                showToast(requireContext(), e.message.toString())
                dismissDialogBox()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "onCodeSent:$verificationId")
                mVerificationId = verificationId
                mResendToken = token
            }
        }
    }

    private fun startVerification() {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(args.phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        showLoadingDialog(requireContext(), "Verifying your phone number")
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    Log.d(TAG, "signInWithCredential:$user")
                    dismissDialogBox()
                    sendToProfileFragment()
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException || task.exception != null) {
                        showToast(requireContext(), task.exception?.message.toString())
                        dismissDialogBox()
                    }
                }
            }
    }

    private fun handleResendCode() {
        if (mResendToken != null) {
            showLoadingDialog(requireContext(), "Sending Verification Code")
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(args.phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(callbacks)
                .setForceResendingToken(mResendToken!!)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    private fun sendToProfileFragment() {
        val action = OtpFragmentDirections.actionOtpFragmentToProfileFragment(phoneNumber = args.phoneNumber)
        findNavController().navigate(action)
    }

}


