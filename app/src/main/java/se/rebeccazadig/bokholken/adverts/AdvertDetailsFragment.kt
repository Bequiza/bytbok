package se.rebeccazadig.bokholken.adverts

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import se.rebeccazadig.bokholken.R
import androidx.navigation.fragment.findNavController
import se.rebeccazadig.bokholken.data.Advert
import se.rebeccazadig.bokholken.data.Result
import se.rebeccazadig.bokholken.databinding.FragmentAdvertDetailsBinding
import se.rebeccazadig.bokholken.utils.formatDateForDisplay

class AdvertDetailsFragment : Fragment() {

    private val args: AdvertDetailsFragmentArgs by navArgs()
    private var _binding: FragmentAdvertDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdvertDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        fetchAdvertDetails(args.advertId)
    }

    private fun fetchAdvertDetails(advertId: String) {
        val database = Firebase.database
        val books = database.getReference("Books").child(advertId)

        books.get().addOnSuccessListener { dataSnapshot ->
            val advert = dataSnapshot.getValue<Advert>()
            advert?.let {
                displayAdvertDetails(it, advertId)
                Result.Success
            }
        }.addOnFailureListener { exception ->
            Result.Failure("${exception.message}")
            Log.e("AdvertDetailsFragment", "Failed to fetch advert details", exception)
        }

    }

    private fun displayAdvertDetails(advert: Advert, advertId: String) {
        binding.titleAdvertTV.text = advert.title
        binding.authorAdvertTV.text = getString(R.string.author_advert_tv, advert.author)
        binding.cityAdvertTV.text = getString(R.string.location_advert_tv, advert.city)
        binding.contactUserAdvertTV.text = getString(R.string.contact_advert_tv, advert.contact)
        binding.genreFardigTV.text = getString(R.string.genre_advert_tv, advert.genre)

        val formattedDate =
            advert.creationTime?.let { formatDateForDisplay(binding.root.context, it) }
        binding.creationTimeTV.text = getString(R.string.creation_time_label, formattedDate)

        // Fetch and display the image
        val imageRef = Firebase.storage.reference.child("annonser").child(advertId)
        imageRef.downloadUrl.addOnSuccessListener { uri ->
            val imageUrl = uri.toString()
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(binding.publishedAdvertImage)
        }.addOnFailureListener {
            Glide.with(this)
                .load(R.drawable.placeholder_image)
                .into(binding.publishedAdvertImage)
        }
    }
}
