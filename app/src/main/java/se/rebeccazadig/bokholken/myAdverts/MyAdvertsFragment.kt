package se.rebeccazadig.bokholken.myAdverts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import se.rebeccazadig.bokholken.databinding.FragmentMyAdvertsBinding

class MyAdvertsFragment : Fragment() {

    private val viewModel: MyAdvertsViewModel by viewModels() // viewmodel
    private lateinit var binding: FragmentMyAdvertsBinding

//    var aAdapter = AdvertsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMyAdvertsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this // databinding
        binding.vm = viewModel // databinding kopplad till denna fragmentets viewmodel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        aAdapter.minSidaFrag = this
//
//        val annonsRecview = binding.myAdvertsRV
//
//        val layoutmanager = GridLayoutManager(context, 2)
//        annonsRecview.layoutManager = layoutmanager
//
//        annonsRecview.adapter = aAdapter
//
//        aAdapter.notifyDataSetChanged()
//
//        loadBooks()
    }

//    fun clickReadmore(clickannons: Advert) {
//        //  var goreadmore =
//        //      MyPageFragmentDirections.actionMyPageFragmentToCreateAdvertsFragment(clickannons.adid)
//        //  findNavController().navigate(goreadmore)
//    }

//    fun loadBooks() {
//        val database = Firebase.database
//
//        val books = database.getReference("Books")
//
//        val mybooks = books.orderByChild("adcreator").equalTo(Firebase.auth.currentUser!!.uid)
//
//        val bookListener = object : ValueEventListener {
//
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val fbfruits = mutableListOf<Advert>()
//                dataSnapshot.children.forEach { childsnap ->
//                    var tempad = childsnap.getValue<Advert>()!!
//                    tempad.adid = childsnap.key!!
//                    fbfruits.add(tempad)
//                }
//
//                aAdapter.filtreradeAnnonser = fbfruits
//                aAdapter.notifyDataSetChanged()
//
//                Log.i("pia11debug", fbfruits.toString())
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Getting Post failed, log a message
//            }
//        }
//        mybooks.addListenerForSingleValueEvent(bookListener)
//    }
}
