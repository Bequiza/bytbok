package se.rebeccazadig.bokholken.myAdverts

class MyAdvertsRepository private constructor() {

    // var aAdapter = AdvertsAdapter()
    // // aAdapter.minSidaFrag = this

    // fun loadBooks() {
    //     val database = Firebase.database

    //     val books = database.getReference("Books")

    //     val mybooks = books.orderByChild("adcreator").equalTo(Firebase.auth.currentUser!!.uid)

    //     val bookListener = object : ValueEventListener {

    //         override fun onDataChange(dataSnapshot: DataSnapshot) {
    //             val fbfruits = mutableListOf<Adverts>()
    //             dataSnapshot.children.forEach { childsnap ->
    //                 var tempad = childsnap.getValue<Adverts>()!!
    //                 tempad.adid = childsnap.key!!
    //                 fbfruits.add(tempad)
    //             }

    //             aAdapter.filtreradeAnnonser = fbfruits
    //             aAdapter.notifyDataSetChanged()

    //             Log.i("pia11debug", fbfruits.toString())
    //         }

    //         override fun onCancelled(databaseError: DatabaseError) {
    //             // Getting Post failed, log a message
    //         }
    //     }
    //     mybooks.addListenerForSingleValueEvent(bookListener)
    // }

    companion object {
        private var instance: MyAdvertsRepository? = null

        fun getInstance() = instance ?: MyAdvertsRepository().also {
            instance = it
        }
    }
}
