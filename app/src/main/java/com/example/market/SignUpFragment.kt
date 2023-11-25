package com.example.market

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class SignUpFragment : Fragment() {
    private var auth: FirebaseAuth? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val signupId = view.findViewById<EditText>(R.id.sign_up_id)
        val signupPassword = view.findViewById<EditText>(R.id.sign_up_password)

        val signUpButton = view.findViewById<Button>(R.id.sign_up)
        signUpButton.setOnClickListener {
            val id = signupId.text.toString()
            val password = signupPassword.text.toString()

            auth?.createUserWithEmailAndPassword(id, password)
                ?.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // 등록 성공
                        val user = auth?.currentUser
                        // ListFragment로 전환
                        val transaction =
                            requireActivity().supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.fragment_container, ListFragment())
                        transaction.addToBackStack(null)
                        transaction.commit()
                    } else {
                        // 등록 실패
                        // 에러 처리
                    }
                }
        }
    }
}
