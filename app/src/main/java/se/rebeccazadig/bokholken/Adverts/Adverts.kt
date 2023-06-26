package se.rebeccazadig.bokholken.Adverts

data class Adverts(
    val bokTitel: String? = null,
    val bokForfattare: String? = null,
    val genre: String? = null,
    val stad: String? = null,
    var kontaktsatt: String? = null,
) {

    var adid = ""
    var adcreator = ""
}
