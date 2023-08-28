package se.rebeccazadig.bokholken.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import se.rebeccazadig.bokholken.data.User

sealed class Result<out T> {
    data class Failure(val message: String) : Result<Nothing>()
    data class Success<T>(val data: T) : Result<T>()
}

class LoginRepository private constructor(private val userStorage: IUserStorage) /*primary constructor*/ {

    private val myAuth = Firebase.auth
    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("users")

    val isLoggedIn = MutableLiveData(true)
    private val _isLoggedIn = MutableLiveData(myAuth.currentUser != null)
   //val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    var cachedUser: User? = null
        private set

    /*init {
        myAuth.addAuthStateListener {
            _isLoggedIn.value = it.currentUser != null
        }
    }*/

    fun isLoggedIn(): Boolean {
        val isLoggedIn = myAuth.currentUser != null
        if (isLoggedIn) cachedUser = userStorage.loadUser()
        return isLoggedIn
    }

    suspend fun loginInRepo(email: String, password: String)/*input*/: Result<Unit> /*output*/ {
        Log.i("Emma", "LOGIN IN REPOSITORY email=$email password=$password")

        return withContext(Dispatchers.IO) {
            delay(1000L)
            try {
                myAuth.signInWithEmailAndPassword(email, password).await()
                val user =
                    databaseReference.child(myAuth.currentUser!!.uid).get().await().getValue(User::class.java)
                if (user != null) {
                    cacheUser(user)
                }
                Result.Success(Unit)
            } catch (e: Exception) {
                Log.i("Emma", "loginInRepo FAILURE =$e")
                Result.Failure("${e.message}")
            }
        }
    }

    suspend fun registerInRepo(email: String, password: String): Result<Unit> {
        Log.i("Emma", "REGISTER USER IN REPOSITORY email=$email password=$password")

        return try {
            val result = myAuth.createUserWithEmailAndPassword(email, password).await()
            Log.i("Emma", "registerInRepo SUCCESS=$result")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.i("Emma", "registerInRepo FAILURE =$e")
            Result.Failure("${e.message}")
        }
    }

    fun logOutInRepo() {
        myAuth.signOut()
    }

    suspend fun deleteAccount(): Result<Unit> { // Delete account finns i my page Viewmodel
        val user = myAuth.currentUser
            ?: kotlin.run { return Result.Failure("") }

        return try {
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


    fun getUserId(): String {
        val uid = myAuth.currentUser?.uid
        return if (uid == null) {
            assert(false) { "At this point currentUser must not (cannot) be null." }
            ""
        } else {
            uid
        }
    }

    fun cacheUser(user: User) {
        cachedUser = user
        userStorage.saveUser(user)
    }

    private fun clearCachedUser() {
        cachedUser = null
        userStorage.deleteUser()
    }

    companion object {
        private var instance: LoginRepository? = null

        fun getInstance(
            userStorage: IUserStorage,
        ): LoginRepository = instance ?: LoginRepository(userStorage).also {
            instance = it
        }
    }
}