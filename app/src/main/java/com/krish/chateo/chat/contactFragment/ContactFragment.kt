package com.krish.chateo.chat.contactFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.krish.chateo.databinding.FragmentContactBinding
import com.krish.chateo.model.User
import com.krish.chateo.util.showToast

private const val TAG = "ContactFragment"

class ContactFragment : Fragment() , CardClick {
    private lateinit var binding: FragmentContactBinding
    private val contactList: ArrayList<User> by lazy { ArrayList() }
    private val contactAdapter: ContactAdapter by lazy { ContactAdapter(listener = this@ContactFragment) }
    private val mFirestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentContactBinding.inflate(inflater, container, false)
        initViews()
        getContactDetails()
        return binding.root
    }

    private fun initViews() {
        binding.contactRecyclerView.adapter = contactAdapter
        binding.contactRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.contactRecyclerView.hasFixedSize()
        binding.swipeToRefresh.setOnRefreshListener {
            getContactDetails()
        }
        startRefresh()
    }


    private fun getContactDetails() {
        mFirestore
            .collection("users")
            .whereNotEqualTo("uid", auth.uid)
            .addSnapshotListener { document, error ->
                contactList.clear()
                error?.let { exception ->
                    showToast(requireContext(), "${exception.message}")
                    stopRefresh()
                    return@addSnapshotListener
                }
                document?.let { snapshot ->
                    try {
                        for (snap in snapshot) {
                            val contact = snap.toObject<User>()
                            contactList.add(contact)
                        }
                        contactAdapter.setData(contactList)
                        stopRefresh()
                    } catch (e: Exception) {
                        showToast(requireContext(), "${e.message}")
                        stopRefresh()
                    }
                }

            }

    }
    private fun startRefresh() {
        binding.swipeToRefresh.isRefreshing = true
    }

    private fun stopRefresh() {
        if (contactList.isEmpty()){
            binding.emptyAnimation.visibility = View.VISIBLE
        }else{
            binding.emptyAnimation.visibility = View.INVISIBLE
        }
        binding.swipeToRefresh.isRefreshing = false
    }

    override fun onContactClick(contact: User) {
        val action = ContactFragmentDirections.actionContactFragmentToMessageActivity(receiver = contact)
        findNavController().navigate(action)
    }
}
