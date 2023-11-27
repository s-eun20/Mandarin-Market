package com.example.market

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.example.market.databinding.FragmentEditBinding
import com.example.market.model.Product
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EditFragment : BaseFragment() {
    companion object {
        fun getInstance(product: Product): EditFragment {
            return EditFragment().apply {
                arguments = bundleOf("product" to product)
            }
        }
    }

    private var _product: Product? = null
    private val product get() = _product!!

    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!

    private val firestore by lazy { Firebase.firestore }


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
        _binding = FragmentEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            priceTextView2.hint = product.price
            sellTextView2.hint = product.sell

            button4.setOnClickListener {
                updateProduct()
            }
        }
    }

    private fun updateProduct() {
        var modifiedPrice = binding.priceTextView2.text.toString()
        var modifiedSellStatus = binding.sellTextView2.text.toString()

        if (modifiedPrice.isBlank()) {
            modifiedPrice = product.price.orEmpty()
        }
        if (modifiedSellStatus.isBlank()) {
            modifiedSellStatus = product.sell.orEmpty()
        }

        val productRef = firestore.collection("products").document(product.documentId)
        productRef.update(
            mapOf(
                "price" to modifiedPrice,
                "sell" to modifiedSellStatus
            )
        )

        onBackPressed()
    }
}
