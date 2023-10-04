package se.rebeccazadig.bokholken.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import se.rebeccazadig.bokholken.R
import se.rebeccazadig.bokholken.data.ContactType
import se.rebeccazadig.bokholken.databinding.FragmentEditUserProfileBinding
import se.rebeccazadig.bokholken.main.MainActivity
import se.rebeccazadig.bokholken.utils.navigateBack
import se.rebeccazadig.bokholken.utils.showToast

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

        (activity as? MainActivity)?.hideBottomNavBar()

        binding.editUserToolbar.setNavigationOnClickListener { navigateBack() }


        if (savedInstanceState == null) {
            viewModel.initializeUserData()
        }

        viewModel.preferredContactMethod.observe(viewLifecycleOwner) { handleCheckedChange() }
        setupTextChangeListener()
        observeViewModelChanges()
    }

    private fun getContactTypeFromRadioButtonId(id: Int): ContactType? {
        return when (id) {
            R.id.radioBtnEmail -> ContactType.EMAIL
            R.id.radioBtnPhone -> ContactType.PHONE
            else -> null
        }
    }

    private fun setupTextChangeListener() {
        binding.phoneNumberEditTextInput.addTextChangedListener(afterTextChanged = { s ->
            val trimmedText = s.toString().trim()
            viewModel.userPhoneNumber.value = trimmedText
            viewModel.validatePhoneNumber(trimmedText)
        })

        binding.contactMethodRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            viewModel.preferredContactMethod.value = getContactTypeFromRadioButtonId(checkedId)
        }
    }

    private fun handleCheckedChange() {
        when (viewModel.preferredContactMethod.value) {
            ContactType.EMAIL -> binding.radioBtnEmail.isChecked = true
            ContactType.PHONE -> binding.radioBtnPhone.isChecked = true
            else -> {}
        }
    }

    private fun observeViewModelChanges() {
        viewModel.run {
            userPhoneNumber.observe(viewLifecycleOwner) { phone ->
                phone?.let { validatePhoneNumber(it) }
            }

            isButtonDisabled.observe(viewLifecycleOwner) { isDisabled ->
                binding.editUserButton.isEnabled = !isDisabled
            }

            phoneNumberValidationResult.observe(viewLifecycleOwner) { validationResult ->
                binding.phoneNumberEditTextInput.error = validationResult
            }
            uiStateSave.observe(viewLifecycleOwner) { uiStateSave ->
                uiStateSave?.message?.let { message->
                    showToast(message)
                }
            }
        }
    }
}