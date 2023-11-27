package com.example.market

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.market.databinding.FragmentChatBinding
import com.example.market.databinding.ItemChatMyMessageBinding
import com.example.market.databinding.ItemChatYourMessageBinding
import com.example.market.model.ChatMessage
import com.example.market.model.ChatRoom
import com.example.market.model.Product
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import java.util.Date

@Suppress("DEPRECATION")
class ChatFragment : BaseFragment() {
    companion object {
        fun getInstance(product: Product): ChatFragment? {
            val auth = Firebase.auth

            val participants = listOf(product.userId, auth.uid)
                .mapNotNull { it }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()

            if (participants.size != 2) return null

            val roomId = "${product.documentId}${participants.joinToString(separator = "")}"
            val names = hashMapOf(
                product.userId to product.name,
                auth.uid!! to (auth.currentUser?.email ?: "")
            )

            val room = ChatRoom(roomId, product.documentId, participants, names)
            return getInstance(room)
        }

        fun getInstance(room: ChatRoom): ChatFragment {
            return ChatFragment().apply {
                arguments = bundleOf("room" to room)
            }
        }
    }

    private var _room: ChatRoom? = null
    private val room get() = _room!!

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { Firebase.auth }
    private val db by lazy { Firebase.firestore }

    private val messages by lazy {
        db.collection("chats")
            .document(room.documentId)
            .collection("messages")
            //.orderBy("timestamp", Query.Direction.DESCENDING)
            .snapshots()
            .mapLatest { it.documents.mapNotNull { it.toObject(ChatMessage::class.java) } }
    }
    private val adapter by lazy { ChatAdapter(auth.uid!!) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _room = arguments?.getParcelable("room")
        if (savedInstanceState != null) {
            _room = savedInstanceState.getParcelable("room")
        }

        assert(_room != null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable("room", room)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUi()
        bindData()
    }

    private fun initUi() = with(binding) {
        chatRecyclerView.adapter = adapter

        sendBtn.setOnClickListener { sendMessage() }
    }

    private fun bindData() {
        lifecycleScope.launch {
            messages.collectLatest {
                adapter.submitList(it)
            }
        }
    }

    private fun sendMessage() {
        hideKeyboard()

        val message = binding.messageEdit.text.toString().trim()
        if (message.isEmpty()) {
            showToastMessage("메시지를 입력해주세요.")
        }

        binding.messageEdit.setText("")

        val chatMessage =
            ChatMessage("", auth.uid!!, auth.currentUser!!.email ?: "", message, Date())
        val chatRoom = ChatRoom(
            room.documentId,
            room.productId,
            room.participants,
            room.names,
            chatMessage.userId,
            chatMessage.userName,
            chatMessage.message,
            //chatMessage.timestamp
        )

        val roomReference = db.collection("chats").document(room.documentId)

        db.runBatch {
            it.set(roomReference, chatRoom)
            it.set(roomReference.collection("messages").document(), chatMessage)
        }
    }


    private class ChatAdapter(val uid: String) :
        ListAdapter<ChatMessage, RecyclerView.ViewHolder>(diffUtil) {

        override fun getItemViewType(position: Int): Int {
            return if (getItem(position).userId == uid) 0 else 1
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val context = parent.context
            val inflater = LayoutInflater.from(context)

            if (viewType == 0) {
                val binding = ItemChatMyMessageBinding.inflate(inflater, parent, false).apply {
                    dateContainer.isVisible = false
                    //timeTextView.isVisible = false
                }

                return MyMessageItemViewHolder(binding)

            } else {
                val binding = ItemChatYourMessageBinding.inflate(inflater, parent, false).apply {
                    dateContainer.isVisible = false
                    //timeTextView.isVisible = false
                }

                return YourMessageItemViewHolder(binding)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val context = holder.itemView.context
            val item = currentList[position]

            if (holder is MyMessageItemViewHolder) {
                with(holder.binding) {

                    messageTextView.text = item.message
                }
            } else if (holder is YourMessageItemViewHolder) {
                with(holder.binding) {
                    messageTextView.text = item.message

                }
            }
        }

        class MyMessageItemViewHolder(val binding: ItemChatMyMessageBinding) :
            RecyclerView.ViewHolder(binding.root)

        class YourMessageItemViewHolder(val binding: ItemChatYourMessageBinding) :
            RecyclerView.ViewHolder(binding.root)

        companion object {
            var diffUtil: DiffUtil.ItemCallback<ChatMessage> =
                object : DiffUtil.ItemCallback<ChatMessage>() {
                    override fun areItemsTheSame(
                        oldItem: ChatMessage,
                        newItem: ChatMessage
                    ): Boolean {
                        return oldItem.documentId == newItem.documentId
                    }

                    override fun areContentsTheSame(
                        oldItem: ChatMessage,
                        newItem: ChatMessage
                    ): Boolean {
                        return oldItem.message == newItem.message

                    }
                }
        }
    }
}