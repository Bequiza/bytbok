package se.rebeccazadig.bokholken.login

import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import se.rebeccazadig.bokholken.data.User

class UserRepository private constructor() {

    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("users")

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

    suspend fun fetchUser(userId: String): Pair<Result, User?> {

        return withContext(Dispatchers.IO) {
            try {
                val snapshot = databaseReference.child(userId).get().await()
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    Pair(Result.Success, user)
                } else {
                    Pair(Result.Failure("User not found"), null)
                }
            } catch (e: Exception) {
            Pair(Result.Failure(e.message ?: "error fetching user!"), null)
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
