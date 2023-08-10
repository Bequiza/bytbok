package se.rebeccazadig.bokholken.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

internal data class UiState(
    val message: String?,
)

class LoginRegisterViewModel : ViewModel() {

    private val loginRepo = LoginRepository.getInstance()

    val email = MutableLiveData("")
    val password = MutableLiveData("")
    val isLoginMode = MutableLiveData(true)
    val inProgress = MutableLiveData(false)

    private val _uiState = MutableLiveData(UiState(null))
    internal val uiState: LiveData<UiState> get() = _uiState

    val isButtonDisabled = MediatorLiveData<Boolean>().apply {
        addSource(email) {
            value = (email.value ?: "").isBlank() || (password.value ?: "").isBlank()
        }
        addSource(password) {
            value = (email.value ?: "").isBlank() || (password.value ?: "").isBlank()
        }
    }

    fun loginOrRegisterInVM() {
        inProgress.value = true
        viewModelScope.launch {
            val emailValue = email.value.orEmpty()
            val passwordValue = password.value.orEmpty()

            val result: Result = if (isLoginMode.value == true) {
                loginRepo.loginInRepo(email = emailValue, password = passwordValue)
            } else {
                loginRepo.registerInRepo(email = emailValue, password = passwordValue)
            }
            inProgress.postValue(false)
            when (result) { /*Läs mer om when, when är lika som if och else if*/
                is Result.Failure -> {
                    _uiState.value = UiState(result.message)
                }
                is Result.Success -> {
                    _uiState.value = UiState(null)
                }
            }
        }
    }

    fun changeMode() {
        isLoginMode.value = (isLoginMode.value ?: false).not()
    }
}
