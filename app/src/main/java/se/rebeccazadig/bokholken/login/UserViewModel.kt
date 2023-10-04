package se.rebeccazadig.bokholken.login

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import se.rebeccazadig.bokholken.R
import se.rebeccazadig.bokholken.data.ContactType
import se.rebeccazadig.bokholken.models.User
import se.rebeccazadig.bokholken.utils.PhoneNumberValidator
import se.rebeccazadig.bokholken.utils.hideKeyboard
import se.rebeccazadig.bokholken.utils.navigateBack

data class UiStateSave(
    val message: String?,
)

class UserViewModel(app: Application) : AndroidViewModel(app) {

    private var initialUserName: String? = null
    private var initialUserPhoneNumber: String? = null
    private var initialPreferredContactMethod: ContactType? = null

    private val loginRepo = LoginRepository.getInstance()
    private val userRepo = UserRepository.getInstance()
    private val phoneNumberValidator = PhoneNumberValidator(app)

    private val _uiStateSave = MutableLiveData(UiStateSave(null))
    val uiStateSave: LiveData<UiStateSave> get() = _uiStateSave

    val preferredContactMethod = MutableLiveData<ContactType?>()

    val user = MutableLiveData<User?>()
    val userName = MutableLiveData("")
    val userPhoneNumber = MutableLiveData("")
    private val userEmail = MutableLiveData("")
    val phoneNumberValidationResult = MutableLiveData<String?>()
    private val isPhoneNumberValid = MutableLiveData(true)

    val inProgress = MutableLiveData(false)

    val isButtonDisabled = MediatorLiveData<Boolean>().apply {
        addSource(userPhoneNumber) { updateButtonState() }
        addSource(userName) { updateButtonState() }
        addSource(preferredContactMethod) { updateButtonState() }
    }

    private fun updateButtonState() {
        val isDataChanged = hasDataChanged()

        isButtonDisabled.value = !isDataChanged ||
                userName.value.isNullOrBlank() ||
                userPhoneNumber.value.isNullOrBlank() ||
                isPhoneNumberValid.value == false ||
                preferredContactMethod.value == null
    }

    fun logOutInVm() {
        viewModelScope.launch {
            try {
                loginRepo.logOutInRepo()
                _uiStateSave.postValue(
                    UiStateSave(
                        message = getApplication<Application>().getString(
                            R.string.logout_success
                        )
                    )
                )
            } catch (e: Exception) {
                _uiStateSave.postValue(UiStateSave(message = e.localizedMessage))
            }
        }
    }

    fun saveUserProfile(view: View) {
        inProgress.value = true

        viewModelScope.launch {
            val userId = loginRepo.getUserId()
            val user = User(
                id = userId,
                name = userName.value ?: "",
                phoneNumber = userPhoneNumber.value ?: "",
                email = userEmail.value ?: "",
                preferredContactMethod = preferredContactMethod.value ?: ContactType.PHONE
            )

            val result = userRepo.saveUser(user)

            inProgress.postValue(false)

            when (result) {
                is Result.Failure -> {
                    _uiStateSave.value = UiStateSave(result.message)
                }
                is Result.Success -> {
                    _uiStateSave.value =
                        UiStateSave(message = getApplication<Application>().getString(R.string.user_profile_save_success_message))
                    view.navigateBack()
                    view.hideKeyboard()
                }
            }
        }
    }

    fun fetchUserData() {
        val userId = loginRepo.getUserId()
        viewModelScope.launch {
            if (userId.isNotEmpty()) {
                when (val result = userRepo.fetchUser(userId)) {
                    is Result.Success<User> -> {
                        user.value = result.data
                        userName.value = result.data.name
                        userPhoneNumber.value = result.data.phoneNumber
                        userEmail.value = result.data.email
                        preferredContactMethod.value = result.data.preferredContactMethod
                        initialUserName = result.data.name
                        initialUserPhoneNumber = result.data.phoneNumber
                        initialPreferredContactMethod = result.data.preferredContactMethod
                    }

                    is Result.Failure -> {
                        Log.e("UserDataFetch", result.message)
                    }
                }
            }
        }
    }

    fun initializeUserData() {
        viewModelScope.launch {
            fetchUserData()
            userName.value = user.value?.name
            userPhoneNumber.value = user.value?.phoneNumber
            preferredContactMethod.value = user.value?.preferredContactMethod
        }
    }

    fun nullUiStateSave() {
        _uiStateSave.value = UiStateSave(message = null)
    }

    fun deleteAccountInVM(email: String, password: String) {
        inProgress.value = true

        viewModelScope.launch {
            val reAuthResult = loginRepo.reAuthenticate(email, password)

            if (reAuthResult is Result.Success) {
                when (val deleteResult = loginRepo.deleteAccount()) {
                    is Result.Failure -> {
                        _uiStateSave.value = UiStateSave(deleteResult.message)
                    }

                    is Result.Success -> {
                        _uiStateSave.value = UiStateSave(
                            message = getApplication<Application>().getString(
                                R.string.account_deleted
                            )
                        )
                    }
                }
            } else if (reAuthResult is Result.Failure) {
                _uiStateSave.value = UiStateSave(reAuthResult.message)
            }

            inProgress.postValue(false)
        }
    }

    fun validatePhoneNumber(phone: String): Boolean {
        val validationResult = phoneNumberValidator.validate(phone)

        phoneNumberValidationResult.value = validationResult.errorMessage
        isPhoneNumberValid.value = validationResult.isValid

        return validationResult.isValid
    }

    private fun hasDataChanged(): Boolean {
        return userName.value != initialUserName ||
                userPhoneNumber.value != initialUserPhoneNumber ||
                preferredContactMethod.value != initialPreferredContactMethod
    }
}