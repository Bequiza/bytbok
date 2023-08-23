package se.rebeccazadig.bokholken.models

data class Advert(
    var imageUrl: String? = null,
    val title: String? = null,
    val author: String? = null,
    val genre: String? = null,
    var adId: String? = null,
    var adCreator: String? = null,
    var creationTime: Long? = null,
    var location: String? = null
)