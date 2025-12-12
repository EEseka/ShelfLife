package com.eeseka.shelflife.shared.data.mappers

import com.eeseka.shelflife.shared.data.dto.UserSerializable
import com.eeseka.shelflife.shared.domain.auth.User

fun UserSerializable.toDomain(): User {
    return if (isAnonymous) {
        User.Guest(
            id = id
        )
    } else {
        User.Authenticated(
            id = id,
            email = email ?: "",
            firstName = firstName ?: "",
            lastName = lastName ?: "",
            profilePictureUrl = profilePictureUrl
        )
    }
}

fun User.toSerializable(): UserSerializable {
    return when (this) {
        is User.Guest -> UserSerializable(
            id = id,
            isAnonymous = true
        )

        is User.Authenticated -> UserSerializable(
            id = id,
            email = email,
            firstName = firstName,
            lastName = lastName,
            profilePictureUrl = profilePictureUrl,
            isAnonymous = false
        )
    }
}