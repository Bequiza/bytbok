package se.rebeccazadig.bokholken.adverts

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import se.rebeccazadig.bokholken.myAdverts.UiState

class AdvertViewModel : ViewModel() {
    private val advertsRepo = AdvertsRepository.getInstance()

    private val _advertsLiveData = advertsRepo.advertsLiveData
    val advertsLiveData: LiveData<List<Adverts>> get() = _advertsLiveData
    private val _uiState = MutableLiveData(UiState(false, null))
    internal val uiState: LiveData<UiState> get() = _uiState

    val isInternetAvailable = MutableLiveData<Boolean>()
    val searchQuery = MutableLiveData("")

    // This LiveData holds the filtered list
    private val _filteredAdverts = MediatorLiveData<List<Adverts>>()
    val filteredAdverts: LiveData<List<Adverts>> get() = _filteredAdverts


    private val _advertSaveStatus = MutableLiveData<Boolean>()
    val advertSaveStatus: LiveData<Boolean> get() = _advertSaveStatus

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

    fun saveAdvert(advert: Adverts) {
        viewModelScope.launch {
            try {
                advertsRepo.saveAdvert(advert)
                _advertSaveStatus.value = true
            } catch (e: Exception) {
                // Log the error
                _advertSaveStatus.value = false
            }
        }
    }

    fun updateInternetStatus(context: Context) {
        isInternetAvailable.value = advertsRepo.isNetworkAvailable(context)
    }

    fun registerNetworkCallback(context: Context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()

        connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isInternetAvailable.postValue(true)
            }

            override fun onLost(network: Network) {
                isInternetAvailable.postValue(false)
            }
        })
    }
}