package com.eeseka.shelflife.shared.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserSerializable(
    val id: String,
    // Nullable fields allow this one class to handle both Guest and Auth users
    val email: String? = null,
    val fullName: String? = null,
    val isAnonymous: Boolean = false,
    val profilePictureUrl: String? = null
)