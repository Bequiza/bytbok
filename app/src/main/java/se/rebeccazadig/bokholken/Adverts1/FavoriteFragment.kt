package se.rebeccazadig.bokholken.adverts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import se.rebeccazadig.bokholken.data.Advert
import se.rebeccazadig.bokholken.databinding.FragmentFavoriteBinding

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdvertViewModel by viewModels()
    private val favoriteAdapter = AdvertsAdapter(
        onAdvertClick = { advert ->
            navigateToAdvertDetail(advert)
        }
    )

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

        binding.favoritesRV.layoutManager = LinearLayoutManager(context)
        binding.favoritesRV.adapter = favoriteAdapter
    }

    private fun navigateToAdvertDetail(advert: Advert) {
        val action =
            FavoriteFragmentDirections.actionFavoritesFragmentToPublishedAdvertsFragment(advert.adId!!)
        findNavController().navigate(action)
    }
}