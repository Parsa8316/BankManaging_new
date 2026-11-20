package com.bankmanaging.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bankmanaging.app.R
import com.bankmanaging.app.model.BankTransaction
import com.bankmanaging.app.model.CurrencyUnit
import com.bankmanaging.app.model.ExtractionMode
import com.bankmanaging.app.model.SmsPattern
import com.bankmanaging.app.model.TransactionKind
import com.bankmanaging.app.ui.theme.LocalFinancialColors
import com.bankmanaging.app.ui.util.Formatters
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternEditorScreen(
    existingPattern: SmsPattern?,
    persianDigits: Boolean,
    onTestPattern: (SmsPattern, String) -> BankTransaction,
    onSave: (SmsPattern) -> Unit,
    onBack: () -> Unit
) {
    val isEditing = existingPattern != null
    var name by remember { mutableStateOf(existingPattern?.name.orEmpty()) }
    var nameError by remember { mutableStateOf(false) }
    var bankDisplayName by remember { mutableStateOf(existingPattern?.bankDisplayName.orEmpty()) }
    var senderKeywordsText by remember { mutableStateOf(existingPattern?.senderKeywords?.joinToString("، ").orEmpty()) }
    var bodyKeywordsText by remember { mutableStateOf(existingPattern?.bodyIdentifierKeywords?.joinToString("، ").orEmpty()) }
    var currencyUnit by remember { mutableStateOf(existingPattern?.currencyUnit ?: CurrencyUnit.RIAL) }

    var incomeKeywordsText by remember {
        mutableStateOf(
            existingPattern?.incomeKeywords?.joinToString("، ")
                ?: "واریز، سود، بستانکار، انتقال به"
        )
    }
    var outcomeKeywordsText by remember {
        mutableStateOf(
            existingPattern?.outcomeKeywords?.joinToString("، ")
                ?: "برداشت، خرید، پرداخت، بدهکار، انتقال از"
        )
    }
    var ignoreKeywordsText by remember {
        mutableStateOf(
            existingPattern?.ignoreKeywords?.joinToString("، ")
                ?: "رمز پویا، رمز دوم، کد تایید"
        )
    }

    var amountMode by remember { mutableStateOf(existingPattern?.amountMode ?: ExtractionMode.AUTO_KEYWORD) }
    var amountLineText by remember { mutableStateOf((existingPattern?.amountLineIndex ?: 3).toString()) }
    var amountKeywordsText by remember {
        mutableStateOf(
            existingPattern?.amountKeywords?.joinToString("، ")
                ?: "مبلغ، واریز، برداشت، ریال"
        )
    }
    var amountRegex by remember { mutableStateOf(existingPattern?.amountRegex.orEmpty()) }

    var balanceMode by remember { mutableStateOf(existingPattern?.balanceMode ?: ExtractionMode.AUTO_KEYWORD) }
    var balanceLineText by remember { mutableStateOf((existingPattern?.balanceLineIndex ?: 4).toString()) }
    var balanceKeywordsText by remember {
        mutableStateOf(
            existingPattern?.balanceKeywords?.joinToString("، ")
                ?: "مانده، موجودی، موجودی حساب"
        )
    }
    var balanceRegex by remember { mutableStateOf(existingPattern?.balanceRegex.orEmpty()) }

    var typeLineText by remember { mutableStateOf((existingPattern?.typeLineIndex ?: 0).toString()) }
    var bankLineText by remember { mutableStateOf((existingPattern?.bankNameLineIndex ?: 0).toString()) }
    var dateLineText by remember { mutableStateOf((existingPattern?.dateLineIndex ?: 0).toString()) }
    var timeLineText by remember { mutableStateOf((existingPattern?.timeLineIndex ?: 0).toString()) }

    var sampleSms by remember {
        mutableStateOf(
            existingPattern?.sampleSms?.ifBlank { null } ?: """
                بانک نمونه
                واریز به حساب
                مبلغ: 4,500,000 ریال
                مانده: 27,800,000 ریال
                1404/08/15 13:45
            """.trimIndent()
        )
    }

    fun splitKeywords(raw: String): List<String> = raw
        .split(",", "،", "\n")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    fun buildDraftPattern(): SmsPattern = SmsPattern(
        id = existingPattern?.id ?: "custom_${UUID.randomUUID()}",
        name = name.trim().ifBlank { "الگوی جدید" },
        bankDisplayName = bankDisplayName.trim(),
        senderKeywords = splitKeywords(senderKeywordsText),
        bodyIdentifierKeywords = splitKeywords(bodyKeywordsText),
        isBuiltIn = existingPattern?.isBuiltIn ?: false,
        isEnabled = existingPattern?.isEnabled ?: true,
        currencyUnit = currencyUnit,
        incomeKeywords = splitKeywords(incomeKeywordsText),
        outcomeKeywords = splitKeywords(outcomeKeywordsText),
        ignoreKeywords = splitKeywords(ignoreKeywordsText),
        amountMode = amountMode,
        amountLineIndex = Formatters.parseOptionalLong(amountLineText)?.toInt()?.coerceAtLeast(0) ?: 0,
        amountKeywords = splitKeywords(amountKeywordsText),
        amountRegex = amountRegex.trim(),
        balanceMode = balanceMode,
        balanceLineIndex = Formatters.parseOptionalLong(balanceLineText)?.toInt()?.coerceAtLeast(0) ?: 0,
        balanceKeywords = splitKeywords(balanceKeywordsText),
        balanceRegex = balanceRegex.trim(),
        typeLineIndex = Formatters.parseOptionalLong(typeLineText)?.toInt()?.coerceAtLeast(0) ?: 0,
        bankNameLineIndex = Formatters.parseOptionalLong(bankLineText)?.toInt()?.coerceAtLeast(0) ?: 0,
        dateLineIndex = Formatters.parseOptionalLong(dateLineText)?.toInt()?.coerceAtLeast(0) ?: 0,
        timeLineIndex = Formatters.parseOptionalLong(timeLineText)?.toInt()?.coerceAtLeast(0) ?: 0,
        sampleSms = sampleSms.trim()
    )

    val livePreview = remember(
        name, bankDisplayName, senderKeywordsText, bodyKeywordsText, currencyUnit,
        incomeKeywordsText, outcomeKeywordsText, ignoreKeywordsText,
        amountMode, amountLineText, amountKeywordsText, amountRegex,
        balanceMode, balanceLineText, balanceKeywordsText, balanceRegex,
        typeLineText, bankLineText, dateLineText, timeLineText, sampleSms
    ) {
        if (sampleSms.isBlank()) null else onTestPattern(buildDraftPattern(), sampleSms)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) stringResource(R.string.editor_title_edit) else stringResource(R.string.editor_title_new),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.editor_back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = true
                            } else {
                                onSave(buildDraftPattern())
                            }
                        },
                        modifier = Modifier.testTag("editor_save_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = stringResource(R.string.editor_save),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: Identity
                EditorSection(title = stringResource(R.string.editor_section_identity)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = false
                        },
                        label = { Text(stringResource(R.string.editor_pattern_name)) },
                        isError = nameError,
                        supportingText = if (nameError) {
                            { Text(stringResource(R.string.editor_error_name_required)) }
                        } else null,
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_name_input")
                    )
                    OutlinedTextField(
                        value = bankDisplayName,
                        onValueChange = { bankDisplayName = it },
                        label = { Text(stringResource(R.string.editor_bank_display_name)) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = senderKeywordsText,
                        onValueChange = { senderKeywordsText = it },
                        label = { Text(stringResource(R.string.editor_sender_keywords)) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = bodyKeywordsText,
                        onValueChange = { bodyKeywordsText = it },
                        label = { Text(stringResource(R.string.editor_body_keywords)) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = stringResource(R.string.editor_currency_unit),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = currencyUnit == CurrencyUnit.RIAL,
                            onClick = { currencyUnit = CurrencyUnit.RIAL },
                            label = { Text(stringResource(R.string.editor_unit_rial)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = currencyUnit == CurrencyUnit.TOMAN,
                            onClick = { currencyUnit = CurrencyUnit.TOMAN },
                            label = { Text(stringResource(R.string.editor_unit_toman)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Section 2: Classification keywords
                EditorSection(title = stringResource(R.string.editor_section_classification)) {
                    OutlinedTextField(
                        value = incomeKeywordsText,
                        onValueChange = { incomeKeywordsText = it },
                        label = { Text(stringResource(R.string.editor_income_keywords)) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = outcomeKeywordsText,
                        onValueChange = { outcomeKeywordsText = it },
                        label = { Text(stringResource(R.string.editor_outcome_keywords)) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = ignoreKeywordsText,
                        onValueChange = { ignoreKeywordsText = it },
                        label = { Text(stringResource(R.string.editor_ignore_keywords)) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Section 3: Amount Extraction
                EditorSection(title = stringResource(R.string.editor_section_amount)) {
                    ExtractionModeRow(selected = amountMode, onSelect = { amountMode = it })
                    when (amountMode) {
                        ExtractionMode.AUTO_KEYWORD -> {
                            OutlinedTextField(
                                value = amountKeywordsText,
                                onValueChange = { amountKeywordsText = it },
                                label = { Text(stringResource(R.string.editor_keywords_hint)) },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        ExtractionMode.LINE_INDEX -> {
                            OutlinedTextField(
                                value = amountLineText,
                                onValueChange = { amountLineText = it },
                                label = { Text(stringResource(R.string.editor_line_number)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        ExtractionMode.REGEX -> {
                            OutlinedTextField(
                                value = amountRegex,
                                onValueChange = { amountRegex = it },
                                label = { Text(stringResource(R.string.editor_regex_hint)) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Section 4: Balance Extraction
                EditorSection(title = stringResource(R.string.editor_section_balance)) {
                    ExtractionModeRow(selected = balanceMode, onSelect = { balanceMode = it })
                    when (balanceMode) {
                        ExtractionMode.AUTO_KEYWORD -> {
                            OutlinedTextField(
                                value = balanceKeywordsText,
                                onValueChange = { balanceKeywordsText = it },
                                label = { Text(stringResource(R.string.editor_keywords_hint)) },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        ExtractionMode.LINE_INDEX -> {
                            OutlinedTextField(
                                value = balanceLineText,
                                onValueChange = { balanceLineText = it },
                                label = { Text(stringResource(R.string.editor_line_number)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        ExtractionMode.REGEX -> {
                            OutlinedTextField(
                                value = balanceRegex,
                                onValueChange = { balanceRegex = it },
                                label = { Text(stringResource(R.string.editor_regex_hint)) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Section 5: Optional Line Overrides
                EditorSection(title = stringResource(R.string.editor_section_extra)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = typeLineText,
                            onValueChange = { typeLineText = it },
                            label = { Text(stringResource(R.string.editor_type_line)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = bankLineText,
                            onValueChange = { bankLineText = it },
                            label = { Text(stringResource(R.string.editor_bank_line)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = dateLineText,
                            onValueChange = { dateLineText = it },
                            label = { Text(stringResource(R.string.editor_date_line)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = timeLineText,
                            onValueChange = { timeLineText = it },
                            label = { Text(stringResource(R.string.editor_time_line)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Section 6: Live Tester
                EditorSection(title = stringResource(R.string.editor_section_tester)) {
                    OutlinedTextField(
                        value = sampleSms,
                        onValueChange = { sampleSms = it },
                        placeholder = { Text(stringResource(R.string.editor_tester_hint)) },
                        minLines = 4,
                        maxLines = 8,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_sample_sms_input")
                    )

                    livePreview?.let { preview ->
                        LivePreviewResultCard(
                            transaction = preview,
                            persianDigits = persianDigits
                        )
                    }
                }

                Button(
                    onClick = {
                        if (name.isBlank()) {
                            nameError = true
                        } else {
                            onSave(buildDraftPattern())
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("editor_save_button")
                ) {
                    Text(
                        text = stringResource(R.string.editor_save),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun EditorSection(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
private fun ExtractionModeRow(
    selected: ExtractionMode,
    onSelect: (ExtractionMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FilterChip(
            selected = selected == ExtractionMode.AUTO_KEYWORD,
            onClick = { onSelect(ExtractionMode.AUTO_KEYWORD) },
            label = { Text(stringResource(R.string.editor_mode_auto), maxLines = 1) },
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            selected = selected == ExtractionMode.LINE_INDEX,
            onClick = { onSelect(ExtractionMode.LINE_INDEX) },
            label = { Text(stringResource(R.string.editor_mode_line), maxLines = 1) },
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            selected = selected == ExtractionMode.REGEX,
            onClick = { onSelect(ExtractionMode.REGEX) },
            label = { Text(stringResource(R.string.editor_mode_regex), maxLines = 1) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LivePreviewResultCard(
    transaction: BankTransaction,
    persianDigits: Boolean
) {
    val financial = LocalFinancialColors.current
    val kindColor = when (transaction.kind) {
        TransactionKind.INCOME -> financial.income
        TransactionKind.OUTCOME -> financial.outcome
        TransactionKind.OTHER -> financial.other
    }
    val kindLabel = when (transaction.kind) {
        TransactionKind.INCOME -> "واریز (دریافتی)"
        TransactionKind.OUTCOME -> "برداشت (خرجی)"
        TransactionKind.OTHER -> "سایر / غیرتراکنشی"
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("editor_live_preview_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.editor_tester_result),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            PreviewRow("نوع تراکنش", kindLabel, kindColor)
            PreviewRow("نام بانک", transaction.bankName)
            PreviewRow("عنوان عملیات", transaction.type)
            PreviewRow(
                "مبلغ استخراج‌شده",
                Formatters.formatToman(transaction.amountToman, persianDigits),
                kindColor
            )
            PreviewRow(
                "موجودی استخراج‌شده",
                transaction.balanceToman?.let { Formatters.formatToman(it, persianDigits) } ?: "یافت نشد"
            )
            PreviewRow("تاریخ و ساعت", transaction.formattedDateTime(persianDigits))
        }
    }
}

@Composable
private fun PreviewRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}
