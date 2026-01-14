package com.eeseka.shelflife.shared.domain.networking

import com.eeseka.shelflife.shared.domain.pantry.PantryItem
import com.eeseka.shelflife.shared.domain.util.DataError
import com.eeseka.shelflife.shared.domain.util.Result

interface ApiService {
    suspend fun getProductByBarcode(barcode: Long): Result<PantryItem, DataError.Remote>
}