package se.rebeccazadig.bokholken.Adverts

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
// import se.rebeccazadig.bokholken.FardigAnnonsFragmentArgs
import se.rebeccazadig.bokholken.databinding.FragmentPublishedAdvertsBinding

class PublishedAdvertsFragment : Fragment() {

//    val args: PublishedAdvertsFragmentArgs by navArgs()
    private var _binding: FragmentPublishedAdvertsBinding? = null
    private val binding get() = _binding!!

//    var currentannons = Adverts()
//
//    var adid = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPublishedAdvertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        adid = args.annonsid
//
//        val database = Firebase.database
//
//        val books = database.getReference("Books").child(adid)
//
//        books.get().addOnSuccessListener {
//            currentannons = it.getValue<Adverts>()!!
//            currentannons.adid = it.key!!
//
//            binding.titleAdvertTV.text = currentannons.title
//            binding.authorAdvertTV.text =
//                currentannons.author
//            binding.cityAdvertTV.text = currentannons.city
//            binding.contactUserAdvertTV.text =
//                currentannons.contact
//            binding.genreAdvertTV.text =
//                "Genre: " + currentannons.genre
//        }
//
//        downloadimage()
//
//        binding.favoriteButton.setOnClickListener {
//            val sharedPref =
//                activity?.getSharedPreferences("se.rebecca.bytbok", Context.MODE_PRIVATE)
//
//            val favbooks = sharedPref!!.getStringSet("favbooks", setOf<String>())!!.toMutableSet()
//            if (favbooks.contains(currentannons.adid)) {
//                favbooks!!.remove(currentannons.adid)
//                Toast.makeText(requireContext(), "Borttagen från favisar", Toast.LENGTH_SHORT)
//                    .show()
//            } else {
//                favbooks!!.add(currentannons.adid)
//                Toast.makeText(requireContext(), "Tillagd i favoriter", Toast.LENGTH_SHORT).show()
//            }
//
//            with(sharedPref!!.edit()) {
//                putStringSet("favbooks", favbooks)
//                apply()
//            }
//
//            // findNavController().navigate(R.id.action_fardigAnnonsFragment_to_gilladeObjektFragment)
//            findNavController().popBackStack()
//        }
    }

//    private fun downloadimage() {
//        var storageRef = Firebase.storage.reference
//        var imageRef = storageRef.child("annonser").child(adid)
//
//        imageRef.getBytes(1000000).addOnSuccessListener {
//            var bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
//
//            var theimage = requireView().findViewById<ImageView>(R.id.publishedAdvertImage)
//            theimage.setImageBitmap(bitmap)
//        }.addOnFailureListener {
//        }
//    }
}
