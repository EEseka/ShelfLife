package com.eeseka.shelflife.shared.presentation.util

import com.eeseka.shelflife.shared.domain.util.DataError
import shelflife.shared.generated.resources.Res
import shelflife.shared.generated.resources.error_auth_conflict
import shelflife.shared.generated.resources.error_auth_forbidden
import shelflife.shared.generated.resources.error_auth_no_internet
import shelflife.shared.generated.resources.error_auth_unauthorized
import shelflife.shared.generated.resources.error_auth_unknown
import shelflife.shared.generated.resources.error_disk_full
import shelflife.shared.generated.resources.error_not_found_local
import shelflife.shared.generated.resources.error_remote_bad_request
import shelflife.shared.generated.resources.error_remote_conflict
import shelflife.shared.generated.resources.error_remote_forbidden
import shelflife.shared.generated.resources.error_remote_no_internet
import shelflife.shared.generated.resources.error_remote_not_found
import shelflife.shared.generated.resources.error_remote_payload_too_large
import shelflife.shared.generated.resources.error_remote_serialization
import shelflife.shared.generated.resources.error_remote_server
import shelflife.shared.generated.resources.error_remote_service_unavailable
import shelflife.shared.generated.resources.error_remote_timeout
import shelflife.shared.generated.resources.error_remote_too_many_requests
import shelflife.shared.generated.resources.error_remote_unauthorized
import shelflife.shared.generated.resources.error_remote_unknown
import shelflife.shared.generated.resources.error_storage_bad_request
import shelflife.shared.generated.resources.error_storage_conflict
import shelflife.shared.generated.resources.error_storage_no_internet
import shelflife.shared.generated.resources.error_storage_not_found
import shelflife.shared.generated.resources.error_storage_permission_denied
import shelflife.shared.generated.resources.error_storage_quota_exceeded
import shelflife.shared.generated.resources.error_storage_serialization
import shelflife.shared.generated.resources.error_storage_server
import shelflife.shared.generated.resources.error_storage_timeout
import shelflife.shared.generated.resources.error_storage_too_many_requests
import shelflife.shared.generated.resources.error_storage_unknown
import shelflife.shared.generated.resources.error_unknown_local

fun DataError.toUiText(): UiText {
    val resource = when (this) {
        // Auth errors
        DataError.Auth.UNAUTHORIZED -> Res.string.error_auth_unauthorized
        DataError.Auth.FORBIDDEN -> Res.string.error_auth_forbidden
        DataError.Auth.CONFLICT -> Res.string.error_auth_conflict
        DataError.Auth.NO_INTERNET -> Res.string.error_auth_no_internet
        DataError.Auth.UNKNOWN -> Res.string.error_auth_unknown

        // Storage errors (Firebase Firestore/Storage)
        DataError.RemoteStorage.BAD_REQUEST -> Res.string.error_storage_bad_request
        DataError.RemoteStorage.NOT_FOUND -> Res.string.error_storage_not_found
        DataError.RemoteStorage.REQUEST_TIMEOUT -> Res.string.error_storage_timeout
        DataError.RemoteStorage.CONFLICT -> Res.string.error_storage_conflict
        DataError.RemoteStorage.NO_INTERNET -> Res.string.error_storage_no_internet
        DataError.RemoteStorage.SERVER_ERROR -> Res.string.error_storage_server
        DataError.RemoteStorage.SERIALIZATION -> Res.string.error_storage_serialization
        DataError.RemoteStorage.PERMISSION_DENIED -> Res.string.error_storage_permission_denied
        DataError.RemoteStorage.QUOTA_EXCEEDED -> Res.string.error_storage_quota_exceeded
        DataError.RemoteStorage.TOO_MANY_REQUESTS -> Res.string.error_storage_too_many_requests
        DataError.RemoteStorage.UNKNOWN -> Res.string.error_storage_unknown

        // Remote API errors (OpenFoodFacts, etc.)
        DataError.Remote.BAD_REQUEST -> Res.string.error_remote_bad_request
        DataError.Remote.NOT_FOUND -> Res.string.error_remote_not_found
        DataError.Remote.REQUEST_TIMEOUT -> Res.string.error_remote_timeout
        DataError.Remote.TOO_MANY_REQUESTS -> Res.string.error_remote_too_many_requests
        DataError.Remote.NO_INTERNET -> Res.string.error_remote_no_internet
        DataError.Remote.SERVER_ERROR -> Res.string.error_remote_server
        DataError.Remote.SERIALIZATION -> Res.string.error_remote_serialization
        DataError.Remote.UNKNOWN -> Res.string.error_remote_unknown
        DataError.Remote.UNAUTHORIZED -> Res.string.error_remote_unauthorized
        DataError.Remote.FORBIDDEN -> Res.string.error_remote_forbidden
        DataError.Remote.CONFLICT -> Res.string.error_remote_conflict
        DataError.Remote.PAYLOAD_TOO_LARGE -> Res.string.error_remote_payload_too_large
        DataError.Remote.SERVICE_UNAVAILABLE -> Res.string.error_remote_service_unavailable

        // Local errors
        DataError.LocalStorage.DISK_FULL -> Res.string.error_disk_full
        DataError.LocalStorage.NOT_FOUND -> Res.string.error_not_found_local
        DataError.LocalStorage.UNKNOWN -> Res.string.error_unknown_local
    }
    return UiText.Resource(resource)
}