package com.eeseka.shelflife.shared.domain.auth

sealed interface User {
    val id: String

    data class Guest(
        override val id: String
    ) : User

    data class Authenticated(
        override val id: String,
        val email: String,
        val firstName: String,
        val lastName: String,
        val profilePictureUrl: String? = null
    ) : User
}