package com.example.topseriesapp.utils

interface ConnectivityChecker {
    suspend fun isOnline(): Boolean
}