package se.rebeccazadig.bokholken.login

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRepository private constructor() {

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")

    suspend fun saveUser(user: User): Result {
        delay(2_000)
        return withContext(Dispatchers.IO) {
            try {
                databaseReference.child(user.id).setValue(user).await()
                Result.Success
            } catch (e: Exception) {
                Result.Failure(e.message ?: "Error saving user!")
            }
        }
    }

    companion object {
        private var instance: UserRepository? = null

        fun getInstance() = instance ?: UserRepository().also {
            instance = it
        }
    }
}
