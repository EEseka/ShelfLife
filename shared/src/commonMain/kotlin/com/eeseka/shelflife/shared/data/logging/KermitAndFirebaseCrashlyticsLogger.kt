package com.eeseka.shelflife.shared.data.logging

import co.touchlab.kermit.Logger
import com.eeseka.shelflife.shared.domain.logging.ShelfLifeLogger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics

object KermitAndFirebaseCrashlyticsLogger : ShelfLifeLogger {

    override fun debug(message: String) {
        Logger.d(message)
    }

    override fun info(message: String) {
        Logger.i(message)
        try {
            Firebase.crashlytics.log(message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun warn(message: String) {
        Logger.w(message)

        try {
            Firebase.crashlytics.log("WARN: $message")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun error(message: String, throwable: Throwable?) {
        Logger.e(message, throwable)

        try {
            if (throwable != null) {
                // If we have a real exception, record it.
                // This uploads the report (next time app opens).
                Firebase.crashlytics.recordException(throwable)

                // Attach the custom message so I know what happened
                Firebase.crashlytics.setCustomKey("last_error_context", message)
            } else {
                // If it's just an error message without a crash (e.g. "Sync Failed"),
                // we create a synthetic exception so Crashlytics tracks it.
                val syntheticException = Exception("Non-Fatal Error: $message")
                Firebase.crashlytics.recordException(syntheticException)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}