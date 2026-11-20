package com.bankmanaging.app.model

data class TransactionFilter(
    val includeIncome: Boolean = true,
    val includeOutcome: Boolean = true,
    val includeOther: Boolean = true,
    val selectedTypes: Set<String> = emptySet(),
    val selectedBanks: Set<String> = emptySet(),
    val minAmount: Long? = null,
    val maxAmount: Long? = null,
    val minBalance: Long? = null,
    val maxBalance: Long? = null,
    val fromDate: PersianDate? = null,
    val toDate: PersianDate? = null,
    val searchQuery: String = ""
) {
    val activeCount: Int
        get() {
            var count = 0
            if (!includeIncome || !includeOutcome || !includeOther) count++
            if (selectedTypes.isNotEmpty()) count++
            if (selectedBanks.isNotEmpty()) count++
            if (minAmount != null || maxAmount != null) count++
            if (minBalance != null || maxBalance != null) count++
            if (fromDate != null || toDate != null) count++
            if (searchQuery.isNotBlank()) count++
            return count
        }

    val isActive: Boolean get() = activeCount > 0
}
