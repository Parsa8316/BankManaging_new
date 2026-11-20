package com.bankmanaging.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bankmanaging.app.R
import com.bankmanaging.app.data.ThemeMode
import com.bankmanaging.app.model.CurrencyUnit
import com.bankmanaging.app.model.ExtractionMode
import com.bankmanaging.app.model.SmsPattern
import com.bankmanaging.app.viewmodel.BankUiState

@Composable
fun SettingsScreen(
    uiState: BankUiState,
    onAddSender: (String) -> String?,
    onDeleteSender: (String) -> Unit,
    onTogglePattern: (String, Boolean) -> Unit,
    onAddNewPattern: () -> Unit,
    onEditPattern: (String) -> Unit,
    onDuplicatePattern: (SmsPattern) -> Unit,
    onDeletePattern: (String) -> Unit,
    onResetDefaultPatterns: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onPersianDigitsChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var senderInput by remember { mutableStateOf("") }
    var senderError by remember { mutableStateOf<String?>(null) }
    var senderPendingDelete by remember { mutableStateOf<String?>(null) }
    var patternPendingDelete by remember { mutableStateOf<SmsPattern?>(null) }

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
            item(key = "title") {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item(key = "segmented_tabs") {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        modifier = Modifier.testTag("settings_tab_senders")
                    ) {
                        Text(stringResource(R.string.settings_tab_senders))
                    }
                    SegmentedButton(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        modifier = Modifier.testTag("settings_tab_patterns")
                    ) {
                        Text(stringResource(R.string.settings_tab_patterns))
                    }
                }
            }

            if (selectedTab == 0) {
                item(key = "add_sender_card") {
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
                            OutlinedTextField(
                                value = senderInput,
                                onValueChange = {
                                    senderInput = it
                                    senderError = null
                                },
                                label = { Text(stringResource(R.string.settings_sender_hint)) },
                                isError = senderError != null,
                                supportingText = senderError?.let { err -> { Text(err) } },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("settings_sender_input")
                            )
                            Button(
                                onClick = {
                                    val error = onAddSender(senderInput)
                                    if (error == null) {
                                        senderInput = ""
                                    } else {
                                        senderError = error
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("settings_add_sender_button")
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(stringResource(R.string.settings_add_sender), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (uiState.bankSenders.isEmpty()) {
                    item(key = "empty_senders") {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.settings_senders_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(18.dp)
                            )
                        }
                    }
                } else {
                    items(uiState.bankSenders, key = { "sender_$it" }) { sender ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Sms,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = sender,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { senderPendingDelete = sender },
                                    modifier = Modifier.testTag("delete_sender_$sender")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = stringResource(R.string.patterns_delete),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                item(key = "appearance_card") {
                    AppearanceSettingsCard(
                        themeMode = uiState.themeMode,
                        usePersianDigits = uiState.usePersianDigits,
                        onThemeModeChange = onThemeModeChange,
                        onPersianDigitsChange = onPersianDigitsChange
                    )
                }
            } else {
                // Custom SMS Patterns Tab
                item(key = "patterns_intro") {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoFixHigh,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = stringResource(R.string.settings_tab_patterns),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Text(
                                text = stringResource(R.string.patterns_intro),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onAddNewPattern,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("settings_add_pattern_button")
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(stringResource(R.string.patterns_add_new), fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = onResetDefaultPatterns,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.testTag("settings_reset_patterns_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.RestartAlt,
                                        contentDescription = stringResource(R.string.patterns_reset_defaults),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                items(uiState.smsPatterns, key = { it.id }) { pattern ->
                    SmsPatternCard(
                        pattern = pattern,
                        onToggle = { enabled -> onTogglePattern(pattern.id, enabled) },
                        onEdit = { onEditPattern(pattern.id) },
                        onDuplicate = { onDuplicatePattern(pattern) },
                        onDelete = { patternPendingDelete = pattern }
                    )
                }
            }

            item(key = "bottom_space") {
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    senderPendingDelete?.let { sender ->
        AlertDialog(
            onDismissRequest = { senderPendingDelete = null },
            title = { Text(stringResource(R.string.settings_delete_sender_title)) },
            text = { Text(stringResource(R.string.settings_delete_sender_confirm, sender)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSender(sender)
                        senderPendingDelete = null
                    }
                ) {
                    Text(
                        text = stringResource(R.string.settings_confirm_yes),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { senderPendingDelete = null }) {
                    Text(stringResource(R.string.settings_confirm_no))
                }
            }
        )
    }

    patternPendingDelete?.let { pattern ->
        AlertDialog(
            onDismissRequest = { patternPendingDelete = null },
            title = { Text(stringResource(R.string.patterns_delete_confirm_title)) },
            text = { Text(stringResource(R.string.patterns_delete_confirm_msg, pattern.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePattern(pattern.id)
                        patternPendingDelete = null
                    }
                ) {
                    Text(
                        text = stringResource(R.string.settings_confirm_yes),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { patternPendingDelete = null }) {
                    Text(stringResource(R.string.settings_confirm_no))
                }
            }
        )
    }
}

@Composable
private fun SmsPatternCard(
    pattern: SmsPattern,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pattern_card_${pattern.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = pattern.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (pattern.isBuiltIn) {
                                MaterialTheme.colorScheme.surfaceVariant
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            }
                        ) {
                            Text(
                                text = if (pattern.isBuiltIn) {
                                    stringResource(R.string.patterns_builtin_badge)
                                } else {
                                    stringResource(R.string.patterns_custom_badge)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (pattern.isBuiltIn) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    val unitText = if (pattern.currencyUnit == CurrencyUnit.RIAL) "ریال ← تومان" else "تومان"
                    val amountModeText = when (pattern.amountMode) {
                        ExtractionMode.AUTO_KEYWORD -> "مبلغ هوشمند"
                        ExtractionMode.LINE_INDEX -> "مبلغ از خط ${pattern.amountLineIndex}"
                        ExtractionMode.REGEX -> "مبلغ با Regex"
                    }
                    Text(
                        text = "$unitText  •  $amountModeText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = pattern.isEnabled,
                    onCheckedChange = onToggle,
                    modifier = Modifier.testTag("pattern_switch_${pattern.id}")
                )
            }

            if (pattern.senderKeywords.isNotEmpty()) {
                Text(
                    text = "فرستنده‌ها: ${pattern.senderKeywords.joinToString("، ")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("pattern_edit_${pattern.id}")
                ) {
                    Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.patterns_edit))
                }

                OutlinedButton(
                    onClick = onDuplicate,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("pattern_duplicate_${pattern.id}")
                ) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.patterns_duplicate))
                }

                if (!pattern.isBuiltIn) {
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("pattern_delete_${pattern.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = stringResource(R.string.patterns_delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppearanceSettingsCard(
    themeMode: ThemeMode,
    usePersianDigits: Boolean,
    onThemeModeChange: (ThemeMode) -> Unit,
    onPersianDigitsChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_appearance),
                style = MaterialTheme.typography.titleSmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = themeMode == ThemeMode.SYSTEM,
                    onClick = { onThemeModeChange(ThemeMode.SYSTEM) },
                    label = { Text(stringResource(R.string.settings_theme_system)) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = themeMode == ThemeMode.LIGHT,
                    onClick = { onThemeModeChange(ThemeMode.LIGHT) },
                    label = { Text(stringResource(R.string.settings_theme_light)) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = themeMode == ThemeMode.DARK,
                    onClick = { onThemeModeChange(ThemeMode.DARK) },
                    label = { Text(stringResource(R.string.settings_theme_dark)) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.settings_persian_digits),
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = usePersianDigits,
                    onCheckedChange = onPersianDigitsChange,
                    modifier = Modifier.testTag("settings_persian_digits_switch")
                )
            }
        }
    }
}
