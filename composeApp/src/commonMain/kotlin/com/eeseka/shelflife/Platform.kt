package com.eeseka.shelflife

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform