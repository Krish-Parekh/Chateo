package com.krish.chateo.chat.chatFragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.krish.chateo.databinding.FragmentChatBinding
import com.krish.chateo.model.Inbox
import com.krish.chateo.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "CHAT_FRAGMENT"
class ChatFragment : Fragment() , CardClick{
    private lateinit var binding: FragmentChatBinding
    private val chatAdapter by lazy { ChatAdapter(requireContext(), this@ChatFragment) }
    private val inboxList = mutableListOf<Inbox>()
    private val mAuth by lazy { FirebaseAuth.getInstance() }
    private val mDatabase by lazy { FirebaseDatabase.getInstance() }
    private val mFireStore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(inflater,container, false)

        initView()
        getInboxData()

        return binding.root
    }

    private fun getInboxData() {
        mDatabase.reference
            .child("chats/${mAuth.uid!!}")
            .addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                inboxList.clear()
                for(snap in snapshot.children){
                    val inboxObj = snap.getValue(Inbox::class.java)!!
                    Log.d(TAG, "onDataChange: $inboxObj")
                    inboxList.add(inboxObj)
                }
                chatAdapter.setData(inboxList)
                stopRefresh()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "error : ${error.message} ")
                stopRefresh()
            }

        })
    }

    private fun initView() {
        startRefresh()
        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
        }
        binding.swipeToRefresh.setOnRefreshListener {
            val workerScope = CoroutineScope(Dispatchers.Main)
            workerScope.launch {
                delay(2000)
                binding.swipeToRefresh.isRefreshing = false
            }
        }
        stopRefresh()
    }

    private fun startRefresh() {
        binding.swipeToRefresh.isRefreshing = true
    }

    private fun stopRefresh() {
        if (inboxList.isEmpty()){
            binding.emptyAnimation.visibility = View.VISIBLE
            binding.emptyChatError.visibility = View.VISIBLE
        }else{
            binding.emptyAnimation.visibility = View.INVISIBLE
            binding.emptyChatError.visibility = View.INVISIBLE
        }
        binding.swipeToRefresh.isRefreshing = false
    }

    override fun inBoxClick(uid: String) {
        mFireStore
            .collection("users")
            .document(uid)
            .addSnapshotListener { value, error ->
                error?.let {exception ->
                    Log.d(TAG, "Error :${exception.message}")
                    return@addSnapshotListener
                }
                value?.let { snapshot ->
                    try {
                        val user = snapshot.toObject(User::class.java)!!
                        val action = ChatFragmentDirections.actionChatFragmentToMessageActivity(receiver = user)
                        findNavController().navigate(action)
                    }catch (e : Exception){
                        Log.d(TAG, "Error : ${e.message}")
                    }
                }
            }
    }

}