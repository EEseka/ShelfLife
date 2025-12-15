package com.eeseka.shelflife.shared.domain.util

sealed interface DataError : Error {
    enum class Remote : DataError {
        BAD_REQUEST,        // 400: Sending bad data to OpenFoodFacts
        UNAUTHORIZED,       // 401: User token expired / Invalid Credentials
        FORBIDDEN,          // 403: Tried to delete account without recent login
        NOT_FOUND,          // 404: Barcode not found in OpenFoodFacts
        REQUEST_TIMEOUT,    // 408: Request took too long
        CONFLICT,           // 409: "User already exists" in Firebase
        TOO_MANY_REQUESTS,  // 429: Scanned too many barcodes too fast
        NO_INTERNET,        // Offline
        SERVER_ERROR,       // 500: OpenFoodFacts or Firebase server crash
        SERIALIZATION,      // JSON parsing failed
        UNKNOWN             // Catch-all
    }

    enum class Local : DataError {
        DISK_FULL,          // Device out of space (Room/Storage)
        NOT_FOUND,          // Item not found in local Room DB
        UNKNOWN             // Local IO error
    }
}