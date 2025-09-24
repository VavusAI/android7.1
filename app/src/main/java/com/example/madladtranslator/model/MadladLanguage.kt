package com.example.madladtranslator.model

data class MadladLanguage(
    val code: String,
    val name: String,
    val script: String? = null
) {
    override fun toString(): String = buildString {
        append(name)
        append(" (")
        append(code)
        script?.let {
            append(" · ")
            append(it)
        }
        append(")")
    }
}