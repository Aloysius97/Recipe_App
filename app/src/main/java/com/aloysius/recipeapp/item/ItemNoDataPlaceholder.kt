package com.aloysius.recipeapp.item

import android.view.View
import com.aloysius.recipeapp.R
import com.aloysius.recipeapp.databinding.ItemNoDataPlaceholderBinding
import com.xwray.groupie.viewbinding.BindableItem

class ItemNoDataPlaceholder:BindableItem<ItemNoDataPlaceholderBinding>() {
    override fun bind(viewBinding: ItemNoDataPlaceholderBinding, position: Int) {

    }

    override fun getLayout() = R.layout.item_no_data_placeholder

    override fun initializeViewBinding(view: View) = ItemNoDataPlaceholderBinding.bind(view)
}