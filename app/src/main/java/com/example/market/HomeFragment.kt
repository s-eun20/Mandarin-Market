package com.example.market

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction

@Suppress("DEPRECATION")
class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val homeSignUpButton = view.findViewById<Button>(R.id.home_sign_up)
        homeSignUpButton.setOnClickListener {
            replaceFragment(SignUpFragment())
        }

        val homeSignInButton = view.findViewById<Button>(R.id.home_sign_in)
        homeSignInButton.setOnClickListener {
            replaceFragment(SignInFragment())
        }

        return view
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
