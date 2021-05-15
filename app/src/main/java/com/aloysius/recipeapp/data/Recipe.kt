package com.aloysius.recipeapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Recipe(
    @PrimaryKey(autoGenerate = true) var uid: Int? = null,
    @ColumnInfo(name = "recipe_name") var recipeName: String,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "difficulty") var difficulty: Int,
    @ColumnInfo(name = "total_favourite") var totalFavourite: Int? = 0,
    @ColumnInfo(name = "step_list") var stepLists: List<String>,
    @ColumnInfo(name = "image_list") var imageLists: List<String>,
    @ColumnInfo(name = "recipe_type_list") var typeLists: List<RecipeType>,
    @ColumnInfo(name = "ingredient_type_list") var ingredientLists: List<Ingredient>
)