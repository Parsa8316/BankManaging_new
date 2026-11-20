package com.bankmanaging.app.data

import com.bankmanaging.app.model.BankTransaction
import com.bankmanaging.app.model.CurrencyUnit
import com.bankmanaging.app.model.DefaultSmsPatterns
import com.bankmanaging.app.model.ExtractionMode
import com.bankmanaging.app.model.PersianDate
import com.bankmanaging.app.model.SmsPattern
import com.bankmanaging.app.model.TransactionKind

object SmsParser {

    fun normalizePersianText(input: String): String {
        val latin = PersianDate.toLatinDigits(input)
        return latin
            .replace('ي', 'ی')
            .replace('ك', 'ک')
            .replace('ة', 'ه')
            .replace('٬', ',')
            .replace('،', ',')
            .replace('\u200E', ' ')
            .replace('\u200F', ' ')
            .replace('\u00A0', ' ')
    }

    fun normalizeSender(sender: String): String {
        val cleaned = normalizePersianText(sender)
            .replace(" ", "")
            .replace("-", "")
            .trim()
        return when {
            cleaned.startsWith("+98") -> cleaned
            cleaned.startsWith("0098") -> "+98" + cleaned.drop(4)
            cleaned.startsWith("98") && cleaned.length >= 9 -> "+$cleaned"
            cleaned.startsWith("0") && cleaned.length >= 10 -> "+98" + cleaned.drop(1)
            else -> cleaned
        }
    }

    fun senderMatches(address: String, configuredSender: String): Boolean {
        if (address.isBlank() || configuredSender.isBlank()) return false
        val normAddress = normalizeSender(address).lowercase()
        val normConfig = normalizeSender(configuredSender).lowercase()
        if (normAddress.contains(normConfig) || normConfig.contains(normAddress)) return true
        val strippedAddress = normAddress.removePrefix("+98").removePrefix("0")
        val strippedConfig = normConfig.removePrefix("+98").removePrefix("0")
        return strippedConfig.isNotEmpty() && strippedAddress.contains(strippedConfig)
    }

    fun parseMessage(
        id: Long,
        rawMessage: String,
        senderAddress: String,
        timestampMillis: Long,
        patterns: List<SmsPattern>
    ): BankTransaction {
        val enabledPatterns = patterns.filter { it.isEnabled }
        val pattern = selectBestPattern(rawMessage, senderAddress, enabledPatterns)
            ?: DefaultSmsPatterns.generalIranBank
        return parseWithPattern(id, rawMessage, senderAddress, timestampMillis, pattern)
    }

    fun selectBestPattern(
        rawMessage: String,
        senderAddress: String,
        patterns: List<SmsPattern>
    ): SmsPattern? {
        if (patterns.isEmpty()) return null
        val normBody = normalizePersianText(rawMessage).lowercase()

        // Priority 1: Custom (non-built-in) patterns matching sender or body identifier
        val customPatterns = patterns.filter { !it.isBuiltIn }
        val builtInSpecific = patterns.filter { it.isBuiltIn && it.id != DefaultSmsPatterns.generalIranBank.id }
        val generalFallback = patterns.firstOrNull { it.id == DefaultSmsPatterns.generalIranBank.id }

        fun findMatch(candidates: List<SmsPattern>): SmsPattern? {
            // Exact sender match first
            candidates.firstOrNull { pattern ->
                pattern.senderKeywords.any { kw -> senderMatches(senderAddress, kw) }
            }?.let { return it }

            // Body identifier match second
            candidates.firstOrNull { pattern ->
                pattern.bodyIdentifierKeywords.any { kw ->
                    val normKw = normalizePersianText(kw).trim().lowercase()
                    normKw.isNotEmpty() && normBody.contains(normKw)
                }
            }?.let { return it }

            return null
        }

        findMatch(customPatterns)?.let { return it }
        findMatch(builtInSpecific)?.let { return it }

        // Priority 3: Custom pattern with no sender/body identifiers acts as user-defined fallback
        customPatterns.firstOrNull {
            it.senderKeywords.isEmpty() && it.bodyIdentifierKeywords.isEmpty()
        }?.let { return it }

        return generalFallback ?: patterns.firstOrNull()
    }

