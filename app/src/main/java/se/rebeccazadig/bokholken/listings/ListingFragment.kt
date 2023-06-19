package se.rebeccazadig.bokholken.listings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import se.rebeccazadig.bokholken.R
import se.rebeccazadig.bokholken.databinding.FragmentListingBinding

class ListingFragment : Fragment() {

    var aAdapter = ListingAdapter()
    private lateinit var searchView: SearchView

    private val viewModel: ListingViewModel by viewModels()
    private var _binding: FragmentListingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentListingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchView = binding.searchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })

        aAdapter.annonsfrag = this

        val annonsRecview = binding.allaAnnonserRV

        val layoutmanager = GridLayoutManager(context, 2)
        annonsRecview.layoutManager = layoutmanager

        annonsRecview.adapter = aAdapter

        aAdapter.notifyDataSetChanged()

        loadBooks()

        binding.nyAnnonsButton.setOnClickListener {
            var goreadmore = ListingFragmentDirections.actionAnnonsFragmentToSkapaAnnonsFragment("")
            findNavController().navigate(goreadmore)
        }
    }

    fun clickReadmore(clickannons: Listing) {
        var goreadmore =
            ListingFragmentDirections.actionAnnonsFragmentToFardigAnnonsFragment(clickannons.adid)
        findNavController().navigate(goreadmore)
    }

    fun loadBooks() {
        val database = Firebase.database

        val books = database.getReference("Books")

        books.get().addOnSuccessListener {
            val fbfruits = mutableListOf<Listing>()
            it.children.forEach { childsnap ->
                var tempad = childsnap.getValue<Listing>()!!
                tempad.adid = childsnap.key!!
                fbfruits.add(tempad)
            }

            view?.let { fragview ->
                // fragview.findViewById<TextView>(R.id.antalAnnonserTV).text = fbfruits.size.toString()
                fragview.findViewById<TextView>(R.id.bytBokInfoTV).text =
                    "Alla tillgängliga böcker " + fbfruits.size.toString()
            }

            aAdapter.filtreradeAnnonser = fbfruits
            aAdapter.allaAnnonser = fbfruits
            aAdapter.notifyDataSetChanged()

            Log.i("pia11debug", fbfruits.toString())
        }
    }

    private fun filterList(query: String?) {
        if (query != null) {
            val filteredList = mutableListOf<Listing>()
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
