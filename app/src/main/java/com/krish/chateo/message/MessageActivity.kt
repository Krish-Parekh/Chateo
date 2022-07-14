package com.krish.chateo.message

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.krish.chateo.databinding.ActivityMessageBinding
import com.krish.chateo.model.*
import com.krish.chateo.util.KeyboardVisibilityUtil
import com.krish.chateo.util.isSameDayAs
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "MESSAGE_ACTIVITY"

class MessageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMessageBinding
    private val currentUid: String by lazy { FirebaseAuth.getInstance().uid.toString() }
    private val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
    private val fireStore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    lateinit var currentUser: User

    private val messageAdapter: MessageAdapter by lazy { MessageAdapter(currentUid = currentUid) }
    private val args by navArgs<MessageActivityArgs>()
    private val messageList = mutableListOf<ChatEvent>()

    private lateinit var keyboardVisibilityHelper: KeyboardVisibilityUtil

    private val friendId by lazy { args.receiver.uid }
    private val friendName by lazy { args.receiver.name }
    private val friendImage by lazy { args.receiver.imageUrl }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(GoogleEmojiProvider())
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        keyboardVisibilityHelper = KeyboardVisibilityUtil(binding.root) {
            binding.rvMessage.scrollToPosition(messageList.size - 1)
        }
        val emojiPopup = EmojiPopup(binding.root, binding.etMessageBox)
        binding.emojiBtn.setOnClickListener {
            emojiPopup.toggle()
        }

        // getting the current user details
        fireStore.collection("users").document(currentUid).get()
            .addOnSuccessListener { user ->
                currentUser = user.toObject(User::class.java)!!
            }

        // Binding All the views with action
        binding.ivProfile.load(friendImage) {
            crossfade(true)
        }

        binding.tvUsername.text = friendName
        binding.rvMessage.apply {
            adapter = messageAdapter
            layoutManager = LinearLayoutManager(this@MessageActivity)
        }

        listenToMessages()

        binding.btnSend.setOnClickListener {
            binding.etMessageBox.text?.let { message ->
                if (message.isNotEmpty()) {
                    sendMessage(message.toString())
                    message.clear()
                }
            }
        }

        binding.swipeToLoad.setOnRefreshListener {
            val workerScope = CoroutineScope(Dispatchers.Main)
            workerScope.launch {
                delay(2000)
                binding.swipeToLoad.isRefreshing = false
            }
        }
    }

    private fun listenToMessages() {
        setMessage(friendId = friendId)
            .orderByKey()
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val message = snapshot.getValue(Message::class.java)!!
                    addMessage(message)
                    messageAdapter.setData(messageList)
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildRemoved(snapshot: DataSnapshot) {}

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun addMessage(message: Message) {
        val eventBefore = messageList.lastOrNull()
        if ((eventBefore != null && !eventBefore.sentAt.isSameDayAs(message.sentAt)) || (eventBefore == null)) {
            messageList.add(
                DateHeader(
                    sentAt = message.sentAt,
                    context = this
                )
            )
        }
        messageList.add(message)
        binding.rvMessage.scrollToPosition(messageList.size - 1)
    }

    private fun sendMessage(message: String) {
        val messageId = setMessage(friendId = friendId).push().key
        checkNotNull(messageId)
        val messageMap = Message(
            message = message,
            senderId = currentUid,
            messageId = messageId
        )
        setMessage(friendId = friendId)
            .child(messageId)
            .setValue(messageMap)
            .addOnSuccessListener {
                Log.d(TAG, "Message Sent Successfully")
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Message Sent Error : ${exception.message}")
            }

        // To update the last message which we see inside the inbox
        updateLastMessage(messageMap, currentUid)

    }

    private fun updateLastMessage(messageMap: Message, currentUid: String) {
        val inboxMap = Inbox(
            message = messageMap.message,
            from = currentUid,
            to = friendId,
            name = friendName,
            image = friendImage,
            count = 0
        )

        setInbox(toUser = currentUid, fromUser = friendId)
            .setValue(inboxMap)
            .addOnSuccessListener {
                Log.d(TAG, "Success ")
            }
            .addOnFailureListener { error ->
                Log.d(TAG, "Error : ${error.message}")
            }

        setInbox(toUser = friendId, fromUser = currentUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.getValue(Inbox::class.java)
                    Log.d(TAG, "Value : $value")
                    Log.d(TAG, "Current UID : $currentUid")
                    inboxMap.apply {
                        from = friendId
                        to = currentUid
                        name = currentUser.name
                        image = currentUser.imageUrl
                        count = 1
                    }

                    value?.let { inbox ->
                        if (inbox.to == currentUid) {
                            inboxMap.count = value.count + 1
                            Log.d(TAG, "Inside this")
                        }
                    }
                    setInbox(toUser = friendId, fromUser = currentUid)
                        .setValue(inboxMap)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "Error : ${error.message}")
                }
            })
    }

    private fun markAsRead() {
        setInbox(toUser = currentUid, fromUser = friendId).child("count").setValue(0)
    }

    private fun setMessage(friendId: String): DatabaseReference {
        return database.reference.child("messages/${getId(friendId)}")
    }

    /**
     * Here we are maintaining the reference for the chat between
     * Person 1 and Person 2
     * So Lets say person 1 sends message to person 2 then
     * Inside the inbox of person 1 we will see person 2
     * Inside the inbox of person 2 we will see person 1
     * */

    private fun setInbox(toUser: String, fromUser: String): DatabaseReference {
        return database.reference.child("chats/${toUser}/${fromUser}")
    }


    /**
     * Person 1 = Uid1
     * Person 2 = Uid2
     * For Creating ChatRoom Between this two we are creating a unique
     * Id by using the Uid of both the person
     * */
    private fun getId(friendId: String): String {
        return if (friendId > currentUid) {
            currentUid + friendId
        } else {
            friendId + currentUid
        }
    }

    override fun onResume() {
        super.onResume()
        binding.root.viewTreeObserver.addOnGlobalLayoutListener(keyboardVisibilityHelper.visibilityListener)
    }

    override fun onPause() {
        super.onPause()
        binding.root.viewTreeObserver.addOnGlobalLayoutListener(keyboardVisibilityHelper.visibilityListener)
    }
}