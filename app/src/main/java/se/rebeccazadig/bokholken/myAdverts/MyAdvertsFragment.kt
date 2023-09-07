package se.rebeccazadig.bokholken.myAdverts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import se.rebeccazadig.bokholken.R
import se.rebeccazadig.bokholken.adverts.AdvertsAdapter
import se.rebeccazadig.bokholken.databinding.FragmentMyAdvertsBinding
import se.rebeccazadig.bokholken.models.Advert
import se.rebeccazadig.bokholken.utils.navigateBack
import se.rebeccazadig.bokholken.utils.showConfirmationDialog

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
        onEditAdvertClick = {},
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
    }

    private fun deleteMyAdvert(advert: Advert) {
        showConfirmationDialog(
            context = requireContext(),
            titleResId = R.string.delete_advert_title,
            messageResId = R.string.delete_advert_message,
            positiveAction = { viewModel.deleteAdvert(advert)}
        )
    }

    private fun navigateToAdvertDetail(advert: Advert) {
        val action =
            MyAdvertsFragmentDirections.actionMyAdvertsFragmentToAdvertsDetailsFragment(advert.adId!!)
        findNavController().navigate(action)
    }
}
