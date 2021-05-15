package com.aloysius.recipeapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.aloysius.recipeapp.R
import com.aloysius.recipeapp.databinding.ActivityViewImageBinding

class ViewImageActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityViewImageBinding

    companion object {
        const val IMAGE_URI = "image_url"

        @JvmStatic
        fun start(context: Context, bundle: Bundle? = null) {
            val starter = Intent(context, ViewImageActivity::class.java)
            bundle?.run {
                starter.putExtras(this)
            }
            context.startActivity(starter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_view_image)

        mBinding.toolbar.title = ""
        setSupportActionBar(mBinding.toolbar)

        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeButtonEnabled(true)
        }

        if (intent.hasExtra(IMAGE_URI)) {
            Glide.with(this)
                .load(intent.getStringExtra(IMAGE_URI))
                .fitCenter()
                .into(mBinding.photoView)
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return true
    }
}