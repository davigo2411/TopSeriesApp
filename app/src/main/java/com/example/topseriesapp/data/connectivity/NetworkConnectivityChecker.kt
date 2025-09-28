package com.example.topseriesapp.data.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.topseriesapp.utils.ConnectivityChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NetworkConnectivityChecker(private val context: Context) : ConnectivityChecker,
    com.example.topseriesapp.ui.showsdetails.ConnectivityChecker {

    override suspend fun isOnline(): Boolean = withContext(Dispatchers.IO) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
                ?: return@withContext false

        val network = connectivityManager.activeNetwork ?: return@withContext false
        val activeNetworkCapabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return@withContext false

        return@withContext when {
            activeNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            activeNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> {
                activeNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        activeNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            }
            else -> false
        }
    }
}
