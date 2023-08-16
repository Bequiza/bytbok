package se.rebeccazadig.bokholken.adverts

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import se.rebeccazadig.bokholken.data.Advert
import se.rebeccazadig.bokholken.data.Result

class AdvertsRepository private constructor() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseRef = FirebaseDatabase.getInstance().getReference(NODE_BOOKS)

    private val _advertsLiveData = MutableLiveData<List<Advert>>()
    val advertsLiveData: LiveData<List<Advert>> get() = _advertsLiveData

    init {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val advertsList = dataSnapshot.children.mapNotNull { it.getValue(Advert::class.java) }
                _advertsLiveData.value = advertsList.sortedByDescending { it.creationTime }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("DatabaseError", "Error fetching data: ${databaseError.message}")
            }
        })
    }

    suspend fun saveAdvert(advert: Advert): Result {
        delay(2_000)
        return withContext(Dispatchers.IO) {
            try {
                val adId = databaseRef.push().key ?: return@withContext Result.Failure("Failed to generate Ad ID")
                advert.adId = adId
                advert.adCreator = firebaseAuth.currentUser?.uid ?: ""
                advert.creationTime = System.currentTimeMillis()
                databaseRef.child(adId).setValue(advert).await()
                Result.Success
            } catch (e: Exception) {
                Result.Failure(e.message ?: "Error saving advert!")
            }
        }
    }

    companion object {
        private const val NODE_BOOKS = "Books"

        private var instance: AdvertsRepository? = null

        fun getInstance(): AdvertsRepository {
            if (instance == null) {
                instance = AdvertsRepository()
            }
            return instance!!
        }
    }
}