    fun parseWithPattern(
        id: Long,
        rawMessage: String,
        senderAddress: String,
        timestampMillis: Long,
        pattern: SmsPattern
    ): BankTransaction {
        val normMessage = normalizePersianText(rawMessage)
        val rows = normMessage
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val kind = classifyKind(normMessage, rows, pattern)
        val typeLabel = extractType(rows, normMessage, kind, pattern)
        val bankName = extractBankName(rows, senderAddress, pattern)

        val rawAmount = extractMoney(
            rows = rows,
            fullText = normMessage,
            mode = pattern.amountMode,
            lineIndex = pattern.amountLineIndex,
            keywords = pattern.amountKeywords,
            regexStr = pattern.amountRegex,
            excludeKeywords = pattern.balanceKeywords,
            isBalance = false
        )
        val rawBalance = extractMoney(
            rows = rows,
            fullText = normMessage,
            mode = pattern.balanceMode,
            lineIndex = pattern.balanceLineIndex,
            keywords = pattern.balanceKeywords,
            regexStr = pattern.balanceRegex,
            excludeKeywords = emptyList(),
            isBalance = true
        )

        val unitInText = detectCurrencyOverride(normMessage, pattern.currencyUnit)
        val amountToman = convertToToman(rawAmount ?: 0L, unitInText)
        val balanceToman = rawBalance?.let { convertToToman(it, unitInText) }

        val fallbackDate = PersianDate.fromTimestamp(timestampMillis)
        val (fallbackHour, fallbackMinute) = PersianDate.timeFromTimestamp(timestampMillis)

        val extractedDate = extractDate(rows, normMessage, pattern.dateLineIndex) ?: fallbackDate
        val (hour, minute) = extractTime(rows, normMessage, pattern.timeLineIndex)
            ?: (fallbackHour to fallbackMinute)

        return BankTransaction(
            id = id,
            kind = kind,
            type = typeLabel,
            amountToman = amountToman,
            balanceToman = balanceToman,
            persianDate = extractedDate,
            hour = hour,
            minute = minute,
            timestampMillis = timestampMillis,
            bankName = bankName,
            senderAddress = senderAddress,
            rawMessage = rawMessage.trim(),
            matchedPatternName = pattern.name
        )
    }

    private fun classifyKind(
        normMessage: String,
        rows: List<String>,
        pattern: SmsPattern
    ): TransactionKind {
        val lowerFull = normMessage.lowercase()

        // Check ignore/OTP keywords first so dynamic passwords aren't misclassified as expenses
        if (pattern.ignoreKeywords.any { kw ->
                val norm = normalizePersianText(kw).trim().lowercase()
                norm.isNotEmpty() && lowerFull.contains(norm)
            }
        ) {
            return TransactionKind.OTHER
        }

        // Prefer checking the type line first if configured, then fall back to entire message
        val primaryLine = if (pattern.typeLineIndex > 0 && pattern.typeLineIndex <= rows.size) {
            rows[pattern.typeLineIndex - 1].lowercase()
        } else {
            lowerFull
        }

        fun matchesAny(target: String, keywords: List<String>): Boolean {
            return keywords.any { kw ->
                val norm = normalizePersianText(kw).trim().lowercase()
                norm.isNotEmpty() && target.contains(norm)
            }
        }

        val lineIncome = matchesAny(primaryLine, pattern.incomeKeywords)
        val lineOutcome = matchesAny(primaryLine, pattern.outcomeKeywords)
        if (lineIncome && !lineOutcome) return TransactionKind.INCOME
        if (lineOutcome && !lineIncome) return TransactionKind.OUTCOME

        val fullIncome = matchesAny(lowerFull, pattern.incomeKeywords)
        val fullOutcome = matchesAny(lowerFull, pattern.outcomeKeywords)
        if (fullIncome && !fullOutcome) return TransactionKind.INCOME
        if (fullOutcome && !fullIncome) return TransactionKind.OUTCOME

        // Sign heuristic for banks like Mellat/Melli using + or - adjacent to amount
        if (Regex("""[\d,]+\s*\+|\+\s*[\d,]{4,}""").containsMatchIn(normMessage)) {
            return TransactionKind.INCOME
        }
        if (Regex("""[\d,]+\s*-|-\s*[\d,]{4,}""").containsMatchIn(normMessage)) {
            return TransactionKind.OUTCOME
        }

        return TransactionKind.OTHER
    }

