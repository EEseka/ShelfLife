package com.eeseka.shelflife.shared.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserSerializable(
    val id: String,
    val email: String,
    val firstname: String,
    val lastname: String,
    val isAnonymous: Boolean = false,
    val profilePictureUrl: String? = null
)