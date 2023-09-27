package se.rebeccazadig.bokholken.adverts

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import se.rebeccazadig.bokholken.R
import se.rebeccazadig.bokholken.databinding.FragmentCreateAdvertBinding
import se.rebeccazadig.bokholken.main.MainActivity
import se.rebeccazadig.bokholken.utils.ImageUtils
import se.rebeccazadig.bokholken.utils.navigateBack
import se.rebeccazadig.bokholken.utils.showToast

class CreateAdvertFragment : Fragment() {

    private var _binding: FragmentCreateAdvertBinding? = null
    private val binding get() = _binding!!
    private val args: CreateAdvertFragmentArgs by navArgs()

    private val viewModel: AdvertViewModel by viewModels()

    private var adImage: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateAdvertBinding.inflate(inflater, container, false)
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

        (activity as? MainActivity)?.hideBottomNavBar()

        setupToolbar()
        setupImageViewClick()

        viewModel.initializeAdvertData(args.advertId)

        viewModel.advertSaveStatus.observe(viewLifecycleOwner) { uiStateSave ->
            uiStateSave?.message?.let { message ->
                showToast(message)
                viewModel.resetUiStateSave()
                navigateBack()
            }
        }

        viewModel.currentAdvertImageUrl.observe(viewLifecycleOwner) { imageUrl ->
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(binding.advertImageView)
            } else {
                binding.advertImageView.setImageResource(R.drawable.placeholder_image)
            }
        }

        binding.publishButton.setOnClickListener {
            viewModel.saveOrUpdateAdvert(
                title = binding.titleET.text.toString(),
                author = binding.authorET.text.toString(),
                genre = binding.genreET.text.toString(),
                location = binding.locationEt.text.toString(),
                adImage = adImage
            )
        }
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { selectedUri ->
                ImageUtils.getBitmapFromUri(requireActivity().contentResolver, selectedUri)
                    ?.let { image ->
                        adImage = ImageUtils.resizeBitmap(image, 800)
                        binding.advertImageView.setImageBitmap(adImage)
                        viewModel.adImage.value = adImage
                    }
            }
        }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            navigateBack()
        }
    }

    private fun setupImageViewClick() {
        binding.advertImageView.setOnClickListener {
            getContent.launch("image/*")
        }
    }
}