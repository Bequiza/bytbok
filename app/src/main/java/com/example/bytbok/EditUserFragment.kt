package com.example.bytbok

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class EditUserFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.sparaÄndringarButton).setOnClickListener {

            var editUserName = view.findViewById<EditText>(R.id.editAnvandarNamnET).text.toString()
            var editKontakt = view.findViewById<EditText>(R.id.editKontaktsattET).text.toString()
            var editStad = view.findViewById<EditText>(R.id.editStadET).text.toString()

            val database = Firebase.database
            val myRef = database.getReference("EditMinSida")

            var someChanges = RedigeraSida(editUserName, editKontakt, editStad)
            myRef.push().setValue(someChanges)


            //val action = AnnonsFragmentDirections.actionAnnonsFragmentToSkapaAnnonsFragment()
            //findNavController().navigate(R.id.action_editUserFragment_to_minSidaFragment)

        }
    }
}
