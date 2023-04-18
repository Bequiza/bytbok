package com.example.bytbok

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import layout.AnnonsAdapter

class MinSidaFragment : Fragment() {

    var aAdapter = AnnonsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_min_sida, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        aAdapter.minSidaFrag = this


        val annonsRecview = view.findViewById<RecyclerView>(R.id.minaAnnonserRV)

        val layoutmanager = GridLayoutManager(context, 2)
        annonsRecview.layoutManager = layoutmanager


        annonsRecview.adapter = aAdapter

        aAdapter.notifyDataSetChanged()

        loadBooks()

        view.findViewById<Button>(R.id.raderaKontoButton).setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Are you sure you want to Delete?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    deleteaccount()
                }
                .setNegativeButton("No") { dialog, id ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }

        view.findViewById<Button>(R.id.logOutButton).setOnClickListener {
            Firebase.auth.signOut()
        }

    }

    fun deleteaccount() {

        val user = Firebase.auth.currentUser!!

        Log.d("PIADELETE", "User " + user.uid)


        user.delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("PIADELETE", "User account deleted.")
                } else {
                    Log.d("PIADELETE", "DELETE FAIL ")
                    Log.d("PIADELETE", task.exception!!.toString())
                }
            }
    }

    fun clickReadmore(clickannons : Annons) {
        var goreadmore = MinSidaFragmentDirections.actionMinSidaFragmentToSkapaAnnonsFragment(clickannons.adid)
        findNavController().navigate(goreadmore)
    }

    fun loadBooks() {

        val database = Firebase.database

        val books = database.getReference("Books")

        val mybooks = books.orderByChild("adcreator").equalTo(Firebase.auth.currentUser!!.uid)

        val bookListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val fbfruits = mutableListOf<Annons>()
                dataSnapshot.children.forEach { childsnap ->
                    var tempad = childsnap.getValue<Annons>()!!
                    tempad.adid = childsnap.key!!
                    fbfruits.add(tempad)
                }

                aAdapter.filtreradeAnnonser = fbfruits
                aAdapter.notifyDataSetChanged()

                Log.i("pia11debug",fbfruits.toString())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
            }
        }
        mybooks.addListenerForSingleValueEvent(bookListener)

    }
}