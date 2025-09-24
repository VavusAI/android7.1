package com.example.madladtranslator.data

import android.content.Context
import com.example.madladtranslator.R
import com.example.madladtranslator.model.MadladLanguage
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.io.BufferedReader

class LocalLanguageCatalog(context: Context) {
    private val moshi: Moshi = Moshi.Builder().build()
    private val languages: List<MadladLanguage>

    init {
        val type = Types.newParameterizedType(List::class.java, MadladLanguage::class.java)
        val adapter: JsonAdapter<List<MadladLanguage>> = moshi.adapter(type)
        val rawLanguages = context.resources.openRawResource(R.raw.madlad_languages)
            .bufferedReader()
            .use(BufferedReader::readText)
        languages = adapter.fromJson(rawLanguages).orEmpty().sortedBy { it.name }
    }

    fun getAll(): List<MadladLanguage> = languages
}