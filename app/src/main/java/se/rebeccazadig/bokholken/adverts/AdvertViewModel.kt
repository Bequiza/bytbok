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

    val title = MutableLiveData<String?>()
    val author = MutableLiveData<String?>()
    val genre = MutableLiveData<String?>()
    val location = MutableLiveData<String?>()
    val adImage: MutableLiveData<Bitmap?> = MutableLiveData()
    val isEditMode = MutableLiveData(true)
    val toolbarTitle = MutableLiveData<String>()
    val currentAdvertImageUrl = MutableLiveData<String?>()
    private var currentAdvert: Advert? = null


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

    fun saveAdvert(adImage: Bitmap?) {
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
            advertsRepo.updateAdvert(advert, adImage)
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

    fun saveOrUpdateAdvertImage(adImage: Bitmap?) {
        if (currentAdvert != null) {
            updateAdvert(adImage)
        } else {
            saveAdvert(adImage)
        }
    }

    private fun updateAdvert(adImage: Bitmap?) {
        val currentAdvertId = currentAdvert?.adId
        if (currentAdvertId == null) {
            _advertSaveStatus.value = UiStateSave(message = "Error: Advert id is missing!")
            return
        }
        isSavingInProgress.value = true

        val advert = Advert(
            adId = currentAdvertId,
            title = title.value ?: "",
            author = author.value ?: "",
            genre = genre.value ?: "",
            location = location.value ?: "",
            adCreator = currentAdvert?.adCreator,
            creationTime = currentAdvert?.creationTime,
            imageUrl = currentAdvert?.imageUrl
        )

        viewModelScope.launch {
            val updateResult = advertsRepo.updateAdvert(advert, adImage)
            isSavingInProgress.postValue(false)
            when (updateResult) {
                is Result.Failure -> {
                    _advertSaveStatus.value =
                        UiStateSave(updateResult.message)
                }

                is Result.Success -> {
                    _advertSaveStatus.value =
                        UiStateSave(message = app.getString(R.string.advert_updated_succesfully))
                }
            }
        }
    }

    fun initializeAdvertData(adId: String?) {
        if (!adId.isNullOrEmpty()) {
            isEditMode.value = false
            fetchCurrentAdvertDetails(adId)
            toolbarTitle.value = app.getString(R.string.edit_advert_title)
        } else {
            title.value = ""
            author.value = ""
            genre.value = ""
            location.value = ""
            currentAdvert = null
            toolbarTitle.value = app.getString(R.string.create_ads_text)
        }
    }

    private fun fetchCurrentAdvertDetails(advertId: String) {
        viewModelScope.launch {
            val advertDetails = advertsRepo.fetchAdvertDetails(advertId)
            if (advertDetails != null) {
                title.value = advertDetails.title
                author.value = advertDetails.author
                genre.value = advertDetails.genre
                location.value = advertDetails.location
                currentAdvert = advertDetails
                currentAdvertImageUrl.value = advertDetails.imageUrl
            } else {
                _advertSaveStatus.value = UiStateSave(message = "Error fetching advert details!")
            }
        }
    }

    fun createOrUpdateAdvert(title: String, author: String, genre: String, location: String): Advert {
        return if (currentAdvert != null) {
            // We are in edit mode
            Advert(
                adId = currentAdvert?.adId,
                title = title,
                author = author,
                genre = genre,
                location = location,
                adCreator = currentAdvert?.adCreator,
                creationTime = currentAdvert?.creationTime
            )
        } else {
            // We are in create mode
            Advert(
                title = title,
                author = author,
                genre = genre,
                location = location
            )
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