package com.bankmanaging.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bankmanaging.app.data.PreferencesRepository
import com.bankmanaging.app.data.SmsParser
import com.bankmanaging.app.data.SmsRepository
import com.bankmanaging.app.data.ThemeMode
import com.bankmanaging.app.model.BankTransaction
import com.bankmanaging.app.model.DefaultSmsPatterns
import com.bankmanaging.app.model.SmsPattern
import com.bankmanaging.app.model.TransactionFilter
import com.bankmanaging.app.model.TransactionKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class BankUiState(
    val hasSmsPermission: Boolean = false,
    val isLoading: Boolean = false,
    val allTransactions: List<BankTransaction> = emptyList(),
    val filteredTransactions: List<BankTransaction> = emptyList(),
    val totalIncome: Long = 0L,
    val totalOutcome: Long = 0L,
    val netFlow: Long = 0L,
    val latestBalance: Long? = null,
    val availableBanks: List<String> = emptyList(),
    val availableTypes: List<String> = emptyList(),
    val filter: TransactionFilter = TransactionFilter(),
    val bankSenders: List<String> = emptyList(),
    val smsPatterns: List<SmsPattern> = DefaultSmsPatterns.allDefaults,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val usePersianDigits: Boolean = true,
    val snackbarMessage: String? = null
)

class BankViewModel(application: Application) : AndroidViewModel(application) {

