package com.example.bytbok

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import layout.AnnonsAdapter
import java.util.*
import kotlin.collections.ArrayList

class AnnonsFragment : Fragment() {

    var aAdapter = AnnonsAdapter()
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_annons, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchView = view.findViewById(R.id.searchView)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
               filterList(newText)
                return true
            }

        })

        aAdapter.annonsfrag = this

        val annonsRecview = view.findViewById<RecyclerView>(R.id.allaAnnonserRV)

        val layoutmanager = GridLayoutManager(context, 2)
        annonsRecview.layoutManager = layoutmanager

        annonsRecview.adapter = aAdapter

        aAdapter.notifyDataSetChanged()

        loadBooks()

        view.findViewById<Button>(R.id.nyAnnonsButton).setOnClickListener {

            var goreadmore = AnnonsFragmentDirections.actionAnnonsFragmentToSkapaAnnonsFragment("")
            findNavController().navigate(goreadmore)
        }
    }

    fun clickReadmore(clickannons : Annons) {
        var goreadmore = AnnonsFragmentDirections.actionAnnonsFragmentToFardigAnnonsFragment(clickannons.adid)
        findNavController().navigate(goreadmore)
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


            view?.let { fragview ->
                //fragview.findViewById<TextView>(R.id.antalAnnonserTV).text = fbfruits.size.toString()
                fragview.findViewById<TextView>(R.id.bytBokInfoTV).text = "Alla tillgängliga böcker " + fbfruits.size.toString()
            }

            aAdapter.filtreradeAnnonser = fbfruits
            aAdapter.allaAnnonser = fbfruits
            aAdapter.notifyDataSetChanged()

            Log.i("pia11debug",fbfruits.toString())

        }
    }

    private fun filterList(query : String?) {
        if (query != null) {
            val filteredList = mutableListOf<Annons>()
            for (i in aAdapter.allaAnnonser) {
                if (i.stad!!.lowercase().contains(query.lowercase())) {
                    filteredList.add(i)
                    continue
                }
                if (i.bokTitel!!.lowercase().contains(query.lowercase())) {
                    filteredList.add(i)
                    continue
                }
                if (i.bokForfattare!!.lowercase().contains(query.lowercase())) {
                    filteredList.add(i)
                    continue
                }
            }

            aAdapter.filtreradeAnnonser = filteredList
            aAdapter.notifyDataSetChanged()

            if (filteredList.isEmpty()) {
                Toast.makeText(requireContext(), "Hittade inget", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
