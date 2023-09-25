package se.rebeccazadig.bokholken.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import se.rebeccazadig.bokholken.adverts.AdvertsRepository
import se.rebeccazadig.bokholken.data.ContactType
import se.rebeccazadig.bokholken.data.ErrorCode
import se.rebeccazadig.bokholken.data.FireBaseReferences
import se.rebeccazadig.bokholken.models.Advert
import se.rebeccazadig.bokholken.models.User

sealed class Result<out T> {
    data class Failure(val message: String) : Result<Nothing>()
    data class Success<T>(val data: T) : Result<T>()
}

class LoginRepository private constructor() {

    private val advertsRepo = AdvertsRepository.getInstance()

    private val myAuth = Firebase.auth
    private val userDatabaseReference = FireBaseReferences.userDatabaseRef
    private val advertRef = FireBaseReferences.advertDatabaseRef
    private val favoriteRef = FireBaseReferences.favoritesDatabaseRef
    private val storageRef = FireBaseReferences.advertImagesStorageRef


    private val _isLoggedIn = MutableLiveData(myAuth.currentUser != null)
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    init {
        myAuth.addAuthStateListener {
            if (it.currentUser == null) {
                advertsRepo.clearFavorites()
            }
            _isLoggedIn.value = it.currentUser != null
        }
    }

    private suspend fun handleFirebaseExceptions(
        block: suspend () -> Unit
    ): Result<Unit> {
        return try {
            block()
            Result.Success(Unit)
        } catch (e: FirebaseNetworkException) {
            val errorCode = ErrorCode.NETWORK_ERROR.firebaseCode
            Log.i("Emma", "NETWORK FAILURE =$e")
            Result.Failure(errorCode)
        } catch (e: FirebaseAuthException) {
            val errorCode = ErrorCode.fromFirebaseCode(e.errorCode)
            Log.i("Emma", "AUTH FAILURE =$e")
            Result.Failure(errorCode.firebaseCode)
        }
    }

    suspend fun loginInRepo(email: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            delay(1000L)
            handleFirebaseExceptions {
                myAuth.signInWithEmailAndPassword(email, password).await()
            }
        }
    }

    suspend fun registerInRepo(
        email: String, password: String, name: String,
        phone: String, contactMethod: ContactType
    ): Result<Unit> {
        return handleFirebaseExceptions {
            val result = myAuth.createUserWithEmailAndPassword(email, password).await()
            val newUser = User(
                id = result.user!!.uid,
                name = name,
                phoneNumber = phone,
                email = email,
                preferredContactMethod = contactMethod
            )
            userDatabaseReference.child(result.user!!.uid).setValue(newUser).await()
        }
    }

    fun logOutInRepo() {
        myAuth.signOut()
    }

    suspend fun deleteAccount(): Result<Unit> { // Delete account finns i my page Viewmodel
        val user = myAuth.currentUser
            ?: kotlin.run { return Result.Failure("User not found") }
        val userId = user.uid

        return try {
            val userAdverts = advertRef.orderByChild("adCreator").equalTo(userId).get().await()
            userAdverts.children.forEach { advertSnapshot ->
                val advert = advertSnapshot.getValue(Advert::class.java)
                advert?.imageUrl?.let {
                    storageRef.storage.getReferenceFromUrl(it).delete().await()
                }
                advertSnapshot.ref.removeValue().await()
            }
            favoriteRef.child(userId).removeValue().await()
            userDatabaseReference.child(userId).removeValue().await()
            user.delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("DeleteAccount", "Error deleting user account: ${e.message}")
            Result.Failure("${e.message}")
        }
    }

    suspend fun reAuthenticate(email: String, password: String): Result<Unit> {
        val user = myAuth.currentUser
        if (user != null && email.isNotEmpty() && password.isNotEmpty()) {
            val credential = EmailAuthProvider.getCredential(email, password)
            return try {
                user.reauthenticate(credential).await()
                Result.Success(Unit)
            } catch (e: Exception) {
                Log.e("ReAuthenticate", "Error re-authenticating: ${e.message}")
                Result.Failure(e.message ?: "Error during re-authentication.")
            }
        }
        return Result.Failure("Error during re-authentication.")
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            myAuth.sendPasswordResetEmail(email).await()
            Result.Success(Unit)
        } catch (e: FirebaseAuthException) {
            Log.e("ResetPassword", "Error sending password reset email: ${e.errorCode}")
            Result.Failure(e.errorCode)
        }
    }

    fun getUserId(): String {
        val uid = myAuth.currentUser?.uid
        return if (uid == null) {
            assert(false) { "At this point currentUser must not (cannot) be null." }
            ""
        } else {
            uid
        }
    }

    companion object {
        private var instance: LoginRepository? = null

        fun getInstance() = instance ?: LoginRepository().also {
            instance = it
        }
    }
}