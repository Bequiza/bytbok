package se.rebeccazadig.bokholken.login

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import se.rebeccazadig.bokholken.data.ContactType
import se.rebeccazadig.bokholken.utils.PhoneNumberValidator

data class LoginUiState(
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
)

class LoginRegisterViewModel(app: Application) : AndroidViewModel(app) {

    private val loginRepo = LoginRepository.getInstance()
    private val phoneNumberValidator = PhoneNumberValidator(app)

    val name = MutableLiveData("")
    val email = MutableLiveData("")
    val password = MutableLiveData("")
    val phoneNumber = MutableLiveData("")
    val isLoginMode = MutableLiveData(true)
    val preferredContactMethod = MutableLiveData(ContactType.PHONE)

    val phoneNumberValidationResult = MutableLiveData<String?>()
    private val isPhoneNumberValid = MutableLiveData(true)
    val inProgress = MutableLiveData(false)
    private val _loginUiState = MutableLiveData(LoginUiState())
    val loginUiState: LiveData<LoginUiState> get() = _loginUiState

    private val _passwordResetResult = MutableLiveData<Result<Unit>>()
    val passwordResetResult: LiveData<Result<Unit>> = _passwordResetResult

    val isButtonDisabled = MediatorLiveData<Boolean>().apply {
        addSource(email) { updateButtonState() }
        addSource(password) { updateButtonState() }
        addSource(name) { updateButtonState() }
        addSource(phoneNumber) { updateButtonState() }
        addSource(isPhoneNumberValid) { updateButtonState() }
        addSource(isLoginMode) { updateButtonState() }
    }

    private fun updateButtonState() {
        val commonFieldsFilled =
            email.value?.isNotBlank() == true && password.value?.isNotBlank() == true
        val contactIsValid = isPhoneNumberValid.value == true
        val phoneFieldFilled = phoneNumber.value?.isNotBlank() == true

        if (isLoginMode.value == true) {
            isButtonDisabled.value = !commonFieldsFilled
        } else {
            val nameFieldFilled = name.value?.isNotBlank() == true
            isButtonDisabled.value = !(commonFieldsFilled && nameFieldFilled && phoneFieldFilled && contactIsValid)
        }
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
                val phoneValue = phoneNumber.value.orEmpty()

                loginRepo.registerInRepo(
                    email = emailValue,
                    password = passwordValue,
                    name = nameValue,
                    phone = phoneValue,
                    contactMethod = ContactType.valueOf(preferredContactMethod.value?.name ?: ContactType.UNKNOWN.name)
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

    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                val result = loginRepo.resetPassword(email)
                _passwordResetResult.postValue(result)
            } catch (e: Exception) {
                Log.e("ResetPassword", "Error resetting password: ", e)
                _passwordResetResult.postValue(Result.Failure(e.message ?: "Unknown error"))
            }
        }
    }

    fun validatePhoneNumber(phone: String): Boolean {
        val validationResult = phoneNumberValidator.validate(phone)

        phoneNumberValidationResult.value = validationResult.errorMessage
        isPhoneNumberValid.value = validationResult.isValid

        return validationResult.isValid
    }

    fun changeMode() {
        isLoginMode.value = (isLoginMode.value ?: false).not()
    }
}