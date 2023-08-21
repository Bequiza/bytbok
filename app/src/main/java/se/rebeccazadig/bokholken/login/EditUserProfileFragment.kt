package se.rebeccazadig.bokholken.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import se.rebeccazadig.bokholken.databinding.FragmentEditUserProfileBinding

class EditUserProfileFragment : Fragment() {

    private val viewModel: UserViewModel by viewModels()
    private lateinit var binding: FragmentEditUserProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentEditUserProfileBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.vm = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.editUserToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }
}