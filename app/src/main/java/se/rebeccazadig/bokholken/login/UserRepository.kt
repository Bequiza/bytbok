package se.rebeccazadig.bokholken.login

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class UserRepository private constructor() {

    private val myAuth = Firebase.auth

    companion object {
        private var instance: UserRepository? = null

        fun getInstance() = instance ?: UserRepository().also {
            instance = it
        }
    }
}