package se.rebeccazadig.bokholken.myAdverts

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import se.rebeccazadig.bokholken.adverts.AdvertsRepository
import se.rebeccazadig.bokholken.data.Advert

class MyAdvertsRepository private constructor() {


    private val myAuth = Firebase.auth
    private val databaseRef = FirebaseDatabase.getInstance().getReference("Adverts")


    companion object {
        private var instance: MyAdvertsRepository? = null

        fun getInstance() = instance ?: MyAdvertsRepository().also {
            instance = it
        }
    }
}
