package com.aloysius.recipeapp.bindingmodel

import android.content.Context
import com.aloysius.recipeapp.R
import com.aloysius.recipeapp.data.Recipe

class ItemRecipeBindingModel(var data: Recipe) {
    fun getRecipeName(): String {
        return data.recipeName
    }

    fun getRecipeDesc(): String {
        return data.description
    }

    fun getDisplayedDifficultyLevel(context: Context): String {
        return context.getString(R.string.difficulty_level, data.difficulty)
    }

    fun getRecipeThumbnailUrl(): String {
        return data.imageLists.first()
    }
}