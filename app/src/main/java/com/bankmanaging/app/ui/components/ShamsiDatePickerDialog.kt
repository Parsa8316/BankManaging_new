package com.bankmanaging.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.bankmanaging.app.R
import com.bankmanaging.app.model.PersianDate
import com.bankmanaging.app.ui.util.Formatters

@Composable
fun ShamsiDatePickerDialog(
    initialDate: PersianDate?,
    persianDigits: Boolean,
    onDismiss: () -> Unit,
    onClear: () -> Unit,
    onDateSelected: (PersianDate) -> Unit
) {
    val today = remember { PersianDate.today() }
    val seed = initialDate ?: today

    var displayYear by remember { mutableIntStateOf(seed.year) }
    var displayMonth by remember { mutableIntStateOf(seed.month) }
    var selectedDate by remember { mutableStateOf(seed) }

    val daysInMonth = remember(displayYear, displayMonth) {
        PersianDate.daysInMonth(displayYear, displayMonth)
    }
    val startOffset = remember(displayYear, displayMonth) {
        PersianDate.firstDayOfMonthOffset(displayYear, displayMonth)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.datepicker_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = selectedDate.formatWithMonthName(persianDigits),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(16.dp))

                // Month & Year header with navigation arrows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (displayMonth == 1) {
                                displayMonth = 12
                                displayYear -= 1
                            } else {
                                displayMonth -= 1
                            }
                        },
                        modifier = Modifier.testTag("datepicker_prev_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = "ماه قبل"
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = PersianDate.MONTH_NAMES.getOrElse(displayMonth - 1) { "" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = Formatters.formatDigits(displayYear.toString(), persianDigits),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = {
                            if (displayMonth == 12) {
                                displayMonth = 1
                                displayYear += 1
                            } else {
                                displayMonth += 1
                            }
                        },
                        modifier = Modifier.testTag("datepicker_next_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = "ماه بعد"
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Weekday headers
                Row(modifier = Modifier.fillMaxWidth()) {
                    PersianDate.WEEKDAY_SHORT_NAMES.forEach { dayName ->
                        Text(
                            text = dayName,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Calendar grid (up to 6 rows of 7 days)
                val totalCells = startOffset + daysInMonth
                val rows = (totalCells + 6) / 7
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (r in 0 until rows) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (c in 0 until 7) {
                                val cellIndex = r * 7 + c
                                val dayNum = cellIndex - startOffset + 1
                                if (dayNum in 1..daysInMonth) {
                                    val cellDate = PersianDate(displayYear, displayMonth, dayNum)
                                    val isSelected = cellDate == selectedDate
                                    val isToday = cellDate == today
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isSelected -> MaterialTheme.colorScheme.primary
                                                    isToday -> MaterialTheme.colorScheme.primaryContainer
                                                    else -> MaterialTheme.colorScheme.surface
                                                }
                                            )
                                            .clickable { selectedDate = cellDate },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = Formatters.formatDigits(dayNum.toString(), persianDigits),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                                isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    }
                                } else {
                                    Spacer(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        TextButton(
                            onClick = {
                                displayYear = today.year
                                displayMonth = today.month
                                selectedDate = today
                            },
                            modifier = Modifier.testTag("datepicker_today_btn")
                        ) {
                            Text(stringResource(R.string.datepicker_today))
                        }
                        if (initialDate != null) {
                            TextButton(
                                onClick = {
                                    onClear()
                                    onDismiss()
                                },
                                modifier = Modifier.testTag("datepicker_clear_btn")
                            ) {
                                Text(
                                    text = stringResource(R.string.datepicker_clear),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Row {
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.datepicker_cancel))
                        }
                        TextButton(
                            onClick = {
                                onDateSelected(selectedDate)
                                onDismiss()
                            },
                            modifier = Modifier.testTag("datepicker_confirm_btn")
                        ) {
                            Text(
                                text = stringResource(R.string.datepicker_confirm),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
