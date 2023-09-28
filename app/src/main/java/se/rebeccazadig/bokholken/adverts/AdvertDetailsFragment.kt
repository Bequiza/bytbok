package se.rebeccazadig.bokholken.adverts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import se.rebeccazadig.bokholken.R
import se.rebeccazadig.bokholken.data.ContactType
import se.rebeccazadig.bokholken.databinding.FragmentAdvertDetailsBinding
import se.rebeccazadig.bokholken.main.MainActivity
import se.rebeccazadig.bokholken.models.Advert
import se.rebeccazadig.bokholken.models.User
import se.rebeccazadig.bokholken.utils.formatDateForDisplay
import se.rebeccazadig.bokholken.utils.gone
import se.rebeccazadig.bokholken.utils.isEmail
import se.rebeccazadig.bokholken.utils.isPhoneNumber
import se.rebeccazadig.bokholken.utils.navigateBack
import se.rebeccazadig.bokholken.utils.showToast

class AdvertDetailsFragment : Fragment() {

    private val args: AdvertDetailsFragmentArgs by navArgs()
    private var _binding: FragmentAdvertDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdvertViewModel by viewModels()
    private var currentUserName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdvertDetailsBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.advertId = args.advertId
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.hideBottomNavBar()

        viewModel.advertDetailsLiveData.observe(viewLifecycleOwner) { pair ->
            pair?.let { (advert, user) ->
                displayAdvertDetails(advert, user)
                viewModel.checkAdvertFavoriteStatus(args.advertId)
            }
        }

        viewModel.isAdvertFavorite.observe(viewLifecycleOwner) { isFavorite ->
            when (isFavorite) {
                true -> binding.favoriteButton.setImageResource(R.drawable.ic_favorite_filled)
                false -> binding.favoriteButton.setImageResource(R.drawable.ic_favorite_border)
                null -> {}
            }
        }

        viewModel.favoriteState.observe(viewLifecycleOwner) { state ->
            if (state.isSuccess) {
                val message = if (viewModel.isAdvertFavorite.value == true) {
                    getString(R.string.added_to_favorites)
                } else {
                    getString(R.string.removed_from_favorites)
                }
                showToast(message)
            } else {
                state.errorMessage?.let { showToast(it) }
            }
        }

        viewModel.cleanUp()

        viewModel.getAdvertDetails(args.advertId)

        binding.toolbar.setNavigationOnClickListener {
            navigateBack()
        }

        if (args.fromMyAdvertsFragment) {
            binding.favoriteButton.gone()
            binding.contactUserAdvertButton.gone()
        }
    }

    private fun displayAdvertDetails(advert: Advert, user: User?) {
        binding.apply {
            titleAdvertTV.text = advert.title
            authorAdvertTV.text = getString(R.string.author_advert_tv, advert.author)
            genreTV.text = getString(R.string.genre_advert_tv, advert.genre)
            locationAdvertTV.text = getString(R.string.location_advert_tv, advert.location)
            adCreatorTV.text = getString(R.string.ad_creator_label, user?.name)

            val formattedDate =
                advert.creationTime?.let { formatDateForDisplay(binding.root.context, it) }
            creationTimeTV.text = getString(R.string.creation_time_label, formattedDate)
        }

        val imageUrl = advert.imageUrl

        Glide.with(binding.publishedAdvertImage.context)
            .load(imageUrl)
            .placeholder(R.drawable.loading_image)
            .error(R.drawable.error_image)
            .into(binding.publishedAdvertImage)

        viewModel.fetchUserName()

        viewModel.currentUserName.observe(viewLifecycleOwner) { name ->
            currentUserName = name
        }

        binding.contactUserAdvertButton.setOnClickListener {
            val contactMethod = user?.preferredContactMethod ?: ContactType.UNKNOWN
            val userEmail = user?.email
            val userPhone = user?.phoneNumber
            handleContactAction(contactMethod, userEmail, userPhone, advert.title, currentUserName ?:"")
        }
    }

    private fun sendEmail(email: String, advertTitle: String, userName: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject, advertTitle))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.email_body, advertTitle, userName))
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, getString(R.string.choose_email_app)))
    }

    private fun dialNumber(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phoneNumber")
        startActivity(intent)
    }

    private fun handleContactAction(contactMethod: ContactType, email: String?, phoneNumber: String?, advertTitle: String?, userName: String) {
        when (contactMethod) {
            ContactType.EMAIL -> {
                if (email?.isEmail() == true && !advertTitle.isNullOrEmpty()) {
                    sendEmail(email, advertTitle, userName)
                } else {
                    showToast(getString(R.string.invalid_email))
                }
            }
            ContactType.PHONE -> {
                if (phoneNumber?.isPhoneNumber() == true) {
                    dialNumber(phoneNumber)
                } else {
                    showToast(getString(R.string.invalid_phone_number))
                }
            }
            else -> showToast(getString(R.string.invalid_contact_info))
        }
    }
}