package se.rebeccazadig.bokholken.login

data class User(

    val id: String,
    val name: String? = null,
    val contact: String? = null,
    val city: String? = null,
)
