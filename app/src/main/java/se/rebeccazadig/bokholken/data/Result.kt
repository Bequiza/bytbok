package se.rebeccazadig.bokholken.data

sealed class Result {
    data class Failure(val message: String) : Result()
    object Success : Result()
}