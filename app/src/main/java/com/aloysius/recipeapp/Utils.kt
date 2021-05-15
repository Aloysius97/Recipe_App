package com.aloysius.recipeapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

object Utils {
    private var myToast: Toast? = null

    private var retrofit: Retrofit? = null


    fun getRetrofitInstance(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.IMGUR_ENDPOINT).client(
                    OkHttpClient.Builder()
                        .connectTimeout(100, TimeUnit.SECONDS)
                        .readTimeout(100, TimeUnit.SECONDS).build()
                )
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    fun View.showErrorSnackBar(msg: String) {
        Snackbar.make(
            this,
            msg,
            Snackbar.LENGTH_SHORT
        )
            .setBackgroundTint(ContextCompat.getColor(this.context, R.color.pd_red))
            .setActionTextColor(Color.WHITE)
            .show()
    }

    fun Context.getCannotBeEmptyString(@StringRes input_name: Int): String {
        return getString(R.string.input_cannot_be_empty, getString(input_name))
    }

    fun View.setShowHide(show: Boolean) {
        visibility = if (show) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    fun Context.copyClipboard(label: String, str: String) {
        val clipboard: ClipboardManager =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText(label, str)
        clipboard.setPrimaryClip(clip)

        val message = getString(R.string.copied_to_clipboard, label)

        if (myToast == null) myToast =
            makeText(this, message, Toast.LENGTH_SHORT)
        else
            myToast?.setText(message)

        myToast?.show()
    }

    fun copyStreamToFile(context: Context, uri: Uri): File {
        val outputFile = File.createTempFile("temp", null)

        context.contentResolver.openInputStream(uri)?.use { input ->
            val outputStream = FileOutputStream(outputFile)
            outputStream.use { output ->
                val buffer = ByteArray(4 * 1024) // buffer size
                while (true) {
                    val byteCount = input.read(buffer)
                    if (byteCount < 0) break
                    output.write(buffer, 0, byteCount)
                }
                output.flush()
            }
        }
        return outputFile
    }
}