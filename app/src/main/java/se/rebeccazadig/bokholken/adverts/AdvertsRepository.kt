package se.rebeccazadig.bokholken.adverts

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import se.rebeccazadig.bokholken.data.Result
import se.rebeccazadig.bokholken.data.User
import se.rebeccazadig.bokholken.models.Advert
import se.rebeccazadig.bokholken.utils.ImageUtils.Companion.toByteArray
import java.util.UUID

private const val SIMULATE_DELAY = 1000L

class AdvertsRepository private constructor() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val advertRef = FirebaseDatabase.getInstance().getReference("Adverts")
    private val usersRef = FirebaseDatabase.getInstance().getReference("users")
    private val storageRef = Firebase.storage.reference.child("advert_images")

    private val _advertsLiveData = MutableLiveData<List<Advert>>()
    val advertsLiveData: LiveData<List<Advert>> get() = _advertsLiveData

    private val _advertDetailLiveData = MutableLiveData<Pair<Advert, User?>?>()
    val advertDetailLiveData: LiveData<Pair<Advert, User?>?> get() = _advertDetailLiveData


    init {
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(usersSnapshot: DataSnapshot) {
                val usersMap = mutableMapOf<String, User>()
                for (snapshot in usersSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)
                    user?.id?.let { usersMap[it] = user }
                }

                advertRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val advertsList =
                            dataSnapshot.children.mapNotNull { it.getValue(Advert::class.java) }
                        _advertsLiveData.value = advertsList.sortedByDescending { it.creationTime }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("DatabaseError", "Error fetching data: ${error.message}")
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseError", "Error fetching users data: ${error.message}")
            }
        })
    }

    suspend fun fetchAdvertAndUserDetails(advertId: String) {
        withContext(Dispatchers.IO) {
            try {
                val advertSnapshot = advertRef.child(advertId).get().await()
                val advertData = advertSnapshot.getValue(Advert::class.java)

                val userId = advertData?.adCreator
                if (userId != null) {
                    val userSnapshot = usersRef.child(userId).get().await()
                    val userData = userSnapshot.getValue(User::class.java)
                    _advertDetailLiveData.postValue(Pair(advertData, userData))
                } else {
                    _advertDetailLiveData.postValue(null)
                    Log.e("AdvertsRepository", "Advert data was null for advertId: $advertId")
                }
            } catch (e: Exception) {
                Log.e("AdvertsRepository", "Error fetching advert and user details", e)
            }
        }
    }

    suspend fun fetchAdvertDetails(advertId: String): Advert? {
        return withContext(Dispatchers.IO) {
            try {
                val advertSnapShot = advertRef.child(advertId).get().await()
                advertSnapShot.getValue(Advert::class.java)
            } catch (e: Exception) {
                Log.e("advertRepo", "Error fetching advert details", e)
                null
            }
        }
    }

    suspend fun saveAdvert(advert: Advert, adImage: Bitmap?): Result {
        delay(SIMULATE_DELAY)
        return withContext(Dispatchers.IO) {
            try {
                val adId = generateRandomAdId()
                advert.adId = adId
                advert.adCreator = getCurrentUserId()
                advert.creationTime = System.currentTimeMillis()

                adImage?.let {
                    val imageRef = storageRef.child(adId)
                    val data = it.toByteArray()
                    imageRef.putBytes(data).await()

                    val imageUrl = imageRef.downloadUrl.await().toString()
                    advert.imageUrl = imageUrl
                }
                advertRef.child(adId).setValue(advert).await()
                Result.Success
            } catch (e: Exception) {
                Result.Failure(e.message ?: "Error saving advert!")
            }
        }
    }

    suspend fun updateAdvert(advert: Advert, newImage: Bitmap?): Result {
        delay(SIMULATE_DELAY)
        return withContext(Dispatchers.IO) {
            try {
                newImage?.let {
                    val adId = advert.adId ?: throw IllegalArgumentException("Advert id cannot be null during update")
                    val imageRef = storageRef.child(adId)
                    val data = it.toByteArray()
                    imageRef.putBytes(data).await()

                    val imageUrl = imageRef.downloadUrl.await().toString()
                    advert.imageUrl = imageUrl
                }

                advertRef.child(advert.adId!!).setValue(advert).await()
                Result.Success
            } catch (e: Exception) {
                Result.Failure(e.message ?: "Error updating advert!")
            }
        }
    }

    suspend fun deleteAdvert(adId: String): Result {
        return withContext(Dispatchers.IO) {
            try {
                advertRef.child(adId).removeValue().await()
            } catch (e: Exception) {
                return@withContext Result.Failure(e.message ?: "Error deleting advert")
            }
            try {
                storageRef.child(adId).delete().await()
            } catch (e: Exception) {
                return@withContext Result.Failure(e.message ?: "Error deleting image fron database")
            }
            Result.Success
        }
    }

    suspend fun updateAdvert(advert: Advert, adImage: Bitmap?, adId: String): Result {
        return withContext(Dispatchers.IO) {
            try {
                adImage?.let {
                    val imageRef = storageRef.child(adId)
                    val data = it.toByteArray()
                    imageRef.putBytes(data).await()
                    val imageUrl = imageRef.downloadUrl.await().toString()
                    advert.imageUrl = imageUrl
                }

                advertRef.child(adId).setValue(advert).await()
                Result.Success
            } catch (e: Exception) {
                Result.Failure(e.message ?: "Error updating advert!")
            }
        }
    }

    private fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    private fun generateRandomAdId(): String {
        return UUID.randomUUID().toString()
    }

    fun cleanUp() {
        _advertDetailLiveData.value = null
    }

    companion object {
        private var instance: AdvertsRepository? = null

        fun getInstance() = instance ?: AdvertsRepository().also {
            instance = it
        }
    }
}

