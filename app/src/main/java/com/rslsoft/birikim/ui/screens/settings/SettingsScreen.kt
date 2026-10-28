package com.rslsoft.birikim.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
                        text = "Ayarlar",
                        style = BirikimTheme.typography.titleLarge,
                        color = BirikimTheme.colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Geri",
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
                            text = "Tema Modu",
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
                            "SYSTEM" to "Otomatik",
                            "LIGHT" to "Açık",
                            "DARK" to "Koyu"
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
                                    text = "Bakiyeyi Varsayılan Gizle",
                                    style = BirikimTheme.typography.body.copy(fontWeight = FontWeight.Medium),
                                    color = BirikimTheme.colors.textPrimary
                                )
                                Text(
                                    text = "Uygulama açılışında bakiye gizli olsun",
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
                                    text = "Biyometrik Kilit",
                                    style = BirikimTheme.typography.body.copy(fontWeight = FontWeight.Medium),
                                    color = BirikimTheme.colors.textPrimary
                                )
                                Text(
                                    text = "Açılışta parmak izi / yüz doğrulama iste",
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
                                    text = "Hatırlatıcı Bildirimleri",
                                    style = BirikimTheme.typography.body.copy(fontWeight = FontWeight.Medium),
                                    color = BirikimTheme.colors.textPrimary
                                )
                                Text(
                                    text = "Yaklaşan fatura ve abonelik bildirimi al",
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
                        text = "Veri Yönetimi",
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
                                text = "Verileri Dışa Aktar (CSV)",
                                style = BirikimTheme.typography.body.copy(fontWeight = FontWeight.Medium),
                                color = BirikimTheme.colors.textPrimary
                            )
                            Text(
                                text = "İşlem geçmişini CSV dosyası olarak indir",
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
                                text = "Tüm Verileri Sil",
                                style = BirikimTheme.typography.body.copy(fontWeight = FontWeight.Medium),
                                color = BirikimTheme.colors.expense
                            )
                            Text(
                                text = "İşlemler, hedefler ve ayarlar sıfırlanır",
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
                title = { Text("Tüm Verileri Sil?", style = BirikimTheme.typography.titleLarge) },
                text = { Text("Bu işlem geri alınamaz. Tüm kayıtların ve bakiyen silinecek.") },
                confirmButton = {
                    HPillButton(
                        text = "Sil",
                        onClick = {
                            viewModel.clearAllData()
                            showClearDataDialog = false
                        },
                        backgroundColor = BirikimTheme.colors.expense
                    )
                },
                dismissButton = {
                    HPillButton(
                        text = "İptal",
                        onClick = { showClearDataDialog = false },
                        backgroundColor = BirikimTheme.colors.background,
                        textColor = BirikimTheme.colors.textPrimary
                    )
                }
            )
        }
    }
}
