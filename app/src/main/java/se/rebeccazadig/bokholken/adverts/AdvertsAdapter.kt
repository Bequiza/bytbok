package se.rebeccazadig.bokholken.adverts

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import se.rebeccazadig.bokholken.R
import se.rebeccazadig.bokholken.databinding.ListItemBinding
import se.rebeccazadig.bokholken.models.Advert
import se.rebeccazadig.bokholken.utils.formatDateForDisplay

private object AdvertDiffCallback : DiffUtil.ItemCallback<Advert>() {
    override fun areItemsTheSame(oldItem: Advert, newItem: Advert): Boolean {
        return oldItem.adId == newItem.adId
    }

    override fun areContentsTheSame(oldItem: Advert, newItem: Advert): Boolean {
        return oldItem == newItem
    }
}

class AdvertsAdapter : ListAdapter<Advert, AdvertsAdapter.ViewHolder>(AdvertDiffCallback) {

    var onAdvertClick: ((Advert) -> Unit)? = null
    var onDeleteAdvertClick: ((Advert) -> Unit)? = null
    var onEditAdvertClick: ((Advert) -> Unit)? = null
    var showIcons: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val advert = getItem(position)
        holder.bind(advert)
        holder.itemView.setOnClickListener { onAdvertClick?.invoke(advert) }
    }

    inner class ViewHolder(val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(advert: Advert) {
            binding.titleTV.text = advert.title
            binding.authorTV.text = advert.author
            binding.locationTV.text = advert.location

            binding.deleteAdvertButton.isVisible = showIcons
            binding.editAdvertButton.isVisible = showIcons

            binding.deleteAdvertButton.setOnClickListener {
                Log.d("--adapter", "bind: deleteclick")
                onDeleteAdvertClick?.invoke(advert)
            }

            binding.editAdvertButton.setOnClickListener {
                onEditAdvertClick?.invoke(advert)
            }

            val formattedDate =
                advert.creationTime?.let { formatDateForDisplay(binding.root.context, it) }
            binding.creationTimeTV.text = formattedDate

            Glide.with(binding.imageView.context)
                .load(advert.imageUrl)
                .placeholder(R.drawable.loading_image)
                .error(R.drawable.placeholder_image)
                .into(binding.imageView)
        }
    }
}