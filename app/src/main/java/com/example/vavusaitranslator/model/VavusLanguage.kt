package com.example.vavusaitranslator.model

data class VavusLanguage(
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