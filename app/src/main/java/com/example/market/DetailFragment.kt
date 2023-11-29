package com.example.market

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.example.market.databinding.FragmentDetailBinding
import com.example.market.model.Product

@Suppress("DEPRECATION")
class DetailFragment : BaseFragment() {
    companion object {
        fun getInstance(product: Product): DetailFragment {
            return DetailFragment().apply {
                arguments = bundleOf("product" to product)
            }
        }
    }

    private var _product: Product? = null
    private val product get() = _product!!

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!


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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            titleTextView.text = product.title
            priceTextView.text = product.price + "원"
            contentTextView.text = product.content
            sellTextView.text = product.sell

            val sellerName = product.name
            personTextView.text = "$sellerName"

            Glide.with(requireContext())
                .load(product.imageUrl)
                .into(imageView3)

            button3.setOnClickListener {
                ChatFragment.getInstance(product)?.let {
                    replaceFragment(it, "ChatFragment")
                }
            }
        }
    }
}
