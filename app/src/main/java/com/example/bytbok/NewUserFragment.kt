package com.example.bytbok

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class NewUserFragment : Fragment() {

    private lateinit var auth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth

        view.findViewById<Button>(R.id.newUserRegisterbutton).setOnClickListener {

           // val userName = view.findViewById<TextView>(R.id.newUserET).text.toString()
            val userEmail = view.findViewById<TextView>(R.id.newUserEmailET).text.toString()
            val userPassword = view.findViewById<TextView>(R.id.newUserPasswordET).text.toString()

            auth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        //Toast.makeText(requireContext(), "Registrering ok", Toast.LENGTH_SHORT).show()

                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(requireContext(), "Fel vid registrering", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}