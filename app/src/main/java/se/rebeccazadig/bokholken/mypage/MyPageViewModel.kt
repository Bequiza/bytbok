package se.rebeccazadig.bokholken.mypage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import se.rebeccazadig.bokholken.R
import se.rebeccazadig.bokholken.login.AuthResult
import se.rebeccazadig.bokholken.login.LoginRepository

internal data class UiState(
    /*uiState är MLD är observed i frag men all ändring sker i viewmodel*/
    val isDeleted: Boolean,
    val message: String?,
)

class MyPageViewModel : ViewModel() {

    private val loginRepo = LoginRepository.getInstance()
    private val myPageRepo = MyPageRepository.getInstance()

    val inProgress = MutableLiveData(false)
    private val _uiState = MutableLiveData(UiState(false, null))
    internal val uiState: LiveData<UiState> get() = _uiState

    fun logOutInVm() {
        loginRepo.logOutInRepo()
    }

    fun deleteAccountInVM() {
        inProgress.value = true
        viewModelScope.launch {
            val deleteResult: AuthResult = loginRepo.deleteAccount()
            inProgress.postValue(false)

            when (deleteResult) {
                is AuthResult.Failure -> {
                    _uiState.value = UiState(false, deleteResult.message)
                }
                is AuthResult.Success -> {
                    _uiState.value = UiState(true, null)
                }
            }
        }
    }
}
