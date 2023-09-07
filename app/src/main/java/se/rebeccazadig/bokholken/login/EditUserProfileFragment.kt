package se.rebeccazadig.bokholken.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
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
    }

    private fun getContactTypeFromRadioButtonId(id: Int): ContactType? {
        return when (id) {
            R.id.radioBtnEmail -> ContactType.EMAIL
            R.id.radioBtnPhone -> ContactType.PHONE
            else -> null
        }
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
}