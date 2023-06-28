package se.rebeccazadig.bokholken.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import se.rebeccazadig.bokholken.myAdverts.UiState

class UserViewModel : ViewModel() {

    private val loginRepo = LoginRepository.getInstance()
    private val userRepo = UserRepository.getInstance()
    val userName = MutableLiveData("")
    val userContact = MutableLiveData("")
    val userCity = MutableLiveData("")

    val inProgress = MutableLiveData(false)
    private val _uiState = MutableLiveData(UiState(false, null))
    internal val uiState: LiveData<UiState> get() = _uiState

    fun logOutInVm() {
        loginRepo.logOutInRepo()
    }

    fun saveUser() {
        inProgress.value = true

        viewModelScope.launch {
            val userid = loginRepo.getUserId()
            val userInfo = userName.value.orEmpty()
            val userContact = userContact.value.orEmpty()
            val userCity = userCity.value.orEmpty()
            val user = User(id = userid, name = userInfo, contact = userContact, city = userCity)

            userRepo.saveUser(user)
            inProgress.postValue(false)
        }
    }

    fun deleteAccountInVM() {
        inProgress.value = true
        viewModelScope.launch {
            val deleteResult: AuthResult = loginRepo.deleteAccount()
            inProgress.postValue(false)

            when (deleteResult) {
                is AuthResult.Failure -> {
                    _uiState.value = UiState(false, deleteResult.message)
                }
                is AuthResult.Success -> {
                    _uiState.value = UiState(true, null)
                }
            }
        }
    }
}
