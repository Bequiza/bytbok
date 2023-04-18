package com.example.bytbok

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import layout.AnnonsAdapter

class GilladeObjektFragment : Fragment() {

    var aAdapter = AnnonsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gillade_objekt, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        aAdapter.gilladefrag = this

        val annonsRecview = view.findViewById<RecyclerView>(R.id.villLäsaRV)

        val layoutmanager = GridLayoutManager(context, 2)
        annonsRecview.layoutManager = layoutmanager


        annonsRecview.adapter = aAdapter
        // annonsRecview.layoutManager = LinearLayoutManager(requireContext())

        aAdapter.notifyDataSetChanged()

        loadBooks()

        //Koden nedan är fel nånstans
       /*view.findViewById<Button>(R.id.lasMerAnnonsButton).setOnClickListener {

        findNavController().navigate(R.id.action_gilladeObjektFragment_to_fardigAnnonsFragment)
        }*/
    }

    fun loadBooks() {

        val database = Firebase.database

        val books = database.getReference("Books")

        books.get().addOnSuccessListener {
            val fbfruits = mutableListOf<Annons>()
            it.children.forEach { childsnap ->
                var tempad = childsnap.getValue<Annons>()!!
                tempad.adid = childsnap.key!!
                fbfruits.add(tempad)
            }

            val favAnnons = mutableListOf<Annons>()

            val sharedPref = activity?.getSharedPreferences("se.rebecca.bytbok", Context.MODE_PRIVATE)
            var favbooks = sharedPref!!.getStringSet("favbooks", setOf<String>())

            for(book in fbfruits) {
                if(favbooks!!.contains(book.adid)) {
                    favAnnons.add(book)
                }
            }

            aAdapter.filtreradeAnnonser = favAnnons
            aAdapter.notifyDataSetChanged()

            Log.i("pia11debug",fbfruits.toString())

        }
    }

    fun clickReadmore(clickannons : Annons) {
        var goreadmore = GilladeObjektFragmentDirections.actionGilladeObjektFragmentToFardigAnnonsFragment(clickannons.adid)
        findNavController().navigate(goreadmore)
    }
}