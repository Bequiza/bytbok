package se.rebeccazadig.bokholken.adverts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import se.rebeccazadig.bokholken.R
import se.rebeccazadig.bokholken.data.User
import se.rebeccazadig.bokholken.databinding.FragmentAdvertDetailsBinding
import se.rebeccazadig.bokholken.models.Advert
import se.rebeccazadig.bokholken.utils.formatDateForDisplay
import se.rebeccazadig.bokholken.utils.isEmail
import se.rebeccazadig.bokholken.utils.isPhoneNumber
import se.rebeccazadig.bokholken.utils.navigateBack
import se.rebeccazadig.bokholken.utils.showToast

class AdvertDetailsFragment : Fragment() {

    private val args: AdvertDetailsFragmentArgs by navArgs()
    private var _binding: FragmentAdvertDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdvertViewModel by viewModels()

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

        viewModel.cleanUp()

        viewModel.getAdvertDetails(args.advertId)

        binding.toolbar.setNavigationOnClickListener {
           navigateBack()
        }

        viewModel.advertDetailsLiveData.observe(viewLifecycleOwner) { pair ->
            pair?.let { (advert, user) ->
                displayAdvertDetails(advert, user)
            }
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
        Log.d("Fragment", "Setting Image URL to ImageView: $imageUrl ")

        Glide.with(binding.publishedAdvertImage.context)
            .load(imageUrl)
            .placeholder(R.drawable.loading_image)
            .error(R.drawable.error_image)
            .into(binding.publishedAdvertImage)

        binding.contactUserAdvertButton.setOnClickListener {
            handleContactAction(user?.contact.orEmpty(), advert.title, user?.name ?:"")
        }
    }

    private fun sendEmail(email: String, advertTitle: String, userName: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject, advertTitle))
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_body, advertTitle, userName))

        startActivity(Intent.createChooser(intent, getString(R.string.choose_email_app)))
    }

    private fun dialNumber(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phoneNumber")
        startActivity(intent)
    }

    private fun handleContactAction(contact: String, advertTitle: String?, userName: String) {
        when {
            isEmail(contact) -> {
                advertTitle?.let {
                    sendEmail(contact, it, userName)
                }
            }

            isPhoneNumber(contact) -> {
                dialNumber(contact)
            }

            else -> {
                showToast(getString(R.string.invalid_contact_info))
            }
        }
    }
}