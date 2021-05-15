package com.aloysius.recipeapp.item

import android.view.LayoutInflater
import android.view.View
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import com.aloysius.recipeapp.R
import com.aloysius.recipeapp.Utils.copyClipboard
import com.aloysius.recipeapp.bindingmodel.ItemRecipeBindingModel
import com.aloysius.recipeapp.data.Recipe
import com.aloysius.recipeapp.databinding.ContentStepBinding
import com.aloysius.recipeapp.databinding.ItemRecipeBinding
import com.google.android.material.chip.Chip
import com.xwray.groupie.viewbinding.BindableItem

class ItemRecipe(var recipe: Recipe) : BindableItem<ItemRecipeBinding>() {

    private var isExpanded = false
    private var rotationAngle = 0
    private lateinit var mBinding: ItemRecipeBinding

    override fun bind(viewBinding: ItemRecipeBinding, position: Int) {
        isExpanded = false
        mBinding = viewBinding.apply {
            item = ItemRecipeBindingModel(recipe)
        }
        /*with(expandViewClicked) {
            mBinding.tvRecipeName.setOnClickListener(this)
            mBinding.tvDifficultyLevel.setOnClickListener(this)
            mBinding.ivExpand.setOnClickListener(this)
        }*/
        setupStepList()
        setUpIngredientList()
        setUpRecipeList()
    }

    /**Attempt to do expandable view in listing screen,
     * but having some bugs that couldn't risk spending time solve right now,
     * so this features is hidden **/
    private var expandViewClicked = View.OnClickListener { view ->
        /*isExpanded = !isExpanded
        if (isExpanded) {
            mBinding.llMoreContent.show()
        } else {
            mBinding.llMoreContent.hide()
        }
        rotationAngle = if (rotationAngle == 0) 180 else 0 //toggle
        mBinding.ivExpand.animate().rotation(rotationAngle.toFloat()).setDuration(300).start()*/
    }

    private fun setUpRecipeList() {
        mBinding.chipRecipeType.removeAllViews()
        recipe.typeLists.forEach { eachType ->
            val chip = LayoutInflater.from(mBinding.root.context)
                .inflate(R.layout.ingredient_chip, mBinding.chipRecipeType, false) as Chip

            with(chip) {
                text = eachType.typeName
                id = ViewCompat.generateViewId()
                isCloseIconVisible = false

                setOnClickListener {
                    mBinding.root.context.copyClipboard(
                        eachType.typeName,
                        eachType.typeName)
                }
            }

            mBinding.chipRecipeType.addView(chip, 0)
        }
    }

    private fun setUpIngredientList() {
        mBinding.chipIngredient.removeAllViews()
        recipe.ingredientLists.forEach { eachIngredient ->
            val chip = LayoutInflater.from(mBinding.root.context)
                .inflate(R.layout.ingredient_chip, mBinding.chipIngredient, false) as Chip

            with(chip) {
                text = eachIngredient.ingredientName
                id = ViewCompat.generateViewId()
                isCloseIconVisible = false

                setOnClickListener {
                    mBinding.root.context.copyClipboard(
                        eachIngredient.ingredientName,
                        eachIngredient.ingredientName
                    )
                }
            }

            mBinding.chipIngredient.addView(chip, 0)
        }
    }

    private fun setupStepList() {
        var noOfStep = 1

        mBinding.llSteps.removeAllViews()
        recipe.stepLists.forEach {
            val stepViewBinding: ContentStepBinding = DataBindingUtil.inflate(
                LayoutInflater.from(mBinding.root.context),
                R.layout.content_step,
                mBinding.llSteps,
                false
            )
            stepViewBinding.tvNoStep.text = "$noOfStep."
            stepViewBinding.tvStepDesc.text = it
            mBinding.llSteps.addView(stepViewBinding.root)
            noOfStep++
        }
        mBinding.llSteps.invalidate()
    }

    override fun getLayout() = R.layout.item_recipe
    override fun initializeViewBinding(view: View) = ItemRecipeBinding.bind(view)
}