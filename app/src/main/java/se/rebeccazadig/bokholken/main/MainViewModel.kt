package se.rebeccazadig.bokholken.main

import androidx.lifecycle.ViewModel
import se.rebeccazadig.bokholken.login.LoginRepository

class MainViewModel() : ViewModel() {

    private val loginRepo = LoginRepository.getInstance()
    val isLoggedIn = loginRepo.isLoggedIn
}
