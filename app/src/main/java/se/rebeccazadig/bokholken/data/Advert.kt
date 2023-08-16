package se.rebeccazadig.bokholken.data

data class Advert(
    val imageUri: String? = null,
    val title: String? = null,
    val author: String? = null,
    val genre: String? = null,
    val city: String? = null,
    val contact: String? = null,
    var adId: String? = null,
    var adCreator: String? = null,
    var creationTime: Long? = null,
    var isFavorite: Boolean = false
)