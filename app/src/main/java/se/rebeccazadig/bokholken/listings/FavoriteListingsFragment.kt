package se.rebeccazadig.bokholken.listings

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
// import se.rebeccazadig.bokholken.GilladeObjektFragmentDirections
import se.rebeccazadig.bokholken.R

class FavoriteListingsFragment : Fragment() {

    var aAdapter = ListingAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_favorite_listings, container, false)
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

        // Koden nedan är fel nånstans
        /*view.findViewById<Button>(R.id.lasMerAnnonsButton).setOnClickListener {

         findNavController().navigate(R.id.action_gilladeObjektFragment_to_fardigAnnonsFragment)
         }*/
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

            val favAnnons = mutableListOf<Listing>()

            val sharedPref =
                activity?.getSharedPreferences("se.rebecca.bytbok", Context.MODE_PRIVATE)
            var favbooks = sharedPref!!.getStringSet("favbooks", setOf<String>())

            for (book in fbfruits) {
                if (favbooks!!.contains(book.adid)) {
                    favAnnons.add(book)
                }
            }

            aAdapter.filtreradeAnnonser = favAnnons
            aAdapter.notifyDataSetChanged()

            Log.i("pia11debug", fbfruits.toString())
        }
    }

    fun clickReadmore(clickannons: Listing) {
        var goreadmore =
            FavoriteListingsFragmentDirections.actionGilladeObjektFragmentToFardigAnnonsFragment(
                clickannons.adid,
            )
        findNavController().navigate(goreadmore)
    }
}
