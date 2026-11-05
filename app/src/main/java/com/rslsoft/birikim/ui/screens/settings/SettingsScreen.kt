package com.rslsoft.birikim.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rslsoft.birikim.R
import com.rslsoft.birikim.ui.components.*
import com.rslsoft.birikim.ui.theme.BirikimTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showClearDataDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.exportMessage) {
        val msg = uiState.exportMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearExportMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_screen_title),
                        style = BirikimTheme.typography.titleLarge,
                        color = BirikimTheme.colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.btn_cancel),
                            tint = BirikimTheme.colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BirikimTheme.colors.background
                )
            )
        },
        containerColor = BirikimTheme.colors.background,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = BirikimTheme.dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(BirikimTheme.dimens.cardSpacing)
        ) {
            // Theme Section
            item {
                HCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = null,
                            tint = BirikimTheme.colors.primary
                        )
                        Text(
                            text = stringResource(R.string.theme_mode_title),
                            style = BirikimTheme.typography.cardTitle,
                            color = BirikimTheme.colors.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val themes = listOf(
                            "SYSTEM" to stringResource(R.string.theme_system),
                            "LIGHT" to stringResource(R.string.theme_light),
                            "DARK" to stringResource(R.string.theme_dark)
                        )

                        themes.forEach { (mode, label) ->
                            val isSelected = uiState.themeMode == mode
                            HPillButton(
                                text = label,
                                onClick = { viewModel.setThemeMode(mode) },
                                backgroundColor = if (isSelected) BirikimTheme.colors.primary else BirikimTheme.colors.background,
                                textColor = if (isSelected) Color.White else BirikimTheme.colors.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Language Section
            item {
                HCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Language,
                            contentDescription = null,
                            tint = BirikimTheme.colors.primary
                        )
                        Text(
                            text = stringResource(R.string.sec_language),
                            style = BirikimTheme.typography.cardTitle,
                            color = BirikimTheme.colors.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val languages = listOf(
                            "system" to stringResource(R.string.lang_system),
                            "tr" to stringResource(R.string.lang_tr),
                            "en" to stringResource(R.string.lang_en),
                            "de" to stringResource(R.string.lang_de)
                        )

                        languages.forEach { (lang, label) ->
                            val isSelected = uiState.appLanguage == lang
                            HPillButton(
                                text = label,
                                onClick = { viewModel.setAppLanguage(lang) },
                                backgroundColor = if (isSelected) BirikimTheme.colors.primary else BirikimTheme.colors.background,
                                textColor = if (isSelected) Color.White else BirikimTheme.colors.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Security & Preferences
            item {
                HCard(modifier = Modifier.fillMaxWidth()) {
                    // Hide balance default
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.VisibilityOff,
                                contentDescription = null,
                                tint = BirikimTheme.colors.primary
                            )
                            Column {
                                Text(
                                    text = stringResource(R.string.hide_balance_default),
                                    style = BirikimTheme.typography.body.copy(fontWeight = FontWeight.Medium),
                                    color = BirikimTheme.colors.textPrimary
                                )
                                Text(
                                    text = stringResource(R.string.hide_balance_default_desc),
                                    style = BirikimTheme.typography.label,
                                    color = BirikimTheme.colors.textSecondary
                                )
                            }
                        }

                        Switch(
                            checked = uiState.hideBalanceDefault,
                            onCheckedChange = { viewModel.setHideBalanceDefault(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BirikimTheme.colors.divider)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Biometric Lock
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Fingerprint,
                                contentDescription = null,
                                tint = BirikimTheme.colors.primary
                            )
                            Column {
                                Text(
                                    text = stringResource(R.string.biometric_lock),
                                    style = BirikimTheme.typography.body.copy(fontWeight = FontWeight.Medium),
                                    color = BirikimTheme.colors.textPrimary
                                )
                                Text(
                                    text = stringResource(R.string.biometric_desc),
                                    style = BirikimTheme.typography.label,
                                    color = BirikimTheme.colors.textSecondary
                                )
                            }
                        }

                        Switch(
                            checked = uiState.biometricEnabled,
                            onCheckedChange = { viewModel.setBiometricEnabled(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BirikimTheme.colors.divider)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Notifications
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = null,
                                tint = BirikimTheme.colors.primary
                            )
                            Column {
                                Text(
                                    text = stringResource(R.string.payment_reminders),
                                    style = BirikimTheme.typography.body.copy(fontWeight = FontWeight.Medium),
                                    color = BirikimTheme.colors.textPrimary
                                )
                                Text(
                                    text = stringResource(R.string.payment_reminders_desc),
                                    style = BirikimTheme.typography.label,
                                    color = BirikimTheme.colors.textSecondary
                                )
                            }
                        }

                        Switch(
                            checked = uiState.notificationsEnabled,
                            onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                        )
                    }
                }
            }

            // Data Management
            item {
                HCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.sec_data_mgmt),
                        style = BirikimTheme.typography.cardTitle,
                        color = BirikimTheme.colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Export CSV
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.exportDataToCsv() }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = null,
                            tint = BirikimTheme.colors.primary
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.export_csv),
                                style = BirikimTheme.typography.body.copy(fontWeight = FontWeight.Medium),
                                color = BirikimTheme.colors.textPrimary
                            )
                            Text(
                                text = stringResource(R.string.export_csv_desc),
                                style = BirikimTheme.typography.label,
                                color = BirikimTheme.colors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BirikimTheme.colors.divider)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Clear All Data
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showClearDataDialog = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = BirikimTheme.colors.expense
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.clear_all_data),
                                style = BirikimTheme.typography.body.copy(fontWeight = FontWeight.Medium),
                                color = BirikimTheme.colors.expense
                            )
                            Text(
                                text = stringResource(R.string.clear_data_desc),
                                style = BirikimTheme.typography.label,
                                color = BirikimTheme.colors.textSecondary
                            )
                        }
                    }
                }
            }

            // About & Legal
            item {
                HCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.sec_about_legal),
                        style = BirikimTheme.typography.cardTitle,
                        color = BirikimTheme.colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val context = LocalContext.current

                    // Privacy Policy Website
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://re8711.github.io/birikim/")
                                )
                                context.startActivity(intent)
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Security,
                            contentDescription = null,
                            tint = BirikimTheme.colors.primary
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.privacy_policy_title),
                                style = BirikimTheme.typography.body.copy(fontWeight = FontWeight.Medium),
                                color = BirikimTheme.colors.textPrimary
                            )
                            Text(
                                text = stringResource(R.string.privacy_policy_desc),
                                style = BirikimTheme.typography.label,
                                color = BirikimTheme.colors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BirikimTheme.colors.divider)
                    Spacer(modifier = Modifier.height(12.dp))

                    // App Version
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = BirikimTheme.colors.textSecondary
                        )
                        Column {
                            Text(
                                text = "Birikim",
                                style = BirikimTheme.typography.body.copy(fontWeight = FontWeight.Medium),
                                color = BirikimTheme.colors.textPrimary
                            )
                            Text(
                                text = stringResource(R.string.app_version, "1.0.0"),
                                style = BirikimTheme.typography.label,
                                color = BirikimTheme.colors.textSecondary
                            )
                        }
                    }
                }
            }
        }

        // Clear All Confirmation Dialog
        if (showClearDataDialog) {
            AlertDialog(
                onDismissRequest = { showClearDataDialog = false },
                title = { Text(stringResource(R.string.clear_data_confirm_title), style = BirikimTheme.typography.titleLarge) },
                text = { Text(stringResource(R.string.clear_data_confirm_msg)) },
                confirmButton = {
                    HPillButton(
                        text = stringResource(R.string.btn_delete_all),
                        onClick = {
                            viewModel.clearAllData()
                            showClearDataDialog = false
                        },
                        backgroundColor = BirikimTheme.colors.expense
                    )
                },
                dismissButton = {
                    HPillButton(
                        text = stringResource(R.string.btn_cancel),
                        onClick = { showClearDataDialog = false },
                        backgroundColor = BirikimTheme.colors.background,
                        textColor = BirikimTheme.colors.textPrimary
                    )
                }
            )
        }
    }
}