package com.eeseka.shelflife.pantry.presentation.pantry_list_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.shelflife.pantry.domain.PantryRepository
import com.eeseka.shelflife.pantry.presentation.pantry_list_detail.PantryAction
import com.eeseka.shelflife.pantry.presentation.pantry_detail.PantryDetailEvent
import com.eeseka.shelflife.pantry.presentation.pantry_list.PantryListEvent
import com.eeseka.shelflife.shared.domain.logging.ShelfLifeLogger
import com.eeseka.shelflife.shared.domain.networking.ApiService
import com.eeseka.shelflife.shared.domain.pantry.PantryItem
import com.eeseka.shelflife.shared.domain.util.onFailure
import com.eeseka.shelflife.shared.domain.util.onSuccess
import com.eeseka.shelflife.shared.presentation.util.UiText
import com.eeseka.shelflife.shared.presentation.util.toUiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import shelflife.feature.pantry.generated.resources.Res
import shelflife.feature.pantry.generated.resources.item_created
import shelflife.feature.pantry.generated.resources.item_deleted
import shelflife.feature.pantry.generated.resources.item_updated
import shelflife.feature.pantry.generated.resources.scan_failed
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)
class PantryViewModel(
    private val repository: PantryRepository,
    private val apiService: ApiService,
    private val logger: ShelfLifeLogger
) : ViewModel() {
    private val _state = MutableStateFlow(PantryState())

    // Observe only what matters for the database (Query & Filter)
    private val itemsFlow = _state
        .map { it.searchQuery to it.selectedLocationFilter }
        .distinctUntilChanged()
        .flatMapLatest { (query, location) ->
            when {
                query.isNotBlank() -> {
                    if (location != null) repository.searchPantryItemsByLocation(query, location)
                    else repository.searchPantryItems(query)
                }

                location != null -> repository.getPantryItemsByLocation(location)
                else -> repository.getAllPantryItems()
            }
        }

    val state = combine(_state, itemsFlow) { currentState, items ->
        currentState.copy(items = items)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5_000L),
        initialValue = PantryState()
    )

    private val _pantryListEventChannel = Channel<PantryListEvent>()
    val pantryListEvents = _pantryListEventChannel.receiveAsFlow()

    private val _pantryDetailEventChannel = Channel<PantryDetailEvent>()
    val pantryDetailEvents = _pantryDetailEventChannel.receiveAsFlow()

    init {
        syncRemoteData(isRefreshing = false)
    }

    fun onAction(action: PantryAction) {
        when (action) {
            PantryAction.OnRefresh -> syncRemoteData(isRefreshing = true)

            // --- Navigation ---
            is PantryAction.OnItemClick -> {
                _state.update {
                    it.copy(
                        selectedItem = action.item,
                        isDetailOpen = true
                    )
                }
            }

            PantryAction.OnDetailClose -> {
                _state.update {
                    it.copy(
                        isDetailOpen = false
                    )
                }
            }

            // --- FAB / Scanner ---
            PantryAction.OnScannerClick -> {
                _state.update { it.copy(isScannerOpen = true) }
            }

            PantryAction.OnScannerDismiss -> {
                _state.update { it.copy(isScannerOpen = false) }
            }

            is PantryAction.OnBarcodeDetected -> handleBarcodeDetected(action.barcode)
            PantryAction.OnManualEntryClick -> startManualEntry()
            PantryAction.OnCreateSheetDismiss -> {
                _state.update {
                    it.copy(
                        isCreateItemSheetOpen = false,
                        draftItem = null
                    )
                }
            }

            PantryAction.OnEditSheetOpen -> {
                _state.update { it.copy(isEditItemSheetOpen = true) }
            }

            PantryAction.OnEditSheetDismiss -> {
                _state.update { it.copy(isEditItemSheetOpen = false) }
            }

            // --- Search & Filter ---
            is PantryAction.OnSearchQueryChange -> {
                _state.update { it.copy(searchQuery = action.query) }
            }

            is PantryAction.OnLocationFilterChange -> {
                _state.update { it.copy(selectedLocationFilter = action.location) }
            }

            // --- CRUD ---
            is PantryAction.OnCreateNewItem -> createNewItem(action.item)
            is PantryAction.OnUpdateItem -> updateItem(action.item)
            is PantryAction.OnDeleteItem -> deleteItem(action.id)
        }
    }

    private fun syncRemoteData(isRefreshing: Boolean) {
        if (isRefreshing) _state.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            repository.syncRemotePantry()
                .onFailure { logger.error("Sync failed: ${it.toUiText().asStringAsync()}") }
            if (isRefreshing) _state.update { it.copy(isRefreshing = false) }
            else _state.update { it.copy(isLoading = false) }
        }
    }

    private fun handleBarcodeDetected(barcode: String) {
        _state.update { it.copy(isScannerLoading = true) }

        viewModelScope.launch {
            apiService.getProductByBarcode(barcode.toLongOrNull() ?: 0L)
                .onSuccess { apiItem ->
                    _state.update {
                        it.copy(
                            isScannerLoading = false,
                            isScannerOpen = false,
                            isCreateItemSheetOpen = true,
                            draftItem = apiItem
                        )
                    }
                }
                .onFailure { error ->
                    logger.error("Scan failed: $error")
                    _state.update { it.copy(isScannerLoading = false, isScannerOpen = false) }
                    _pantryListEventChannel.send(PantryListEvent.Error(UiText.Resource(Res.string.scan_failed)))
                }
        }
    }

    private fun startManualEntry() {
        val emptyItem = createEmptyPantryItem()
        _state.update {
            it.copy(
                isCreateItemSheetOpen = true,
                draftItem = emptyItem
            )
        }
    }

    private fun createNewItem(item: PantryItem) {
        _state.update { it.copy(isCreatingNewItem = true) }
        viewModelScope.launch {
            repository.createPantryItem(item)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isCreatingNewItem = false,
                            isCreateItemSheetOpen = false,
                            draftItem = null,
                            selectedItem = item
                        )
                    }
                    _pantryListEventChannel.send(PantryListEvent.Success(UiText.Resource(Res.string.item_created)))
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            isCreatingNewItem = false,
                            isCreateItemSheetOpen = false,
                            draftItem = null
                        )
                    }
                    _pantryListEventChannel.send(PantryListEvent.Error(error.toUiText()))
                }
        }
    }

    private fun updateItem(item: PantryItem) {
        _state.update { it.copy(isUpdatingItem = true) }
        viewModelScope.launch {
            repository.updatePantryItem(item)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isUpdatingItem = false,
                            isEditItemSheetOpen = false,
                            selectedItem = item
                        )
                    }
                    _pantryDetailEventChannel.send(PantryDetailEvent.Success(UiText.Resource(Res.string.item_updated)))
                }.onFailure { error ->
                    _state.update { it.copy(isUpdatingItem = false, isEditItemSheetOpen = false) }
                    _pantryDetailEventChannel.send(PantryDetailEvent.Error(error.toUiText()))
                }
        }
    }

    private fun deleteItem(id: String) {
        _state.update { it.copy(isDeletingItem = true) }
        viewModelScope.launch {
            repository.deletePantryItem(id)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isDeletingItem = false,
                            selectedItem = null,
                            isDetailOpen = false
                        )
                    }
                    _pantryListEventChannel.send(PantryListEvent.Success(UiText.Resource(Res.string.item_deleted)))
                }
                .onFailure { error ->
                    _state.update { it.copy(isDeletingItem = false) }
                    _pantryListEventChannel.send(PantryListEvent.Error(error.toUiText()))
                }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun createEmptyPantryItem(): PantryItem {
        val today = Clock.System.now().toLocalDateTime(TimeZone.Companion.currentSystemDefault()).date
        return PantryItem(
            id = Uuid.Companion.random().toString(),
            barcode = "",
            name = "",
            expiryDate = today,
            purchaseDate = today
        )
    }
}