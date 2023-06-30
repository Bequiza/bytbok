package se.rebeccazadig.bokholken.login

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class UserRepository private constructor() {

    suspend fun saveUser(user: User): Result {
        delay(2_000)
        return withContext(Dispatchers.IO) {
            if (user.name.startsWith("A", ignoreCase = true)) {
                Log.i("Emma", "Saving user ${user.name}, ${user.contact}, ${user.city}, ${user.id}")
                Result.Success
            } else {
                val errorMessage = "Name must start with 'A'"
                Result.Failure(errorMessage)
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
