package se.rebeccazadig.bokholken.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import se.rebeccazadig.bokholken.R
import se.rebeccazadig.bokholken.data.ContactType
import se.rebeccazadig.bokholken.databinding.FragmentEditUserProfileBinding
import se.rebeccazadig.bokholken.utils.navigateBack

class EditUserProfileFragment : Fragment() {

    private val viewModel: UserViewModel by viewModels()
    private var _binding: FragmentEditUserProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEditUserProfileBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.vm = viewModel
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editUserToolbar.setNavigationOnClickListener { navigateBack() }

        viewModel.preferredContactMethod.observe(viewLifecycleOwner) {
            handleCheckedChange()
        }

        viewModel.initializeUserData()

        setupTextChangeListener()
        observeViewModelChanges()

        viewModel.validatePhoneNumber(binding.phoneNumberEditTextInput.text.toString().trim())
    }

    private fun getContactTypeFromRadioButtonId(id: Int): ContactType? {
        return when (id) {
            R.id.radioBtnEmail -> ContactType.EMAIL
            R.id.radioBtnPhone -> ContactType.PHONE
            else -> null
        }
    }

    private fun setupTextChangeListener() {
        binding.phoneNumberEditTextInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                viewModel.userContact.value = s.toString().trim()
                viewModel.validatePhoneNumber(s.toString().trim())
            }
        })
    }

    private fun handleCheckedChange() {
        when (viewModel.preferredContactMethod.value) {
            ContactType.EMAIL -> binding.radioBtnEmail.isChecked = true
            ContactType.PHONE -> binding.radioBtnPhone.isChecked = true
            else -> {
                ContactType.UNKNOWN
            }
        }

        binding.contactMethodRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            viewModel.preferredContactMethod.value = getContactTypeFromRadioButtonId(checkedId)
        }
    }

    private fun observeViewModelChanges() {
        viewModel.contactValidationResult.observe(viewLifecycleOwner) { validationResult ->
            binding.phoneNumberEditTextInput.error = validationResult
        }
    }
}