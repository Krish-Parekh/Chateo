package com.krish.chateo.chat

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.krish.chateo.R
import com.krish.chateo.databinding.ActivityChatBinding
import com.krish.chateo.model.Inbox

private const val TAG = "CHAT_ACTIVITY"

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val mFirebaseDatabase: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
    private val mFireStore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private var count = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.chatNavHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)

        updateUserStatus(true)
        badgeSetup()
    }

    private fun badgeSetup() {
        mFirebaseDatabase.reference
            .child("chats/${mFirebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    count = 0
                    for (snap in snapshot.children) {
                        val chat = snap.getValue(Inbox::class.java)!!
                        count += chat.count
                    }
                    if (count > 0) {
                        binding.bottomNavigation.getOrCreateBadge(R.id.chatFragment).number = count
                    } else {
                        binding.bottomNavigation.removeBadge(R.id.chatFragment)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "Error: ${error.message}")
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        updateUserStatus(false)

    }

    private fun updateUserStatus(status: Boolean) {
        val currentUid = mFirebaseAuth.uid
        val currentTime: String by lazy { System.currentTimeMillis().toString() }
        val statusUpdateMap = mapOf(Pair("onlineStatus", status), Pair("lastActive", currentTime))
        mFireStore.collection("users").document(currentUid!!).update(statusUpdateMap)
            .addOnSuccessListener {
                Log.d(TAG, "Status updated Success")
            }
            .addOnFailureListener {
                Log.d(TAG, "Error while updating : ${it.message} ")
            }
    }


}