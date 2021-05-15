package com.aloysius.recipeapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentTransaction
import androidx.room.Room
import com.aloysius.recipeapp.Utils.setShowHide
import com.aloysius.recipeapp.data.Ingredient
import com.aloysius.recipeapp.data.Recipe
import com.aloysius.recipeapp.data.RecipeType
import com.aloysius.recipeapp.data.User
import com.aloysius.recipeapp.database.AppDatabase
import com.aloysius.recipeapp.databinding.ActivityRecipeListingBinding
import com.aloysius.recipeapp.item.ItemNoDataPlaceholder
import com.aloysius.recipeapp.item.ItemRecipe
import com.aloysius.recipeapp.R
import com.google.android.material.chip.Chip
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section

class RecipeListingActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityRecipeListingBinding
    lateinit var myAppDatabase: AppDatabase

    private var mAdapter = GroupAdapter<GroupieViewHolder>()
    private var mSection = Section()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_recipe_listing)
        mBinding.toolbar.title = getString(R.string.app_name)

        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeButtonEnabled(true)
        }

        mBinding.toolbar.inflateMenu(R.menu.menu_recipe_list)
        mBinding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_filter) {
                mBinding.llFilterType.setShowHide(mBinding.llFilterType.visibility == View.GONE)
            }
            true
        }

        init()
        getData()
    }

    private fun getData() {

        val typeNames = mBinding.chipRecipeType.children.filterIsInstance<Chip>().filter {
            it.isChecked
        }.map {
            it.text
        }.toMutableList()

        var filteredRecipeList = myAppDatabase.recipeDao().getAll()

        if (!typeNames.isNullOrEmpty()) {
            filteredRecipeList = myAppDatabase.recipeDao().getAll()
                .filter { it.typeLists.map { it.typeName }.intersect(typeNames).any() }
        }

        with(mSection) {
            clear()
            notifyChanged()
            addAll(filteredRecipeList.map {
                ItemRecipe(it)
            })
            setPlaceholder(ItemNoDataPlaceholder())
        }

        mBinding.swipeRefresh.isRefreshing = false
    }

    private fun init() {
        populateSampleData()

        mBinding.rvRecipe.adapter = mAdapter
        mAdapter.add(mSection)

        mAdapter.setOnItemClickListener { item, view ->
            if (item is ItemRecipe) {
                showRecipeDialog(item.recipe)
            }
        }

        mBinding.swipeRefresh.setOnRefreshListener {
            getData()
        }

        mBinding.fabNewRecipe.setOnClickListener {
            showRecipeDialog()
        }

        val recipeTypes = myAppDatabase.recipeTypeDao().getAll()
        recipeTypes.forEach {
            addRecipeType(it.typeName)
        }
    }

    private fun showRecipeDialog(recipe: Recipe? = null) {
        val newRecipeDialog = NewRecipeDialog.newInstance(recipe)
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction.add(android.R.id.content, newRecipeDialog)
            .addToBackStack(NewRecipeDialog.TAG).commit()

        mBinding.fabNewRecipe.hide()
    }

    private fun addRecipeType(typeName: String) {
        val chip = LayoutInflater.from(this)
            .inflate(R.layout.recipe_type_filter_chip, mBinding.chipRecipeType, false) as Chip

        with(chip) {
            text = typeName
            id = ViewCompat.generateViewId()
            setOnCheckedChangeListener { _, _ ->
                val checkedRecipeType = mBinding.chipRecipeType.checkedChipIds.size
                mBinding.tvRecipeType.text = getString(
                    R.string.select_recipe_types_no,
                    checkedRecipeType
                )
                getData()
            }
        }

        mBinding.chipRecipeType.addView(chip)
    }

    private fun populateSampleData() {
        myAppDatabase =
            Room.databaseBuilder(applicationContext, AppDatabase::class.java, "recipe-database")
                .allowMainThreadQueries().build()

        if (myAppDatabase.userDao().getAll().isNullOrEmpty()) {
            myAppDatabase.userDao().insertAll(
                User(
                    firstName = "Aloysius",
                    lastName = "1",
                    username = "Aloysius1",
                    password = "Abcd1234."
                )
            )
        }
        if (myAppDatabase.ingredientDao().getAll().isNullOrEmpty()) {
            myAppDatabase.ingredientDao()
                .insertAll(
                    Ingredient(ingredientName = "Salt"),
                    Ingredient(ingredientName = "Pepper"),
                    Ingredient(ingredientName = "Olive oil"),
                    Ingredient(ingredientName = "Vegetable oil"),
                    Ingredient(ingredientName = "All-purpose flour"),
                    Ingredient(ingredientName = "Sugar")
                )
        }

        /**Reference https://kotlinlang.org/docs/functions.html#variable-number-of-arguments-varargs
         *for below code as the insertAll function is accepting vararg (accept arguments 1 by 1)
         *so need to use the spread operator to spread the array**/
        if (myAppDatabase.recipeTypeDao().getAll().isNullOrEmpty()) {
            val recipeTypes = resources.getStringArray(R.array.recipeTypes)
            myAppDatabase.recipeTypeDao().insertAll(
                *recipeTypes.map { eachTypeName ->
                    RecipeType(typeName = eachTypeName)
                }.toTypedArray()
            )
        }
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)
        getData()
        mBinding.fabNewRecipe.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mBinding.fabNewRecipe.show()
    }
}