package se.rebeccazadig.bokholken.utils

import android.content.Context
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
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


fun String.isEmail(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isPhoneNumber(): Boolean {
    return this.matches("^\\d{10}$".toRegex())
}

fun Context.hideKeyboard(view: View) {
    val inputManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun View.hideKeyboard() {
    context.hideKeyboard(this)
}