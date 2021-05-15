package com.aloysius.recipeapp.data

import androidx.room.*
import com.aloysius.recipeapp.data.DataConverter

@Entity
class User(
    @PrimaryKey(autoGenerate = true) val uid: Int? = null,
    @ColumnInfo(name = "first_name") val firstName: String,
    @ColumnInfo(name = "last_name") val lastName: String,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "password") val password: String,
    @TypeConverters(DataConverter::class)
    @ColumnInfo(name = "recipe_list") val recipeLists: List<Int>? = null,
    @TypeConverters(DataConverter::class)
    @ColumnInfo(name = "favourite_list") val favouriteLists: List<Int>? = null
)