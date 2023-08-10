package se.rebeccazadig.bokholken.login

import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

internal data class UiStateSave(
    val message: String?,
)

class UserViewModel : ViewModel() {

    private val loginRepo = LoginRepository.getInstance()
    private val userRepo = UserRepository.getInstance()

    private val _uiStateSave = MutableLiveData(UiStateSave(null))
    internal val uiStateSave: LiveData<UiStateSave> get() = _uiStateSave

    val userName = MutableLiveData("")
    val userContact = MutableLiveData("")
    val userCity = MutableLiveData("")

    val inProgress = MutableLiveData(false)

    val isButtonDisabled = MediatorLiveData<Boolean>().apply {
        addSource(userContact) {
            value = it.isBlank() || userCity.value.isNullOrBlank()
        }
        addSource(userCity) {
            value = it.isBlank() || userContact.value.isNullOrBlank()
        }
    }

    fun logOutInVm() {
        loginRepo.logOutInRepo()
    }

    fun saveUser(view: View) {
        inProgress.value = true

        viewModelScope.launch {
            val userId = loginRepo.getUserId()

            val user = User(
                id = userId,
                name = userName.value ?: "",
                contact = userContact.value ?: "",
                city = userCity.value ?: ""
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
                }
            }
            closeKeyboard(view)
        }
    }


    private fun closeKeyboard(view: View) {
        val inputMethodManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
                val deleteResult = loginRepo.deleteAccount()

                when (deleteResult) {
                    is Result.Failure -> {
                        _uiStateSave.value = UiStateSave(deleteResult.message)
                    }

                    is Result.Success -> {
                        _uiStateSave.value = UiStateSave(message = "Konto Raderat")
                    }
                }
            } else if (reAuthResult is Result.Failure) {
                _uiStateSave.value = UiStateSave(reAuthResult.message)
            }

            inProgress.postValue(false)
        }
    }
}
