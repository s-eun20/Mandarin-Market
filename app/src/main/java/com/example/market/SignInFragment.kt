package com.example.market

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class SignInFragment : Fragment() {
    private var auth: FirebaseAuth? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)

        FirebaseApp.initializeApp(requireContext())
        auth = FirebaseAuth.getInstance()

        val signInId = view.findViewById<EditText>(R.id.sign_in_id)
        val signInPassword = view.findViewById<EditText>(R.id.sign_in_password)
        val buttonSignIn = view.findViewById<Button>(R.id.sign_in)

        buttonSignIn.setOnClickListener {
            val id = signInId.text.toString()
            val password = signInPassword.text.toString()
            auth?.signInWithEmailAndPassword(id, password)
                ?.addOnCompleteListener(requireActivity(), OnCompleteListener<AuthResult> { task ->
                    if (task.isSuccessful) {
                        // 로그인 성공
                        Toast.makeText(requireContext(), "로그인 성공", Toast.LENGTH_SHORT).show()

                        // ListFragment로 이동
                        val transaction: FragmentTransaction =
                            requireFragmentManager().beginTransaction()
                        transaction.replace(R.id.fragment_container, ListFragment())
                        transaction.addToBackStack(null)
                        transaction.commit()
                    } else {
                        // 로그인 실패
                        Toast.makeText(requireContext(), "로그인 실패", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        return view
    }
}
