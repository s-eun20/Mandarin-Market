package com.example.market

import android.content.Context.INPUT_METHOD_SERVICE
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment

open class BaseFragment : Fragment() {
    protected fun replaceFragment(fragment: Fragment, tag: String) {
        requireActivity().supportFragmentManager.run {
            if (findFragmentByTag(tag) != null) return

            beginTransaction()
                .replace(R.id.fragment_container2, fragment, tag)
                .addToBackStack(null)
                .commit()
        }
    }

    protected fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    protected fun showToastMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    protected fun hideKeyboard() {
        val imm = requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus ?: View(requireContext())
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}