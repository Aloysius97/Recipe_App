package com.aloysius.recipeapp.bindingmodel

import android.view.View
import com.aloysius.recipeapp.item.ItemStepModel

class ItemStepBindingModel(var data: ItemStepModel) {
    fun getNoOfStep(): String {
        return "${data.noOfStep}."
    }

    fun getStepDesc(): String {
        return data.stepDesc
    }

    fun ivUpVisibility(): Int {
        return if (data.showEditing && !data.isFirst) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    fun ivDownVisibility(): Int {
        return if (data.showEditing && !data.isLast) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    fun ivRemoveVisibility(): Int {
        return if (data.showEditing) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}