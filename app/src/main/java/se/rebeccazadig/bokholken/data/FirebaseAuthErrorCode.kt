package se.rebeccazadig.bokholken.data

import se.rebeccazadig.bokholken.R

enum class ErrorCode(val firebaseCode: String, val errorMessageResId: Int) {
    INVALID_CUSTOM_TOKEN("ERROR_INVALID_CUSTOM_TOKEN", R.string.error_invalid_custom_token),
    CUSTOM_TOKEN_MISMATCH("ERROR_CUSTOM_TOKEN_MISMATCH", R.string.error_custom_token_mismatch),
    INVALID_CREDENTIAL("ERROR_INVALID_CREDENTIAL", R.string.error_invalid_credential),
    INVALID_EMAIL("ERROR_INVALID_EMAIL", R.string.error_invalid_email),
    WRONG_PASSWORD("ERROR_WRONG_PASSWORD", R.string.error_wrong_password),
    USER_MISMATCH("ERROR_USER_MISMATCH", R.string.error_user_mismatch),
    REQUIRES_RECENT_LOGIN("ERROR_REQUIRES_RECENT_LOGIN", R.string.error_requires_recent_login),
    ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL("ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL", R.string.error_account_exists_with_different_credential),
    EMAIL_ALREADY_IN_USE("ERROR_EMAIL_ALREADY_IN_USE", R.string.error_email_already_in_use),
    CREDENTIAL_ALREADY_IN_USE("ERROR_CREDENTIAL_ALREADY_IN_USE", R.string.error_credential_already_in_use),
    USER_DISABLED("ERROR_USER_DISABLED", R.string.error_user_disabled),
    USER_TOKEN_EXPIRED("ERROR_USER_TOKEN_EXPIRED", R.string.error_user_token_expired),
    INVALID_USER_TOKEN("ERROR_INVALID_USER_TOKEN", R.string.error_invalid_user_token),
    OPERATION_NOT_ALLOWED("ERROR_OPERATION_NOT_ALLOWED", R.string.error_operation_not_allowed),
    WEAK_PASSWORD("ERROR_WEAK_PASSWORD", R.string.error_weak_password),
    MISSING_EMAIL("ERROR_MISSING_EMAIL", R.string.error_missing_email),
    USER_NOT_FOUND("ERROR_USER_NOT_FOUND", R.string.error_user_not_found),
    TOO_MANY_REQUESTS("ERROR_TOO_MANY_REQUESTS", R.string.error_too_many_requests),
    NETWORK_ERROR("NETWORK_ERROR", R.string.error_no_internet_connection),
    UNKNOWN("", R.string.error_unknown);  // Fallback for any unexpected error

    companion object {
        fun fromFirebaseCode(firebaseCode: String): ErrorCode {
            return values().firstOrNull { it.firebaseCode == firebaseCode } ?: UNKNOWN
        }
    }
}