package se.rebeccazadig.bokholken.adverts

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import se.rebeccazadig.bokholken.R
import se.rebeccazadig.bokholken.data.Result
import se.rebeccazadig.bokholken.data.UiStateSave
import se.rebeccazadig.bokholken.models.Advert

class AdvertViewModel(private val app: Application) : AndroidViewModel(app) {

    private val advertsRepo = AdvertsRepository.getInstance()

    val isSavingInProgress = MutableLiveData(false)
    val searchQuery = MutableLiveData("")

    val title = MutableLiveData<String>()
    val author = MutableLiveData<String>()
    val genre = MutableLiveData<String>()
    val location = MutableLiveData<String>()
    val adImage: MutableLiveData<Bitmap?> = MutableLiveData()

    val isButtonDisabled: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        val update = {
            value = listOf(
                title,
                author,
                genre,
                location
            ).any { it.value.isNullOrEmpty() } || isSavingInProgress.value == true
        }
        listOf(
            title,
            author,
            genre,
            location,
            isSavingInProgress
        ).forEach { addSource(it) { update() } }
    }

    private val _advertSaveStatus = MutableLiveData<UiStateSave>(null)
    val advertSaveStatus: LiveData<UiStateSave> get() = _advertSaveStatus

    private val advertsLiveData: LiveData<List<Advert>> get() = advertsRepo.advertsLiveData

    val advertDetailsLiveData = advertsRepo.advertDetailLiveData

    private val _filteredAdverts = MediatorLiveData<List<Advert>>().apply {
        addSource(searchQuery) { filterAdverts() }
        addSource(advertsLiveData) { filterAdverts() }
    }
    val filteredAdverts: LiveData<List<Advert>> get() = _filteredAdverts

    private fun filterAdverts() {
        val query = searchQuery.value ?: ""
        val currentAdverts = advertsLiveData.value.orEmpty()

        _filteredAdverts.value = currentAdverts.filter { advert ->
            query.isBlank() ||
                    listOfNotNull(advert.title, advert.author, advert.genre, advert.location)
                        .any { field -> field.contains(query, ignoreCase = true) }
        }
    }

    fun saveAdvert( adImage: Bitmap?) {
        isSavingInProgress.value = true
        val advert = Advert(
            title = title.value ?: "",
            author = author.value ?: "",
            genre = genre.value ?: "",
            location = location.value ?: ""
        )
        viewModelScope.launch {
            val saveResult = advertsRepo.saveAdvert(advert, adImage)
            isSavingInProgress.postValue(false)
            when (saveResult) {
                is Result.Failure -> {
                    _advertSaveStatus.value =
                        UiStateSave(saveResult.message)
                }

                is Result.Success -> {
                    _advertSaveStatus.value =
                        UiStateSave(message = app.getString(R.string.advert_saved_successfully))
                }
            }

        }
    }

    fun resetUiStateSave() {
        _advertSaveStatus.value = UiStateSave(message = null)
    }

    fun getAdvertDetails(advertId: String) {
        viewModelScope.launch {
            advertsRepo.fetchAdvertAndUserDetails(advertId)
        }
    }

    fun cleanUp() {
        advertsRepo.cleanUp()
    }
}