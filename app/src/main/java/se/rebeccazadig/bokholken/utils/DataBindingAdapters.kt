package se.rebeccazadig.bokholken.utils

import androidx.appcompat.widget.Toolbar
import androidx.databinding.BindingAdapter

@BindingAdapter("app:toolbarTitle")
fun setToolbarTitle(toolbar: Toolbar, title: String?) {
    toolbar.title = title ?: ""
}