package se.rebeccazadig.bokholken.myAdverts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import se.rebeccazadig.bokholken.adverts.AdvertsRepository
import se.rebeccazadig.bokholken.data.Result
import se.rebeccazadig.bokholken.models.Advert

class MyAdvertsViewModel(app: Application) : AndroidViewModel(app) {

    private val advertsRepo = AdvertsRepository.getInstance()
    private val _myAdvertsLiveData = MutableLiveData<List<Advert>>()

    val myAdvertsLiveData: LiveData<List<Advert>> get() = _myAdvertsLiveData

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
        }
    }
}