package se.rebeccazadig.bokholken.mypage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import se.rebeccazadig.bokholken.databinding.FragmentMyPageBinding
import se.rebeccazadig.bokholken.listings.Listing
import se.rebeccazadig.bokholken.listings.ListingAdapter

class MyPageFragment : Fragment() {

    private val viewModel: MyPageViewModel by viewModels() // viewmodel
    private lateinit var binding: FragmentMyPageBinding

    var aAdapter = ListingAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMyPageBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this // databinding
        binding.vm = viewModel // databinding kopplad till denna fragmentets viewmodel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        aAdapter.minSidaFrag = this

        val annonsRecview = binding.minaAnnonserRV

        val layoutmanager = GridLayoutManager(context, 2)
        annonsRecview.layoutManager = layoutmanager

        annonsRecview.adapter = aAdapter

        aAdapter.notifyDataSetChanged()

        loadBooks()

        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->

            uiState.message?.let/*om allt till vänster om ?+.let inte null, visa toast*/ {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun clickReadmore(clickannons: Listing) {
        var goreadmore =
            MyPageFragmentDirections.actionMinSidaFragmentToSkapaAnnonsFragment(clickannons.adid)
        findNavController().navigate(goreadmore)
    }

    fun loadBooks() {
        val database = Firebase.database

        val books = database.getReference("Books")

        val mybooks = books.orderByChild("adcreator").equalTo(Firebase.auth.currentUser!!.uid)

        val bookListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val fbfruits = mutableListOf<Listing>()
                dataSnapshot.children.forEach { childsnap ->
                    var tempad = childsnap.getValue<Listing>()!!
                    tempad.adid = childsnap.key!!
                    fbfruits.add(tempad)
                }

                aAdapter.filtreradeAnnonser = fbfruits
                aAdapter.notifyDataSetChanged()

                Log.i("pia11debug", fbfruits.toString())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
            }
        }
        mybooks.addListenerForSingleValueEvent(bookListener)
    }
}
