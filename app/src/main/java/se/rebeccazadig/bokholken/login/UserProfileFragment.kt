package se.rebeccazadig.bokholken.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import se.rebeccazadig.bokholken.data.User
import se.rebeccazadig.bokholken.databinding.FragmentUserProfileBinding

class UserProfileFragment : Fragment() {

    private val viewModel: UserViewModel by viewModels()
    private lateinit var binding: FragmentUserProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentUserProfileBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.vm = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.fetchUserData()

        viewModel.user.observe(viewLifecycleOwner) { user ->
            binding.userNameTV.text = user.name
            binding.contactTV.text = user.contact
            binding.locationTV.text = user.city
        }

        binding.editUserButton.setOnClickListener {
            val editUserButton = UserProfileFragmentDirections.actionUserProfileFragment2ToUserInfoFragment()
            findNavController().navigate(editUserButton)
        }

        binding.myAdvertsButton.setOnClickListener {
            val myAdvertsButton = UserProfileFragmentDirections.actionUserProfileFragment2ToMyAdvertsFragment()
            findNavController().navigate(myAdvertsButton)
        }
    }
}