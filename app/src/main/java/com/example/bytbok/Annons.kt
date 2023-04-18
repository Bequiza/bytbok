package com.example.bytbok

import android.widget.EditText

data class Annons ( val bokTitel : String? = null,
                    val bokForfattare : String? = null,
                    val genre : String? = null,
                    val stad : String? = null,
                    var kontaktsatt : String? = null) {

    var adid = ""
    var adcreator = ""
}