package se.rebeccazadig.bokholken.adverts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import se.rebeccazadig.bokholken.databinding.FragmentAdvertsBinding
import se.rebeccazadig.bokholken.models.Advert

class AdvertsFragment : Fragment() {

    private var _binding: FragmentAdvertsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdvertViewModel by viewModels()
    private val advertsAdapter = AdvertsAdapter(
        onAdvertClick = { advert ->
            navigateToAdvertDetail(advert)
        },
        onDeleteAdvertClick = {},
        onEditAdvertClick = {}
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdvertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.allAdvertsRV.layoutManager = LinearLayoutManager(context)
        binding.allAdvertsRV.adapter = advertsAdapter

        viewModel.filteredAdverts.observe(viewLifecycleOwner) { adverts ->
            val wasEmpty = advertsAdapter.currentList.isEmpty()
            advertsAdapter.submitList(adverts) {
                if (!wasEmpty) {
                    binding.allAdvertsRV.scrollToPosition(0)
                }
            }
        }

        binding.searchView.also {
            it.setIconifiedByDefault(false)
            it.onActionViewExpanded()
            it.clearFocus()
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.searchQuery.value = query
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchQuery.value = newText
                return true
            }
        })

        binding.newAdvertButton.setOnClickListener {
            val action =
                AdvertsFragmentDirections.actionAdvertsFragmentToCreateAdvertsFragment(null)
            findNavController().navigate(action)
        }
    }

    private fun navigateToAdvertDetail(advert: Advert) {
        val action = advert.adId?.let {
            AdvertsFragmentDirections.actionAdvertsFragmentToAdvertsDetailsFragment(
                advertId = it,
            )
        }
        action?.let { findNavController().navigate(it) }
    }
}


