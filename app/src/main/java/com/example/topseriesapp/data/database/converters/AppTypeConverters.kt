package com.example.topseriesapp.data.database.converters

import androidx.room.TypeConverter
import com.example.topseriesapp.data.model.* // Asegúrate de que importa todos tus modelos de datos
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException // Para capturar errores de parseo específicos de Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class AppTypeConverters {
    private val gson = Gson()

    // --- Genérico para Listas de Objetos ---
    private inline fun <reified T> listToJson(list: List<T>?): String? {
        return gson.toJson(list)
    }

    private inline fun <reified T> jsonToList(jsonString: String?): List<T>? {
        if (jsonString.isNullOrEmpty()) return null
        return try {
            val listType: Type = object : TypeToken<List<T>>() {}.type
            gson.fromJson(jsonString, listType)
        } catch (e: JsonSyntaxException) {
            // Log.e("AppTypeConverters", "Error parsing JSON to List<${T::class.java.simpleName}>: $jsonString", e) // Opcional: Loggear el error
            null // Devolver null si hay error de parsing
        }
    }

    // --- Genérico para Objetos Simples ---
    private inline fun <reified T> objectToJson(obj: T?): String? {
        return gson.toJson(obj)
    }

    private inline fun <reified T> jsonToObject(jsonString: String?): T? {
        if (jsonString.isNullOrEmpty()) return null
        return try {
            gson.fromJson(jsonString, T::class.java)
        } catch (e: JsonSyntaxException) {
            // Log.e("AppTypeConverters", "Error parsing JSON to ${T::class.java.simpleName}: $jsonString", e) // Opcional: Loggear el error
            null
        }
    }


    // --- Genre ---
    @TypeConverter
    fun fromGenreList(genres: List<Genre>?): String? = listToJson(genres)

    @TypeConverter
    fun toGenreList(genresString: String?): List<Genre>? = jsonToList(genresString)

    // --- CreatedBy ---
    @TypeConverter
    fun fromCreatedByList(createdBy: List<CreatedBy>?): String? = listToJson(createdBy)

    @TypeConverter
    fun toCreatedByList(createdByString: String?): List<CreatedBy>? = jsonToList(createdByString)

    // --- List<Int> (ej. Episode Run Time) ---
    @TypeConverter
    fun fromIntList(list: List<Int>?): String? = listToJson(list)

    @TypeConverter
    fun toIntList(jsonString: String?): List<Int>? = jsonToList(jsonString)

    // --- List<String> (ej. Languages, Origin Country) ---
    @TypeConverter
    fun fromStringList(list: List<String>?): String? = listToJson(list)

    @TypeConverter
    fun toStringList(jsonString: String?): List<String>? = jsonToList(jsonString)

    // --- Network ---
    @TypeConverter
    fun fromNetworkList(networks: List<Network>?): String? = listToJson(networks)

    @TypeConverter
    fun toNetworkList(networksString: String?): List<Network>? = jsonToList(networksString)

    // --- ProductionCompany ---
    @TypeConverter
    fun fromProductionCompanyList(companies: List<ProductionCompany>?): String? = listToJson(companies)

    @TypeConverter
    fun toProductionCompanyList(companiesString: String?): List<ProductionCompany>? = jsonToList(companiesString)

    // --- ProductionCountry ---
    @TypeConverter
    fun fromProductionCountryList(countries: List<ProductionCountry>?): String? = listToJson(countries)

    @TypeConverter
    fun toProductionCountryList(countriesString: String?): List<ProductionCountry>? = jsonToList(countriesString)

    // --- Season ---
    @TypeConverter
    fun fromSeasonList(seasons: List<Season>?): String? = listToJson(seasons)

    @TypeConverter
    fun toSeasonList(seasonsString: String?): List<Season>? = jsonToList(seasonsString)

    // --- SpokenLanguage ---
    @TypeConverter
    fun fromSpokenLanguageList(languages: List<SpokenLanguage>?): String? = listToJson(languages)

    @TypeConverter
    fun toSpokenLanguageList(languagesString: String?): List<SpokenLanguage>? = jsonToList(languagesString)

    // --- EpisodeToAir (Last and Next) ---
    @TypeConverter
    fun fromEpisodeToAir(episode: EpisodeToAir?): String? = objectToJson(episode)

    @TypeConverter
    fun toEpisodeToAir(episodeString: String?): EpisodeToAir? = jsonToObject(episodeString)
}

