package se.rebeccazadig.bokholken.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    data class Failure(val message: String) : AuthResult()
    object Success : AuthResult()
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

    suspend fun loginInRepo(email: String, password: String)/*input*/: AuthResult /*output*/ {
        Log.i("Emma", "LOGIN IN REPOSITORY email=$email password=$password")

        return try {
            val result = myAuth.signInWithEmailAndPassword(email, password).await()
            if (result.user != null) {
                AuthResult.Success
            } else {
                AuthResult.Failure(message = "User does not exist")
            }
        } catch (e: Exception) {
            AuthResult.Failure("${e.message}")
        }
    }

    suspend fun registerInRepo(email: String, password: String): AuthResult {
        Log.i("Emma", "REGISTER USER IN REPOSITORY email=$email password=$password")

        return try {
            val result = myAuth.createUserWithEmailAndPassword(email, password).await()
            Log.i("Emma", "registerInRepo SUCCESS=$result")
            AuthResult.Success
        } catch (e: Exception) {
            Log.i("Emma", "registerInRepo FAILURE =$e")
            AuthResult.Failure("${e.message}") // Är detta rätt?
            // Ska inte visa hardcoded message, ska visa exception meddelande från firebase
        }
    }

    fun isLoggedIn(): Boolean {
        return myAuth.currentUser != null
    }

    fun logOutInRepo() {
        myAuth.signOut()
    }

    suspend fun deleteAccount(): AuthResult { // Delete account finns i my page Viewmodel
        val user = myAuth.currentUser
            ?: kotlin.run { return AuthResult.Failure("") }

        return try {
            user.delete().await()
            AuthResult.Success
        } catch (e: Exception) {
            Log.e("DeleteAccount", "Error deleting user account: ${e.message}")
            AuthResult.Failure("${e.message}")
        }
    }
    companion object {
        private var instance: LoginRepository? = null

        fun getInstance() = instance ?: LoginRepository().also {
            instance = it
        }
    }
}

// auth.signInWithEmailAndPassword(userEmail, userPassword)
//                .addOnCompleteListener(requireActivity()) { task ->
//                    if (task.isSuccessful) {
//                        // Sign in success, update UI with the signed-in user's information
//                        // Toast.makeText(requireContext(), "Login ok", Toast.LENGTH_SHORT).show()
//                    } else {
//                        // If sign in fails, display a message to the user.
//                        Toast.makeText(requireContext(), "Fel vid login", Toast.LENGTH_SHORT).show()
//                    }
//                }
// suspend fun deleteAccount() {
//    //  myAuth.currentUser
//
//    val user = myAuth.currentUser ?: myAuth.currentUser //Firebase.auth.currentUser!! // rewrite, använd inte !! använd elvis istället
//
//    Log.d("PIADELETE", "User " + user.uid) // AndroidSTudio vill ändra till if sats
//    // skriv om kod och använd coroutines nedanför
//    // läs om exception, finns massor som kan gå fel, logga ut sen in och då kan man radera
//    user.delete() // AndroidSTudio vill ändra till if sats
//        .addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                Log.d("PIADELETE", "User account deleted.")
//            } else {
//                Log.d("PIADELETE", "DELETE FAIL ")
//                Log.d("PIADELETE", task.exception!!.toString())
//            }
//        }
// }
// Log.d("PIADELETE", "User " + user.uid) // AndroidSTudio vill ändra till if sats
// skriv om kod och använd coroutines nedanför
// läs om exception, finns massor som kan gå fel, logga ut sen in och då kan man radera

// Log.d("PIADELETE", "User account deleted.")

//  Log.d("PIADELETE", "DELETE FAIL ")
//  Log.d("PIADELETE", task.exception!!.toString())
// val user = myAuth.currentUser ?: myAuth.currentUser //Firebase.auth.currentUser!! // rewrite, använd inte !! använd elvis istället
// AndroidSTudio vill ändra till if sats
