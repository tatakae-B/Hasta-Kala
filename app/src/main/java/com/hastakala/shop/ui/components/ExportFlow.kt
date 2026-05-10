package com.hastakala.shop.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hastakala.shop.R
import com.hastakala.shop.utils.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExportBottomSheet(
    onDismiss: () -> Unit,
    onExportRequested: (ExportType, ExportFormat, ExportTimeRange, Long, Long) -> Unit
) {
    var selectedType by remember { mutableStateOf(ExportType.SALES) }
    var selectedFormat by remember { mutableStateOf(ExportFormat.PDF) }
    var selectedRange by remember { mutableStateOf(ExportTimeRange.THIS_MONTH) }
    
    // Custom Date Range State
    var customStartDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var customEndDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val dateRangePickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val start = dateRangePickerState.selectedStartDateMillis ?: System.currentTimeMillis()
                        val end = dateRangePickerState.selectedEndDateMillis ?: start
                        customStartDate = start
                        customEndDate = end
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDatePicker = false 
                    if (customStartDate == 0L) {
                        selectedRange = ExportTimeRange.THIS_MONTH
                    }
                }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = { Text(stringResource(R.string.label_select_range), modifier = Modifier.padding(16.dp)) },
                modifier = Modifier.fillMaxWidth().height(500.dp)
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = stringResource(R.string.title_export_data),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Data Categories
            ExportSection(title = stringResource(R.string.label_select_data)) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExportType.entries.forEach { type ->
                        val labelId = when(type) {
                            ExportType.SALES -> R.string.export_type_sales
                            ExportType.ORDERS -> R.string.export_type_orders
                            ExportType.REVENUE -> R.string.export_type_revenue
                            ExportType.INVENTORY -> R.string.export_type_inventory
                            ExportType.CUSTOMERS -> R.string.export_type_customers
                            ExportType.EXPENSES -> R.string.export_type_expenses
                            ExportType.ARTISAN -> R.string.export_type_artisan
                        }
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(stringResource(labelId)) },
                            leadingIcon = if (selectedType == type) {
                                { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Time Range
            ExportSection(title = stringResource(R.string.label_select_range)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ExportTimeRange.entries) { range ->
                        val labelId = when(range) {
                            ExportTimeRange.TODAY -> R.string.filter_today
                            ExportTimeRange.YESTERDAY -> R.string.filter_yesterday
                            ExportTimeRange.THIS_WEEK -> R.string.filter_week
                            ExportTimeRange.THIS_MONTH -> R.string.filter_month
                            ExportTimeRange.LAST_MONTH -> R.string.filter_last_month
                            ExportTimeRange.THIS_YEAR -> R.string.filter_this_year
                            ExportTimeRange.CUSTOM -> R.string.filter_custom
                        }
                        FilterChip(
                            selected = selectedRange == range,
                            onClick = { 
                                selectedRange = range
                                if (range == ExportTimeRange.CUSTOM) {
                                    showDatePicker = true
                                }
                            },
                            label = { Text(stringResource(labelId)) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Format Selection
            ExportSection(title = stringResource(R.string.label_select_format)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FormatCard(
                        title = "PDF",
                        subtitle = "Professional Report",
                        icon = Icons.Default.PictureAsPdf,
                        selected = selectedFormat == ExportFormat.PDF,
                        onClick = { selectedFormat = ExportFormat.PDF },
                        modifier = Modifier.weight(1f)
                    )
                    FormatCard(
                        title = "CSV",
                        subtitle = "Spreadsheet Data",
                        icon = Icons.Default.Description,
                        selected = selectedFormat == ExportFormat.CSV,
                        onClick = { selectedFormat = ExportFormat.CSV },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val (start, end) = getRangeTimestamps(selectedRange, customStartDate, customEndDate)
                    onExportRequested(selectedType, selectedFormat, selectedRange, start, end)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(Icons.Default.FileDownload, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.btn_generate_export), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun ExportSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        content()
    }
}

@Composable
fun FormatCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, 
                    null, 
                    tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun getRangeTimestamps(range: ExportTimeRange, customStart: Long, customEnd: Long): Pair<Long, Long> {
    val cal = Calendar.getInstance()
    val now = cal.timeInMillis
    
    // Helper to clear time
    fun Calendar.clearTime() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    return when(range) {
        ExportTimeRange.TODAY -> {
            cal.clearTime()
            cal.timeInMillis to now
        }
        ExportTimeRange.YESTERDAY -> {
            cal.add(Calendar.DAY_OF_YEAR, -1)
            cal.clearTime()
            val start = cal.timeInMillis
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            start to cal.timeInMillis
        }
        ExportTimeRange.THIS_WEEK -> {
            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            cal.clearTime()
            cal.timeInMillis to now
        }
        ExportTimeRange.THIS_MONTH -> {
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.clearTime()
            cal.timeInMillis to now
        }
        ExportTimeRange.LAST_MONTH -> {
            cal.add(Calendar.MONTH, -1)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.clearTime()
            val start = cal.timeInMillis
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            cal.set(Calendar.HOUR_OF_DAY, 23)
            start to cal.timeInMillis
        }
        ExportTimeRange.THIS_YEAR -> {
            cal.set(Calendar.DAY_OF_YEAR, 1)
            cal.clearTime()
            cal.timeInMillis to now
        }
        ExportTimeRange.CUSTOM -> customStart to customEnd
    }
}
