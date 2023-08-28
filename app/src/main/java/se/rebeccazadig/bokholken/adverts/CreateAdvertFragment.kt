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
import se.rebeccazadig.bokholken.data.Advert
import se.rebeccazadig.bokholken.databinding.FragmentCreateAdvertBinding
import java.io.ByteArrayOutputStream

class CreateAdvertFragment : Fragment() {

    private var _binding: FragmentCreateAdvertBinding? = null
    private val binding get() = _binding!!
    private val args: CreateAdvertFragmentArgs by navArgs()

    private val viewModel: AdvertViewModel by viewModels()

    private var currentAd: Advert? = null
    private var adImage: Bitmap? = null
    private var draftAd: Advert? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateAdvertBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupPublishButton()
        setupLoadingObserver()
        setupAdvertSaveObserver()
        setupImageViewClick()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val adId = args.annonsid
        adId.takeIf { it.isNotEmpty() }?.let {
            fetchAdvertDetails(it)
            downloadImage(it)
        }
    }

    private fun setupPublishButton() {
        binding.publishButton.setOnClickListener {
            if (!areFieldsValid()) {
                showToast("All fields are required!")
                return@setOnClickListener
            }

            draftAd = createAdvertFromInput()

            draftAd?.let { advert ->
                if (currentAd == null) {
                    viewModel.saveAdvert(advert)
                } else {
                    currentAd?.adId?.let { existingAdId ->
                        advert.adId = existingAdId
                        viewModel.saveAdvert(advert)
                    }
                }
            }
        }
    }

    private fun createAdvertFromInput(): Advert {
        val addBookTitle = binding.titleET.text.toString()
        val addBookWriter = binding.authorET.text.toString()
        val addGenre = binding.genreET.text.toString()
        val addCity = binding.cityET.text.toString()
        val addContactMethod = binding.contactET.text.toString()

        val someBooks = Advert(
            title = addBookTitle,
            author = addBookWriter,
            city = addCity,
            genre = addGenre,
            contact = addContactMethod
        )

        //Firebase.auth.currentUser?.let { currentUser ->
        //    someBooks.adCreator = currentUser.uid
        //}

        return someBooks
    }

    private fun setupLoadingObserver() {
        viewModel.inProgress.observe(viewLifecycleOwner) { isLoading ->
            binding.progressbarContainer.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupAdvertSaveObserver() {
        viewModel.advertSaveStatus.observe(viewLifecycleOwner) { isSuccessful ->
            if (isSuccessful) {
                onAdvertSaveSuccess()
            } else {
                onAdvertSaveFailure()
            }
        }
    }

    private fun onAdvertSaveSuccess() {
        lifecycleScope.launch {
            adImage?.let {
                val adId = draftAd?.adId ?: return@launch
                uploadImage(adId)
                delay(2000)
            }
            navigateBack()
        }
    }

    private fun onAdvertSaveFailure() {
        showToast("Failed to save advert. Please try again.")
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun setupImageViewClick() {
        binding.advertImageView.setOnClickListener {
            getContent.launch("image/*")
        }
    }

    private fun fetchAdvertDetails(adId: String) {
        val database = Firebase.database
        val books = database.getReference("Books").child(adId)

        books.get().addOnSuccessListener { dataSnapshot ->
            val advert = dataSnapshot.getValue<Advert>()
            advert?.let {
                it.adId = dataSnapshot.key
                updateUIWithAdvertDetails(it)
            } ?: run {
                // Handle error, maybe show a message or log the error
                Log.e("CreateAdvertFragment", "Advert not found for id: $adId")
            }
        }
    }

    private fun updateUIWithAdvertDetails(advert: Advert) {
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
        adImage?.let { image ->
            val storageRef = Firebase.storage.reference
            val imageRef = storageRef.child("annonser").child(adId)

            val data = image.toByteArray()

            showProgressBar()

            imageRef.putBytes(data)
                .addOnSuccessListener { onImageUploadSuccess() }
                .addOnFailureListener { onImageUploadFailure() }
        }
    }

    private fun Bitmap.toByteArray(): ByteArray {
        val baos = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        return baos.toByteArray()
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    private fun onImageUploadSuccess() {
        if (isAdded) {
            hideProgressBar()
            navigateBack()
        }
    }

    private fun onImageUploadFailure() {
        if (isAdded) {
            hideProgressBar()
            showToast("Failed to upload image. Please try again.")
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
        return binding.titleET.text?.isNotEmpty() == true &&
                binding.authorET.text?.isNotEmpty() == true &&
                binding.genreET.text?.isNotEmpty() == true &&
                binding.cityET.text?.isNotEmpty() == true &&
                binding.contactET.text?.isNotEmpty() == true
    }

    private fun navigateBack() {
        Toast.makeText(requireContext(), "Advert saved successfully!", Toast.LENGTH_SHORT).show()
        if (isAdded) {
            findNavController().popBackStack()
        }
    }
}