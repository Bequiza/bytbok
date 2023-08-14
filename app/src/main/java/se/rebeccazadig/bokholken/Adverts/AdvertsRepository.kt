package se.rebeccazadig.bokholken.adverts

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdvertsRepository private constructor() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseRef = FirebaseDatabase.getInstance().getReference("Books")
    private val favoritesRef = FirebaseDatabase.getInstance().getReference("favorites")

    private val _advertsLiveData = MutableLiveData<List<Adverts>>()
    val advertsLiveData: LiveData<List<Adverts>> get() = _advertsLiveData

    init {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val advertsList = dataSnapshot.children.mapNotNull { it.getValue(Adverts::class.java) }

                // Sort the list based on creationTime in descending order
                val sortedList = advertsList.sortedByDescending { it.creationTime }

                _advertsLiveData.value = sortedList
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle and possibly log the error
            }
        })
    }

    fun saveAdvert(advert: Adverts) {
        val adId = databaseRef.push().key
        advert.adId = adId ?: ""
        advert.adCreator = firebaseAuth.currentUser?.uid ?: ""

        // Set the creationTime to the current epoch timestamp
        advert.creationTime = System.currentTimeMillis()

        if (adId != null) {
            databaseRef.child(adId).setValue(advert)
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> return true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> return true
            }
        }
        return false
    }


    /*fun fetchFavorites(): LiveData<List<Adverts>> {
        val userId = firebaseAuth.currentUser?.uid ?: return MutableLiveData()
        val liveData = MutableLiveData<List<Adverts>>()

        favoritesRef.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val adverts = mutableListOf<Adverts>()
                for (advertSnapshot in dataSnapshot.children) {
                    val advert = advertSnapshot.getValue(Adverts::class.java)
                    advert?.let {
                        adverts.add(it)
                    }
                }
                liveData.value = adverts
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })

        return liveData
    }*/


    companion object {
        private var instance: AdvertsRepository? = null

        fun getInstance(): AdvertsRepository {
            if (instance == null) {
                instance = AdvertsRepository()
            }
            return instance!!
        }
    }
}

