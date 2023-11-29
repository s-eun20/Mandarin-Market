// MyPageFragment.kt
package com.example.market

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.market.databinding.FragmentMyPageBinding
import com.example.market.databinding.ListItemBinding
import com.example.market.model.Product
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MyPageFragment : BaseFragment() {
    private var _binding: FragmentMyPageBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { Firebase.auth }
    private val db by lazy { Firebase.firestore }
    private val myProducts by lazy { db.collection("products").whereEqualTo("userId", auth.uid).snapshots() }

    private val adapter by lazy { MyProductAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            myRecyclerView.adapter = adapter.apply {
                onItemClickListener = {
                    replaceFragment(EditFragment.getInstance(it), "EditFragment")
                }

                onChatListClickListener = {
                    replaceFragment(ChatListFragment.getInstance(it), "ChatListFragment")
                }
            }
        }

        loadMyProducts()
    }

    private fun loadMyProducts() {
        lifecycleScope.launch {
            myProducts.collectLatest {
                adapter.submitList(it.documents.mapNotNull { doc ->
                    doc.toObject(Product::class.java)
                })
            }
        }
    }

    private class MyProductAdapter() : ListAdapter<Product, MyProductAdapter.ViewHolder>(diffUtil) {
        var onItemClickListener: ((Product) -> Unit)? = null
        var onChatListClickListener: ((Product) -> Unit)? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding =
                ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val product = getItem(position)

            with(holder.binding) {
                root.setOnClickListener {
                    onItemClickListener?.invoke(product)
                }

                chatListButton.setOnClickListener {
                    onChatListClickListener?.invoke(product)
                }

                Glide.with(root.context)
                    .load(product.imageUrl)
                    .into(imageView)

                titleTextView.text = product.title
                priceTextView.text = product.price + "원"
                sellTextView.text = product.sell
            }
        }

        class ViewHolder(val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root)

        companion object {
            val diffUtil = object : DiffUtil.ItemCallback<Product>() {
                override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
                    return oldItem.documentId == newItem.documentId
                }

                override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
                    return oldItem.price == newItem.price &&
                            oldItem.sell == newItem.sell
                }
            }
        }
    }
}
