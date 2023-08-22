package se.rebeccazadig.bokholken.adverts

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import se.rebeccazadig.bokholken.databinding.FragmentCreateAdvertBinding
import se.rebeccazadig.bokholken.utils.ImageUtils
import se.rebeccazadig.bokholken.utils.navigateBack
import se.rebeccazadig.bokholken.utils.showToast

class CreateAdvertFragment : Fragment() {

    private var _binding: FragmentCreateAdvertBinding? = null
    private val binding get() = _binding!!
    private val args: CreateAdvertFragmentArgs by navArgs()

    private val viewModel by lazy {
        ViewModelProvider(this)[AdvertViewModel::class.java]
    }

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

        setupToolbar()
        setupImageViewClick()

        viewModel.advertSaveStatus.observe(viewLifecycleOwner) { uiStateSave ->
            uiStateSave?.message?.let { message ->
                showToast(message)
                viewModel.resetUiStateSave()
                navigateBack()
            }
        }
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { selectedUri ->
                val selectedImage =
                    ImageUtils.getBitmapFromUri(requireActivity().contentResolver, selectedUri)

                selectedImage?.let { image ->
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