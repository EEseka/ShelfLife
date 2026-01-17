package com.eeseka.shelflife.shared.domain.export

import com.eeseka.shelflife.shared.domain.pantry.PantryItem

object CsvGenerator {
    private const val HEADER = "Name,Brand,Barcode,Quantity,Unit,Expiry Date,Purchase Date,Location"

    fun generatePantryCsv(items: List<PantryItem>): String {
        val rows = items.joinToString(separator = "\n") { item ->
            val safeName = escapeCsv(item.name)
            val safeBrand = escapeCsv(item.brand ?: "")
            val safeBarcode = escapeCsv(item.barcode)

            // String Template
            "$safeName,$safeBrand,$safeBarcode,${item.quantity},${item.quantityUnit},${item.expiryDate},${item.purchaseDate},${item.storageLocation}"
        }
        return "$HEADER\n$rows"
    }

    private fun escapeCsv(value: String): String {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"${value.replace("\"", "\"\"")}\""
        }
        return value
    }
}