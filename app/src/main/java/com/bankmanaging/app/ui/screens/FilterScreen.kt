package com.bankmanaging.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bankmanaging.app.R
import com.bankmanaging.app.model.PersianDate
import com.bankmanaging.app.model.TransactionFilter
import com.bankmanaging.app.ui.components.ShamsiDatePickerDialog
import com.bankmanaging.app.ui.util.Formatters
import com.bankmanaging.app.viewmodel.BankUiState

private val ClassicBluTypes = listOf(
    "آن باکس", "واریز پول", "سود شما",
    "برداشت پول", "این باکس", "پرداخت قبض",
    "شارژ شدی", "آنلاین شدی"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterScreen(
    uiState: BankUiState,
    onApplyFilter: (TransactionFilter) -> Unit,
    onResetFilter: () -> Unit,
    onShowError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val initial = uiState.filter
    var includeIncome by remember(initial) { mutableStateOf(initial.includeIncome) }
    var includeOutcome by remember(initial) { mutableStateOf(initial.includeOutcome) }
    var includeOther by remember(initial) { mutableStateOf(initial.includeOther) }
    var selectedTypes by remember(initial) { mutableStateOf(initial.selectedTypes) }
    var selectedBanks by remember(initial) { mutableStateOf(initial.selectedBanks) }
    var minAmountText by remember(initial) { mutableStateOf(initial.minAmount?.toString().orEmpty()) }
    var maxAmountText by remember(initial) { mutableStateOf(initial.maxAmount?.toString().orEmpty()) }
    var minBalanceText by remember(initial) { mutableStateOf(initial.minBalance?.toString().orEmpty()) }
    var maxBalanceText by remember(initial) { mutableStateOf(initial.maxBalance?.toString().orEmpty()) }
    var fromDate by remember(initial) { mutableStateOf(initial.fromDate) }
    var toDate by remember(initial) { mutableStateOf(initial.toDate) }
    var descriptionQuery by remember(initial) { mutableStateOf(initial.searchQuery) }

    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }

    val allTypeChoices = remember(uiState.availableTypes) {
        (uiState.availableTypes + ClassicBluTypes).distinct()
    }
    val rangeErrorText = stringResource(R.string.filter_invalid_range)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.filter_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            // 1. Transaction Kind
            FilterSectionCard(title = stringResource(R.string.filter_kind_section)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    KindChip(
                        label = stringResource(R.string.filter_kind_income),
                        selected = includeIncome,
                        onClick = { includeIncome = !includeIncome },
                        tag = "filter_kind_income"
                    )
                    KindChip(
                        label = stringResource(R.string.filter_kind_outcome),
                        selected = includeOutcome,
                        onClick = { includeOutcome = !includeOutcome },
                        tag = "filter_kind_outcome"
                    )
                    KindChip(
                        label = stringResource(R.string.filter_kind_other),
                        selected = includeOther,
                        onClick = { includeOther = !includeOther },
                        tag = "filter_kind_other"
                    )
                }
            }

            // 2. Message Types
            FilterSectionCard(
                title = stringResource(R.string.filter_types_section),
                subtitle = if (selectedTypes.isEmpty()) "همه موارد" else "${Formatters.formatDigits(selectedTypes.size.toString(), uiState.usePersianDigits)} مورد انتخاب شده"
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    allTypeChoices.forEach { typeName ->
                        val isSelected = typeName in selectedTypes
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedTypes = if (isSelected) selectedTypes - typeName else selectedTypes + typeName
                            },
                            label = { Text(typeName) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            }

            // 3. Banks
            if (uiState.availableBanks.isNotEmpty()) {
                FilterSectionCard(
                    title = stringResource(R.string.filter_banks_section),
                    subtitle = if (selectedBanks.isEmpty()) "همه بانک‌ها" else "${Formatters.formatDigits(selectedBanks.size.toString(), uiState.usePersianDigits)} بانک"
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        uiState.availableBanks.forEach { bank ->
                            val isSelected = bank in selectedBanks
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedBanks = if (isSelected) selectedBanks - bank else selectedBanks + bank
                                },
                                label = { Text(bank) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }

            // 4. Amount Range
            FilterSectionCard(title = stringResource(R.string.filter_amount_section)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = minAmountText,
                        onValueChange = { minAmountText = it },
                        label = { Text(stringResource(R.string.filter_min_amount)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("filter_min_amount_input")
                    )
                    OutlinedTextField(
                        value = maxAmountText,
                        onValueChange = { maxAmountText = it },
                        label = { Text(stringResource(R.string.filter_max_amount)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("filter_max_amount_input")
                    )
                }
            }

            // 5. Balance Range
            FilterSectionCard(title = stringResource(R.string.filter_balance_section)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = minBalanceText,
                        onValueChange = { minBalanceText = it },
                        label = { Text(stringResource(R.string.filter_min_balance)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("filter_min_balance_input")
                    )
                    OutlinedTextField(
                        value = maxBalanceText,
                        onValueChange = { maxBalanceText = it },
                        label = { Text(stringResource(R.string.filter_max_balance)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("filter_max_balance_input")
                    )
                }
            }

            // 6. Shamsi Date Range
            FilterSectionCard(title = stringResource(R.string.filter_date_section)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DateSelectorTile(
                        label = stringResource(R.string.filter_from_date),
                        date = fromDate,
                        persianDigits = uiState.usePersianDigits,
                        onClick = { showFromDatePicker = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("filter_from_date_button")
                    )
                    DateSelectorTile(
                        label = stringResource(R.string.filter_to_date),
                        date = toDate,
                        persianDigits = uiState.usePersianDigits,
                        onClick = { showToDatePicker = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("filter_to_date_button")
                    )
                }
            }

            // 7. Description search
            FilterSectionCard(title = stringResource(R.string.filter_description_section)) {
                OutlinedTextField(
                    value = descriptionQuery,
                    onValueChange = { descriptionQuery = it },
                    placeholder = { Text(stringResource(R.string.filter_description_hint)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("filter_description_input")
                )
            }

            // Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        includeIncome = true
                        includeOutcome = true
                        includeOther = true
                        selectedTypes = emptySet()
                        selectedBanks = emptySet()
                        minAmountText = ""
                        maxAmountText = ""
                        minBalanceText = ""
                        maxBalanceText = ""
                        fromDate = null
                        toDate = null
                        descriptionQuery = ""
                        onResetFilter()
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("filter_reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.filter_reset))
                }

                Button(
                    onClick = {
                        val minAmt = Formatters.parseOptionalLong(minAmountText)
                        val maxAmt = Formatters.parseOptionalLong(maxAmountText)
                        val minBal = Formatters.parseOptionalLong(minBalanceText)
                        val maxBal = Formatters.parseOptionalLong(maxBalanceText)

                        if ((minAmt != null && maxAmt != null && minAmt > maxAmt) ||
                            (minBal != null && maxBal != null && minBal > maxBal) ||
                            (fromDate != null && toDate != null && fromDate!! > toDate!!)
                        ) {
                            onShowError(rangeErrorText)
                            return@Button
                        }

                        onApplyFilter(
                            TransactionFilter(
                                includeIncome = includeIncome,
                                includeOutcome = includeOutcome,
                                includeOther = includeOther,
                                selectedTypes = selectedTypes,
                                selectedBanks = selectedBanks,
                                minAmount = minAmt,
                                maxAmount = maxAmt,
                                minBalance = minBal,
                                maxBalance = maxBal,
                                fromDate = fromDate,
                                toDate = toDate,
                                searchQuery = descriptionQuery
                            )
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(52.dp)
                        .testTag("filter_apply_button")
                ) {
                    Text(
                        text = stringResource(R.string.filter_apply),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showFromDatePicker) {
        ShamsiDatePickerDialog(
            initialDate = fromDate,
            persianDigits = uiState.usePersianDigits,
            onDismiss = { showFromDatePicker = false },
            onClear = { fromDate = null },
            onDateSelected = { fromDate = it }
        )
    }

    if (showToDatePicker) {
        ShamsiDatePickerDialog(
            initialDate = toDate,
            persianDigits = uiState.usePersianDigits,
            onDismiss = { showToDatePicker = false },
            onClear = { toDate = null },
            onDateSelected = { toDate = it }
        )
    }
}

@Composable
private fun FilterSectionCard(
    title: String,
    subtitle: String? = null,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun KindChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) {
            { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
        } else null,
        modifier = Modifier.testTag(tag)
    )
}

@Composable
private fun DateSelectorTile(
    label: String,
    date: PersianDate?,
    persianDigits: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = date?.format("/", persianDigits) ?: stringResource(R.string.filter_select_date),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (date != null) FontWeight.Bold else FontWeight.Normal,
                    color = if (date != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
