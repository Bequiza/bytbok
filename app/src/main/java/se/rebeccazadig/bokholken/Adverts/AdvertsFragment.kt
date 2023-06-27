package se.rebeccazadig.bokholken.Adverts // ktlint-disable package-name

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
import se.rebeccazadig.bokholken.databinding.FragmentAdvertsBinding

class AdvertsFragment : Fragment() {

//    var aAdapter = AdvertsAdapter()
//    private lateinit var searchView: SearchView

    private val viewModel: AdvertViewModel by viewModels()
    private var _binding: FragmentAdvertsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAdvertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        searchView = binding.searchView
//
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                return false
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                filterList(newText)
//                return true
//            }
//        })
//
//        aAdapter.annonsfrag = this
//
//        val annonsRecview = binding.allAdvertsRV
//
//        val layoutmanager = GridLayoutManager(context, 2)
//        annonsRecview.layoutManager = layoutmanager
//
//        annonsRecview.adapter = aAdapter
//
//        aAdapter.notifyDataSetChanged()
//
//        loadBooks()
//
//        binding.newAdvertButton.setOnClickListener {
//            var goreadmore = AdvertsFragmentDirections.actionAdvertsFragmentToCreateAdvertsFragment("")
//            findNavController().navigate(goreadmore)
//        }
    }

//    fun clickReadmore(clickannons: Adverts) {
//        var goreadmore =
//            AdvertsFragmentDirections.actionAdvertsFragmentToPublishedAdvertsFragment(clickannons.adid)
//        findNavController().navigate(goreadmore)
//    }
//
//    fun loadBooks() {
//        val database = Firebase.database
//
//        val books = database.getReference("Books")
//
//        books.get().addOnSuccessListener {
//            val fbfruits = mutableListOf<Adverts>()
//            it.children.forEach { childsnap ->
//                var tempad = childsnap.getValue<Adverts>()!!
//                tempad.adid = childsnap.key!!
//                fbfruits.add(tempad)
//            }
//
//            view?.let { fragview ->
//                // fragview.findViewById<TextView>(R.id.antalAnnonserTV).text = fbfruits.size.toString()
//                fragview.findViewById<TextView>(R.id.InfoAdvertsTV).text =
//                    "Alla tillgängliga böcker " + fbfruits.size.toString()
//            }
//
//            aAdapter.filtreradeAnnonser = fbfruits
//            aAdapter.allaAnnonser = fbfruits
//            aAdapter.notifyDataSetChanged()
//
//            Log.i("pia11debug", fbfruits.toString())
//        }
//    }
//
//    private fun filterList(query: String?) {
//        if (query != null) {
//            val filteredList = mutableListOf<Adverts>()
//            for (i in aAdapter.allaAnnonser) {
//                if (i.city!!.lowercase().contains(query.lowercase())) {
//                    filteredList.add(i)
//                    continue
//                }
//                if (i.title!!.lowercase().contains(query.lowercase())) {
//                    filteredList.add(i)
//                    continue
//                }
//                if (i.author!!.lowercase().contains(query.lowercase())) {
//                    filteredList.add(i)
//                    continue
//                }
//            }
//
//            aAdapter.filtreradeAnnonser = filteredList
//            aAdapter.notifyDataSetChanged()
//
//            if (filteredList.isEmpty()) {
//                Toast.makeText(requireContext(), "Hittade inget", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
}