    private fun extractType(
        rows: List<String>,
        normMessage: String,
        kind: TransactionKind,
        pattern: SmsPattern
    ): String {
        if (pattern.typeLineIndex > 0 && pattern.typeLineIndex <= rows.size) {
            val candidate = rows[pattern.typeLineIndex - 1].trim()
            if (candidate.isNotEmpty() && candidate.length <= 40) {
                return candidate
            }
        }

        val lowerFull = normMessage.lowercase()
        val keywordsToCheck = when (kind) {
            TransactionKind.INCOME -> pattern.incomeKeywords
            TransactionKind.OUTCOME -> pattern.outcomeKeywords
            TransactionKind.OTHER -> pattern.ignoreKeywords
        }
        val matchedKeyword = keywordsToCheck.firstOrNull { kw ->
            val norm = normalizePersianText(kw).trim().lowercase()
            norm.length >= 2 && lowerFull.contains(norm)
        }
        if (matchedKeyword != null) return matchedKeyword.trim()

        return when (kind) {
            TransactionKind.INCOME -> "واریز"
            TransactionKind.OUTCOME -> "برداشت"
            TransactionKind.OTHER -> "پیام اطلاع‌رسانی"
        }
    }

    private fun extractBankName(
        rows: List<String>,
        senderAddress: String,
        pattern: SmsPattern
    ): String {
        if (pattern.bankNameLineIndex > 0 && pattern.bankNameLineIndex <= rows.size) {
            val candidate = rows[pattern.bankNameLineIndex - 1].trim()
            if (candidate.isNotEmpty() && candidate.length <= 28 && !candidate.any { it.isDigit() }) {
                return candidate
            }
        }
        if (pattern.bankDisplayName.isNotBlank()) {
            return pattern.bankDisplayName.trim()
        }
        if (rows.isNotEmpty()) {
            val firstLine = rows[0].trim()
            if (firstLine.length in 2..24 && !firstLine.any { it.isDigit() }) {
                return firstLine
            }
        }
        return senderAddress.ifBlank { pattern.name }
    }

    private fun extractMoney(
        rows: List<String>,
        fullText: String,
        mode: ExtractionMode,
        lineIndex: Int,
        keywords: List<String>,
        regexStr: String,
        excludeKeywords: List<String>,
        isBalance: Boolean
    ): Long? {
        // Mode 1: Explicit Regex
        if (mode == ExtractionMode.REGEX && regexStr.isNotBlank()) {
            runCatching {
                val match = Regex(normalizePersianText(regexStr)).find(fullText)
                val groupValue = match?.groupValues?.getOrNull(1) ?: match?.value
                groupValue?.let { parseLongFromNumberToken(it) }
            }.getOrNull()?.let { return it }
        }

        // Mode 2: Explicit Line Index (1-based)
        if (mode == ExtractionMode.LINE_INDEX && lineIndex > 0 && lineIndex <= rows.size) {
            val line = rows[lineIndex - 1]
            extractBestMoneyFromLine(line)?.let { return it }
        }

        // Mode 3: Smart Keyword Line Search (also serves as safe fallback for Line Index if SMS format shifted slightly)
        val normKeywords = keywords.map { normalizePersianText(it).trim().lowercase() }.filter { it.isNotEmpty() }
        val normExcludes = excludeKeywords.map { normalizePersianText(it).trim().lowercase() }.filter { it.isNotEmpty() }

        for (line in rows) {
            val lowerLine = line.lowercase()
            val excluded = normExcludes.any { ex -> lowerLine.contains(ex) }
            if (excluded) continue

            val matchedKw = normKeywords.firstOrNull { kw -> lowerLine.contains(kw) }
            if (matchedKw != null) {
                extractNumberNearKeyword(line, matchedKw)?.let { return it }
                extractBestMoneyFromLine(line)?.let { return it }
            }
        }

        // For non-balance amounts, fallback to any line with ریال/تومان not containing balance keywords
        if (!isBalance) {
            for (line in rows) {
                val lowerLine = line.lowercase()
                if (normExcludes.any { lowerLine.contains(it) }) continue
                if (lowerLine.contains("ریال") || lowerLine.contains("تومان")) {
                    extractBestMoneyFromLine(line)?.let { return it }
                }
            }
        }

        return null
    }

