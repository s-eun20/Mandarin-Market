package com.example.market

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.market.databinding.FragmentListBinding
import com.example.market.databinding.ListItemBinding
import com.example.market.model.Product
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ListFragment : BaseFragment() {
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { Firebase.auth }
    private val db by lazy { Firebase.firestore }
    private val products by lazy { db.collection("products").snapshots() }

    private val adapter by lazy { ProductAdapter() }

    private lateinit var spinner: Spinner
    private var selectedFilter: String = "전체"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            btnWrite.setOnClickListener {
                replaceFragment(WriteFragment(), "WriteFragment")
            }

            recyclerView.adapter = adapter.apply {
                onItemClickListener = {
                    if (it.userId == auth.uid) {
                        replaceFragment(EditFragment.getInstance(it), "EditFragment")
                    } else {
                        replaceFragment(DetailFragment.getInstance(it), "DetailFragment")
                    }
                }

                onChatListClickListener = {
                    replaceFragment(ChatListFragment.getInstance(it), "ChatListFragment")
                }
            }

            // Spinner 설정
            spinner = spinnerFilter
            val filterArray = resources.getStringArray(R.array.itemList)
            val spinnerAdapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filterArray)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = spinnerAdapter

            // Spinner 선택 리스너 설정
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedFilter = filterArray[position]
                    loadProductsWithFilter()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // 아무 작업도 하지 않음
                }
            }
        }

        loadProducts()
    }

    private fun loadProducts() {
        lifecycleScope.launch {
            products.collectLatest {
                adapter.submitList(it.documents.mapNotNull { it.toObject(Product::class.java) })
            }
        }
    }

    private fun loadProductsWithFilter() {
        lifecycleScope.launch {
            // Filtered query based on the selected filter
            val query = when (selectedFilter) {
                "전체" -> db.collection("products")
                // Add more cases based on your filter options

                else -> {
                    // Handle any other filter options
                    db.collection("products").whereEqualTo("sell", selectedFilter)
                }
            }

            query.snapshots().collectLatest {
                adapter.submitList(it.documents.mapNotNull { it.toObject(Product::class.java) })
            }
        }
    }

    private class ProductAdapter() : ListAdapter<Product, ProductAdapter.ViewHolder>(diffUtil) {
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
