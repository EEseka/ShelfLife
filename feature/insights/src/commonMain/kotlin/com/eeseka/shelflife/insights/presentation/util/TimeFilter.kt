package com.eeseka.shelflife.insights.presentation.util

import com.eeseka.shelflife.shared.presentation.util.UiText
import shelflife.feature.insights.generated.resources.Res
import shelflife.feature.insights.generated.resources.all_time
import shelflife.feature.insights.generated.resources.last_30_days
import shelflife.feature.insights.generated.resources.this_month

enum class TimeFilter(val label: UiText) {
    ALL_TIME(UiText.Resource(Res.string.all_time)),
    THIS_MONTH(UiText.Resource(Res.string.this_month)),
    LAST_30_DAYS(UiText.Resource(Res.string.last_30_days))
}