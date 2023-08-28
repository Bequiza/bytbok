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
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import kotlinx.coroutines.launch
import se.rebeccazadig.bokholken.data.User

data class UiStateSave(
    val message: String?,
)

class UserViewModel(app: Application) : AndroidViewModel(app) {

    private val loginRepo = LoginRepository.getInstance(UserStorage(app))
    private val userRepo = UserRepository.getInstance()

    private val _uiStateSave = MutableLiveData(UiStateSave(null))
    internal val uiStateSave: LiveData<UiStateSave> get() = _uiStateSave

    val user = MutableLiveData<User?>()
    val userName = MutableLiveData("")
    val userContact = MutableLiveData("")

    val inProgress = MutableLiveData(false)

    val isButtonDisabled = MediatorLiveData<Boolean>().apply {
        addSource(userContact) { value = it.isBlank() }
    }
    val isButtonDisabled2 = userContact.map { it.isBlank() }
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
                contact = userContact.value ?: ""
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

                    view.findNavController().popBackStack()

                }
            }
            closeKeyboard(view)
        }
    }

    // Kolla fetchUser som vitaliy gjort i hans app och skriv om
    fun fetchUserData() {
        val userId = loginRepo.getUserId() ?: return
        viewModelScope.launch {
            if (userId.isNotEmpty()) {
                when (val result = userRepo.fetchUser(userId)) {
                    is Result.Success -> {
                        user.value = result.data
                        Log.i("UserDataFetch", "User fetched: ${result.data.name}")
                    }

                    is Result.Failure -> {
                        Log.e("UserDataFetch", result.message)
                    }
                }
            }
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
