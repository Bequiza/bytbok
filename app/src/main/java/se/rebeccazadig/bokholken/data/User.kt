package se.rebeccazadig.bokholken.data

data class User(
    val id: String = "",
    val name: String = "",
    val contact: String = ""
) {
    constructor() : this("", "", "")
 }