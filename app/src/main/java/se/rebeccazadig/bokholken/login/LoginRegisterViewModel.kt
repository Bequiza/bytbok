package se.rebeccazadig.bokholken.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.rebeccazadig.bokholken.data.Contact

//internal data class UiState(
//    val message: String?,
//)

data class LoginUiState(
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
)

class LoginRegisterViewModel(app: Application) : AndroidViewModel(app) {

    private val loginRepo = LoginRepository.getInstance(UserStorage(app))

    val name = MutableLiveData("")
    val contact = MutableLiveData("")
    val email = MutableLiveData("")
    val password = MutableLiveData("")
    val isLoginMode = MutableLiveData(true)

    val contactValidationResult = MutableLiveData<String?>()
    private val isContactValid = MutableLiveData(false)
    val inProgress = MutableLiveData(false)
    private val _loginUiState = MutableLiveData(LoginUiState())
    val loginUiState: LiveData<LoginUiState> get() = _loginUiState

    val isButtonDisabled = MediatorLiveData<Boolean>().apply {
        addSource(email) { updateButtonState() }
        addSource(password) { updateButtonState() }
        addSource(name) { updateButtonState() }
        addSource(contact) { updateButtonState() }
        addSource(isContactValid) { updateButtonState() }
        addSource(isLoginMode) {updateButtonState()}
    }

    private fun updateButtonState() {
        if (isLoginMode.value == true) {
            isButtonDisabled.value = listOf(email, password).any { it.value.isNullOrBlank() }
                    || isContactValid.value != true
        } else {
            isButtonDisabled.value = listOf(email, password, name, contact).any { it.value.isNullOrBlank() }
                    || isContactValid.value != true
        }
    }

    fun validateContactInput(contactText: String, contactType: Contact.ContactType): Boolean {
        if(contactText.isEmpty()) {
            contactValidationResult.value = null
            isContactValid.value = true
            return true
        }

        val isValid = when (contactType) {
            Contact.ContactType.EMAIL -> isEmail(contactText).also {
                if (!it) contactValidationResult.value = "Invalid email address"
            }
            Contact.ContactType.PHONE -> isPhoneNumber(contactText).also {
                if (!it) contactValidationResult.value = "Invalid phone number"
            }
            Contact.ContactType.UNKNOWN -> false.also {
                contactValidationResult.value = "Invalid contact method"
            }
        }

        isContactValid.value = isValid
        if (isValid) contactValidationResult.value = null  // Reset error if valid

        return isValid
    }

    fun loginOrRegisterInVM() {
        inProgress.value = true
        viewModelScope.launch {
            val emailValue = email.value.orEmpty()
            val passwordValue = password.value.orEmpty()

            val result = if (isLoginMode.value == true) {
                loginRepo.loginInRepo(email = emailValue, password = passwordValue)
            } else {
                loginRepo.registerInRepo(email = emailValue, password = passwordValue)
            }
            inProgress.postValue(false)
            when (result) {
                is Result.Failure -> {
                    _loginUiState.value = LoginUiState(false, result.message)
                }
                is Result.Success -> {
                    _loginUiState.value = LoginUiState(true)
                }
            }
        }
    }

    fun changeMode() {
        isLoginMode.value = (isLoginMode.value ?: false).not()
    }

    // These region is going to be moved to utils later
    companion object {
        private const val PHONE_NUMBER_REGEX_PATTERN = "^\\d{10}$"
    }

    fun isPhoneNumber(contact: String): Boolean {
        return contact.matches(PHONE_NUMBER_REGEX_PATTERN.toRegex())
    }

    fun isEmail(contact: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(contact).matches()
    }
}