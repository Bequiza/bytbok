package se.rebeccazadig.bokholken.Adverts

import androidx.lifecycle.ViewModel
import se.rebeccazadig.bokholken.login.LoginRepository
import se.rebeccazadig.bokholken.mypage.MyPageRepository

class AdvertViewModel : ViewModel() {

    private val advertRepo = AdvertsRepository.getInstance()
}
