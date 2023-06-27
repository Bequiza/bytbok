package se.rebeccazadig.bokholken.Adverts

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import se.rebeccazadig.bokholken.R
import se.rebeccazadig.bokholken.databinding.FragmentCreateAdvertBinding
// import se.rebeccazadig.bokholken.SkapaAnnonsFragmentArgs
import java.io.ByteArrayOutputStream

class CreateAdvertFragment : Fragment() {

    private var _binding: FragmentCreateAdvertBinding? = null
    private val binding get() = _binding!!
    val args: CreateAdvertFragmentArgs by navArgs()

    val user = Firebase.auth.currentUser

    var currentannons: Adverts? = null

    var annonsbild: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentCreateAdvertBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var adid = args.annonsid

        if (adid != "") {
            // VISA RADERA KNAPP
            var raderaKnapp = view.findViewById<Button>(R.id.deleteButton)
            raderaKnapp.visibility = View.VISIBLE

            val database = Firebase.database

            val books = database.getReference("Books").child(adid)

            books.get().addOnSuccessListener {
                currentannons = it.getValue<Adverts>()!!
                currentannons!!.adid = it.key!!

                binding.titleET.setText(currentannons!!.title)
                binding.authorET.setText(currentannons!!.author)
                binding.genreET.setText(currentannons!!.genre)
                binding.cityET.setText(currentannons!!.city)
                binding.contactET.setText(currentannons!!.contact)
            }

            downloadimage(adid)
        }

        binding.publishButton.setOnClickListener {
            var addBokTitel = binding.titleET.text.toString()
            var addBokForfattare = binding.authorET.text.toString()
            var addGenre = binding.genreET.text.toString()
            var addStad = binding.cityET.text.toString()
            var addKontaktsatt = binding.contactET.text.toString()

            val database = Firebase.database
            val myRef = database.getReference("Books")

            var someBooks = Adverts(
                title = addBokTitel,
                author =
                addBokForfattare,
                city = addStad,
                genre = addGenre,
                contact = addKontaktsatt,
            )

            someBooks.adcreator = Firebase.auth.currentUser!!.uid

            if (currentannons == null) {
                var adsave = myRef.push()
                adsave.setValue(someBooks).addOnSuccessListener {
                    uploadimage(adsave.key!!)
                }
            } else {
                myRef.child(currentannons!!.adid).setValue(someBooks).addOnSuccessListener {
                    uploadimage(currentannons!!.adid)
                }
            }

            // val action = SkapaAnnonsFragmentDirections.actionSkapaAnnonsFragmentToAnnonsFragment()
            // findNavController().navigate(R.id.action_skapaAnnonsFragment_to_annonsFragment)
            findNavController().popBackStack()
        }

        binding.advertImageView.setOnClickListener {
            getContent.launch("image/*")
        }
    }

    fun uploadimage(adid: String) {
        if (annonsbild != null) {
            var storageRef = Firebase.storage.reference
            var imageRef = storageRef.child("annonser").child(adid)

            val baos = ByteArrayOutputStream()
            annonsbild!!.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val data = baos.toByteArray()

            imageRef.putBytes(data).addOnFailureListener {
                Log.i("PIA11DEBUG", "UPLOAD FAIL")
            }.addOnSuccessListener {
                Log.i("PIA11DEBUG", "UPLOAD OK")
            }
        }
    }

    val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->

        Log.i("PIA11DEBUG", "Vi fick resultat")

        uri?.let {
            val source = ImageDecoder.createSource(requireActivity().contentResolver, it)
            var valdbild = ImageDecoder.decodeBitmap(source)

            var imagescale = 800 / valdbild.width

            annonsbild =
                Bitmap.createScaledBitmap(valdbild, 800, valdbild.height * imagescale, false)

            val theimage = requireView().findViewById<ImageView>(R.id.advertImageView)
            theimage.setImageBitmap(annonsbild)
        }
    }

    private fun downloadimage(adid: String) {
        var storageRef = Firebase.storage.reference
        var imageRef = storageRef.child("annonser").child(adid)

        imageRef.getBytes(1000000).addOnSuccessListener {
            var bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)

            var theimage = requireView().findViewById<ImageView>(R.id.advertImageView)
            theimage.setImageBitmap(bitmap)
        }.addOnFailureListener {
        }
    }
}
