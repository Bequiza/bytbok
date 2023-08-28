package se.rebeccazadig.bokholken.login

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
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
import se.rebeccazadig.bokholken.data.Contact
import se.rebeccazadig.bokholken.databinding.FragmentLoginBinding

class LoginRegisterFragment : Fragment() {

    private val viewModel: LoginRegisterViewModel by viewModels()
    private lateinit var binding: FragmentLoginBinding




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater, container, false).apply {
            lifecycleOwner = this@LoginRegisterFragment
            vm = viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRadioGroupListener()
        setupTextChangeListener()
        observeViewModelChanges()
    }

    private fun setupRadioGroupListener() {
        binding.contactMethodRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            binding.contact.text = null
            handleCheckedChange(checkedId)
        }
        handleCheckedChange(binding.contactMethodRadioGroup.checkedRadioButtonId)
    }

    private fun handleCheckedChange(checkedId: Int) {
        val contactType = when (checkedId) {
            R.id.radioBtnPhone -> {
                configureForPhoneNumber()
                Contact.ContactType.PHONE
            }
            R.id.radioBtnEmail -> {
                configureForEmail()
                Contact.ContactType.EMAIL
            }
            else -> {
                // Handle unexpected behavior, e.g., log an error or reset the UI.
                Contact.ContactType.UNKNOWN
            }
        }
        // This can be called here if you want to validate immediately on radio change.
        viewModel.validateContactInput(binding.contact.text.toString().trim(), contactType)
    }

    private fun configureForPhoneNumber() {
        binding.contactLayout.hint = "Mobilnummer"
        binding.contact.inputType = InputType.TYPE_CLASS_PHONE
        binding.contact.error = null
    }

    private fun configureForEmail() {
        binding.contactLayout.hint = "E-post"
        binding.contact.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        binding.contact.error = null
    }

    private fun setupTextChangeListener() {
        binding.contact.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                val contactType = when (binding.contactMethodRadioGroup.checkedRadioButtonId) {
                    R.id.radioBtnPhone -> Contact.ContactType.PHONE
                    R.id.radioBtnEmail -> Contact.ContactType.EMAIL
                    else -> Contact.ContactType.UNKNOWN
                }
                viewModel.validateContactInput(s.toString().trim(), contactType)
            }
        })
    }

    private fun observeViewModelChanges() {
        viewModel.contactValidationResult.observe(viewLifecycleOwner) { validationResult ->
            binding.contactLayout.error = validationResult
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