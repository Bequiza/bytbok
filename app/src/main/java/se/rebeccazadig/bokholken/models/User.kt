package se.rebeccazadig.bokholken.models

import se.rebeccazadig.bokholken.data.ContactType

data class User(
    val id: String? = null,
    val name: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val preferredContactMethod: ContactType = ContactType.UNKNOWN
)