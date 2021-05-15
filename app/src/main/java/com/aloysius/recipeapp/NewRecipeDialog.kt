package com.aloysius.recipeapp

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.LayoutInflater
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.bumptech.glide.Glide
import com.aloysius.recipeapp.Utils.copyClipboard
import com.aloysius.recipeapp.Utils.getCannotBeEmptyString
import com.aloysius.recipeapp.Utils.showErrorSnackBar
import com.aloysius.recipeapp.Utils.setShowHide
import com.aloysius.recipeapp.data.Ingredient
import com.aloysius.recipeapp.data.Recipe
import com.aloysius.recipeapp.database.AppDatabase
import com.aloysius.recipeapp.databinding.DialogNewRecipeBinding
import com.aloysius.recipeapp.item.ItemNewStep
import com.aloysius.recipeapp.item.ItemStep
import com.aloysius.recipeapp.item.ItemStepModel
import com.aloysius.recipeapp.viewmodel.RecipeViewModel
import com.aloysius.recipeapp.R
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.xwray.groupie.Group
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import java.util.*

@RuntimePermissions
class NewRecipeDialog : DialogFragment() {

    private lateinit var mBinding: DialogNewRecipeBinding
    private lateinit var mViewModel: RecipeViewModel
    private lateinit var myAppDatabase: AppDatabase

    private var mAdapter = GroupAdapter<GroupieViewHolder>()
    private var mSection = Section()

    private var itemNewStep: ItemNewStep? = null
    private var ingredientItems: MutableList<String> = mutableListOf()

    private var isEditingStep = false

    private var imageUploaded = false
    private var ingredientListUpdated = false

    private var recipe: Recipe? = null

