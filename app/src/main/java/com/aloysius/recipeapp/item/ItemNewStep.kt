package com.aloysius.recipeapp.item

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.aloysius.recipeapp.R
import com.aloysius.recipeapp.Utils.getCannotBeEmptyString
import com.aloysius.recipeapp.databinding.ItemNewStepBinding
import com.xwray.groupie.viewbinding.BindableItem

class ItemNewStep(var noOfStep: Int = 1, var listener: View.OnClickListener) :
    BindableItem<ItemNewStepBinding>() {

    lateinit var stepDesc: String
    lateinit var mBinding: ItemNewStepBinding

    override fun bind(viewBinding: ItemNewStepBinding, position: Int) {
        mBinding = viewBinding
        viewBinding.tvNoStep.text = "$noOfStep."

        viewBinding.btnNew.setOnClickListener {
            if (!viewBinding.etStep.text.isNullOrBlank()) {
                stepDesc = viewBinding.etStep.text.toString()
                listener.onClick(it)
                viewBinding.etStep.text!!.clear()
                (viewBinding.root.context as AppCompatActivity).currentFocus?.clearFocus()
                noOfStep++
                notifyChanged()
            } else {
                viewBinding.etStep.error = viewBinding.root.context.getCannotBeEmptyString(R.string.steps)
            }
        }
    }

    fun appendIngredientName(ingredient: String) {
        var updatedString = ingredient
        if (!mBinding.etStep.text.toString().isNullOrEmpty()) {
            if (mBinding.etStep.text.toString().trim().last().toString() != (".")) {
                updatedString = ingredient.toLowerCase()
            }
        }
        return mBinding.etStep.append("$updatedString ")
    }

    fun hasFocus(): Boolean {
        return mBinding.etStep.hasFocus()
    }

    fun setError(msg: String){
        mBinding.etStep.error = msg
    }

    override fun getLayout() = R.layout.item_new_step
    override fun initializeViewBinding(view: View) = ItemNewStepBinding.bind(view)
}