    private val smsRepository = SmsRepository(application.applicationContext)
    private val prefsRepository = PreferencesRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(
        BankUiState(hasSmsPermission = smsRepository.hasSmsPermission())
    )
    val uiState: StateFlow<BankUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                prefsRepository.bankSenders,
                prefsRepository.smsPatterns,
                prefsRepository.themeMode,
                prefsRepository.usePersianDigits
            ) { senders, patterns, themeMode, persianDigits ->
                PrefsSnapshot(senders, patterns, themeMode, persianDigits)
            }.collect { snap ->
                _uiState.update { current ->
                    current.copy(
                        bankSenders = snap.senders,
                        smsPatterns = snap.patterns,
                        themeMode = snap.themeMode,
                        usePersianDigits = snap.persianDigits
                    )
                }
                refreshTransactions(snap.senders, snap.patterns)
            }
        }
    }

    private data class PrefsSnapshot(
        val senders: List<String>,
        val patterns: List<SmsPattern>,
        val themeMode: ThemeMode,
        val persianDigits: Boolean
    )

    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(hasSmsPermission = granted) }
        if (granted) {
            refreshTransactions()
        }
    }

    fun refreshTransactions(
        sendersOverride: List<String>? = null,
        patternsOverride: List<SmsPattern>? = null
    ) {
        val hasPerm = smsRepository.hasSmsPermission()
        _uiState.update { it.copy(hasSmsPermission = hasPerm, isLoading = true) }
        if (!hasPerm) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }

        val currentState = _uiState.value
        val senders = sendersOverride ?: currentState.bankSenders
        val patterns = patternsOverride ?: currentState.smsPatterns

        viewModelScope.launch {
            val transactions = smsRepository.readBankTransactions(senders, patterns)
            applyTransactionsAndFilter(transactions, _uiState.value.filter)
        }
    }

    fun updateFilter(newFilter: TransactionFilter) {
        applyTransactionsAndFilter(_uiState.value.allTransactions, newFilter)
    }

    fun updateSearchQuery(query: String) {
        val updated = _uiState.value.filter.copy(searchQuery = query)
        applyTransactionsAndFilter(_uiState.value.allTransactions, updated)
    }

    fun resetFilter() {
        applyTransactionsAndFilter(_uiState.value.allTransactions, TransactionFilter())
    }

    private fun applyTransactionsAndFilter(
        all: List<BankTransaction>,
        filter: TransactionFilter
    ) {
        val filtered = filterTransactions(all, filter)
        var sumIncome = 0L
        var sumOutcome = 0L
        for (tx in filtered) {
            when (tx.kind) {
                TransactionKind.INCOME -> sumIncome += tx.amountToman
                TransactionKind.OUTCOME -> sumOutcome += tx.amountToman
                TransactionKind.OTHER -> Unit
            }
        }
        val latestBalance = filtered.firstOrNull { it.balanceToman != null }?.balanceToman

        val banks = all.map { it.bankName }.filter { it.isNotBlank() }.distinct().sorted()
        val types = all.map { it.type }.filter { it.isNotBlank() }.distinct().sorted()

        _uiState.update {
            it.copy(
                isLoading = false,
                allTransactions = all,
                filteredTransactions = filtered,
                totalIncome = sumIncome,
                totalOutcome = sumOutcome,
                netFlow = sumIncome - sumOutcome,
                latestBalance = latestBalance,
                availableBanks = banks,
                availableTypes = types,
                filter = filter
            )
        }
    }

    private fun filterTransactions(
        source: List<BankTransaction>,
        filter: TransactionFilter
    ): List<BankTransaction> {
        val normQuery = SmsParser.normalizePersianText(filter.searchQuery).trim().lowercase()

        return source.filter { tx ->
            val kindOk = when (tx.kind) {
                TransactionKind.INCOME -> filter.includeIncome
                TransactionKind.OUTCOME -> filter.includeOutcome
                TransactionKind.OTHER -> filter.includeOther
            }
            if (!kindOk) return@filter false

            if (filter.selectedTypes.isNotEmpty() && tx.type !in filter.selectedTypes) {
                return@filter false
            }

            if (filter.selectedBanks.isNotEmpty() && tx.bankName !in filter.selectedBanks) {
                return@filter false
            }

            if (filter.minAmount != null && tx.amountToman < filter.minAmount) {
                return@filter false
            }
            if (filter.maxAmount != null && tx.amountToman > filter.maxAmount) {
                return@filter false
            }

            if (filter.minBalance != null) {
                val bal = tx.balanceToman ?: return@filter false
                if (bal < filter.minBalance) return@filter false
            }
            if (filter.maxBalance != null) {
                val bal = tx.balanceToman ?: return@filter false
                if (bal > filter.maxBalance) return@filter false
            }

            if (filter.fromDate != null && tx.persianDate < filter.fromDate) {
                return@filter false
            }
            if (filter.toDate != null && tx.persianDate > filter.toDate) {
                return@filter false
            }

            if (normQuery.isNotEmpty()) {
                val haystack = SmsParser.normalizePersianText(
                    "${tx.rawMessage} ${tx.type} ${tx.bankName} ${tx.senderAddress}"
                ).lowercase()
                if (!haystack.contains(normQuery)) return@filter false
            }

            true
        }
    }

    fun addBankSender(rawInput: String): String? {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) {
            return "لطفاً شماره یا نام فرستنده پیامک را وارد کنید."
        }
        val normalized = SmsParser.normalizeSender(trimmed)
        val existing = _uiState.value.bankSenders
        if (existing.any { SmsParser.normalizeSender(it).equals(normalized, ignoreCase = true) }) {
            return "این فرستنده قبلاً اضافه شده است."
        }
        val updated = existing + normalized
        viewModelScope.launch {
            prefsRepository.saveBankSenders(updated)
            showSnackbar("فرستنده «$normalized» با موفقیت ذخیره شد.")
        }
        return null
    }

    fun removeBankSender(sender: String) {
        val updated = _uiState.value.bankSenders.filterNot { it == sender }
        viewModelScope.launch {
            prefsRepository.saveBankSenders(updated)
            showSnackbar("فرستنده «$sender» حذف شد.")
        }
    }

    fun saveSmsPattern(pattern: SmsPattern) {
        val current = _uiState.value.smsPatterns
        val exists = current.any { it.id == pattern.id }
        val updated = if (exists) {
            current.map { if (it.id == pattern.id) pattern else it }
        } else {
            listOf(pattern) + current
        }
        viewModelScope.launch {
            prefsRepository.saveSmsPatterns(updated)
            showSnackbar("الگوی «${pattern.name}» ذخیره شد.")
        }
    }

    fun togglePatternEnabled(patternId: String, enabled: Boolean) {
        val updated = _uiState.value.smsPatterns.map {
            if (it.id == patternId) it.copy(isEnabled = enabled) else it
        }
        viewModelScope.launch {
            prefsRepository.saveSmsPatterns(updated)
        }
    }

    fun duplicatePattern(pattern: SmsPattern): SmsPattern {
        val copy = pattern.copy(
            id = "custom_${UUID.randomUUID()}",
            name = "${pattern.name} (کپی)",
            isBuiltIn = false,
            isEnabled = true
        )
        saveSmsPattern(copy)
        return copy
    }

    fun deletePattern(patternId: String) {
        val target = _uiState.value.smsPatterns.firstOrNull { it.id == patternId } ?: return
        val updated = _uiState.value.smsPatterns.filterNot { it.id == patternId }
        viewModelScope.launch {
            prefsRepository.saveSmsPatterns(updated)
            showSnackbar("الگوی «${target.name}» حذف شد.")
        }
    }

    fun resetDefaultPatterns() {
        val customOnly = _uiState.value.smsPatterns.filter { !it.isBuiltIn }
        val restored = customOnly + DefaultSmsPatterns.allDefaults
        viewModelScope.launch {
            prefsRepository.saveSmsPatterns(restored)
            showSnackbar("الگوهای پیش‌فرض بازگردانی شدند.")
        }
    }

    fun getPatternById(patternId: String?): SmsPattern? {
        if (patternId.isNullOrBlank()) return null
        return _uiState.value.smsPatterns.firstOrNull { it.id == patternId }
    }

    fun testPattern(pattern: SmsPattern, sampleSms: String, sender: String = ""): BankTransaction {
        return SmsParser.parseWithPattern(
            id = 0L,
            rawMessage = sampleSms,
            senderAddress = sender.ifBlank { pattern.senderKeywords.firstOrNull().orEmpty() },
            timestampMillis = System.currentTimeMillis(),
            pattern = pattern
        )
    }

    fun analyzeMessageWithActivePatterns(sampleSms: String, sender: String = ""): BankTransaction {
        return SmsParser.parseMessage(
            id = 0L,
            rawMessage = sampleSms,
            senderAddress = sender,
            timestampMillis = System.currentTimeMillis(),
            patterns = _uiState.value.smsPatterns
        )
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            prefsRepository.setThemeMode(mode)
        }
    }

    fun setUsePersianDigits(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.setUsePersianDigits(enabled)
        }
    }

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    fun consumeSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
