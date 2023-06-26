package se.rebeccazadig.bokholken.Adverts

data class Adverts(
    val bookTitle: String? = null,
    val bookAuthor: String? = null,
    val genre: String? = null,
    val city: String? = null,
    var contact: String? = null,
) {

    var adid = ""
    var adcreator = ""
}
