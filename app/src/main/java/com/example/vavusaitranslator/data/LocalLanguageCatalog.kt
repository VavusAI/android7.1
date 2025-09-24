package com.example.vavusaitranslator.data

import android.content.Context
import com.example.vavusaitranslator.R
import com.example.vavusaitranslator.model.VavusLanguage
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.BufferedReader

class LocalLanguageCatalog(context: Context) {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val languages: List<VavusLanguage>

    init {
        val type = Types.newParameterizedType(List::class.java, VavusLanguage::class.java)
        val adapter: JsonAdapter<List<VavusLanguage>> = moshi.adapter(type)
        val rawLanguages = context.resources.openRawResource(R.raw.vavus_languages)
            .bufferedReader()
            .use(BufferedReader::readText)
        languages = adapter.fromJson(rawLanguages).orEmpty().sortedBy { it.name }
    }

    fun getAll(): List<VavusLanguage> = languages
}