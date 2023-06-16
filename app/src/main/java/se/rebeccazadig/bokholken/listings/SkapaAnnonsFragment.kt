package se.rebeccazadig.bokholken.listings

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
import android.widget.EditText
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
// import se.rebeccazadig.bokholken.SkapaAnnonsFragmentArgs
import java.io.ByteArrayOutputStream

class SkapaAnnonsFragment : Fragment() {

    val args: SkapaAnnonsFragmentArgs by navArgs()

    val user = Firebase.auth.currentUser

    var currentannons: Annons? = null

    var annonsbild: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_skapa_annons, container, false)
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
                currentannons = it.getValue<Annons>()!!
                currentannons!!.adid = it.key!!

                view.findViewById<EditText>(R.id.titelET).setText(currentannons!!.bokTitel)
                view.findViewById<EditText>(R.id.forfattareET)
                    .setText(currentannons!!.bokForfattare)
                view.findViewById<EditText>(R.id.genreET).setText(currentannons!!.genre)
                view.findViewById<EditText>(R.id.stadET).setText(currentannons!!.stad)
                view.findViewById<EditText>(R.id.kontaktsättET).setText(currentannons!!.kontaktsatt)
            }

            downloadimage(adid)
        }

        view.findViewById<Button>(R.id.publiceraButton).setOnClickListener {
            var addBokTitel = view.findViewById<EditText>(R.id.titelET).text.toString()
            var addBokForfattare = view.findViewById<EditText>(R.id.forfattareET).text.toString()
            var addGenre = view.findViewById<EditText>(R.id.genreET).text.toString()
            var addStad = view.findViewById<EditText>(R.id.stadET).text.toString()
            var addKontaktsatt = view.findViewById<EditText>(R.id.kontaktsättET).text.toString()

            val database = Firebase.database
            val myRef = database.getReference("Books")

            var someBooks = Annons(
                bokTitel = addBokTitel,
                bokForfattare =
                addBokForfattare,
                stad = addStad,
                genre = addGenre,
                kontaktsatt = addKontaktsatt,
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

        view.findViewById<ImageView>(R.id.läggtillbildIV).setOnClickListener {
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

            val theimage = requireView().findViewById<ImageView>(R.id.läggtillbildIV)
            theimage.setImageBitmap(annonsbild)
        }
    }

    private fun downloadimage(adid: String) {
        var storageRef = Firebase.storage.reference
        var imageRef = storageRef.child("annonser").child(adid)

        imageRef.getBytes(1000000).addOnSuccessListener {
            var bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)

            var theimage = requireView().findViewById<ImageView>(R.id.läggtillbildIV)
            theimage.setImageBitmap(bitmap)
        }.addOnFailureListener {
        }
    }
}
