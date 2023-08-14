package se.rebeccazadig.bokholken.myAdverts

import androidx.lifecycle.ViewModel
import se.rebeccazadig.bokholken.login.LoginRepository

data class UiState(
    /*uiState är MLD är observed i frag men all ändring sker i viewmodel*/
    val isDeleted: Boolean,
    val message: String?,
)

class MyAdvertsViewModel : ViewModel() {

    private val loginRepo = LoginRepository.getInstance()
    private val myAdvertsRepo = MyAdvertsRepository.getInstance()
}
