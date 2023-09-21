package se.rebeccazadig.bokholken.myAdverts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import se.rebeccazadig.bokholken.R
import se.rebeccazadig.bokholken.adverts.AdvertsAdapter
import se.rebeccazadig.bokholken.databinding.FragmentMyAdvertsBinding
import se.rebeccazadig.bokholken.models.Advert
import se.rebeccazadig.bokholken.utils.navigateBack
import se.rebeccazadig.bokholken.utils.showConfirmationDialog
import se.rebeccazadig.bokholken.utils.showToast

class MyAdvertsFragment : Fragment() {

    private val viewModel: MyAdvertsViewModel by viewModels()
    private var _binding: FragmentMyAdvertsBinding? = null
    private val binding get() = _binding!!

    private val advertsAdapter = AdvertsAdapter(
        onAdvertClick = { advert ->
            navigateToAdvertDetail(advert)
        },
        onDeleteAdvertClick = { advert ->
            deleteMyAdvert(advert)
        },
        onEditAdvertClick = { advert ->
            navigateToEditAdvert(advert)
        },
        showIcons = true
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMyAdvertsBinding.inflate(inflater, container, false)
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
        binding.myAdvertToolbar.setNavigationOnClickListener {
            navigateBack()
        }

        binding.myAdvertsRV.layoutManager = LinearLayoutManager(context)
        binding.myAdvertsRV.adapter = advertsAdapter

        viewModel.myAdvertsLiveData.observe(viewLifecycleOwner) { adverts ->
            advertsAdapter.submitList(adverts)
        }

        viewModel.deleteAdvertStatus.observe(viewLifecycleOwner) { deleteAdvert ->
            deleteAdvert?.message?.let { message ->
                showToast(message)
            }
        }
    }

    private fun deleteMyAdvert(advert: Advert) {
        showConfirmationDialog(
            context = requireContext(),
            titleResId = R.string.delete_advert_title,
            messageResId = R.string.delete_advert_message,
            positiveAction = { viewModel.deleteAdvert(advert) }
        )
    }

    private fun navigateToAdvertDetail(advert: Advert) {
        val action = advert.adId?.let {
            MyAdvertsFragmentDirections.actionMyAdvertsFragmentToAdvertsDetailsFragment(
                advertId = it,
                fromMyAdvertsFragment = true
            )
        }
        action?.let { findNavController().navigate(it) }
    }

    private fun navigateToEditAdvert(advert: Advert) {
        val action = advert.adId?.let {
            MyAdvertsFragmentDirections.actionMyPageFragmentToCreateAdvertsFragment(
                advertId = it,
            )
        }
        action?.let { findNavController().navigate(it) }
    }
}
