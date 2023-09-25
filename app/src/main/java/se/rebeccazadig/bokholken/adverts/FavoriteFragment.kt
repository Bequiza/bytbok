package se.rebeccazadig.bokholken.adverts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import se.rebeccazadig.bokholken.databinding.FragmentFavoriteBinding
import se.rebeccazadig.bokholken.models.Advert

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdvertViewModel by viewModels()
    private val advertsAdapter = AdvertsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdvertsAdapter()

        binding.favoritesRV.layoutManager = LinearLayoutManager(context)
        binding.favoritesRV.adapter = advertsAdapter

        viewModel.fetchFavoriteAdverts()

        viewModel.favoritesLiveData.observe(viewLifecycleOwner) { favoriteAdverts ->
            advertsAdapter.submitList(favoriteAdverts)
        }
    }

    private fun setupAdvertsAdapter() {
        advertsAdapter.onAdvertClick = { advert ->
            navigateToAdvertDetail(advert)
        }
    }

    private fun navigateToAdvertDetail(advert: Advert) {
        val action = advert.adId?.let {
            FavoriteFragmentDirections.actionFavoritesFragmentToPublishedAdvertsFragment(
                advertId = it,
            )
        }
        action?.let { findNavController().navigate(it) }
    }
}