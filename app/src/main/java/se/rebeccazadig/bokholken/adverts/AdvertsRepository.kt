package se.rebeccazadig.bokholken.adverts

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import se.rebeccazadig.bokholken.data.FireBaseReferences
import se.rebeccazadig.bokholken.data.Result
import se.rebeccazadig.bokholken.data.User
import se.rebeccazadig.bokholken.models.Advert
import se.rebeccazadig.bokholken.utils.ImageUtils.Companion.toByteArray
import java.util.UUID

private const val SIMULATE_DELAY = 1000L

class AdvertsRepository private constructor() {

    private val firebaseAuth = FireBaseReferences.authInstance
    private val advertRef = FireBaseReferences.advertDatabaseRef
    private val usersRef = FireBaseReferences.userDatabaseRef
    private val favoritesRef = FireBaseReferences.favoritesDatabaseRef
    private val storageRef = FireBaseReferences.advertImagesStorageRef

    private val _advertsLiveData = MutableLiveData<List<Advert>>()
    val advertsLiveData: LiveData<List<Advert>> get() = _advertsLiveData

    private val _advertDetailLiveData = MutableLiveData<Pair<Advert, User?>?>()
    val advertDetailLiveData: LiveData<Pair<Advert, User?>?> get() = _advertDetailLiveData

    private val _favoritesLiveData = MutableLiveData<List<Advert>>()
    val favoritesLiveData: LiveData<List<Advert>> get() = _favoritesLiveData


    init {
        fetchUsersAndInitializeAdverts()
    }

    private fun fetchUsersAndInitializeAdverts() {
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(usersSnapshot: DataSnapshot) {
                initializeAdvertsListener()
            }
            override fun onCancelled(error: DatabaseError) = logError("Error fetching users data", error)
        })
    }

    private fun initializeAdvertsListener() {
        advertRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val advertsList = getAdvertsListFromSnapshot(dataSnapshot)
                _advertsLiveData.value = sortAdvertsByCreationTime(advertsList)
            }
            override fun onCancelled(error: DatabaseError) = logError("Error fetching data", error)
        })
    }

    private fun getAdvertsListFromSnapshot(snapshot: DataSnapshot): List<Advert> {
        return snapshot.children.mapNotNull { it.getValue(Advert::class.java) }
    }

    private fun sortAdvertsByCreationTime(adverts: List<Advert>): List<Advert> {
        return adverts.sortedByDescending { it.creationTime }
    }

    private fun logError(message: String, error: DatabaseError) {
        Log.e("DatabaseError", "$message: ${error.message}")
    }

    suspend fun markAdvertAsFavorite(advertId: String) {
        val userId = getCurrentUserId() ?: return
        favoritesRef.child(userId).child(advertId).setValue(true).await()
    }

    suspend fun removeAdvertFromFavorites(advertId: String) {
        val userId = getCurrentUserId() ?: return
        favoritesRef.child(userId).child(advertId).removeValue().await()
    }

    suspend fun isAdvertFavorite(advertId: String): Boolean {
        val userId = getCurrentUserId() ?: return false
        val snapshot = favoritesRef.child(userId).child(advertId).get().await()
        return snapshot.exists()
    }

    fun fetchAdvertsAndUsers() {
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(usersSnapshot: DataSnapshot) {
                val usersMap = mutableMapOf<String, User>()
                for (snapshot in usersSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)
                    user?.id?.let { usersMap[it] = user }
                }
                fetchAdverts()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseError", "Error fetching users data: ${error.message}")
            }
        })
    }

    private fun fetchAdverts() {
        advertRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val advertsList = dataSnapshot.children.mapNotNull { it.getValue(Advert::class.java) }
                _advertsLiveData.value = advertsList.sortedByDescending { it.creationTime }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseError", "Error fetching data: ${error.message}")
            }
        })
    }

    fun fetchAdvertDetails(advertId: String) {
        advertRef.child(advertId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val advert = dataSnapshot.getValue(Advert::class.java)
                if (advert != null) {
                    val userId = advert.adCreator
                    usersRef.child(userId ?: "").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            val user = userSnapshot.getValue(User::class.java)
                            _advertDetailLiveData.postValue(Pair(advert, user))
                        }

                        override fun onCancelled(userError: DatabaseError) {
                            Log.e("DatabaseError", "Error fetching user data: ${userError.message}")
                        }
                    })
                } else {
                    _advertDetailLiveData.postValue(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseError", "Error fetching advert data: ${error.message}")
                _advertDetailLiveData.postValue(null)
            }
        })
    }

    fun fetchFavoriteAdverts() {
        val userId = getCurrentUserId() ?: return
        favoritesRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val favoriteAdIds = dataSnapshot.children.mapNotNull { it.key }

                advertRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(allAdvertsSnapshot: DataSnapshot) {
                        val allAdverts = allAdvertsSnapshot.children.mapNotNull { it.getValue(Advert::class.java) }
                        val favoriteAdverts = allAdverts.filter { it.adId in favoriteAdIds }
                        _favoritesLiveData.value = favoriteAdverts
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("DatabaseError", "Error fetching all adverts: ${error.message}")
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseError", "Error fetching favorite adverts: ${error.message}")
            }
        })
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
                    val adId = advert.adId ?: throw IllegalArgumentException("Advert ID cannot be null during update")
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
                return@withContext Result.Failure(e.message ?: "Error deleting image from database")
            }
            Result.Success
        }
    }

    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    private fun generateRandomAdId(): String {
        return UUID.randomUUID().toString()
    }

    fun cleanUp() {
        _advertDetailLiveData.value = null
    }

    fun clearFavorites() {
        _favoritesLiveData.value = emptyList()
    }

    companion object {
        private var instance: AdvertsRepository? = null

        fun getInstance() = instance ?: AdvertsRepository().also {
            instance = it
        }
    }
}