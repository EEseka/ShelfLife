package com.eeseka.shelflife.pantry.presentation.util

fun String.cleanTag(): String {
    return this
        .substringAfter(":")
        .replace("-", " ")
        .split(" ")
        .joinToString(" ") {
            it.lowercase().replaceFirstChar { char -> char.uppercase() }
        }
}