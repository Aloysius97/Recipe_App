package com.aloysius.recipeapp.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.aloysius.recipeapp.data.RecipeType

@Dao
interface RecipeTypeDao {
    @Query("SELECT * FROM recipetype")
    fun getAll(): List<RecipeType>

    @Query("SELECT * FROM recipetype WHERE uid IN (:recipeTypeIds)")
    fun findAllByIds(recipeTypeIds: IntArray): List<RecipeType>

    @Insert
    fun insertAll(vararg recipeTypes: RecipeType)

    @Delete
    fun delete(recipetype: RecipeType)
}