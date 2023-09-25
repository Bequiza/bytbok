package se.rebeccazadig.bokholken.adverts

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import se.rebeccazadig.bokholken.R
import se.rebeccazadig.bokholken.data.Result
import se.rebeccazadig.bokholken.data.UiState
import se.rebeccazadig.bokholken.models.Advert
import se.rebeccazadig.bokholken.models.User

data class FavoriteUiState(
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class AdvertViewModel(private val app: Application) : AndroidViewModel(app) {

    private val advertsRepo = AdvertsRepository.getInstance()

    val isSavingInProgress = MutableLiveData(false)
    private val _advertSaveStatus = MutableLiveData<UiState>(null)
    val advertSaveStatus: LiveData<UiState> get() = _advertSaveStatus
    private val advertsLiveData: LiveData<List<Advert>> get() = advertsRepo.advertsLiveData
    val advertDetailsLiveData = advertsRepo.advertDetailLiveData

    private val _myAdvertsLiveData = MutableLiveData<List<Advert>>()
    val myAdvertsLiveData: LiveData<List<Advert>> get() = _myAdvertsLiveData

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
            value = listOf(title, author, genre, location)
                .any { it.value.isNullOrEmpty() } || isSavingInProgress.value == true
        }
        listOf(title, author, genre, location, isSavingInProgress)
            .forEach { addSource(it) { update() } }
    }

    val searchQuery = MutableLiveData("")
    private val _filteredAdverts = MediatorLiveData<List<Advert>>().apply {
        addSource(searchQuery) { filterAdverts() }
        addSource(advertsLiveData) { filterAdverts() }
    }

    val filteredAdverts: LiveData<List<Advert>> get() = _filteredAdverts

    private val _isAdvertFavorite = MutableLiveData<Boolean?>()
    val favoritesLiveData: LiveData<List<Advert>> get() = advertsRepo.favoritesLiveData
    val isAdvertFavorite: LiveData<Boolean?> get() = _isAdvertFavorite

    private val _favoriteState = MutableLiveData<FavoriteUiState>()
    val favoriteState: LiveData<FavoriteUiState> get() = _favoriteState

    init {
        fetchMyAdverts()
    }

    private fun fetchMyAdverts() {
        val currentUserUid = advertsRepo.getCurrentUserId()
        advertsRepo.fetchAdvertsAndUsers()
        _myAdvertsLiveData.value = advertsLiveData.value?.filter { it.adCreator == currentUserUid }
    }

    private fun filterAdverts() {
        val query = searchQuery.value ?: ""
        val currentAdverts = advertsLiveData.value.orEmpty()

        _filteredAdverts.value = currentAdverts.filter { advert ->
            query.isBlank() ||
                    listOfNotNull(advert.title, advert.author, advert.genre, advert.location)
                        .any { field -> field.contains(query, ignoreCase = true) }
        }
    }

    private fun processAdvert(
        title: String,
        author: String,
        genre: String,
        location: String,
        adImage: Bitmap?,
        update: Boolean = false
    ) {
        isSavingInProgress.value = true

        val advert = if (update) {
            val currentAdvertId = currentAdvert?.adId
            if (currentAdvertId == null) {
                _advertSaveStatus.value = UiState(message = app.getString(R.string.advert_id_missing_error))
                return
            }

            Advert(
                adId = currentAdvertId,
                title = title,
                author = author,
                genre = genre,
                location = location,
                adCreator = currentAdvert?.adCreator,
                creationTime = currentAdvert?.creationTime,
                imageUrl = currentAdvert?.imageUrl
            )
        } else {
            Advert(
                title = title,
                author = author,
                genre = genre,
                location = location
            )
        }

        viewModelScope.launch {
            try {
                val result = if (update) {
                    advertsRepo.updateAdvert(advert, adImage)
                } else {
                    advertsRepo.saveAdvert(advert, adImage)
                }

                isSavingInProgress.postValue(false)

                when (result) {
                    is Result.Failure-> {
                        _advertSaveStatus.value = UiState(result.message)
                    }
                    is Result.Success-> {
                        val successMessage = if (update) {
                            app.getString(R.string.advert_updated_successfully)
                        } else {
                            app.getString(R.string.advert_saved_successfully)
                        }
                        _advertSaveStatus.value = UiState(message = successMessage)
                    }
                }
            } catch (e: Exception) {
                _advertSaveStatus.value = UiState(
                    message = e.localizedMessage ?: app.getString(R.string.generic_error)
                )
            }
        }
    }

    fun saveOrUpdateAdvert(
        title: String?,
        author: String?,
        genre: String?,
        location: String?,
        adImage: Bitmap?
    ) {
        val isFieldsNotEmpty = listOf(title, author, genre, location).all { !it.isNullOrEmpty() }

        if (isFieldsNotEmpty) {
            if (currentAdvert != null) {
                processAdvert(
                    title = title ?: "",
                    author = author ?: "",
                    genre = genre ?: "",
                    location = location ?: "",
                    adImage = adImage,
                    update = true
                )
            } else {
                processAdvert(
                    title = title ?: "",
                    author = author ?: "",
                    genre = genre ?: "",
                    location = location ?: "",
                    adImage = adImage
                )
            }
        } else {
            _advertSaveStatus.value = UiState(message = app.getString(R.string.advert_missing_fields))
        }
    }

    fun initializeAdvertData(adId: String?) {
        if (adId.isNullOrEmpty()) {
            resetToDefaultValues()
            toolbarTitle.value = app.getString(R.string.create_ads_text)
        } else {
            isEditMode.value = false
            fetchCurrentAdvertDetails(adId)
            toolbarTitle.value = app.getString(R.string.edit_advert_title)
        }
    }

    private fun resetToDefaultValues() {
        title.value = ""
        author.value = ""
        genre.value = ""
        location.value = ""
        currentAdvert = null
    }

    private fun fetchCurrentAdvertDetails(advertId: String) {
        advertsRepo.advertDetailLiveData.observeForever(object : Observer<Pair<Advert, User?>?> {
            override fun onChanged(value: Pair<Advert, User?>?) {
                if (value != null) {
                    title.value = value.first.title
                    author.value = value.first.author
                    genre.value = value.first.genre
                    location.value = value.first.location
                    currentAdvert = value.first
                    currentAdvertImageUrl.value = value.first.imageUrl
                    advertsRepo.advertDetailLiveData.removeObserver(this)
                } else {
                    _advertSaveStatus.value = UiState(message = "Error fetching advert details!")
                }
            }
        })
        advertsRepo.fetchAdvertDetails(advertId)
    }

    fun fetchFavoriteAdverts() {
        advertsRepo.fetchFavoriteAdverts()
    }

    fun toggleFavoriteStatus(advertId: String) {
        viewModelScope.launch {
            val state: FavoriteUiState = if (isAdvertFavorite.value == true) {
                advertsRepo.removeAdvertFromFavorites(advertId)
                _isAdvertFavorite.postValue(false)
                FavoriteUiState(isSuccess = true)
            } else {
                advertsRepo.markAdvertAsFavorite(advertId)
                _isAdvertFavorite.postValue(true)
                FavoriteUiState(isSuccess = true)
            }
            _favoriteState.postValue(state)
        }
    }

    fun checkAdvertFavoriteStatus(advertId: String) {
        viewModelScope.launch {
            val isFavorite = advertsRepo.isAdvertFavorite(advertId)
            _isAdvertFavorite.postValue(isFavorite)
        }
    }

    fun resetUiStateSave() {
        _advertSaveStatus.value = UiState(message = null)
    }

    fun getAdvertDetails(advertId: String) {
        viewModelScope.launch {
            advertsRepo.fetchAdvertDetails(advertId)
        }
    }

    fun cleanUp() {
        advertsRepo.cleanUp()
    }
}