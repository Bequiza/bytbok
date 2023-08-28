package se.rebeccazadig.bokholken.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import se.rebeccazadig.bokholken.login.LoginRepository
import se.rebeccazadig.bokholken.login.UserRepository
import se.rebeccazadig.bokholken.login.UserStorage

class MainActivityViewModel(app: Application) : AndroidViewModel(app) {

    private val loginRepo = LoginRepository.getInstance(UserStorage(app))

    fun isLoggedIn() = loginRepo.isLoggedIn()

}