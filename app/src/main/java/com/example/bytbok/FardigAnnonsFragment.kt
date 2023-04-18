package com.example.bytbok

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class FardigAnnonsFragment : Fragment() {

    val args: FardigAnnonsFragmentArgs by navArgs()

    var currentannons = Annons()

    var adid = ""
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fardig_annons, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adid = args.annonsid

        val database = Firebase.database

        val books = database.getReference("Books").child(adid)

        books.get().addOnSuccessListener {
            currentannons = it.getValue<Annons>()!!
            currentannons.adid = it.key!!

            view.findViewById<TextView>(R.id.titelFardigAnnonsTV).text = currentannons.bokTitel
            view.findViewById<TextView>(R.id.forfattareFardigAnnonsTV).text = currentannons.bokForfattare
            view.findViewById<TextView>(R.id.stadFardigAnnonsTV).text = currentannons.stad
            view.findViewById<TextView>(R.id.telEmailFardigAnnonsTV).text = currentannons.kontaktsatt
            view.findViewById<TextView>(R.id.genreFardigAnnons).text = "Genre: " + currentannons.genre

        }

        downloadimage()

        view.findViewById<Button>(R.id.villLasaFardigAnnonsButton).setOnClickListener {

            val sharedPref = activity?.getSharedPreferences("se.rebecca.bytbok", Context.MODE_PRIVATE)

            var favbooks = sharedPref!!.getStringSet("favbooks", setOf<String>())!!.toMutableSet()
            if(favbooks!!.contains(currentannons.adid)) {
                favbooks!!.remove(currentannons.adid)
            } else {
                favbooks!!.add(currentannons.adid)
            }

            with (sharedPref!!.edit()) {
                putStringSet("favbooks", favbooks)
                apply()
            }

            //findNavController().navigate(R.id.action_fardigAnnonsFragment_to_gilladeObjektFragment)
            findNavController().popBackStack()

        }
    }

    private fun downloadimage() {

        var storageRef = Firebase.storage.reference
        var imageRef = storageRef.child("annonser").child(adid)

        imageRef.getBytes(1000000).addOnSuccessListener {
            var bitmap = BitmapFactory.decodeByteArray(it, 0,it.size)

            var theimage = requireView().findViewById<ImageView>(R.id.bildFardigAnnons)
            theimage.setImageBitmap(bitmap)

        }.addOnFailureListener{

        }
    }
}