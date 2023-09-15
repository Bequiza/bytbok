package se.rebeccazadig.bokholken.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

object FireBaseReferences {

    val authInstance: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabaseInstance: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val firebaseStorageInstance: FirebaseStorage = FirebaseStorage.getInstance()

    val userDatabaseRef: DatabaseReference = firebaseDatabaseInstance.getReference("users_profile")
    val advertDatabaseRef: DatabaseReference = firebaseDatabaseInstance.getReference("Adverts")
    val favoritesDatabaseRef: DatabaseReference = firebaseDatabaseInstance.getReference("favorites")
    val advertImagesStorageRef: StorageReference = firebaseStorageInstance.reference.child("advert_images")

}