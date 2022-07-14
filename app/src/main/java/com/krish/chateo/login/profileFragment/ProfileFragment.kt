package com.krish.chateo.login.profileFragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.krish.chateo.R
import com.krish.chateo.databinding.FragmentProfileBinding
import com.krish.chateo.model.User
import com.krish.chateo.util.dismissDialogBox
import com.krish.chateo.util.showLoadingDialog
import com.krish.chateo.util.showToast
import java.util.*

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var selectImageFromGallery: ActivityResultLauncher<String>
    private val args by navArgs<ProfileFragmentArgs>()
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val mFirebaseStorage by lazy { FirebaseStorage.getInstance() }
    private val mFirestore by lazy { FirebaseFirestore.getInstance() }
    private lateinit var imageUri: Uri
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        initViews()
        return binding.root
    }

    private fun initViews(){
        selectImageFromGallery =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                uri?.let {
                    binding.profileImage.setImageURI(uri)
                    imageUri = uri
                }
            }

        binding.btnSave.setOnClickListener {
            val firstName = binding.etFirstName.text.toString()
            val lastName = binding.etLastName.text.toString()
            val fullName = "$firstName $lastName"
            val uid = auth.uid.toString()
            if (this::imageUri.isInitialized && validateUserDetails(firstName, lastName)){
                uploadImages(imageUri, fullName, uid)
            }else{
                showToast(requireContext(), "Please fill the complete form")
            }
        }

        binding.ivAddIcon.setOnClickListener {
            selectImageFromGallery()
        }
    }


    private fun uploadImages(imageUri: Uri, fullName: String, uid: String) {
        val filename = UUID.randomUUID().toString()
        val imageStoreRef = mFirebaseStorage.getReference("/images/$filename")
        showLoadingDialog(requireContext(),"Uploading your details")

        imageStoreRef.putFile(imageUri)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    imageStoreRef.downloadUrl.addOnCompleteListener { downloadTask ->
                        if (downloadTask.isSuccessful) {
                            saveUserToDatabase(downloadTask.result!!, fullName, uid)
                        }
                        else if (downloadTask.exception != null){
                            showToast(requireContext(), downloadTask.exception?.localizedMessage!!)
                            dismissDialogBox()
                        }
                    }
                }else if(task.exception != null){
                    showToast(requireContext(), task.exception?.localizedMessage!!)
                    dismissDialogBox()
                }
            }
    }

    private fun saveUserToDatabase(imageUrl: Uri, fullName: String, uid: String) {
        val user = User(
            name = fullName,
            phoneNumber = args.phoneNumber,
            imageUrl = imageUrl.toString(),
            uid = uid,
            status = getString(R.string.chateo_status),
        )
        val fireStoreRef = mFirestore.collection("users").document(uid).set(user)
        fireStoreRef.addOnCompleteListener { task ->
            when {
                task.isSuccessful -> {
                    showToast(requireContext(), "Success")
                    sendToChatActivity()
                    clearField()
                }
                task.exception.toString().isNotEmpty() -> {
                    showToast(requireContext(), task.exception.toString())
                    dismissDialogBox()
                }
            }
        }
    }

    private fun clearField() {
        binding.apply {
            profileImage.setImageURI(null)
            etFirstName.text?.clear()
            etLastName.text?.clear()
        }
    }

    private fun validateUserDetails(firstName: String, lastName: String): Boolean {
        return firstName.isNotEmpty() && lastName.isNotEmpty()
    }

    private fun selectImageFromGallery() {
        selectImageFromGallery.launch("image/*")
    }

    private fun sendToChatActivity() {
        findNavController().navigate(R.id.action_profileFragment_to_chatActivity)
    }
}