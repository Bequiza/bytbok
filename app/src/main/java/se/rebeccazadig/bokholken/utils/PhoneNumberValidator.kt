package se.rebeccazadig.bokholken.utils

import android.content.Context
import se.rebeccazadig.bokholken.R

class PhoneNumberValidator(private val context: Context) {

    fun validate(phone: String): ValidationResult {
        return if (phone.isPhoneNumber()) {
            ValidationResult(true, null)
        } else {
            ValidationResult(false, context.getString(R.string.invalid_phone_number))
        }
    }

    data class ValidationResult(val isValid: Boolean, val errorMessage: String?)
}