    companion object {
        const val TAG = "NewRecipeDialog"
        const val RECIPE = "recipe"
        private const val REQUEST_IMAGE = 2021

        fun newInstance(recipe: Recipe? = null): NewRecipeDialog {
            return NewRecipeDialog().apply {
                arguments = Bundle().apply {
                    putString(RECIPE, Gson().toJson(recipe))
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_new_recipe,
            null,
            false
        )

        recipe = Gson().fromJson(arguments?.getString(RECIPE), Recipe::class.java)
        mBinding.btnDeleteRecipe.visibility = if (recipe == null) View.GONE else View.VISIBLE

        val msg =
            if (recipe == null) getString(R.string.new_recipe) else getString(R.string.edit_recipe)
        mBinding.toolbar.title = msg

        (activity as AppCompatActivity?)!!.setSupportActionBar(mBinding.toolbar)

        val actionBar: ActionBar? = (activity as AppCompatActivity?)!!.supportActionBar

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
        }
        setHasOptionsMenu(true)

        myAppDatabase =
            Room.databaseBuilder(requireContext(), AppDatabase::class.java, "recipe-database")
                .allowMainThreadQueries().build()

        mViewModel = ViewModelProvider(this).get(RecipeViewModel::class.java)
        mViewModel.uploadImageLiveData.observe(viewLifecycleOwner, {
            imageUploaded = true
            proceedSaveNewRecipe()
        })
        mViewModel.errorLiveData.observe(viewLifecycleOwner, {
            mBinding.llProgress.setShowHide(false)
            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
        })

        init()

        return mBinding.root
    }

    private fun init() {

        setUpRecipeIngredients()
        setUpRecipeSteps()
        setUpRecipeTypes()
        updateDifficultyLabel(1f)

        //Populate existing Recipe data
        if (recipe != null) {
            imageUploaded = true
            mViewModel.imageUrl = recipe!!.imageLists.first()
            Glide.with(requireContext())
                .load(recipe!!.imageLists.first())
                .centerCrop()
                .into(mBinding.ivImage)
            mBinding.etRecipeName.setText(recipe?.recipeName)
            mBinding.etRecipeDesc.setText(recipe?.description)
            recipe?.difficulty?.toFloat()?.run {
                updateDifficultyLabel(this)
                mBinding.sliderRecipeDifficulty.value = this
            }
            recipe?.ingredientLists?.forEach { addIngredient(it.ingredientName) }
            var noOfStep = 1
            recipe?.stepLists?.forEach {
                addStep(noOfStep++, it)
            }
            itemNewStep?.noOfStep = noOfStep
            val typeNames = recipe?.typeLists?.map {
                it.typeName
            }
            mBinding.chipRecipeType.children.filterIsInstance<Chip>().forEach {
                it.isChecked = it.text in typeNames!!
            }
        }

        mBinding.btnUpload.setOnClickListener {
            showGallery()
        }

        mBinding.sliderRecipeDifficulty.addOnChangeListener { slider, value, fromUser ->
            updateDifficultyLabel(slider.value)
        }

        mBinding.btnDeleteRecipe.setOnClickListener {
            recipe?.run {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.delete_recipe))
                    .setMessage(getString(R.string.delete_recipe_confirmation_msg))
                    .setNegativeButton(android.R.string.cancel) { _, _ ->

                    }
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        myAppDatabase.recipeDao().delete(this)
                        imageUploaded = true
                        ingredientListUpdated = true
                        dismissWithAnimation()
                    }
                    .show()
            }
        }
    }

    private fun setUpRecipeTypes() {
        val recipeTypes = myAppDatabase.recipeTypeDao().getAll()
        for (eachRecipeType in recipeTypes) {
            addRecipeType(eachRecipeType.typeName)
        }
    }

    private fun setUpRecipeSteps() {
        mBinding.rvSteps.adapter = mAdapter
        mAdapter.add(mSection)

        itemNewStep = ItemNewStep(listener = itemNewStepListener).also {
            mSection.setFooter(it)
        }

        mBinding.tvEdit.setOnClickListener {
            isEditingStep = !isEditingStep

            with(mSection.groups) {
                forEach {
                    if (it is ItemStep) {
                        it.itemStepModel.showEditing = isEditingStep
                    }
                }
                updateStepListWithReorder(this)
            }

            if (isEditingStep) {
                mBinding.tvEdit.text = getString(R.string.done)
                mSection.removeFooter()
            } else {
                mBinding.tvEdit.text = getString(R.string.edit)
                mSection.setFooter(itemNewStep!!)
            }
        }

        mAdapter.setOnItemClickListener { item, _ ->
            if (item is ItemStep) {
                requireContext().copyClipboard(
                    "Step ${item.itemStepModel.noOfStep}",
                    item.itemStepModel.stepDesc
                )
            }
        }
    }

    private fun setUpRecipeIngredients() {
        ingredientItems.addAll(myAppDatabase.ingredientDao().getAll().map {
            it.ingredientName
        })
        val adapter = ArrayAdapter(requireContext(), R.layout.ingredient_list_item, ingredientItems)
        mBinding.actvIngredient.setAdapter(adapter)
        mBinding.btnAddIngredient.setOnClickListener {
            val ingredient = mBinding.actvIngredient.text.toString().capitalize()

            if (ingredient.isNotBlank()) {

                val selectedIngredients = mBinding.chipIngredient.children.map {
                    (it as Chip).text
                }

                if (!selectedIngredients.contains(ingredient)) {

                    addIngredient(ingredient)

                    if (!ingredientItems.contains(ingredient)) {
                        ingredientItems.add(ingredient)
                        adapter.addAll(ingredient)
                        adapter.notifyDataSetChanged()
                    }
                } else {
                    mBinding.chipIngredient.children.forEach {
                        if ((it as Chip).text == ingredient) {
                            mBinding.chipIngredient.removeView(it)
                            mBinding.chipIngredient.addView(it, 0)
                        }
                    }
                }

                mBinding.actvIngredient.text.clear()
            } else {
                Toast.makeText(
                    requireContext(),
                    requireContext().getCannotBeEmptyString(R.string.ingredients),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    private fun updateDifficultyLabel(value: Float) {
        mBinding.tvDifficultyLevel.text =
            getString(R.string.difficulty_level_int, value.toInt())
    }

    private var itemNewStepListener = View.OnClickListener {
        itemNewStep?.let {
            addStep(it.noOfStep, it.stepDesc)
        }
    }

    private fun addIngredient(ingredientName: String) {
        val chip = LayoutInflater.from(requireContext())
            .inflate(R.layout.ingredient_chip, mBinding.chipIngredient, false) as Chip

        with(chip) {
            text = ingredientName
            id = ViewCompat.generateViewId()
            setOnCloseIconClickListener {
                mBinding.chipIngredient.removeView(it)
                updateIngredientCount()
            }
            setOnClickListener {
                if (itemNewStep?.hasFocus() == true) {
                    itemNewStep?.appendIngredientName(ingredientName)
                } else {
                    requireContext().copyClipboard(ingredientName, ingredientName)
                }
            }
        }

        mBinding.chipIngredient.addView(chip, 0)
        updateIngredientCount()
    }

    private fun addStep(noOfStep: Int, stepDesc: String) {
        mSection.add(
            ItemStep(
                ItemStepModel(
                    noOfStep,
                    stepDesc,
                ),
                editStepListener
            )
        )

        updateStepListWithReorder(mSection.groups)
    }

    private var editStepListener = object : ItemStep.ItemStepListener {
        override fun onMoveUp(item: ItemStep) {
            val itemPosition = mSection.getPosition(item)
            swapStep(itemPosition, itemPosition - 1)
        }

        override fun onMoveDown(item: ItemStep) {
            val itemPosition = mSection.getPosition(item)
            swapStep(itemPosition, itemPosition + 1)
        }

        override fun onRemove(item: ItemStep) {
            val itemList = mSection.groups
            itemList.remove(item)
            itemNewStep!!.noOfStep--
            updateStepListWithReorder(itemList)
        }
    }

    private fun addRecipeType(typeName: String) {
        val chip = LayoutInflater.from(requireContext())
            .inflate(R.layout.recipe_chip, mBinding.chipRecipeType, false) as Chip

        with(chip) {
            text = typeName
            id = ViewCompat.generateViewId()
            setOnCheckedChangeListener { _, _ ->
                val checkedRecipeType = mBinding.chipRecipeType.checkedChipIds.size
                mBinding.tvSelectRecipeType.text = getString(
                    R.string.select_recipe_types_no,
                    checkedRecipeType
                )
            }
        }

        mBinding.chipRecipeType.addView(chip)
    }

    private fun swapStep(fromPosition: Int, toPosition: Int) {
        val itemList = mSection.groups
        Collections.swap(itemList, fromPosition, toPosition)
        updateStepListWithReorder(itemList)
    }

    private fun updateIngredientCount() {
        val ingredientCount = mBinding.chipIngredient.childCount
        if (ingredientCount > 0) {
            mBinding.tvIngredient.text = getString(R.string.ingredients_no, ingredientCount)
        } else {
            mBinding.tvIngredient.text = getString(R.string.ingredients)
        }
        ingredientListUpdated = false
    }

    private fun updateStepListWithReorder(itemList: List<Group>) {
        itemList.filterIsInstance<ItemStep>().forEach { eachItem ->
            val eachItemIndex = itemList.indexOf(eachItem)
            with(eachItem.itemStepModel) {
                noOfStep = eachItemIndex + 1
                isFirst = eachItemIndex == 0
                isLast = eachItemIndex == itemList.count() - 1
            }
        }
        mSection.update(itemList)
        mSection.notifyChanged()

        val emptyList = itemList.isNotEmpty()

        if (emptyList) {
            mBinding.tvSteps.text = getString(R.string.steps_no, itemList.count())
        } else {
            isEditingStep = false
            mBinding.tvEdit.text = getString(R.string.edit)
            mBinding.tvSteps.text = getString(R.string.steps)
            mSection.setFooter(itemNewStep!!)
        }

        mBinding.tvEdit.setShowHide(emptyList)
    }

    private fun onSaveAttempt() {
        val checkedChipIds =
            mBinding.chipRecipeType.checkedChipIds // Returns a list of the selected chips

        var inputValid = true

        if (imageUri == null && mViewModel.imageUrl == null) {
            mBinding.root.showErrorSnackBar(requireContext().getCannotBeEmptyString(R.string.recipe_image))
            inputValid = false
        }

        if (mBinding.etRecipeName.text.isNullOrBlank()) {
            inputValid = false
            mBinding.etRecipeName.error =
                requireContext().getCannotBeEmptyString(R.string.recipe_name)
        }

        if (mBinding.etRecipeDesc.text.isNullOrBlank()) {
            inputValid = false
            mBinding.etRecipeDesc.error =
                requireContext().getCannotBeEmptyString(R.string.recipe_description)
        }

        if (checkedChipIds.isNullOrEmpty()) {
            inputValid = false
            mBinding.root.showErrorSnackBar(getString(R.string.select_recipe_type_error))
        }

        if (mSection.itemCount < 2) {
            inputValid = false
            mBinding.root.showErrorSnackBar(getString(R.string.specify_recipe_step_error))
            itemNewStep?.setError(requireContext().getCannotBeEmptyString(R.string.steps))
        }

        if (mBinding.chipIngredient.childCount < 1) {
            inputValid = false
            mBinding.root.showErrorSnackBar(getString(R.string.specify_recipe_ingredient_error))
        }

        if (inputValid) {
            mBinding.llProgress.setShowHide(true)

            if (!imageUploaded) {
                mViewModel.uploadImage(Utils.copyStreamToFile(requireContext(), imageUri!!))
            }
            if (!ingredientListUpdated) {
                val ingredientsFromDB = myAppDatabase.ingredientDao().getAll().map {
                    it.ingredientName
                }
                val newlyAddedIngredients = ingredientItems.minus(ingredientsFromDB)

                if (!newlyAddedIngredients.isNullOrEmpty()) {
                    myAppDatabase.ingredientDao().insertAll(*newlyAddedIngredients.map {
                        Ingredient(ingredientName = it)
                    }.toTypedArray())
                }

                ingredientListUpdated = true
                proceedSaveNewRecipe()
            }
        }
    }

    private fun proceedSaveNewRecipe() {
        if (imageUploaded && ingredientListUpdated) {
            mBinding.llProgress.setShowHide(false)

            val selectedIngredientNameList = mBinding.chipIngredient.children.map {
                (it as Chip).text
            }

            val selectedRecipeTypeNameList =
                mBinding.chipRecipeType.children.filterIsInstance<Chip>().filter {
                    it.isChecked
                }.map {
                    it.text
                }

            val newRecipe =
                Recipe(
                    difficulty = mBinding.sliderRecipeDifficulty.value.toInt(),
                    recipeName = mBinding.etRecipeName.text.toString(),
                    description = mBinding.etRecipeDesc.text.toString(),
                    ingredientLists = myAppDatabase.ingredientDao().getAll()
                        .sortedBy { it.ingredientName }
                        .filter { it.ingredientName in selectedIngredientNameList },
                    stepLists = mSection.groups.filterIsInstance<ItemStep>().map {
                        it.itemStepModel.stepDesc
                    },
                    imageLists = listOf(mViewModel.imageUrl!!),
                    typeLists = myAppDatabase.recipeTypeDao().getAll()
                        .sortedBy { it.typeName }
                        .filter { it.typeName in selectedRecipeTypeNameList }
                )

            if (recipe == null) {
                myAppDatabase.recipeDao().insertAll(newRecipe)
            } else {
                newRecipe.uid = recipe!!.uid
                myAppDatabase.recipeDao().update(newRecipe)
            }

            val msg =
                if (recipe == null) getString(R.string.new_recipe_added) else getString(R.string.recipe_edited)
            Toast.makeText(
                requireContext(),
                msg,
                Toast.LENGTH_SHORT
            ).show()

            dismissWithAnimation()
        }
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun showGallery() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_OPEN_DOCUMENT
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(
            Intent.createChooser(intent, getString(R.string.select_image)),
            REQUEST_IMAGE
        )
    }

    private var imageUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {

            imageUri = data?.data ?: return

            when (requestCode) {
                REQUEST_IMAGE -> {

                    val takeFlags = data.flags.and(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .or(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                    try {
                        requireContext().contentResolver.takePersistableUriPermission(
                            imageUri!!,
                            takeFlags
                        )

                        Glide.with(requireContext())
                            .load(imageUri)
                            .centerCrop()
                            .into(mBinding.ivImage)

                        mBinding.ivImage.setOnClickListener {
                            ViewImageActivity.start(requireContext(), Bundle().apply {
                                putString(ViewImageActivity.IMAGE_URI, imageUri.toString())
                            })
                        }

                        imageUploaded = false

                    } catch (e: SecurityException) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Error")
                            .setMessage(e.localizedMessage)
                            .create()
                            .show()
                    }
                }
            }
        }
    }

    private fun dismissWithAnimation() {
        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.duration = 200
        view!!.animation = fadeOut
        view!!.startAnimation(fadeOut)

        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                if (imageUploaded && ingredientListUpdated) {
                    activity?.onActivityReenter(
                        Activity.RESULT_OK,
                        null
                    )
                }
                dismiss()
            }

            override fun onAnimationStart(p0: Animation?) {

            }
        })
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        activity?.menuInflater?.inflate(R.menu.menu_new_recipe, menu);
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_save) {
            onSaveAttempt()
            return true
        } else if (id == android.R.id.home) {
            dismissWithAnimation()
            activity?.findViewById<FloatingActionButton>(R.id.fab_new_recipe)?.show()
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}