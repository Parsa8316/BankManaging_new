package com.bankmanaging.app.ui.util

import com.bankmanaging.app.model.PersianDate
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

object Formatters {
    private val groupingSymbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ','
    }

    fun formatNumber(value: Long, persianDigits: Boolean = true): String {
        val formatted = DecimalFormat("#,###", groupingSymbols).format(value)
        return if (persianDigits) PersianDate.toPersianDigits(formatted) else formatted
    }

    fun formatToman(
        amount: Long,
        persianDigits: Boolean = true,
        showSign: Boolean = false,
        includeUnit: Boolean = true
    ): String {
        val sign = when {
            !showSign -> if (amount < 0) "-" else ""
            amount > 0 -> "+"
            amount < 0 -> "-"
            else -> ""
        }
        val num = formatNumber(abs(amount), persianDigits)
        val unit = if (includeUnit) " تومان" else ""
        return "$sign$num$unit"
    }

    fun formatDigits(text: String, persianDigits: Boolean): String {
        return if (persianDigits) PersianDate.toPersianDigits(text) else PersianDate.toLatinDigits(text)
    }

    fun parseOptionalLong(raw: String): Long? {
        val cleaned = PersianDate.toLatinDigits(raw)
            .replace(",", "")
            .replace("٬", "")
            .replace(" ", "")
            .trim()
        if (cleaned.isEmpty()) return null
        return cleaned.toLongOrNull()
    }
}
