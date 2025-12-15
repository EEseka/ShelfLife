package com.eeseka.shelflife.shared.presentation.util

import com.eeseka.shelflife.shared.domain.util.DataError
import shelflife.shared.generated.resources.Res
import shelflife.shared.generated.resources.error_bad_request
import shelflife.shared.generated.resources.error_conflict
import shelflife.shared.generated.resources.error_disk_full
import shelflife.shared.generated.resources.error_forbidden
import shelflife.shared.generated.resources.error_no_internet
import shelflife.shared.generated.resources.error_not_found_local
import shelflife.shared.generated.resources.error_not_found_remote
import shelflife.shared.generated.resources.error_request_timeout
import shelflife.shared.generated.resources.error_serialization
import shelflife.shared.generated.resources.error_server
import shelflife.shared.generated.resources.error_too_many_requests
import shelflife.shared.generated.resources.error_unauthorized
import shelflife.shared.generated.resources.error_unknown_local
import shelflife.shared.generated.resources.error_unknown_remote

fun DataError.toUiText(): UiText {
    val resource = when (this) {
        DataError.Local.DISK_FULL -> Res.string.error_disk_full
        DataError.Local.NOT_FOUND -> Res.string.error_not_found_local
        DataError.Local.UNKNOWN -> Res.string.error_unknown_local

        DataError.Remote.BAD_REQUEST -> Res.string.error_bad_request
        DataError.Remote.REQUEST_TIMEOUT -> Res.string.error_request_timeout
        DataError.Remote.UNAUTHORIZED -> Res.string.error_unauthorized
        DataError.Remote.FORBIDDEN -> Res.string.error_forbidden
        DataError.Remote.NOT_FOUND -> Res.string.error_not_found_remote
        DataError.Remote.CONFLICT -> Res.string.error_conflict
        DataError.Remote.TOO_MANY_REQUESTS -> Res.string.error_too_many_requests
        DataError.Remote.NO_INTERNET -> Res.string.error_no_internet
        DataError.Remote.SERVER_ERROR -> Res.string.error_server
        DataError.Remote.SERIALIZATION -> Res.string.error_serialization
        DataError.Remote.UNKNOWN -> Res.string.error_unknown_remote
    }
    return UiText.Resource(resource)
}