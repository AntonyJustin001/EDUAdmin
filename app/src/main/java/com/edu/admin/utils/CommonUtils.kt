package com.edu.admin.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.edu.admin.R
import com.edu.admin.data.preference.SharedPreferencesHelper


fun loadScreen(activity: FragmentActivity, next: Fragment, key: String = "", value: String = "") {
    val bundle = Bundle()
    bundle.putString(key, value)
    next.arguments = bundle
    val transaction = activity.supportFragmentManager.beginTransaction()
    transaction.replace(R.id.fragment_container, next)
    if(!(key == "SignInScreen" || key == "SplashScreen"))
    transaction.addToBackStack(next.tag)
    transaction.commit()
}



fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    } else {
        @Suppress("DEPRECATION")
        val networkInfo = connectivityManager.activeNetworkInfo ?: return false
        @Suppress("DEPRECATION")
        return networkInfo.isConnected
    }
}

fun loadImageFromUrl(context: Context, imageUrl: String, imageView: ImageView) {
    Log.e("loadImageFromUrl", "imageUrl - ${imageUrl}")
    Glide.with(context)
        .load(imageUrl)
        .placeholder(R.drawable.no_image) // Optional: Placeholder while loading
        .error(R.drawable.no_image) // Optional: Error image if the load fails
        .into(imageView)
}


lateinit var prefs: SharedPreferencesHelper







