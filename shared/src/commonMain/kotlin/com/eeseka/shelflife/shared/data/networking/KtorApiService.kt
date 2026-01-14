package com.eeseka.shelflife.shared.data.networking

import com.eeseka.shelflife.shared.data.dto.OpenFoodFactsResponse
import com.eeseka.shelflife.shared.data.mappers.toDomain
import com.eeseka.shelflife.shared.domain.networking.ApiService
import com.eeseka.shelflife.shared.domain.pantry.PantryItem
import com.eeseka.shelflife.shared.domain.util.DataError
import com.eeseka.shelflife.shared.domain.util.Result
import com.eeseka.shelflife.shared.domain.util.map
import io.ktor.client.HttpClient
import io.ktor.client.request.get

class KtorApiService(
    private val httpClient: HttpClient
) : ApiService {

    override suspend fun getProductByBarcode(barcode: Long): Result<PantryItem, DataError.Remote> {
        val route = "/api/v2/product/$barcode.json"

        val response = safeCall<OpenFoodFactsResponse> {
            httpClient.get(urlString = constructRoute(route))
        }

        return response.map { apiResponse ->
            if (apiResponse.status == 1 && apiResponse.product != null) {
                apiResponse.product.toDomain(barcode.toString())
            } else {
                // HTTP was 200, but API says "Product not found"
                return Result.Failure(DataError.Remote.NOT_FOUND)
            }
        }
    }
}