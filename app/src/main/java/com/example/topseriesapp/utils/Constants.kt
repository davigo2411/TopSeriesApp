package com.example.topseriesapp.utils


const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"
const val IMAGE_SIZE_W500 = "w500"

fun getImageUrl(posterPath: String?, size: String = IMAGE_SIZE_W500): String? {
    return if (!posterPath.isNullOrEmpty()) {
        "$IMAGE_BASE_URL$size$posterPath"
    } else {
        null
    }
}