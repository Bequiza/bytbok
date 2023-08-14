package se.rebeccazadig.bokholken.adverts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
// import se.rebeccazadig.bokholken.GilladeObjektFragmentDirections
import se.rebeccazadig.bokholken.databinding.FragmentFavoriteBinding

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdvertViewModel by viewModels()
    private val favoriteAdapter = AdvertsAdapter(
        onAdvertClick = { _ ->
            // Handle the list item click if needed
        },
        onReadMoreClick = { advert ->
            navigateToAdvertDetail(advert)
        },
        onHeartClick = { advert ->
            removeFromFavorites(advert)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        // Fetch and observe favorites from Firebase
        /*viewModel.favoritesLiveData.observe(viewLifecycleOwner) { adverts ->
            favoriteAdapter.submitList(adverts)
        }*/
    }

    private fun navigateToAdvertDetail(advert: Adverts) {
        // Logic to navigate or display details for the clicked advert
    }

    private fun removeFromFavorites(advert: Adverts) {
        val userId = Firebase.auth.currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance().getReference("favorites").child(userId).child(advert.adId ?: return)

        dbRef.removeValue().addOnSuccessListener {
            Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show()
            // You can also manually update the RecyclerView here if needed
        }
    }
}
