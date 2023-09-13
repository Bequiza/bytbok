package se.rebeccazadig.bokholken.login

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import se.rebeccazadig.bokholken.data.FireBaseReferences
import se.rebeccazadig.bokholken.data.User

class UserRepository private constructor() {

    private val databaseReference = FireBaseReferences.userDatabaseRef
    suspend fun saveUser(user: User): Result<Unit> {
        delay(2_000)
        return withContext(Dispatchers.IO) {
            try {
                user.id?.let { databaseReference.child(it).setValue(user).await() }
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Failure(e.message ?: "Error saving user!")
            }
        }
    }

    suspend fun fetchUser(userId: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val result =
                    databaseReference.child(userId).get().await().getValue(User::class.java)
                result?.let {
                    Result.Success(it)
                } ?: Result.Failure("User not found")
            } catch (e: Exception) {
                Result.Failure(e.message ?: "error fetching user!")
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
