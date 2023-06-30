package se.rebeccazadig.bokholken.login

import android.database.Observable
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import se.rebeccazadig.bokholken.myAdverts.UiState

internal data class UiStateSave(

    val message: String?,
)

class UserViewModel : ViewModel() {

    private val loginRepo = LoginRepository.getInstance()
    private val userRepo = UserRepository.getInstance()
    val userName = MutableLiveData("")
    val userContact = MutableLiveData("")
    val userCity = MutableLiveData("")

    val inProgress = MutableLiveData(false)
    private val _uiState = MutableLiveData(UiState(false, null))
    internal val uiState: LiveData<UiState> get() = _uiState
    private val _uiStateSave = MutableLiveData(UiStateSave(null))
    internal val uiStateSave: LiveData<UiStateSave> get() = _uiStateSave

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
            val result = userRepo.saveUser(user)

            inProgress.postValue(false)

            when (result) {
                is Result.Failure -> {
                    Log.i("Emma", "FAIL")
                    _uiStateSave.value = UiStateSave(message = "Hoppsan något gick fel")
                }
                is Result.Success -> {
                    Log.i("Emma", "SUCCESS")
                    _uiStateSave.value = UiStateSave(message = "Informationen Sparad")
                }
            }
        }
    }

    fun deleteAccountInVM() {
        inProgress.value = true
        viewModelScope.launch {
            val deleteResult: Result = loginRepo.deleteAccount()
            inProgress.postValue(false)

            when (deleteResult) {
                is Result.Failure -> {
                    _uiState.value = UiState(false, deleteResult.message)
                }
                is Result.Success -> {
                    _uiState.value = UiState(true, null)
                }
            }
        }
    }
    val isButtonDisabled = MediatorLiveData<Boolean>().apply {
        addSource(userContact) {
            value = (userContact.value ?: "").isBlank() || (userCity.value ?: "").isBlank()
        }
        addSource(userCity) {
            value = (userContact.value ?: "").isBlank() || (userCity.value ?: "").isBlank()
        }
    }
}
