package se.rebeccazadig.bokholken.login

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay

class UserRepository private constructor() {

    private val myAuth = Firebase.auth

    suspend fun saveUser(user: User) {
        delay(2000)
        Log.i("Emma", "Saving user ${user.name}, ${user.contact}, ${user.city}")
    }

    companion object {
        private var instance: UserRepository? = null

        fun getInstance() = instance ?: UserRepository().also {
            instance = it
        }
    }
}
