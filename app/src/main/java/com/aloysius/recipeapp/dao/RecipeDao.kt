package com.aloysius.recipeapp.dao

import androidx.room.*
import com.aloysius.recipeapp.data.Recipe

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipe")
    fun getAll(): List<Recipe>

    @Query("SELECT * FROM recipe WHERE uid IN (:recipeIds)")
    fun findAllByIds(recipeIds: IntArray): List<Recipe>

    @Insert
    fun insertAll(vararg recipes: Recipe)

    @Update
    fun update(r: Recipe?)

    @Delete
    fun delete(recipe: Recipe)
}