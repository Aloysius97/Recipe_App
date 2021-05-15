package com.aloysius.recipeapp.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aloysius.recipeapp.dao.IngredientDao
import com.aloysius.recipeapp.dao.RecipeDao
import com.aloysius.recipeapp.dao.RecipeTypeDao
import com.aloysius.recipeapp.dao.UserDao
import com.aloysius.recipeapp.data.*

@Database(
    entities = arrayOf(User::class, Recipe::class, RecipeType::class, Ingredient::class),
    version = 1
)
@TypeConverters(DataConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun recipeDao(): RecipeDao
    abstract fun recipeTypeDao(): RecipeTypeDao
    abstract fun ingredientDao(): IngredientDao
}