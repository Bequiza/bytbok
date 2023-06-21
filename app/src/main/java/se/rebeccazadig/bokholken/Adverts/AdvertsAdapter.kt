package se.rebeccazadig.bokholken.Adverts

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import se.rebeccazadig.bokholken.*
import se.rebeccazadig.bokholken.mypage.MyPageFragment

class AdvertsAdapter() : RecyclerView.Adapter<AdvertsAdapter.ViewHolder>() {

    var minSidaFrag: MyPageFragment? = null
    var annonsfrag: AdvertsFragment? = null
    var gilladefrag: FavoriteFragment? = null

    var allaAnnonser = mutableListOf<Adverts>()
    var filtreradeAnnonser = mutableListOf<Adverts>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bokBild: ImageView
        val bokTitel: TextView
        val bokForfattare: TextView
        val bokStad: TextView
        val lasMerAnnonsButton: Button

        init {
            bokBild = view.findViewById(R.id.imageView)
            bokTitel = view.findViewById(R.id.titleTV)
            bokForfattare = view.findViewById(R.id.authorTV)
            bokStad = view.findViewById(R.id.cityTV)
            lasMerAnnonsButton = view.findViewById(R.id.favoritesButton)
            // villLasa = view.findViewById(R.id.favoriteButton)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.i("pia11debug", "SKAPA RAD")

        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.i("pia11debug", "RITA RAD " + position.toString())

        holder.bokTitel.text = filtreradeAnnonser[position].bokTitel
        holder.bokForfattare.text = filtreradeAnnonser[position].bokForfattare
        holder.bokStad.text = filtreradeAnnonser[position].stad

        holder.lasMerAnnonsButton.setOnClickListener {
            if (annonsfrag != null) {
                annonsfrag!!.clickReadmore(filtreradeAnnonser[position])
            }
            if (minSidaFrag != null) {
                minSidaFrag!!.clickReadmore(filtreradeAnnonser[position])
            }
            if (gilladefrag != null) {
                gilladefrag!!.clickReadmore(filtreradeAnnonser[position])
            }
        }

        holder.bokBild.setImageBitmap(null)

        var storageRef = Firebase.storage.reference
        var imageRef = storageRef.child("annonser").child(filtreradeAnnonser[position].adid)

        imageRef.getBytes(1000000).addOnSuccessListener {
            var bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)

            holder.bokBild.setImageBitmap(bitmap)
        }.addOnFailureListener {
        }
    }

    override fun getItemCount(): Int {
        return filtreradeAnnonser.size
    }
}
