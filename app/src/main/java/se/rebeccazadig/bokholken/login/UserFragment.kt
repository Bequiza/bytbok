package se.rebeccazadig.bokholken.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import se.rebeccazadig.bokholken.R

class UserFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.sparaÄndringarButton).setOnClickListener {
            var editUserName = view.findViewById<EditText>(R.id.editAnvandarNamnET).text.toString()
            var editContact = view.findViewById<EditText>(R.id.editKontaktsattET).text.toString()
            var editCity = view.findViewById<EditText>(R.id.editStadET).text.toString()

            val database = Firebase.database
            val myRef = database.getReference("EditMinSida")

            var someChanges = User(editUserName, editContact, editCity)
            myRef.push().setValue(someChanges)
        }
    }
}
