package com.aloysius.recipeapp.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.aloysius.recipeapp.data.Ingredient

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredient")
    fun getAll(): List<Ingredient>

    @Query("SELECT * FROM ingredient WHERE uid IN (:ingredientIds)")
    fun findAllByIds(ingredientIds: IntArray): List<Ingredient>

    @Insert
    fun insertAll(vararg ingredients: Ingredient)

    @Delete
    fun delete(ingredient: Ingredient)

    @Query("DELETE FROM ingredient")
    fun deleteAll()
}