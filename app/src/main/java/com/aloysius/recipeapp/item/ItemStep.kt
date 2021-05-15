package com.aloysius.recipeapp.item

import android.view.View
import com.aloysius.recipeapp.R
import com.aloysius.recipeapp.bindingmodel.ItemStepBindingModel
import com.aloysius.recipeapp.databinding.ItemStepBinding
import com.xwray.groupie.viewbinding.BindableItem

class ItemStep(
    var itemStepModel: ItemStepModel,
    var listener: ItemStepListener
) :
    BindableItem<ItemStepBinding>(itemStepModel.noOfStep.toLong()) {

    interface ItemStepListener {
        fun onMoveUp(item: ItemStep)
        fun onMoveDown(item: ItemStep)
        fun onRemove(item: ItemStep)
    }

    override fun bind(viewBinding: ItemStepBinding, position: Int) {
        viewBinding.item = ItemStepBindingModel(itemStepModel)

        viewBinding.ivUp.setOnClickListener {
            listener.onMoveUp(this)
        }

        viewBinding.ivDown.setOnClickListener {
            listener.onMoveDown(this)
        }

        viewBinding.ivRemove.setOnClickListener {
            listener.onRemove(this)
        }
    }

    override fun getLayout() = R.layout.item_step
    override fun initializeViewBinding(view: View) = ItemStepBinding.bind(view)
}

//Only for ItemStep usage
class ItemStepModel(
    var noOfStep: Int,
    var stepDesc: String,
    var showEditing: Boolean = false,
    var isFirst: Boolean = false,
    var isLast: Boolean = false
)