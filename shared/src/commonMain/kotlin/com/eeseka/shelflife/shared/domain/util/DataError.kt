package com.eeseka.shelflife.shared.domain.util

sealed interface DataError : Error {
    enum class Auth : DataError {
        UNAUTHORIZED,       // 401: Invalid email or password
        FORBIDDEN,          // 403: Sensitive operation requires recent login
        CONFLICT,           // 409: Account with this email already exists
        NO_INTERNET,        //
        UNKNOWN             // Catch-all auth error
    }

    enum class RemoteStorage : DataError {
        BAD_REQUEST,        // 400: Invalid Firestore/Storage request
        NOT_FOUND,          // 404: Document/file not found
        REQUEST_TIMEOUT,    // 408: Request took too long
        CONFLICT,           // 409: Document already exists
        NO_INTERNET,        // Offline
        SERVER_ERROR,       // 500: Firebase server error
        SERIALIZATION,      // JSON parsing failed (Firestore)
        PERMISSION_DENIED,  // Firestore security rules denied access
        QUOTA_EXCEEDED,     // Storage quota exceeded
        TOO_MANY_REQUESTS,  // 429: Rate limit exceeded
        UNKNOWN             // Catch-all storage error
    }

    enum class Remote : DataError {
        BAD_REQUEST,        // 400: Invalid API request
        UNAUTHORIZED,        // 401: User is not authenticated
        FORBIDDEN,           // 403: User is authenticated but doesn't have permission
        NOT_FOUND,          // 404: Resource not found (e.g., barcode in OpenFoodFacts)
        REQUEST_TIMEOUT,    // 408: Request took too long
        CONFLICT,            // 409: Resource already exists or conflict in state
        PAYLOAD_TOO_LARGE,   // 413: Upload size is too big
        TOO_MANY_REQUESTS,  // 429: API rate limit exceeded
        NO_INTERNET,        // Offline
        SERVER_ERROR,       // 500: API server error
        SERVICE_UNAVAILABLE, // 503: Server is down or maintenance
        SERIALIZATION,      // JSON parsing failed
        UNKNOWN             // Catch-all API error
    }

    enum class LocalStorage : DataError {
        DISK_FULL,          // Device out of space (Room/Storage)
        NOT_FOUND,          // Item not found in local Room DB
        UNKNOWN             // Local IO error
    }
}