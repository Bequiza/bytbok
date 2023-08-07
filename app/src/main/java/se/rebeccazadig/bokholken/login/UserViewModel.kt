package se.rebeccazadig.bokholken.login

import android.app.AlertDialog
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
   //private val _uiState = MutableLiveData(UiState(false, null))
   //internal val uiState: LiveData<UiState> get() = _uiState
    private val _uiStateSave = MutableLiveData(UiStateSave(null))
    internal val uiStateSave: LiveData<UiStateSave> get() = _uiStateSave

    val isButtonDisabled = MediatorLiveData<Boolean>().apply {
        addSource(userContact) {
            value = (userContact.value ?: "").isBlank() || (userCity.value ?: "").isBlank()
        }
        addSource(userCity) {
            value = userContact.value.isNullOrBlank() || (userCity.value ?: "").isBlank()
        }
    }
    fun logOutInVm() {
        loginRepo.logOutInRepo()
    }

    fun saveUser() {
        inProgress.value = true

        viewModelScope.launch {
            val userid = loginRepo.getUserId()
            val userName = userName.value.toString()
            val userContact = userContact.value.toString()
            val userCity = userCity.value.toString()
            val user = User(id = userid, name = userName, contact = userContact, city = userCity)

            userRepo.saveUser(user)
            val result = userRepo.saveUser(user)

            inProgress.postValue(false)

            when (result) {
                is Result.Failure -> {
                    Log.i("Emma", "FAIL")
                    _uiStateSave.value =
                        UiStateSave(result.message)
                }

                is Result.Success -> {
                    Log.i("Emma", "SUCCESS")
                    _uiStateSave.value = UiStateSave(message = "Informationen Sparad")
                }
            }
        }
    }

    fun nullUiStateSave() {
        UiStateSave(message = null)
    }

    fun deleteAccountInVM() {
        inProgress.value = true
        viewModelScope.launch {
            val deleteResult: Result = loginRepo.deleteAccount()
            inProgress.postValue(false)

            when (deleteResult) {
                is Result.Failure -> {
                    _uiStateSave.value = UiStateSave(deleteResult.message)
                    //_uiState.value = UiState(false, deleteResult.message)
                }

                is Result.Success -> {
                    _uiStateSave.value = UiStateSave(message = "Konto Raderat")
                    //_uiState.value = UiState(true, null)
                }
            }
        }
    }
}
