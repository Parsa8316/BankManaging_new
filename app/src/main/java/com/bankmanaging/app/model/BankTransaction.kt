package com.bankmanaging.app.model

enum class TransactionKind {
    INCOME,
    OUTCOME,
    OTHER
}

data class BankTransaction(
    val id: Long,
    val kind: TransactionKind,
    val type: String,
    val amountToman: Long,
    val balanceToman: Long?,
    val persianDate: PersianDate,
    val hour: Int,
    val minute: Int,
    val timestampMillis: Long,
    val bankName: String,
    val senderAddress: String,
    val rawMessage: String,
    val matchedPatternName: String
) {
    fun formattedTime(persianDigits: Boolean = true): String {
        val raw = "%02d:%02d".format(hour, minute)
        return if (persianDigits) PersianDate.toPersianDigits(raw) else raw
    }

    fun formattedDateTime(persianDigits: Boolean = true): String {
        return "${persianDate.format("/", persianDigits)} - ${formattedTime(persianDigits)}"
    }
}
