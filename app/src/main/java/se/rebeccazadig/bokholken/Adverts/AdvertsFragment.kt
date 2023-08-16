package se.rebeccazadig.bokholken.adverts // ktlint-disable package-name

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import se.rebeccazadig.bokholken.R
import se.rebeccazadig.bokholken.databinding.FragmentAdvertsBinding

class AdvertsFragment : Fragment(R.layout.fragment_adverts) {

    private var _binding: FragmentAdvertsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdvertViewModel by viewModels()
    private val advertsAdapter = AdvertsAdapter(
        onAdvertClick = { _ ->
            // Handle the list item click if needed
        },
        onReadMoreClick = { advert ->
            navigateToAdvertDetail(advert)
        },
        onHeartClick = {}
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
        viewModel.registerNetworkCallback(requireContext())

        viewModel.isInternetAvailable.observe(viewLifecycleOwner) { isConnected ->
            if (!isConnected) {
                // No internet connection
                binding.noInternetTextView.visibility = View.VISIBLE
                binding.allAdvertsRV.visibility = View.GONE
            } else {
                // Internet connection is available
                binding.noInternetTextView.visibility = View.GONE
                binding.allAdvertsRV.visibility = View.VISIBLE
            }
        }


        binding.allAdvertsRV.layoutManager = LinearLayoutManager(context)
        binding.allAdvertsRV.adapter = advertsAdapter

        viewModel.advertsLiveData.observe(viewLifecycleOwner) { adverts ->
            Log.d("FragmentData", "Adverts in Fragment: $adverts")
            advertsAdapter.submitList(adverts)
        }

        viewModel.filteredAdverts.observe(viewLifecycleOwner) { adverts ->
            advertsAdapter.submitList(adverts)
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
            val action = AdvertsFragmentDirections.actionAdvertsFragmentToCreateAdvertsFragment("annonsid")
            findNavController().navigate(action)
        }
    }

    private fun navigateToAdvertDetail(advert: Adverts) {
        val action = AdvertsFragmentDirections.actionAdvertsFragmentToPublishedAdvertsFragment(advert.adId!!)
        findNavController().navigate(action)
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}


