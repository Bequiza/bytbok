package se.rebeccazadig.bokholken.listings

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

class AnnonsAdapter() : RecyclerView.Adapter<AnnonsAdapter.ViewHolder>() {

    var minSidaFrag: MyPageFragment? = null
    var annonsfrag: AnnonsFragment? = null
    var gilladefrag: GilladeObjektFragment? = null

    var allaAnnonser = mutableListOf<Annons>()
    var filtreradeAnnonser = mutableListOf<Annons>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bokBild: ImageView
        val bokTitel: TextView
        val bokForfattare: TextView
        val bokStad: TextView
        val lasMerAnnonsButton: Button

        init {
            bokBild = view.findViewById(R.id.imageView)
            bokTitel = view.findViewById(R.id.listTitelTV)
            bokForfattare = view.findViewById(R.id.listForfattareTV)
            bokStad = view.findViewById(R.id.listStadTV)
            lasMerAnnonsButton = view.findViewById(R.id.lasMerAnnonsButton)
            // villLasa = view.findViewById(R.id.villLasaFardigAnnonsButton)
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
