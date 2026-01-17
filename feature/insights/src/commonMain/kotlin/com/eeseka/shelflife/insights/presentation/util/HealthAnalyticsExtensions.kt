package com.eeseka.shelflife.insights.presentation.util

import com.eeseka.shelflife.shared.domain.insight.InsightItem
import com.eeseka.shelflife.shared.domain.insight.InsightStatus

// Returns a map of "a"->5, "b"->2, etc. based on CONSUMED items
fun List<InsightItem>.getNutriScoreDistribution(): Map<String, Int> {
    return this
        .filter { it.status == InsightStatus.CONSUMED && it.nutriScore != null }
        .groupBy { it.nutriScore!!.lowercase() }
        .mapValues { it.value.size }
}

// Returns the count of CONSUMED items that are Ultra-Processed (NOVA 4)
fun List<InsightItem>.getUltraProcessedCount(): Int {
    return this.count { 
        it.status == InsightStatus.CONSUMED && it.novaGroup == 4 
    }
}

// Returns the count of WASTED items that had a good Eco-Score (A or B)
// (i.e., "You wasted environmentally friendly food")
fun List<InsightItem>.getWastedGoodEcoCount(): Int {
    return this.count { 
        it.status == InsightStatus.WASTED && 
        it.ecoScore?.lowercase() in listOf("a", "b") 
    }
}