package se.rebeccazadig.bokholken.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

sealed class Result {
    data class Failure(val message: String) : Result()
    object Success : Result()
}

class LoginRepository private constructor() /*primary constructor*/ {

    private val myAuth = Firebase.auth

    // val isLoggedIn = MutableLiveData(true)
    private val _isLoggedIn = MutableLiveData(myAuth.currentUser != null)
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    init {
        myAuth.addAuthStateListener {
            _isLoggedIn.value = it.currentUser != null
        }
    }

    suspend fun loginInRepo(email: String, password: String)/*input*/: Result /*output*/ {
        Log.i("Emma", "LOGIN IN REPOSITORY email=$email password=$password")

        return try {
            val result = myAuth.signInWithEmailAndPassword(email, password).await()
            Log.i("Emma", "loginInRepo SUCCESS=$result")
            Result.Success
        } catch (e: Exception) {
            Log.i("Emma", "loginInRepo FAILURE =$e")
            Result.Failure("${e.message}")
        }
    }

    suspend fun registerInRepo(email: String, password: String): Result {
        Log.i("Emma", "REGISTER USER IN REPOSITORY email=$email password=$password")

        return try {
            val result = myAuth.createUserWithEmailAndPassword(email, password).await()
            Log.i("Emma", "registerInRepo SUCCESS=$result")
            Result.Success
        } catch (e: Exception) {
            Log.i("Emma", "registerInRepo FAILURE =$e")
            Result.Failure("${e.message}")
        }
    }

    fun isLoggedIn(): Boolean {
        return myAuth.currentUser != null
    }

    fun logOutInRepo() {
        myAuth.signOut()
    }

    suspend fun deleteAccount(): Result { // Delete account finns i my page Viewmodel
        val user = myAuth.currentUser
            ?: kotlin.run { return Result.Failure("") }

        return try {
            user.delete().await()
            Result.Success
        } catch (e: Exception) {
            Log.e("DeleteAccount", "Error deleting user account: ${e.message}")
            Result.Failure("${e.message}")
        }
    }

    suspend fun reAuthenticate(email: String, password: String): Result {
        val user = myAuth.currentUser
        if (user != null && email.isNotEmpty() && password.isNotEmpty()) {
            val credential = EmailAuthProvider.getCredential(email, password)
            return try {
                user.reauthenticate(credential).await()
                Result.Success
            } catch (e: Exception) {
                Log.e("ReAuthenticate", "Error re-authenticating: ${e.message}")
                Result.Failure(e.message ?: "Error during re-authentication.")
            }
        }
        return Result.Failure("Error during re-authentication.")
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