package com.eeseka.shelflife.shared.data.mappers

import com.eeseka.shelflife.shared.data.database.local.entities.InsightItemEntity
import com.eeseka.shelflife.shared.data.dto.InsightItemSerializable
import com.eeseka.shelflife.shared.domain.insight.InsightItem
import com.eeseka.shelflife.shared.domain.insight.InsightStatus
import kotlinx.datetime.LocalDate

fun InsightItemSerializable.toDomain(): InsightItem {
    return InsightItem(
        id = id,
        name = name,
        imageUrl = imageUrl,
        quantity = quantity,
        quantityUnit = quantityUnit,
        status = InsightStatus.valueOf(status),
        actionDate = LocalDate.parse(actionDate),
        updatedAt = updatedAt,
        nutriScore = nutriScore,
        novaGroup = novaGroup,
        ecoScore = ecoScore
    )
}

fun InsightItem.toSerializable(): InsightItemSerializable {
    return InsightItemSerializable(
        id = id,
        name = name,
        imageUrl = imageUrl,
        quantity = quantity,
        quantityUnit = quantityUnit,
        status = status.name,
        actionDate = actionDate.toString(),
        updatedAt = updatedAt,
        nutriScore = nutriScore,
        novaGroup = novaGroup,
        ecoScore = ecoScore
    )
}

fun InsightItemEntity.toDomain(): InsightItem {
    return InsightItem(
        id = id,
        name = name,
        imageUrl = imageUrl,
        quantity = quantity,
        quantityUnit = quantityUnit,
        status = InsightStatus.valueOf(status),
        actionDate = LocalDate.fromEpochDays(actionDate),
        updatedAt = updatedAt,
        nutriScore = nutriScore,
        novaGroup = novaGroup,
        ecoScore = ecoScore
    )
}

fun InsightItem.toEntity(): InsightItemEntity {
    return InsightItemEntity(
        id = id,
        name = name,
        imageUrl = imageUrl,
        quantity = quantity,
        quantityUnit = quantityUnit,
        status = status.name,
        actionDate = actionDate.toEpochDays(),
        updatedAt = updatedAt,
        nutriScore = nutriScore,
        novaGroup = novaGroup,
        ecoScore = ecoScore
    )
}