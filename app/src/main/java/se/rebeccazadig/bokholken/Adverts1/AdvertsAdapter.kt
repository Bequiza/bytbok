package se.rebeccazadig.bokholken.adverts

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import se.rebeccazadig.bokholken.R
import se.rebeccazadig.bokholken.data.Advert
import se.rebeccazadig.bokholken.databinding.ListItemBinding
import se.rebeccazadig.bokholken.utils.formatDateForDisplay

private object AdvertDiffCallback : DiffUtil.ItemCallback<Advert>() {
    override fun areItemsTheSame(oldItem: Advert, newItem: Advert): Boolean {
        return oldItem.adId == newItem.adId
    }

    override fun areContentsTheSame(oldItem: Advert, newItem: Advert): Boolean {
        return oldItem == newItem
    }
}

class AdvertsAdapter(
    private val onAdvertClick: (Advert) -> Unit
) : ListAdapter<Advert, AdvertsAdapter.ViewHolder>(AdvertDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val advert = getItem(position)
        holder.bind(advert)
        holder.itemView.setOnClickListener { onAdvertClick(advert) }
    }

    inner class ViewHolder(val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(advert: Advert) {
            binding.titleTV.text = advert.title
            binding.authorTV.text = advert.author
            binding.cityTV.text = advert.city

            val formattedDate =
                advert.creationTime?.let { formatDateForDisplay(binding.root.context, it) }
            binding.creationTimeTV.text = formattedDate


            advert.adId?.takeIf { it.isNotEmpty() }?.let { adId ->
                Log.d("AD_ID", "adId: $adId")
                val imageRef = Firebase.storage.reference.child("annonser").child(adId)
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    Glide.with(binding.imageView.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(binding.imageView)
                }.addOnFailureListener { exception ->
                    // If there's an error, just set the placeholder
                    Glide.with(binding.imageView.context)
                        .load(R.drawable.placeholder_image)
                        .into(binding.imageView)
                    Log.e("ImageLoadError", "Failed to load , image== AD_ID  adId: $adId", exception)
                }
            } ?: run {
                // If adId is null or empty, just set the placeholder
                Glide.with(binding.imageView.context)
                    .load(R.drawable.placeholder_image)
                    .into(binding.imageView)
            }
        }
    }
}