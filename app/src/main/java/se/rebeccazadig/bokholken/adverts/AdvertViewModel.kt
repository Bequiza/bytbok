package se.rebeccazadig.bokholken.adverts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import se.rebeccazadig.bokholken.data.Advert
import se.rebeccazadig.bokholken.myAdverts.UiState

class AdvertViewModel : ViewModel() {
    private val advertsRepo = AdvertsRepository.getInstance()

    private val _advertsLiveData = advertsRepo.advertsLiveData
    private val _uiState = MutableLiveData(UiState(false, null))
    internal val uiState: LiveData<UiState> get() = _uiState
    val inProgress = MutableLiveData(false)


    private val _adverts = MutableLiveData<List<Advert>?>()
    val adverts: MutableLiveData<List<Advert>?> = _adverts

    val searchQuery = MutableLiveData("")

    private val _filteredAdverts = MediatorLiveData<List<Advert>>()
    val filteredAdverts: LiveData<List<Advert>> get() = _filteredAdverts

    private val _advertSaveStatus = MutableLiveData<Boolean>()
    val advertSaveStatus: LiveData<Boolean> get() = _advertSaveStatus

    private val advertsLiveData: LiveData<List<Advert>> get() = advertsRepo.advertsLiveData


    init {
        _filteredAdverts.addSource(searchQuery) { filterAdverts() }
        _filteredAdverts.addSource(advertsLiveData) { filterAdverts() }
    }

    private fun filterAdverts() {
        val query = searchQuery.value
        _advertsLiveData.value?.let { adverts ->
            _filteredAdverts.value = when {
                query.isNullOrEmpty() -> adverts
                else -> adverts.filter {
                    it.title?.contains(query, true) == true ||
                            it.author?.contains(query, true) == true
                }
            }
        }
    }

    fun saveAdvert(advert: Advert) {
        inProgress.value = true

        viewModelScope.launch {
            try {
                advertsRepo.saveAdvert(advert)
                _advertSaveStatus.value = true
            } catch (e: Exception) {
                _advertSaveStatus.value = false
                inProgress.postValue(false)
            }
        }
    }
}