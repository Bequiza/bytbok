package se.rebeccazadig.bokholken.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import se.rebeccazadig.bokholken.databinding.FragmentLoginBinding

class LoginRegisterFragment : Fragment() {

    private val viewModel: LoginRegisterViewModel by viewModels() // viewmodel
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.vm = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->

            uiState.message?.let/*om allt till vänster om ?+.let inte null, visa toast*/ {
                Toast.makeText(requireContext(), "Hoppsan nått blev fel", Toast.LENGTH_LONG).show()
            }
        }
    }
}
