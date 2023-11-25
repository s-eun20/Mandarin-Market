package com.example.market

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.market.databinding.FragmentChatListBinding
import com.example.market.databinding.ItemChatRoomBinding
import com.example.market.model.ChatRoom
import com.example.market.model.Product
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ChatListFragment : BaseFragment() {
    companion object {
        fun getInstance(product: Product) = ChatListFragment().apply {
            arguments = bundleOf("product" to product)
        }
    }

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { Firebase.auth }
    private val db by lazy { Firebase.firestore }

    private var _product: Product? = null
    private val product get() = _product!!

    private val rooms by lazy {
        db.collection("chats")
            .whereEqualTo("productId", product.documentId)
            .whereArrayContains("participants", auth.uid!!)
            //.orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .snapshots()
            .mapLatest { it.documents.mapNotNull { it.toObject(ChatRoom::class.java) } }
    }

    private val adapter by lazy {
        ChatRoomAdapter().apply {
            onItemClickListener = {
                replaceFragment(ChatFragment.getInstance(it), "ChatFragment")
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _product = arguments?.getParcelable("product")
        if (savedInstanceState != null) {
            _product = savedInstanceState.getParcelable("product")
        }

        assert(_product != null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable("product", product)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUi()
        bindData()
    }

    private fun initUi() = with(binding) {
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                LinearLayoutManager.VERTICAL
            )
        )

        recyclerView.adapter = adapter
    }

    private fun bindData() {
        lifecycleScope.launch {
            rooms.collectLatest {
                adapter.submitList(it)
            }
        }
    }

    private class ChatRoomAdapter : ListAdapter<ChatRoom, ChatRoomAdapter.ChatRoomItemViewHolder>(
        diffUtil
    ) {
        private val auth by lazy { Firebase.auth }

        var onItemClickListener: ((ChatRoom) -> Unit)? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemChatRoomBinding.inflate(layoutInflater)
            return ChatRoomItemViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ChatRoomItemViewHolder, position: Int) {
            val item = getItem(position)

            with(holder.binding) {
                root.setOnClickListener { onItemClickListener?.invoke(item) }

                nameTextView.text = item.names.filterNot { it.key == auth.uid }.values.firstOrNull()
                lastMessageTextView.text = item.lastMessageContent

            }
        }

        class ChatRoomItemViewHolder(val binding: ItemChatRoomBinding) :
            RecyclerView.ViewHolder(binding.root)

        companion object {
            val diffUtil = object : DiffUtil.ItemCallback<ChatRoom>() {
                override fun areItemsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
                    return oldItem.documentId == newItem.documentId
                }

                override fun areContentsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
                    return oldItem.lastMessageContent == newItem.lastMessageContent
                }
            }
        }
    }
}