    private fun extractNumberNearKeyword(line: String, keywordLower: String): Long? {
        val lower = line.lowercase()
        val kwIdx = lower.indexOf(keywordLower)
        if (kwIdx < 0) return null

        // Look right after keyword first (e.g., "موجودی: 12,500,000 ریال")
        val after = line.substring((kwIdx + keywordLower.length).coerceAtMost(line.length))
        val afterMatch = Regex("""[\d,]{2,}""").find(after)
        if (afterMatch != null) {
            val value = parseLongFromNumberToken(afterMatch.value)
            if (value != null && !looksLikeDateOrAccount(afterMatch.value, after, afterMatch.range)) {
                return value
            }
        }

        // Otherwise look right before keyword (e.g., "12,500,000 ریال")
        val before = line.substring(0, kwIdx)
        val beforeMatch = Regex("""[\d,]{2,}""").findAll(before).lastOrNull()
        if (beforeMatch != null) {
            val value = parseLongFromNumberToken(beforeMatch.value)
            if (value != null && !looksLikeDateOrAccount(beforeMatch.value, before, beforeMatch.range)) {
                return value
            }
        }
        return null
    }

    private fun extractBestMoneyFromLine(line: String): Long? {
        // Prefer number directly before ریال or تومان
        val currencyMatch = Regex("""([\d,]{2,})\s*[-+]?\s*(?:ریال|تومان)""").find(line)
        if (currencyMatch != null) {
            parseLongFromNumberToken(currencyMatch.groupValues[1])?.let { return it }
        }
        // Prefer comma-formatted numbers (very characteristic of currency in Iranian SMS)
        val commaMatch = Regex("""\d{1,3}(?:,\d{3})+""").find(line)
        if (commaMatch != null) {
            parseLongFromNumberToken(commaMatch.value)?.let { return it }
        }
        // Fallback: first standalone number that is not part of a date/time
        val candidates = Regex("""(?<![/.\-:])\b\d{3,15}\b(?![/.\-:])""").findAll(line)
        for (c in candidates) {
            parseLongFromNumberToken(c.value)?.let { return it }
        }
        return null
    }

    private fun looksLikeDateOrAccount(token: String, context: String, range: IntRange): Boolean {
        val nextChar = context.getOrNull(range.last + 1)
        val prevChar = context.getOrNull(range.first - 1)
        if (nextChar == '/' || nextChar == ':' || prevChar == '/' || prevChar == ':') return true
        // Very long unformatted numbers (> 12 digits without commas) are usually card/IBAN/account numbers
        if (!token.contains(',') && token.length >= 12) return true
        return false
    }

    private fun parseLongFromNumberToken(token: String): Long? {
        val digitsOnly = token.replace(",", "").replace(" ", "").trim()
        return digitsOnly.toLongOrNull()
    }

    private fun detectCurrencyOverride(normMessage: String, defaultUnit: CurrencyUnit): CurrencyUnit {
        return when {
            normMessage.contains("تومان") && !normMessage.contains("ریال") -> CurrencyUnit.TOMAN
            normMessage.contains("ریال") && !normMessage.contains("تومان") -> CurrencyUnit.RIAL
            else -> defaultUnit
        }
    }

    private fun convertToToman(rawAmount: Long, unit: CurrencyUnit): Long {
        return when (unit) {
            CurrencyUnit.RIAL -> rawAmount / 10L
            CurrencyUnit.TOMAN -> rawAmount
        }
    }

    private fun extractDate(rows: List<String>, normMessage: String, dateLineIndex: Int): PersianDate? {
        if (dateLineIndex > 0 && dateLineIndex <= rows.size) {
            PersianDate.parse(rows[dateLineIndex - 1])?.let { return it }
        }
        // Scan from bottom up (banks almost always place date near the end of the SMS)
        for (i in rows.indices.reversed()) {
            PersianDate.parse(rows[i])?.let { return it }
        }
        return PersianDate.parse(normMessage)
    }

    private fun extractTime(rows: List<String>, normMessage: String, timeLineIndex: Int): Pair<Int, Int>? {
        val timeRegex = Regex("""(?<!\d)([01]?\d|2[0-3]):([0-5]\d)(?::[0-5]\d)?(?!\d)""")
        if (timeLineIndex > 0 && timeLineIndex <= rows.size) {
            timeRegex.find(rows[timeLineIndex - 1])?.let { m ->
                val h = m.groupValues[1].toIntOrNull()
                val min = m.groupValues[2].toIntOrNull()
                if (h != null && min != null) return h to min
            }
        }
        for (i in rows.indices.reversed()) {
            timeRegex.find(rows[i])?.let { m ->
                val h = m.groupValues[1].toIntOrNull()
                val min = m.groupValues[2].toIntOrNull()
                if (h != null && min != null) return h to min
            }
        }
        return timeRegex.find(normMessage)?.let { m ->
            val h = m.groupValues[1].toIntOrNull()
            val min = m.groupValues[2].toIntOrNull()
            if (h != null && min != null) h to min else null
        }
    }
}
