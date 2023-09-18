package se.rebeccazadig.bokholken.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import se.rebeccazadig.bokholken.R
import se.rebeccazadig.bokholken.data.ContactType
import se.rebeccazadig.bokholken.data.ErrorCode
import se.rebeccazadig.bokholken.databinding.DialogResetPasswordBinding
import se.rebeccazadig.bokholken.databinding.FragmentLoginBinding
import se.rebeccazadig.bokholken.utils.DialogMessages
import se.rebeccazadig.bokholken.utils.showAlertWithEditText
import se.rebeccazadig.bokholken.utils.showConfirmationDialog
import se.rebeccazadig.bokholken.utils.showOneButtonDialog

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

        setupListeners()
        observeViewModelChanges()
    }

    private fun setupListeners() {
        binding.apply {
            resetPasswordText.setOnClickListener {
                showResetPasswordDialog { email ->
                    viewModel.resetPassword(email)
                }
            }
            contactMethodRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                val contactType = when (checkedId) {
                    R.id.radioBtnEmail -> ContactType.EMAIL
                    R.id.radioBtnPhone -> ContactType.PHONE
                    else -> return@setOnCheckedChangeListener
                }
                viewModel.preferredContactMethod.value = contactType
            }
            phoneNumber.addTextChangedListener(afterTextChanged = { s ->
                val trimmedText = s.toString().trim()
                viewModel.phoneNumber.value = trimmedText
                viewModel.validatePhoneNumber(trimmedText)
            })
        }
    }

    private fun observeViewModelChanges() {
        viewModel.run {
            passwordResetResult.observe(viewLifecycleOwner, ::handlePasswordResetResult)
            contactValidationResult.observe(viewLifecycleOwner) {
                binding.phoneNumber.error = it
            }
            loginUiState.observe(viewLifecycleOwner, ::handleLoginUiState)
        }
    }

    private fun handleLoginUiState(loginUiState: LoginUiState) {
        if (loginUiState.isSuccess) {
            findNavController().navigate(R.id.advertsFragment)
        } else {
            loginUiState.errorMessage?.let { errorCode ->
                val errorMessageResId = ErrorCode.fromFirebaseCode(errorCode).errorMessageResId
                showErrorDialog(errorMessageResId) {
                    viewModel.email.value = ""
                    viewModel.password.value = ""
                }
            }
        }
    }

    private fun handlePasswordResetResult(result: Result<Unit>) {
        when (result) {
            is Result.Success -> showSuccessDialog()
            is Result.Failure -> {
                val errorCode = ErrorCode.fromFirebaseCode(result.message)
                val errorMessageResId = errorCode.errorMessageResId

                showErrorDialog(errorMessageResId) {
                    showResetPasswordDialog { email ->
                        viewModel.resetPassword(email)
                    }
                }
            }
        }
    }

    private fun showSuccessDialog() {
        viewModel.inProgress.value = false
        showOneButtonDialog(
            context = requireContext(),
            titleResId = R.string.success_title,
            messageResId = R.string.password_reset_success_message,
            positiveButtonTextResId = R.string.ok_dialog_button
        ) {}
    }

    private fun showErrorDialog(
         errorMessageResId: Int,
        retryAction: () -> Unit
    ) {
        viewModel.inProgress.value = false
        showConfirmationDialog(
            context = requireContext(),
            titleResId = R.string.error_title,
            messageResId = errorMessageResId,
            positiveAction = retryAction,
            positiveButtonTextResId = R.string.retry_button
        )
    }

    private fun showResetPasswordDialog(onEmailProvided: (email: String) -> Unit) {
        val binding = DialogResetPasswordBinding.inflate(layoutInflater)

        showAlertWithEditText(
            context = requireContext(),
            view = binding.root,
            editTexts = listOf(binding.emailTextInput),
            dialogMessages = DialogMessages(
                titleText = R.string.password_reset_dialog_title,
                messageResId = R.string.password_reset_dialog_description,
                positiveButtonText = R.string.submit_button_label,
                negativeButtonText = R.string.cancel
            ),
            confirmCallback = {
                onEmailProvided(
                    binding.emailTextInput.text.toString().trim()
                )
            },
            cancelCallback = {
                viewModel.inProgress.value = false
            }
        ) { _, positiveButton ->
            viewModel.inProgress.value = true
            val email = binding.emailTextInput.text.toString().trim()
            positiveButton.isEnabled = email.isNotEmpty()
        }
    }
}