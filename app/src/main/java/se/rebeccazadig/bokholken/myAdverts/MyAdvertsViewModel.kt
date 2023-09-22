package se.rebeccazadig.bokholken.myAdverts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import se.rebeccazadig.bokholken.R
import se.rebeccazadig.bokholken.adverts.AdvertsRepository
import se.rebeccazadig.bokholken.data.Result
import se.rebeccazadig.bokholken.data.UiState
import se.rebeccazadig.bokholken.models.Advert

class MyAdvertsViewModel(private val app: Application) : AndroidViewModel(app) {

    private val advertsRepo = AdvertsRepository.getInstance()
    private val _myAdvertsLiveData = MutableLiveData<List<Advert>>()
    val myAdvertsLiveData: LiveData<List<Advert>> get() = _myAdvertsLiveData

    private val _deleteAdvertStatus = MutableLiveData<UiState>(null)
    val deleteAdvertStatus: LiveData<UiState> get() = _deleteAdvertStatus

    init {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        advertsRepo.advertsLiveData.observeForever { adverts ->
            _myAdvertsLiveData.value = adverts.filter { it.adCreator == currentUserUid }
        }
    }

    fun deleteAdvert(advert: Advert) {

        viewModelScope.launch {
            val result = advert.adId?.let {
                advertsRepo.deleteAdvert(it)
            }

            when (result) {
                is Result.Failure -> {
                    _deleteAdvertStatus.value = UiState(result.message)
                }

                is Result.Success -> {
                    _deleteAdvertStatus.value =
                        UiState(message = app.getString(R.string.advert_deleted_succesfully))
                }

                else -> {
                    _deleteAdvertStatus.value =
                        UiState(message = app.getString(R.string.generic_error))
                }
            }
        }
    }
}