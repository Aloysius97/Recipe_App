package com.aloysius.recipeapp.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class DataConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun toIntList(value: String?): List<Int?>? {
            val listType: Type = object : TypeToken<List<Int?>?>() {}.type
            return Gson().fromJson(value, listType)
        }

        @TypeConverter
        @JvmStatic
        fun fromIntList(list: List<Int?>?): String? {
            val gson = Gson()
            return gson.toJson(list)
        }

        @TypeConverter
        @JvmStatic
        fun toStringList(value: String?): List<String?>? {
            val listType: Type = object : TypeToken<List<String?>?>() {}.type
            return Gson().fromJson(value, listType)
        }

        @TypeConverter
        @JvmStatic
        fun fromStringList(list: List<String?>?): String? {
            val gson = Gson()
            return gson.toJson(list)
        }

        @TypeConverter
        @JvmStatic
        fun toRecipeTypeList(value: String?): List<RecipeType?>? {
            val listType: Type = object : TypeToken<List<RecipeType?>?>() {}.type
            return Gson().fromJson(value, listType)
        }

        @TypeConverter
        @JvmStatic
        fun fromRecipeTypeList(list: List<RecipeType?>?): String? {
            val gson = Gson()
            return gson.toJson(list)
        }

        @TypeConverter
        @JvmStatic
        fun toIngredientList(value: String?): List<Ingredient?>? {
            val listType: Type = object : TypeToken<List<Ingredient?>?>() {}.type
            return Gson().fromJson(value, listType)
        }

        @TypeConverter
        @JvmStatic
        fun fromIngredientList(list: List<Ingredient?>?): String? {
            val gson = Gson()
            return gson.toJson(list)
        }
    }
}