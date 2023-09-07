package se.rebeccazadig.bokholken.utils

import android.content.Context
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController

fun Fragment.showToast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this.context, message, length).show()
}

fun Context.showToast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}

fun Fragment.navigateBack() {
    findNavController().popBackStack()
}

fun View.navigateBack() {
    this.findNavController().popBackStack()
}


fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}


fun isEmail(contact: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(contact).matches()
}

fun isPhoneNumber(contact: String): Boolean {
    return contact.matches("^\\d{10}$".toRegex())
}