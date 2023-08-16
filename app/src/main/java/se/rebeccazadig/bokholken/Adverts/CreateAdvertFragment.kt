package se.rebeccazadig.bokholken.adverts

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import se.rebeccazadig.bokholken.R
import se.rebeccazadig.bokholken.databinding.FragmentCreateAdvertBinding
import java.io.ByteArrayOutputStream

class CreateAdvertFragment : Fragment() {

    private var _binding: FragmentCreateAdvertBinding? = null
    private val binding get() = _binding!!
    private val args: CreateAdvertFragmentArgs by navArgs()

    private val viewModel: AdvertViewModel by viewModels()

    private var currentAd: Adverts? = null
    private var adImage: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateAdvertBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("FragmentLifecycle", "onAttach")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val adId = args.annonsid
        adId.takeIf { it.isNotEmpty() }?.let {
            binding.deleteButton.visibility = View.VISIBLE

            fetchAdvertDetails(it)
            downloadImage(it)
        }


        binding.publishButton.setOnClickListener {
            if (!areFieldsValid()) {
                Toast.makeText(requireContext(), "All fields are required!", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            val addBookTitle = binding.titleET.text.toString()
            val addBookWriter = binding.authorET.text.toString()
            val addGenre = binding.genreET.text.toString()
            val addCity = binding.cityET.text.toString()
            val addContactMethod = binding.contactET.text.toString()

            val someBooks = Adverts(
                title = addBookTitle,
                author = addBookWriter,
                city = addCity,
                genre = addGenre,
                contact = addContactMethod
            )

            Firebase.auth.currentUser?.let { currentUser ->
                someBooks.adCreator = currentUser.uid
            }

            viewModel.advertSaveStatus.observe(viewLifecycleOwner) { isSuccessful ->
                if (isSuccessful) {
                    lifecycleScope.launch {
                        // If there's an image, upload it.
                        adImage?.let {
                            // Use the adId for the image upload
                            val adId = someBooks.adId ?: return@launch
                            uploadImage(adId)

                            // Introduce a delay of 3 seconds (or however long you want)
                            delay(3000)
                        }

                        // After the delay, navigate back
                        navigateBack()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to save advert. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            if (currentAd == null) {
                viewModel.saveAdvert(someBooks)
            } else {
                currentAd?.adId?.let { existingAdId ->
                    someBooks.adId = existingAdId
                    viewModel.saveAdvert(someBooks)
                }
            }
        }

        viewModel.advertSaveStatus.observe(viewLifecycleOwner) { isSuccessful ->
            if (isSuccessful) {
                Toast.makeText(requireContext(), "Advert saved successfully!", Toast.LENGTH_SHORT)
                    .show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Failed to save advert. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.advertImageView.setOnClickListener {
            getContent.launch("image/*")
        }
    }

    private fun fetchAdvertDetails(adId: String) {
        val database = Firebase.database
        val books = database.getReference("Books").child(adId)

        books.get().addOnSuccessListener { dataSnapshot ->
            val advert = dataSnapshot.getValue<Adverts>()
            advert?.let {
                it.adId = dataSnapshot.key
                updateUIWithAdvertDetails(it)
            } ?: run {
                // Handle error, maybe show a message or log the error
                Log.e("CreateAdvertFragment", "Advert not found for id: $adId")
            }
        }
    }

    private fun updateUIWithAdvertDetails(advert: Adverts) {
        currentAd = advert
        binding.run {
            titleET.setText(advert.title)
            authorET.setText(advert.author)
            genreET.setText(advert.genre)
            cityET.setText(advert.city)
            contactET.setText(advert.contact)
        }
    }


    private fun uploadImage(adId: String) {
        adImage?.let {
            val storageRef = Firebase.storage.reference
            val imageRef = storageRef.child("annonser").child(adId)

            val baos = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val data = baos.toByteArray()

            binding.progressBar.visibility = View.VISIBLE

            imageRef.putBytes(data)
                .addOnSuccessListener {
                    if (isAdded) {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            requireContext(),
                            "The image has successfully uploaded.",
                            Toast.LENGTH_SHORT
                        ).show()
                        navigateBack()
                    }
                }
                .addOnFailureListener {
                    if (isAdded) {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            requireContext(),
                            "Failed to upload image. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val source = ImageDecoder.createSource(requireActivity().contentResolver, it)
                val selectedImage = ImageDecoder.decodeBitmap(source)

                val imageScale = 800.0 / selectedImage.width
                adImage = Bitmap.createScaledBitmap(
                    selectedImage,
                    800,
                    (selectedImage.height * imageScale).toInt(),
                    false
                )

                binding.advertImageView.setImageBitmap(adImage)
            }
        }

    private fun downloadImage(adId: String) {
        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("annonser").child(adId)

        imageRef.downloadUrl.addOnSuccessListener { uri ->
            val imageUrl = uri.toString()
            Glide.with(this@CreateAdvertFragment)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(binding.advertImageView)
        }.addOnFailureListener {
            Log.e("CreateAdvertFragment", "Error downloading image", it)
        }
    }

    private fun areFieldsValid(): Boolean {
        return binding.titleET.text.isNotEmpty() &&
                binding.authorET.text.isNotEmpty() &&
                binding.genreET.text.isNotEmpty() &&
                binding.cityET.text.isNotEmpty() &&
                binding.contactET.text.isNotEmpty()
    }

    private fun navigateBack() {
        Toast.makeText(requireContext(), "Advert saved successfully!", Toast.LENGTH_SHORT).show()
        if (isAdded) {
            findNavController().popBackStack()
        }
    }
}