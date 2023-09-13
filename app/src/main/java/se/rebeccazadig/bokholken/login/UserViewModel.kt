package se.rebeccazadig.bokholken.login

import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import se.rebeccazadig.bokholken.R
import se.rebeccazadig.bokholken.data.ContactType
import se.rebeccazadig.bokholken.data.User
import se.rebeccazadig.bokholken.utils.navigateBack

data class UiStateSave(
    val message: String?,
)

class UserViewModel(app: Application) : AndroidViewModel(app) {

    private val loginRepo = LoginRepository.getInstance()
    private val userRepo = UserRepository.getInstance()

    private val _uiStateSave = MutableLiveData(UiStateSave(null))
    internal val uiStateSave: LiveData<UiStateSave> get() = _uiStateSave

    val preferredContactMethod = MutableLiveData<ContactType?>()

    val user = MutableLiveData<User?>()
    val userName = MutableLiveData("")
    val userContact = MutableLiveData("")

    val inProgress = MutableLiveData(false)

    val isButtonDisabled = MediatorLiveData<Boolean>().apply {
        addSource(userContact) { updateButtonState() }
        addSource(userName) { updateButtonState() }
        addSource(preferredContactMethod) { updateButtonState() }
    }

    private fun updateButtonState() {
        isButtonDisabled.value = userName.value.isNullOrBlank() ||
                userContact.value.isNullOrBlank() ||
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

    fun saveUser(view: View) {
        inProgress.value = true

        viewModelScope.launch {
            val userId = loginRepo.getUserId()

            val user = User(
                id = userId,
                name = userName.value ?: "",
                contact = userContact.value ?: "",
                preferredContactMethod = preferredContactMethod.value ?: ContactType.PHONE
            )

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
                    view.navigateBack()
                }
            }
            closeKeyboard(view)
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
                        userContact.value = result.data.contact
                        preferredContactMethod.value = result.data.preferredContactMethod
                        Log.i("UserDataFetch", "User fetched: ${result.data.name}")
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
            userContact.value = user.value?.contact
            preferredContactMethod.value = user.value?.preferredContactMethod
        }
    }

    private fun closeKeyboard(view: View) {
        val inputMethodManager =
            view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
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
}
