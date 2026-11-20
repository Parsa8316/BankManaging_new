package com.bankmanaging.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bankmanaging.app.R
import com.bankmanaging.app.model.BankTransaction
import com.bankmanaging.app.model.DefaultSmsPatterns
import com.bankmanaging.app.model.TransactionKind
import com.bankmanaging.app.ui.components.TransactionDetailSheet
import com.bankmanaging.app.ui.theme.LocalFinancialColors
import com.bankmanaging.app.ui.util.Formatters
import com.bankmanaging.app.viewmodel.BankUiState

@Composable
fun DashboardScreen(
    uiState: BankUiState,
    onRequestSmsPermission: () -> Unit,
    onRefresh: () -> Unit,
    onSearchChange: (String) -> Unit,
    onClearFilters: () -> Unit,
    onOpenFilter: () -> Unit,
    onOpenSettings: () -> Unit,
    onAnalyzeSingleSms: (String, String) -> BankTransaction,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTransaction by remember { mutableStateOf<BankTransaction?>(null) }
    var showQuickAnalyzer by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "header") {
                DashboardTopHeader(
                    transactionCount = uiState.filteredTransactions.size,
                    persianDigits = uiState.usePersianDigits,
                    onAnalyzeClick = { showQuickAnalyzer = true },
                    onRefreshClick = onRefresh
                )
            }

            if (!uiState.hasSmsPermission) {
                item(key = "permission_banner") {
                    PermissionRequestCard(onGrantClick = onRequestSmsPermission)
                }
            }

            item(key = "hero_summary") {
                FinancialHeroCard(
                    totalIncome = uiState.totalIncome,
                    totalOutcome = uiState.totalOutcome,
                    netFlow = uiState.netFlow,
                    latestBalance = uiState.latestBalance,
                    persianDigits = uiState.usePersianDigits
                )
            }

            item(key = "search_bar") {
                SearchAndFilterBar(
                    searchQuery = uiState.filter.searchQuery,
                    activeFilterCount = uiState.filter.activeCount,
                    persianDigits = uiState.usePersianDigits,
                    onSearchChange = onSearchChange,
                    onOpenFilter = onOpenFilter,
                    onClearFilters = onClearFilters
                )
            }

            if (uiState.isLoading) {
                item(key = "loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.filteredTransactions.isEmpty()) {
                item(key = "empty") {
                    EmptyTransactionsCard(
                        hasSenders = uiState.bankSenders.isNotEmpty(),
                        hasActiveFilter = uiState.filter.isActive,
                        onOpenSettings = onOpenSettings,
                        onClearFilters = onClearFilters,
                        onOpenAnalyzer = { showQuickAnalyzer = true }
                    )
                }
            } else {
                items(
                    items = uiState.filteredTransactions,
                    key = { "${it.id}_${it.timestampMillis}" }
                ) { tx ->
                    TransactionItemCard(
                        transaction = tx,
                        persianDigits = uiState.usePersianDigits,
                        onClick = { selectedTransaction = tx }
                    )
                }
            }

            item(key = "bottom_spacer") {
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    selectedTransaction?.let { tx ->
        val copiedMsg = stringResource(R.string.detail_copied_toast)
        TransactionDetailSheet(
            transaction = tx,
            persianDigits = uiState.usePersianDigits,
            onDismiss = { selectedTransaction = null },
            onCopied = { onShowMessage(copiedMsg) }
        )
    }

    if (showQuickAnalyzer) {
        QuickSmsAnalyzerSheet(
            persianDigits = uiState.usePersianDigits,
            onAnalyze = onAnalyzeSingleSms,
            onDismiss = { showQuickAnalyzer = false }
        )
    }
}

@Composable
private fun DashboardTopHeader(
    transactionCount: Int,
    persianDigits: Boolean,
    onAnalyzeClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(
                    R.string.dashboard_transaction_count,
                    Formatters.formatNumber(transactionCount.toLong(), persianDigits)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(
                onClick = onAnalyzeClick,
                modifier = Modifier.testTag("dashboard_quick_analyzer_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Science,
                    contentDescription = stringResource(R.string.dashboard_test_parser),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(
                onClick = onRefreshClick,
                modifier = Modifier.testTag("dashboard_refresh_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = stringResource(R.string.dashboard_refresh),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun FinancialHeroCard(
    totalIncome: Long,
    totalOutcome: Long,
    netFlow: Long,
    latestBalance: Long?,
    persianDigits: Boolean
) {
    val financial = LocalFinancialColors.current
    val totalFlow = (totalIncome + totalOutcome).coerceAtLeast(1L)
    val incomeRatio by animateFloatAsState(
        targetValue = if (totalIncome + totalOutcome == 0L) 0.5f else totalIncome.toFloat() / totalFlow.toFloat(),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "incomeRatio"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(financial.heroStart, financial.heroEnd)
                )
            )
            .drawBehind {
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = size.width * 0.42f,
                    center = Offset(size.width * 0.12f, size.height * 0.05f)
                )
                drawCircle(
                    color = Color(0xFF38BDF8).copy(alpha = 0.12f),
                    radius = size.width * 0.32f,
                    center = Offset(size.width * 0.92f, size.height * 0.95f)
                )
            }
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.dashboard_net_balance),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.78f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = Formatters.formatToman(netFlow, persianDigits, showSign = true),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (latestBalance != null) {
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = stringResource(R.string.dashboard_latest_balance),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = Formatters.formatToman(latestBalance, persianDigits),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Ratio progress bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f))
            ) {
                Box(
                    modifier = Modifier
                        .weight(incomeRatio.coerceIn(0.05f, 0.95f))
                        .fillMaxSize()
                        .background(Color(0xFF34D399))
                )
                Box(
                    modifier = Modifier
                        .weight((1f - incomeRatio).coerceIn(0.05f, 0.95f))
                        .fillMaxSize()
                        .background(Color(0xFFFB7185))
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HeroStatChip(
                    label = stringResource(R.string.dashboard_total_income),
                    amount = Formatters.formatToman(totalIncome, persianDigits),
                    dotColor = Color(0xFF34D399),
                    modifier = Modifier.weight(1f)
                )
                HeroStatChip(
                    label = stringResource(R.string.dashboard_total_outcome),
                    amount = Formatters.formatToman(totalOutcome, persianDigits),
                    dotColor = Color(0xFFFB7185),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HeroStatChip(
    label: String,
    amount: String,
    dotColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.12f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PermissionRequestCard(onGrantClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Sms,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.permission_sms_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = stringResource(R.string.permission_sms_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
            )
            Button(
                onClick = onGrantClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("grant_sms_permission_button")
            ) {
                Text(stringResource(R.string.permission_sms_grant))
            }
        }
    }
}

@Composable
private fun SearchAndFilterBar(
    searchQuery: String,
    activeFilterCount: Int,
    persianDigits: Boolean,
    onSearchChange: (String) -> Unit,
    onOpenFilter: () -> Unit,
    onClearFilters: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(
                        text = stringResource(R.string.dashboard_search_hint),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "پاک کردن جستجو"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("dashboard_search_input")
            )

            FilledTonalButton(
                onClick = onOpenFilter,
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 16.dp),
                modifier = Modifier.testTag("dashboard_filter_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.FilterList,
                    contentDescription = stringResource(R.string.nav_filter),
                    modifier = Modifier.size(20.dp)
                )
                if (activeFilterCount > 0) {
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = Formatters.formatDigits(activeFilterCount.toString(), persianDigits),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        AnimatedVisibility(visible = activeFilterCount > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${Formatters.formatDigits(activeFilterCount.toString(), persianDigits)} ${stringResource(R.string.dashboard_active_filters)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(
                    onClick = onClearFilters,
                    modifier = Modifier.testTag("dashboard_clear_filters_button")
                ) {
                    Text(stringResource(R.string.dashboard_clear_filters))
                }
            }
        }
    }
}

@Composable
private fun TransactionItemCard(
    transaction: BankTransaction,
    persianDigits: Boolean,
    onClick: () -> Unit
) {
    val financial = LocalFinancialColors.current
    val (accentColor, containerColor, icon) = when (transaction.kind) {
        TransactionKind.INCOME -> Triple(financial.income, financial.incomeContainer, Icons.Rounded.ArrowDownward)
        TransactionKind.OUTCOME -> Triple(financial.outcome, financial.outcomeContainer, Icons.Rounded.ArrowUpward)
        TransactionKind.OTHER -> Triple(financial.other, financial.otherContainer, Icons.Rounded.Info)
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("transaction_card_${transaction.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = transaction.type,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = transaction.type,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = transaction.bankName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = transaction.formattedDateTime(persianDigits),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = Formatters.formatToman(
                        amount = if (transaction.kind == TransactionKind.OUTCOME) -transaction.amountToman else transaction.amountToman,
                        persianDigits = persianDigits,
                        showSign = transaction.kind != TransactionKind.OTHER
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
                if (transaction.balanceToman != null) {
                    Text(
                        text = "مانده: ${Formatters.formatNumber(transaction.balanceToman, persianDigits)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTransactionsCard(
    hasSenders: Boolean,
    hasActiveFilter: Boolean,
    onOpenSettings: () -> Unit,
    onClearFilters: () -> Unit,
    onOpenAnalyzer: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AccountBalance,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = stringResource(R.string.dashboard_empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = if (!hasSenders) {
                    stringResource(R.string.dashboard_empty_no_senders)
                } else {
                    stringResource(R.string.dashboard_empty_no_matches)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(4.dp))

            if (hasActiveFilter) {
                Button(
                    onClick = onClearFilters,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.dashboard_clear_filters))
                }
            }

            Button(
                onClick = onOpenSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("empty_go_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.dashboard_go_to_settings))
            }

            OutlinedButton(
                onClick = onOpenAnalyzer,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("empty_quick_analyzer_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Science,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.dashboard_test_parser))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickSmsAnalyzerSheet(
    persianDigits: Boolean,
    onAnalyze: (String, String) -> BankTransaction,
    onDismiss: () -> Unit
) {
    var smsText by remember { mutableStateOf(DefaultSmsPatterns.bluBank.sampleSms) }
    var senderText by remember { mutableStateOf("BluBank") }
    val parsed = remember(smsText, senderText) {
        if (smsText.isBlank()) null else onAnalyze(smsText, senderText)
    }
    val financial = LocalFinancialColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.dashboard_test_parser),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "متن هر پیامک بانکی را وارد کنید تا نحوه تشخیص آن توسط الگوهای فعال را بررسی کنید.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = senderText,
                onValueChange = { senderText = it },
                label = { Text("نام یا شماره فرستنده (اختیاری)") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = smsText,
                onValueChange = { smsText = it },
                label = { Text("متن پیامک بانکی") },
                minLines = 4,
                maxLines = 7,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_analyzer_sms_input")
            )

            parsed?.let { tx ->
                val kindColor = when (tx.kind) {
                    TransactionKind.INCOME -> financial.income
                    TransactionKind.OUTCOME -> financial.outcome
                    TransactionKind.OTHER -> financial.other
                }
                val kindLabel = when (tx.kind) {
                    TransactionKind.INCOME -> "واریز (دریافتی)"
                    TransactionKind.OUTCOME -> "برداشت (خرجی)"
                    TransactionKind.OTHER -> "سایر / اطلاع‌رسانی"
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("وضعیت تشخیص:", style = MaterialTheme.typography.bodySmall)
                            Text(kindLabel, color = kindColor, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("بانک / الگو:", style = MaterialTheme.typography.bodySmall)
                            Text("${tx.bankName} (${tx.matchedPatternName})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("نوع عملیات:", style = MaterialTheme.typography.bodySmall)
                            Text(tx.type, fontWeight = FontWeight.SemiBold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("مبلغ:", style = MaterialTheme.typography.bodySmall)
                            Text(Formatters.formatToman(tx.amountToman, persianDigits), color = kindColor, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("موجودی:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                tx.balanceToman?.let { Formatters.formatToman(it, persianDigits) } ?: "تشخیص داده نشد",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("تاریخ و ساعت:", style = MaterialTheme.typography.bodySmall)
                            Text(tx.formattedDateTime(persianDigits))
                        }
                    }
                }
            }

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.detail_close))
            }
        }
    }
}
