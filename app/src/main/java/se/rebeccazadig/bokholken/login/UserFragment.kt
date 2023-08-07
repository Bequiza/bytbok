package se.rebeccazadig.bokholken.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import se.rebeccazadig.bokholken.databinding.FragmentUserBinding

class UserFragment : Fragment() {

    private val viewModel: UserViewModel by viewModels()
    private lateinit var binding: FragmentUserBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentUserBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.vm = viewModel
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.saveButton.setOnClickListener {
//            var editUserName = view.findViewById<EditText>(R.id.editNameET).text.toString()
//            var editContact = view.findViewById<EditText>(R.id.editContactET).text.toString()
//            var editCity = view.findViewById<EditText>(R.id.editCityET).text.toString()
//
//            val database = Firebase.database
//            val myRef = database.getReference("EditMinSida")
//
//            var someChanges = User(editUserName, editContact, editCity)
//            myRef.push().setValue(someChanges)
//        }

        viewModel.uiStateSave.observe(viewLifecycleOwner) { uiStateSave ->

            uiStateSave.message?.let/*om allt till vänster om ?+.let inte null, visa toast*/ {
                Toast.makeText(requireContext(), "${uiStateSave.message}", Toast.LENGTH_LONG).show()
            }
        }
        viewModel.uiStateSave.observe(viewLifecycleOwner) { uiStateSave ->

            uiStateSave.message?.let/*om allt till vänster om ?+.let inte null, visa toast*/ {
                Toast.makeText(requireContext(), "${uiStateSave.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
