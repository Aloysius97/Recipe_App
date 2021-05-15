package com.aloysius.recipeapp.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.aloysius.recipeapp.R

class DataBindingAdapter {
    companion object {
        @BindingAdapter("imageUrl")
        @JvmStatic
        fun loadImageUrl(view: ImageView, imageUrl: String) {
            Glide.with(view)
                .load(imageUrl)
                .centerCrop()
                .placeholder(view.context.getDrawable(R.drawable.ic_baseline_image_search_24))
                .into(view)
        }
    }
}