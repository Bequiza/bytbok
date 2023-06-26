package se.rebeccazadig.bokholken.Adverts

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import se.rebeccazadig.bokholken.login.LoginRepository

class AdvertsRepository private constructor() {

    private val myAuth = Firebase.auth

    companion object {
        private var instance: AdvertsRepository? = null

        fun getInstance() = instance ?: AdvertsRepository().also {
            instance = it
        }
    }
}
