package se.rebeccazadig.bokholken.adverts

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import se.rebeccazadig.bokholken.R
import se.rebeccazadig.bokholken.databinding.FragmentAdvertsBinding
import se.rebeccazadig.bokholken.main.MainActivity
import se.rebeccazadig.bokholken.models.Advert
import se.rebeccazadig.bokholken.utils.getParcelableCompat
import se.rebeccazadig.bokholken.utils.showToast

class AdvertsFragment : Fragment() {

    private var _binding: FragmentAdvertsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdvertViewModel by viewModels()
    private val advertsAdapter = AdvertsAdapter()
    private var backPressedOnce = false
    private var recyclerState: Parcelable? = null

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

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val callback = object : OnBackPressedCallback(true ) {
            override fun handleOnBackPressed() {
                if (backPressedOnce) {
                    isEnabled = false
                } else {
                    backPressedOnce = true
                    showToast(getString(R.string.press_back_again))

                    Handler(Looper.getMainLooper()).postDelayed({
                        backPressedOnce = false
                    }, 2000)
                }
            }

        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _binding?.let {
            recyclerState = it.allAdvertsRV.layoutManager?.onSaveInstanceState()
            outState.putParcelable("recycler_state", recyclerState)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.showBottomNavBar()

        recyclerState = savedInstanceState?.getParcelableCompat<Parcelable>("recycler_state")

        binding.allAdvertsRV.layoutManager?.onRestoreInstanceState(recyclerState)

        setupAdvertsAdapter()

        binding.allAdvertsRV.layoutManager = LinearLayoutManager(context)
        binding.allAdvertsRV.adapter = advertsAdapter

        viewModel.filteredAdverts.observe(viewLifecycleOwner) { adverts ->
            val wasShorter = advertsAdapter.currentList.size < adverts.size
            advertsAdapter.submitList(adverts) {
                if (wasShorter) {
                    binding.allAdvertsRV.scrollToPosition(0)
                }
            }
        }

        binding.searchView.clearFocus()

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

    private fun setupAdvertsAdapter() {
        advertsAdapter.onAdvertClick = { advert ->
            navigateToAdvertDetail(advert)
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