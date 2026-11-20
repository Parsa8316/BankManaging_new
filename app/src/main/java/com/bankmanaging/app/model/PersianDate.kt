package com.bankmanaging.app.model

import kotlinx.serialization.Serializable
import java.util.Calendar
import java.util.TimeZone

@Serializable
data class PersianDate(
    val year: Int,
    val month: Int,
    val day: Int
) : Comparable<PersianDate> {

    override fun compareTo(other: PersianDate): Int {
        if (year != other.year) return year.compareTo(other.year)
        if (month != other.month) return month.compareTo(other.month)
        return day.compareTo(other.day)
    }

    fun format(separator: String = "/", persianDigits: Boolean = true): String {
        val raw = "%04d%s%02d%s%02d".format(year, separator, month, separator, day)
        return if (persianDigits) toPersianDigits(raw) else raw
    }

    fun formatWithMonthName(persianDigits: Boolean = true): String {
        val monthName = MONTH_NAMES.getOrElse(month - 1) { month.toString() }
        val raw = "$day $monthName $year"
        return if (persianDigits) toPersianDigits(raw) else raw
    }

    companion object {
        val MONTH_NAMES = listOf(
            "فروردین", "اردیبهشت", "خرداد",
            "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر",
            "دی", "بهمن", "اسفند"
        )

        val WEEKDAY_SHORT_NAMES = listOf(
            "ش", "ی", "د", "س", "چ", "پ", "ج"
        )

        fun today(): PersianDate = fromTimestamp(System.currentTimeMillis())

        fun fromTimestamp(epochMillis: Long): PersianDate {
            val cal = Calendar.getInstance(TimeZone.getDefault()).apply {
                timeInMillis = epochMillis
            }
            return fromGregorian(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
        }

        fun timeFromTimestamp(epochMillis: Long): Pair<Int, Int> {
            val cal = Calendar.getInstance(TimeZone.getDefault()).apply {
                timeInMillis = epochMillis
            }
            return cal.get(Calendar.HOUR_OF_DAY) to cal.get(Calendar.MINUTE)
        }

        fun fromGregorian(gy: Int, gm: Int, gd: Int): PersianDate {
            val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
            val gy2 = if (gm > 2) gy + 1 else gy
            var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) +
                ((gy2 + 399) / 400) + gd + gDaysInMonth[gm - 1]
            var jy = -1595 + (33 * (days / 12053))
            days %= 12053
            jy += 4 * (days / 1461)
            days %= 1461
            if (days > 365) {
                jy += (days - 1) / 365
                days = (days - 1) % 365
            }
            val jm: Int
            val jd: Int
            if (days < 186) {
                jm = 1 + (days / 31)
                jd = 1 + (days % 31)
            } else {
                jm = 7 + ((days - 186) / 30)
                jd = 1 + ((days - 186) % 30)
            }
            return PersianDate(jy, jm, jd)
        }

        fun toGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
            val jyAdj = jy + 1595
            var days = -355668 + (365 * jyAdj) + ((jyAdj / 33) * 8) + (((jyAdj % 33) + 3) / 4) +
                jd + (if (jm < 7) (jm - 1) * 31 else ((jm - 7) * 30) + 186)
            var gy = 400 * (days / 146097)
            days %= 146097
            if (days > 36524) {
                gy += 100 * (--days / 36524)
                days %= 36524
                if (days >= 365) days++
            }
            gy += 4 * (days / 1461)
            days %= 1461
            if (days > 365) {
                gy += (days - 1) / 365
                days = (days - 1) % 365
            }
            var gd = days + 1
            val isGregLeap = (gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0)
            val salA = intArrayOf(0, 31, if (isGregLeap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
            var gm = 1
            while (gm <= 12 && gd > salA[gm]) {
                gd -= salA[gm]
                gm++
            }
            return Triple(gy, gm, gd)
        }

        fun isLeapYear(jy: Int): Boolean {
            val (gy, gm, gd) = toGregorian(jy, 12, 30)
            val roundTrip = fromGregorian(gy, gm, gd)
            return roundTrip.year == jy && roundTrip.month == 12 && roundTrip.day == 30
        }

        fun daysInMonth(jy: Int, jm: Int): Int = when {
            jm in 1..6 -> 31
            jm in 7..11 -> 30
            jm == 12 -> if (isLeapYear(jy)) 30 else 29
            else -> 30
        }

        /**
         * Returns weekday offset for Persian calendar (0 = Saturday, 1 = Sunday, ..., 6 = Friday).
         */
        fun firstDayOfMonthOffset(jy: Int, jm: Int): Int {
            val (gy, gm, gd) = toGregorian(jy, jm, 1)
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, gy)
                set(Calendar.MONTH, gm - 1)
                set(Calendar.DAY_OF_MONTH, gd)
            }
            return when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SATURDAY -> 0
                Calendar.SUNDAY -> 1
                Calendar.MONDAY -> 2
                Calendar.TUESDAY -> 3
                Calendar.WEDNESDAY -> 4
                Calendar.THURSDAY -> 5
                Calendar.FRIDAY -> 6
                else -> 0
            }
        }

        fun parse(raw: String): PersianDate? {
            val normalized = toLatinDigits(raw).trim()
            val match = Regex("""(\d{2,4})[/.\-](\d{1,2})[/.\-](\d{1,2})""").find(normalized)
                ?: return null
            var year = match.groupValues[1].toIntOrNull() ?: return null
            val month = match.groupValues[2].toIntOrNull() ?: return null
            val day = match.groupValues[3].toIntOrNull() ?: return null
            if (year in 0..99) {
                year += 1400
            }
            if (year !in 1300..1500 || month !in 1..12) return null
            val maxDay = daysInMonth(year, month)
            if (day !in 1..maxDay) return null
            return PersianDate(year, month, day)
        }

        fun toPersianDigits(input: String): String {
            val sb = StringBuilder(input.length)
            for (ch in input) {
                sb.append(
                    when (ch) {
                        in '0'..'9' -> ('۰'.code + (ch - '0')).toChar()
                        else -> ch
                    }
                )
            }
            return sb.toString()
        }

        fun toLatinDigits(input: String): String {
            val sb = StringBuilder(input.length)
            for (ch in input) {
                sb.append(
                    when (ch) {
                        in '۰'..'۹' -> ('0'.code + (ch - '۰')).toChar()
                        in '٠'..'٩' -> ('0'.code + (ch - '٠')).toChar()
                        else -> ch
                    }
                )
            }
            return sb.toString()
        }
    }
}
