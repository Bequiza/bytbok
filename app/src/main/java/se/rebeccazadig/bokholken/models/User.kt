package se.rebeccazadig.bokholken.data
data class User(
    val id: String? = null,
    val name: String? = null,
    val contact: String? = null,
    val preferredContactMethod: ContactType = ContactType.UNKNOWN
)