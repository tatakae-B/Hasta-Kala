package com.hastakala.shop.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hastakala.shop.R
import com.hastakala.shop.data.PreferenceManager
import com.hastakala.shop.viewmodel.ShopViewModel
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.hastakala.shop.utils.BiometricHelper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: ShopViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val darkMode by viewModel.darkMode.collectAsState()
    val themeColor by viewModel.themeColor.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val autoSaveBills by viewModel.autoSaveBills.collectAsState()
    val saleConfirmation by viewModel.saleConfirmation.collectAsState()
    val defaultQty by viewModel.defaultQty.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val lowStockAlerts by viewModel.lowStockAlerts.collectAsState()
    val minStockThreshold by viewModel.minStockThreshold.collectAsState()
    val stockNotifications by viewModel.stockNotifications.collectAsState()
    val autoBackup by viewModel.autoBackup.collectAsState()
    val exportFormat by viewModel.exportFormat.collectAsState()
    val weeklySummary by viewModel.weeklySummary.collectAsState()
    val backupReminders by viewModel.backupReminders.collectAsState()
    val language by viewModel.language.collectAsState()
    val appLock by viewModel.appLock.collectAsState()
    val unlockMethod by viewModel.unlockMethod.collectAsState()
    val lastBackupTime by viewModel.lastBackupTime.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
            // 1. LANGUAGE
            item {
                var showLanguageDialog by remember { mutableStateOf(false) }
                val languageOptions = listOf(
                    "English" to "English",
                    "Malayalam" to "മലയാളം",
                    "Tamil" to "தமிழ்",
                    "Kannada" to "ಕನ್ನಡ",
                    "Telugu" to "తెలుగు",
                    "Hindi" to "हिन्दी"
                )
                val currentLanguageName = languageOptions.find { it.first == language }?.second ?: language

                SettingsActionItem(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.section_language),
                    subtitle = currentLanguageName,
                    onClick = { showLanguageDialog = true }
                )

                if (showLanguageDialog) {
                    AlertDialog(
                        onDismissRequest = { showLanguageDialog = false },
                        title = { Text(stringResource(R.string.section_language)) },
                        text = {
                            Column {
                                languageOptions.forEach { (langKey, langDisplay) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.updatePreference(PreferenceManager.LANGUAGE, langKey)
                                                showLanguageDialog = false
                                            }
                                            .padding(vertical = 12.dp, horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = langKey == language,
                                            onClick = {
                                                viewModel.updatePreference(PreferenceManager.LANGUAGE, langKey)
                                                showLanguageDialog = false
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = langDisplay, style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showLanguageDialog = false }) {
                                Text(stringResource(R.string.btn_cancel))
                            }
                        }
                    )
                }
            }

            // 2. SECURITY
            item {
                var showPinSetup by remember { mutableStateOf(false) }
                var tempPin by remember { mutableStateOf("") }
                var confirmPin by remember { mutableStateOf("") }
                var isConfirming by remember { mutableStateOf(false) }
                var pinError by remember { mutableStateOf<String?>(null) }
                
                if (showPinSetup) {
                    AlertDialog(
                        onDismissRequest = { 
                            showPinSetup = false 
                            isConfirming = false
                            tempPin = ""
                            confirmPin = ""
                            pinError = null
                        },
                        title = { Text(if (isConfirming) stringResource(R.string.label_pin_setup) else if (appLock) stringResource(R.string.label_pin_setup) else stringResource(R.string.label_pin_setup)) },
                        text = {
                            Column {
                                Text(
                                    if (isConfirming) "Please re-enter your PIN to confirm." else "Enter a 4-digit numeric code to secure your app.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (pinError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                pinError?.let {
                                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = if (isConfirming) confirmPin else tempPin,
                                    onValueChange = { 
                                        if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                            if (isConfirming) confirmPin = it else tempPin = it
                                            pinError = null
                                        }
                                    },
                                    label = { Text(if (isConfirming) "Confirm PIN" else "4-Digit PIN") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                enabled = (if (isConfirming) confirmPin else tempPin).length == 4,
                                onClick = {
                                    if (!isConfirming) {
                                        isConfirming = true
                                    } else {
                                        if (tempPin == confirmPin) {
                                            viewModel.setAppPin(tempPin)
                                            viewModel.updatePreference(PreferenceManager.APP_LOCK, true)
                                            viewModel.updatePreference(PreferenceManager.UNLOCK_METHOD, "PIN")
                                            showPinSetup = false
                                            isConfirming = false
                                            tempPin = ""
                                            confirmPin = ""
                                        } else {
                                            pinError = "PINs do not match. Try again."
                                            confirmPin = ""
                                        }
                                    }
                                }
                            ) { Text(if (isConfirming) stringResource(R.string.btn_continue) else stringResource(R.string.btn_continue)) }
                        },
                        dismissButton = {
                            TextButton(onClick = { 
                                if (isConfirming) {
                                    isConfirming = false
                                    confirmPin = ""
                                    pinError = null
                                } else {
                                    showPinSetup = false 
                                }
                            }) { Text(if (isConfirming) "Back" else stringResource(R.string.btn_cancel)) }
                        }
                    )
                }

                SettingsSection(title = stringResource(R.string.section_security)) {
                    SettingsSwitchItem(
                        icon = Icons.Default.Lock,
                        title = stringResource(R.string.label_app_lock),
                        checked = appLock,
                        onCheckedChange = { isEnabled ->
                            if (isEnabled) {
                                // Default to PIN setup if not enabled
                                showPinSetup = true
                            } else {
                                viewModel.updatePreference(PreferenceManager.APP_LOCK, false)
                            }
                        }
                    )
                    
                    AnimatedVisibility(visible = appLock) {
                        Column {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Unlock Method",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = unlockMethod,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Button(
                                    onClick = {
                                        if (unlockMethod == "Biometric") {
                                            showPinSetup = true
                                        } else {
                                            activity?.let {
                                                BiometricHelper.authenticate(
                                                    activity = it,
                                                    title = it.getString(R.string.auth_title),
                                                    subtitle = it.getString(R.string.auth_subtitle),
                                                    onSuccess = {
                                                        viewModel.updatePreference(PreferenceManager.UNLOCK_METHOD, "Biometric")
                                                    },
                                                    onError = { /* Keep PIN */ }
                                                )
                                            }
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(
                                        text = "Switch to ${if (unlockMethod == "Biometric") "PIN" else "Biometric"}",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. APPEARANCE
            item {
                SettingsSection(title = stringResource(R.string.section_appearance)) {
                    SettingsSwitchItem(
                        icon = Icons.Default.DarkMode,
                        title = stringResource(R.string.label_dark_mode),
                        checked = darkMode,
                        onCheckedChange = { viewModel.updatePreference(PreferenceManager.DARK_MODE, it) }
                    )
                    SettingsSelectorItem(
                        icon = Icons.Default.Palette,
                        title = stringResource(R.string.label_theme_color),
                        selectedOption = themeColor,
                        options = listOf("Brown", "Terracotta", "Olive", "Gold", "Midnight", "Rosewood"),
                        onOptionSelected = { viewModel.updatePreference(PreferenceManager.THEME_COLOR, it) }
                    )
                }
            }


            // 4. BILLING
            item {
                SettingsSection(title = stringResource(R.string.section_billing)) {
                    SettingsSwitchItem(
                        icon = Icons.Default.Save,
                        title = stringResource(R.string.label_auto_save_bills),
                        checked = autoSaveBills,
                        onCheckedChange = { viewModel.updatePreference(PreferenceManager.AUTO_SAVE_BILLS, it) }
                    )
                    SettingsSelectorItem(
                        icon = Icons.Default.Payments,
                        title = stringResource(R.string.label_currency),
                        selectedOption = currency,
                        options = listOf("₹", "$", "€", "£"),
                        onOptionSelected = { viewModel.updatePreference(PreferenceManager.CURRENCY, it) }
                    )
                }
            }

            // 5. INVENTORY
            item {
                SettingsSection(title = stringResource(R.string.section_inventory_settings)) {
                    SettingsSwitchItem(
                        icon = Icons.Default.Warning,
                        title = stringResource(R.string.label_low_stock_alerts),
                        checked = lowStockAlerts,
                        onCheckedChange = { viewModel.updatePreference(PreferenceManager.LOW_STOCK_ALERTS, it) }
                    )
                }
            }

            // Footer Section
            item {
                Spacer(modifier = Modifier.height(48.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.6f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Storefront,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.footer_app_name),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = stringResource(R.string.footer_version),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${stringResource(R.string.footer_designed_developed)} ${stringResource(R.string.footer_developer_name)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
}

@Composable
fun LanguageCard(name: String, isSelected: Boolean, onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsSelectorItem(
    icon: ImageVector,
    title: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Text(text = selectedOption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { option ->
                    val isSelected = option == selectedOption
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                        label = { 
                            Text(
                                text = option,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) 
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsStepperItem(
    icon: ImageVector,
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (value > 1) onValueChange(value - 1) }) {
                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease", tint = MaterialTheme.colorScheme.primary)
            }
            Text(text = value.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
            IconButton(onClick = { onValueChange(value + 1) }) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun SettingsSliderItem(
    icon: ImageVector,
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(text = value.toInt().toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..50f,
            steps = 49
        )
    }
}

@Composable
fun SettingsActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
    }
}
