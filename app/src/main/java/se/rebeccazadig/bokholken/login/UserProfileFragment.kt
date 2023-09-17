package se.rebeccazadig.bokholken.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import se.rebeccazadig.bokholken.R
import se.rebeccazadig.bokholken.databinding.DialogCredentialsBinding
import se.rebeccazadig.bokholken.databinding.FragmentUserProfileBinding
import se.rebeccazadig.bokholken.utils.DialogMessages
import se.rebeccazadig.bokholken.utils.navigateBack
import se.rebeccazadig.bokholken.utils.showAlertWithEditText
import se.rebeccazadig.bokholken.utils.showConfirmationDialog
import se.rebeccazadig.bokholken.utils.showToast

class UserProfileFragment : Fragment() {

    private val viewModel: UserViewModel by viewModels()
    private var _binding: FragmentUserProfileBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentUserProfileBinding.inflate(layoutInflater, container, false)
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
        binding.profileToolbar.setNavigationOnClickListener {
            navigateBack()
        }

        viewModel.fetchUserData()

        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.userNameTV.text = getString(R.string.user_details_username, user.name)
                binding.contactTV.text = getString(R.string.user_details_contact, user.contact)
            }
        }

        binding.editUserButton.setOnClickListener {
            findNavController().navigate(
                UserProfileFragmentDirections.actionUserProfileFragmentToEditUserProfileFragment()
            )
        }

        binding.myAdvertsButton.setOnClickListener {
            findNavController().navigate(
                UserProfileFragmentDirections.actionUserProfileFragmentToMyAdvertsFragment()
            )
        }

        binding.profileToolbar.inflateMenu(R.menu.menu_user)
        binding.profileToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.logout -> {
                    showConfirmationDialog(
                        context = requireContext(),
                        titleResId = R.string.logout_title,
                        messageResId = R.string.logout_message,
                        positiveAction = {
                            viewModel.logOutInVm()
                            findNavController().navigate(R.id.action_to_login_nav_graph)
                        }
                    )
                    true
                }
                R.id.delete_account -> {
                    showConfirmationDialog(
                        context = requireContext(),
                        titleResId = R.string.delete_account_title,
                        messageResId = R.string.delete_account_message,
                        positiveAction = {
                            showCredentialsDialog { email, password ->
                                viewModel.deleteAccountInVM(email, password)
                            }
                        }
                    )
                    true
                }
                else -> false
            }
        }

        viewModel.uiStateSave.observe(viewLifecycleOwner) { uiStateSave ->
            uiStateSave.message?.let {
                showToast("${uiStateSave.message}")

                if (it == getString(R.string.account_deleted)) {
                    findNavController().navigate(R.id.action_to_login_nav_graph)
                }
                viewModel.nullUiStateSave()
            }
        }
    }

    private fun showCredentialsDialog(onCredentialsProvided: (email: String, password: String) -> Unit) {
        val binding = DialogCredentialsBinding.inflate(layoutInflater)

        showAlertWithEditText(
            context = requireContext(),
            view = binding.root,
            editTexts = listOf(binding.emailTextInput, binding.passwordTextInput),
            dialogMessages = DialogMessages(
                titleText = R.string.credentials_dialog_title,
                positiveButtonText = R.string.submit_button_label,
                negativeButtonText = R.string.cancel
            ),
            confirmCallback = {
                onCredentialsProvided(
                    binding.emailTextInput.text.toString().trim(),
                    binding.passwordTextInput.text.toString().trim()
                )
            }
        ) { _, positiveButton ->
            val email = binding.emailTextInput.text.toString().trim()
            val password = binding.passwordTextInput.text.toString().trim()
            positiveButton.isEnabled = email.isNotEmpty() && password.isNotEmpty()
        }
    }
}