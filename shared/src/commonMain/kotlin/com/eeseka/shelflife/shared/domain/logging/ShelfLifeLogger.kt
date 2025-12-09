package com.eeseka.shelflife.shared.domain.logging

interface ShelfLifeLogger {
    fun debug(message: String)
    fun info(message: String)
    fun warn(message: String)
    fun error(message: String, throwable: Throwable? = null)
}