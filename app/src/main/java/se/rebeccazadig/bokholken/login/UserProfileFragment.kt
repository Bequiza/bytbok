package se.rebeccazadig.bokholken.login

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import se.rebeccazadig.bokholken.R
import se.rebeccazadig.bokholken.databinding.DialogCredentialsBinding
import se.rebeccazadig.bokholken.databinding.FragmentUserProfileBinding

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
                        title = getString(R.string.logout_title),
                        message = getString(R.string.logout_message),
                        positiveAction = {
                            viewModel.logOutInVm()
                            findNavController().navigate(R.id.action_to_login_nav_graph)
                        }
                    )
                    true
                }
                R.id.delete_account -> {
                    showConfirmationDialog(
                        title = getString(R.string.delete_account_title),
                        message = getString(R.string.delete_account_message),
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
                Toast.makeText(requireContext(), "${uiStateSave.message}", Toast.LENGTH_LONG).show()
                viewModel.nullUiStateSave()
            }
        }
    }

    private fun showConfirmationDialog(title: String, message: String, positiveAction: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                positiveAction.invoke()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()
            .show()
    }

    private fun showCredentialsDialog(onCredentialsProvided: (email: String, password: String) -> Unit) {
        val binding = DialogCredentialsBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.credentials_dialog_title))
            .setView(binding.root)
            .setPositiveButton(getString(R.string.submit_button_label), null)
            .setNegativeButton(getString(R.string.cancel_button_label)) { _, _ -> }
            .create()

        dialog.show()

        val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE).apply {
            isEnabled = false
            setOnClickListener {
                onCredentialsProvided(binding.emailEditText.text.toString().trim(), binding.passwordEditText.text.toString().trim())
                dialog.dismiss()
            }
        }

        val checkAndUpdateButtonState = {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            positiveButton.isEnabled = email.isNotEmpty() && password.isNotEmpty()
        }

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = checkAndUpdateButtonState()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        binding.emailEditText.addTextChangedListener(textWatcher)
        binding.passwordEditText.addTextChangedListener(textWatcher)
    }
}