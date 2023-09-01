package se.rebeccazadig.bokholken.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import se.rebeccazadig.bokholken.R
import se.rebeccazadig.bokholken.data.ContactType
import se.rebeccazadig.bokholken.databinding.FragmentLoginBinding

class LoginRegisterFragment : Fragment() {

    private val viewModel: LoginRegisterViewModel by viewModels()
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false).apply {
            lifecycleOwner = this@LoginRegisterFragment
            vm = viewModel
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTextChangeListener()
        observeViewModelChanges()
        handleCheckedChange()
    }

    private fun handleCheckedChange() {
        when (binding.contactMethodRadioGroup.checkedRadioButtonId) {
            R.id.radioBtnEmail -> viewModel.preferredContactMethod.value = ContactType.EMAIL
            R.id.radioBtnPhone -> viewModel.preferredContactMethod.value = ContactType.PHONE
        }

        binding.contactMethodRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioBtnEmail -> viewModel.preferredContactMethod.value = ContactType.EMAIL
                R.id.radioBtnPhone -> viewModel.preferredContactMethod.value = ContactType.PHONE
            }
        }

        viewModel.validatePhoneNumber(binding.phoneNumber.text.toString().trim())
    }

    private fun setupTextChangeListener() {
        binding.phoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                viewModel.phoneNumber.value = s.toString().trim()
                viewModel.validatePhoneNumber(s.toString().trim())
            }
        })
    }

    private fun observeViewModelChanges() {
        viewModel.contactValidationResult.observe(viewLifecycleOwner) { validationResult ->
            binding.phoneNumber.error = validationResult
        }

        viewModel.loginUiState.observe(viewLifecycleOwner) { loginUiState ->
            Log.d("Emma", "loginUiState: $loginUiState")
            handleLoginUiState(loginUiState)
        }
    }

    private fun handleLoginUiState(loginUiState: LoginUiState) {
        if (loginUiState.isSuccess) {
            findNavController().navigate(R.id.advertsFragment)
        } else {
            loginUiState.errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }
}