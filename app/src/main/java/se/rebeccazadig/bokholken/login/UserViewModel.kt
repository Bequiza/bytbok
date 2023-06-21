package se.rebeccazadig.bokholken.login

import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {

    private val userRepo = UserRepository.getInstance()
}
