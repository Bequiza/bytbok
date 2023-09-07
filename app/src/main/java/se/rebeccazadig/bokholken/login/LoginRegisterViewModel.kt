package se.rebeccazadig.bokholken.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import se.rebeccazadig.bokholken.R
import se.rebeccazadig.bokholken.data.ContactType
import se.rebeccazadig.bokholken.utils.isPhoneNumber

data class LoginUiState(
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
)

class LoginRegisterViewModel(app: Application) : AndroidViewModel(app) {

    private val loginRepo = LoginRepository.getInstance()

    val name = MutableLiveData("")
    val contact = MutableLiveData("")
    val email = MutableLiveData("")
    val password = MutableLiveData("")
    val phoneNumber = MutableLiveData("")
    val isLoginMode = MutableLiveData(true)
    val preferredContactMethod = MutableLiveData(ContactType.PHONE)


    val contactValidationResult = MutableLiveData<String?>()
    private val isContactValid = MutableLiveData(true)
    val inProgress = MutableLiveData(false)
    private val _loginUiState = MutableLiveData(LoginUiState())
    val loginUiState: LiveData<LoginUiState> get() = _loginUiState

    val isButtonDisabled = MediatorLiveData<Boolean>().apply {
        addSource(email) { updateButtonState() }
        addSource(password) { updateButtonState() }
        addSource(name) { updateButtonState() }
        addSource(phoneNumber) { updateButtonState() }
        addSource(isContactValid) { updateButtonState() }
        addSource(isLoginMode) { updateButtonState() }
    }

    private fun updateButtonState() {
        val commonFieldsFilled =
            email.value?.isNotBlank() == true && password.value?.isNotBlank() == true
        val contactIsValid = isContactValid.value == true

        if (isLoginMode.value == true) {
            isButtonDisabled.value = !commonFieldsFilled
        } else {
            val nameFieldFilled = name.value?.isNotBlank() == true
            isButtonDisabled.value = !(commonFieldsFilled && nameFieldFilled && contactIsValid)
        }
    }

    fun validatePhoneNumber(phone: String): Boolean {
        if (phone.isEmpty()) {
            contactValidationResult.value = null
            isContactValid.value = false
            return false
        }

        val isValid = isPhoneNumber(phone).also {
            if (!it) contactValidationResult.value =
                getApplication<Application>().getString(R.string.invalid_phone_number)
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
                val nameValue = name.value.orEmpty()

                // Determine contact value based on preferred method
                val preferredMethod = preferredContactMethod.value ?: ContactType.UNKNOWN
                val contactValue = when (preferredMethod) {
                    ContactType.EMAIL -> emailValue
                    ContactType.PHONE -> phoneNumber.value
                    else -> contact.value.orEmpty() // Fallback to what user has entered or keep it empty
                }

                loginRepo.registerInRepo(
                    email = emailValue,
                    password = passwordValue,
                    name = nameValue,
                    contact = contactValue ?: "",
                    contactMethod = ContactType.valueOf(preferredMethod.name)
                )
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
}