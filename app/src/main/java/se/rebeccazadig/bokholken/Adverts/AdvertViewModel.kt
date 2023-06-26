package se.rebeccazadig.bokholken.Adverts

import androidx.lifecycle.ViewModel

class AdvertViewModel : ViewModel() {

    private val advertRepo = AdvertsRepository.getInstance()
}
