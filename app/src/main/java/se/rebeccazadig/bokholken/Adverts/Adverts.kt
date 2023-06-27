package se.rebeccazadig.bokholken.Adverts

data class Adverts(
    val title: String,
    val author: String,
    val genre: String,
    val city: String,
    var contact: String,
) {

    var adid = ""
    var adcreator = ""
}
