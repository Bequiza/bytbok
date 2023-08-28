package se.rebeccazadig.bokholken.login

import android.content.Context
import android.util.Log
import se.rebeccazadig.bokholken.data.User

private const val SHARED_PREF_NAME = "BOOK_EXCHANGE_SHARED_PREF"
private const val USER_ID_KEY = "USER_ID_KEY"
private const val USER_NAME_KEY = "USER_NAME_KEY"
private const val USER_CONTACT_KEY = "USER_CONTACT_KEY"

interface IUserStorage {
    fun saveUser(user: User)
    fun loadUser(): User?
    fun deleteUser()
}

class UserStorage(context: Context) : IUserStorage {

    private val sharedPref = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)

    override fun saveUser(user: User) {
        with(sharedPref.edit()) {
            putString(USER_ID_KEY, user.id)
            putString(USER_NAME_KEY, user.name)
            putString(USER_CONTACT_KEY, user.contact)
            apply()
        }
    }

    override fun loadUser(): User? {
        val id = sharedPref.getString(USER_ID_KEY, null)
        val name = sharedPref.getString(USER_NAME_KEY, null)
        val contact = sharedPref.getString(USER_CONTACT_KEY, null)

        if (listOf(id, name, contact).any { it == null }) return null

        return User(id!!, name!!, contact!!)
    }

    override fun deleteUser() {
        with(sharedPref.edit()) {
            remove(USER_ID_KEY)
            remove(USER_NAME_KEY)
            remove(USER_CONTACT_KEY)
            apply()
        }
    }
}