package com.eeseka.shelflife.shared.data.util

import kotlinx.coroutines.flow.Flow

expect class ConnectivityObserver {
    val isConnected: Flow<Boolean>
}