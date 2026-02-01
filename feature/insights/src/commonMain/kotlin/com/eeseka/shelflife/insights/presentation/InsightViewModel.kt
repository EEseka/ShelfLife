package com.eeseka.shelflife.insights.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.shelflife.insights.domain.InsightRepository
import com.eeseka.shelflife.insights.presentation.util.TimeFilter
import com.eeseka.shelflife.insights.presentation.util.getNutriScoreDistribution
import com.eeseka.shelflife.insights.presentation.util.getUltraProcessedCount
import com.eeseka.shelflife.insights.presentation.util.getWastedGoodEcoCount
import com.eeseka.shelflife.shared.domain.insight.InsightStatus
import com.eeseka.shelflife.shared.domain.logging.ShelfLifeLogger
import com.eeseka.shelflife.shared.domain.util.onFailure
import com.eeseka.shelflife.shared.domain.util.onSuccess
import com.eeseka.shelflife.shared.presentation.util.UiText
import com.eeseka.shelflife.shared.presentation.util.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import shelflife.feature.insights.generated.resources.Res
import shelflife.feature.insights.generated.resources.history_cleared
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class InsightViewModel(
    private val repository: InsightRepository,
    private val logger: ShelfLifeLogger
) : ViewModel() {

    private val _state = MutableStateFlow(InsightState())

    val state = combine(_state, repository.getAllInsightItems()) { currentState, allItems ->

        // Apply Filter
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val filtered = when (currentState.selectedTimeFilter) {
            TimeFilter.ALL_TIME -> allItems
            TimeFilter.THIS_MONTH -> allItems.filter {
                it.actionDate.month == today.month && it.actionDate.year == today.year
            }

            TimeFilter.LAST_30_DAYS -> {
                val thirtyDaysAgo = today.minus(30, DateTimeUnit.DAY)
                allItems.filter { it.actionDate >= thirtyDaysAgo }
            }
        }

        // Calculate Stats
        val consumed = filtered.count { it.status == InsightStatus.CONSUMED }
        val wasted = filtered.count { it.status == InsightStatus.WASTED }
        val percentage =
            if (filtered.isNotEmpty()) consumed.toFloat() / filtered.size.toFloat() else 0f
        val nutriStats = filtered.getNutriScoreDistribution()
        val processedCount = filtered.getUltraProcessedCount()
        val wastedEcoCount = filtered.getWastedGoodEcoCount()

        currentState.copy(
            items = filtered,
            consumedCount = consumed,
            wastedCount = wasted,
            consumedPercentage = percentage,
            nutriScoreStats = nutriStats,
            ultraProcessedCount = processedCount,
            wastedGoodEcoCount = wastedEcoCount,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = InsightState()
    )

    private val _events = Channel<InsightEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.syncRemoteInsight()
                .onFailure { logger.error("Sync failed: ${it.toUiText().asStringAsync()}") }
        }
    }

    fun onAction(action: InsightAction) {
        when (action) {
            is InsightAction.OnTimeFilterChange -> {
                _state.update { it.copy(selectedTimeFilter = action.filter) }
            }

            InsightAction.OnClearHistoryClick -> clearAllHistory()
        }
    }

    private fun clearAllHistory() {
        _state.update { it.copy(isDeleting = true) }
        viewModelScope.launch {
            repository.deleteAllInsightItems()
                .onSuccess {
                    _state.update { it.copy(isDeleting = false) }
                    _events.send(InsightEvent.Success(UiText.Resource(Res.string.history_cleared)))
                }
                .onFailure { error ->
                    _state.update { it.copy(isDeleting = false) }
                    _events.send(InsightEvent.Error(error.toUiText()))
                }
        }
    }
}