package com.rslsoft.birikim.ui.screens.calculator

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rslsoft.birikim.data.model.CalculationType
import com.rslsoft.birikim.data.model.Recurrence
import com.rslsoft.birikim.data.model.TransactionType
import com.rslsoft.birikim.domain.CurrencyUtils
import com.rslsoft.birikim.domain.FinanceCalculations
import com.rslsoft.birikim.ui.components.*
import com.rslsoft.birikim.ui.screens.home.parseHexColor
import com.rslsoft.birikim.ui.theme.BirikimTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = BirikimTheme.dimens.screenPadding),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = BirikimTheme.dimens.bottomBarHeight + 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Text(
                    text = "Hesap Makinesi",
                    style = BirikimTheme.typography.titleLarge,
                    color = BirikimTheme.colors.textPrimary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Mode Selector Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val modes = listOf(
                        CalculatorMode.SUBSCRIPTION to "Abonelik/Fatura",
                        CalculatorMode.INSTALLMENT to "Taksit",
                        CalculatorMode.COMPOUND_INTEREST to "Faiz/Birikim",
                        CalculatorMode.NORMAL to "Normal"
                    )

                    modes.forEach { (mode, label) ->
                        val isSelected = uiState.selectedMode == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) BirikimTheme.colors.primary else BirikimTheme.colors.surface)
                                .clickable { viewModel.setMode(mode) }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = BirikimTheme.typography.cardTitle.copy(fontSize = 14.sp),
                                color = if (isSelected) Color.White else BirikimTheme.colors.textPrimary
                            )
                        }
                    }
                }
            }

            // Selected Mode Content
            when (uiState.selectedMode) {
                CalculatorMode.SUBSCRIPTION -> {
                    item {
                        SubscriptionCalculatorSection(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }
                }
                CalculatorMode.INSTALLMENT -> {
                    item {
                        InstallmentCalculatorSection(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }
                }
                CalculatorMode.COMPOUND_INTEREST -> {
                    item {
                        CompoundInterestCalculatorSection(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }
                }
                CalculatorMode.NORMAL -> {
                    item {
                        NormalCalculatorSection(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }
                }
            }

            // Bottom Section: Hesap Geçmişi List
            item {
                if (uiState.historyList.isNotEmpty()) {
                    Column(
                        modifier = Modifier.padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Hesap Geçmişi",
                            style = BirikimTheme.typography.cardTitle,
                            color = BirikimTheme.colors.textPrimary
                        )

                        uiState.historyList.take(10).forEach { history ->
                            HCard(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = when (history.type) {
                                                CalculationType.SUBSCRIPTION -> "Abonelik/Fatura"
                                                CalculationType.INSTALLMENT -> "Taksit"
                                                CalculationType.COMPOUND_INTEREST -> "Faiz/Birikim"
                                                CalculationType.NORMAL -> "Normal Hesap"
                                            },
                                            style = BirikimTheme.typography.label,
                                            color = BirikimTheme.colors.primary
                                        )
                                        Text(
                                            text = history.resultSummary,
                                            style = BirikimTheme.typography.body.copy(fontWeight = FontWeight.Medium),
                                            color = BirikimTheme.colors.textPrimary
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteHistoryItem(history) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = "Sil",
                                            tint = BirikimTheme.colors.textTertiary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Save Transaction Bottom Sheet
        if (uiState.showSaveSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeSaveSheet() },
                shape = BirikimTheme.shapes.bottomSheet,
                containerColor = BirikimTheme.colors.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (uiState.saveType == TransactionType.EXPENSE) "Gider Olarak Kaydet" else "Gelir Olarak Kaydet",
                        style = BirikimTheme.typography.titleLarge,
                        color = BirikimTheme.colors.textPrimary
                    )

                    OutlinedTextField(
                        value = uiState.saveTitle,
                        onValueChange = { viewModel.setSaveTitle(it) },
                        label = { Text("Başlık (ör. Netflix, Kira)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = BirikimTheme.shapes.input,
                        singleLine = true
                    )

                    Text(
                        text = "Kategori Seçin",
                        style = BirikimTheme.typography.label,
                        color = BirikimTheme.colors.textSecondary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.categories.forEach { category ->
                            val isSelected = category.id == uiState.saveCategoryId
                            val catColor = parseHexColor(category.colorHex, BirikimTheme.colors.primary)

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isSelected) catColor else BirikimTheme.colors.background)
                                    .clickable { viewModel.setSaveCategory(category.id) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = category.name,
                                    style = BirikimTheme.typography.label,
                                    color = if (isSelected) Color.White else BirikimTheme.colors.textPrimary
                                )
                            }
                        }
                    }

                    Text(
                        text = "Tutar: ${CurrencyUtils.formatCurrency(uiState.saveAmountKurus)}",
                        style = BirikimTheme.typography.cardTitle,
                        color = if (uiState.saveType == TransactionType.EXPENSE) BirikimTheme.colors.expense else BirikimTheme.colors.income
                    )

                    HPillButton(
                        text = "Kaydet",
                        onClick = { viewModel.confirmSaveTransaction() },
                        backgroundColor = if (uiState.saveType == TransactionType.EXPENSE) BirikimTheme.colors.expense else BirikimTheme.colors.income,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SubscriptionCalculatorSection(
    uiState: CalculatorUiState,
    viewModel: CalculatorViewModel
) {
    HCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Abonelik / Fatura",
            style = BirikimTheme.typography.cardTitle,
            color = BirikimTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (uiState.subAmountText.isEmpty()) "0 ₺" else "${uiState.subAmountText} ₺",
            style = BirikimTheme.typography.balanceDisplay,
            color = BirikimTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Sıklık seçici
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val freqs = listOf(
                FinanceCalculations.SubscriptionFrequency.WEEKLY to "Haftalık",
                FinanceCalculations.SubscriptionFrequency.MONTHLY to "Aylık",
                FinanceCalculations.SubscriptionFrequency.QUARTERLY to "3 Aylık",
                FinanceCalculations.SubscriptionFrequency.SEMI_ANNUALLY to "6 Aylık",
                FinanceCalculations.SubscriptionFrequency.YEARLY to "Yıllık"
            )

            freqs.forEach { (freq, label) ->
                val isSelected = uiState.subFrequency == freq
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) BirikimTheme.colors.primarySubtle else BirikimTheme.colors.background)
                        .clickable { viewModel.setSubFrequency(freq) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        style = BirikimTheme.typography.label,
                        color = if (isSelected) BirikimTheme.colors.primary else BirikimTheme.colors.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.subIncreasePercentText,
            onValueChange = { viewModel.setSubIncrease(it) },
            label = { Text("Yıllık Tahmini Zam Oranı (%)") },
            modifier = Modifier.fillMaxWidth(),
            shape = BirikimTheme.shapes.input,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sayı klavyesi
        HNumberPad(
            onDigit = { viewModel.onSubDigit(it) },
            onDecimal = {},
            onBackspace = { viewModel.onSubBackspace() },
            onClear = { viewModel.onSubClear() }
        )

        val result = uiState.subResult
        if (result != null) {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BirikimTheme.colors.divider)
            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ResultRow("Aylık Maliyet", result.monthlyCost)
                ResultRow("3 Aylık Maliyet", result.quarterlyCost)
                ResultRow("6 Aylık Maliyet", result.semiAnnualCost)
                ResultRow("Yıllık Maliyet", result.yearlyCost, isBold = true)
                ResultRow("5 Yıllık Toplam", result.fiveYearCost, isBold = true)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HPillButton(
                    text = "Gider Kaydet",
                    onClick = { viewModel.openSaveSheet(TransactionType.EXPENSE, result.monthlyCost, "Abonelik") },
                    backgroundColor = BirikimTheme.colors.expense,
                    modifier = Modifier.weight(1f)
                )
                HPillButton(
                    text = "Gelir Kaydet",
                    onClick = { viewModel.openSaveSheet(TransactionType.INCOME, result.monthlyCost, "Gelir") },
                    backgroundColor = BirikimTheme.colors.income,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun InstallmentCalculatorSection(
    uiState: CalculatorUiState,
    viewModel: CalculatorViewModel
) {
    HCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Taksit Hesaplama",
            style = BirikimTheme.typography.cardTitle,
            color = BirikimTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.instTotalAmountText,
            onValueChange = { viewModel.setInstAmount(it) },
            label = { Text("Toplam Borç Tutarı (₺)") },
            modifier = Modifier.fillMaxWidth(),
            shape = BirikimTheme.shapes.input,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.instCountText,
                onValueChange = { viewModel.setInstCount(it) },
                label = { Text("Taksit Sayısı") },
                modifier = Modifier.weight(1f),
                shape = BirikimTheme.shapes.input,
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.instInterestPercentText,
                onValueChange = { viewModel.setInstInterest(it) },
                label = { Text("Aylık Faiz (%)") },
                modifier = Modifier.weight(1f),
                shape = BirikimTheme.shapes.input,
                singleLine = true
            )
        }

        val result = uiState.instResult
        if (result != null) {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BirikimTheme.colors.divider)
            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ResultRow("Aylık Taksit", result.monthlyPayment, isBold = true)
                ResultRow("Toplam Ödeme", result.totalPayment)
                ResultRow("Toplam Faiz", result.totalInterest)
            }

            Spacer(modifier = Modifier.height(16.dp))

            HPillButton(
                text = "Gider Olarak Kaydet",
                onClick = { viewModel.openSaveSheet(TransactionType.EXPENSE, result.monthlyPayment, "Taksit Ödemesi") },
                backgroundColor = BirikimTheme.colors.expense,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CompoundInterestCalculatorSection(
    uiState: CalculatorUiState,
    viewModel: CalculatorViewModel
) {
    HCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Faiz / Birikim Hesaplama",
            style = BirikimTheme.typography.cardTitle,
            color = BirikimTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.compMonthlyDepositText,
            onValueChange = { viewModel.setCompMonthlyDeposit(it) },
            label = { Text("Aylık Biriktirilen Tutar (₺)") },
            modifier = Modifier.fillMaxWidth(),
            shape = BirikimTheme.shapes.input,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.compInterestPercentText,
                onValueChange = { viewModel.setCompInterest(it) },
                label = { Text("Yıllık Faiz Oranı (%)") },
                modifier = Modifier.weight(1f),
                shape = BirikimTheme.shapes.input,
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.compMonthsText,
                onValueChange = { viewModel.setCompMonths(it) },
                label = { Text("Süre (Ay)") },
                modifier = Modifier.weight(1f),
                shape = BirikimTheme.shapes.input,
                singleLine = true
            )
        }

        val result = uiState.compResult
        if (result != null) {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BirikimTheme.colors.divider)
            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ResultRow("Vade Sonu Tutar", result.totalAmount, isBold = true)
                ResultRow("Yatırılan Anapara", result.totalDeposited)
                ResultRow("Kazanılan Faiz", result.totalInterestEarned)
            }
        }
    }
}

@Composable
private fun NormalCalculatorSection(
    uiState: CalculatorUiState,
    viewModel: CalculatorViewModel
) {
    HCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Temel Hesap Makinesi",
            style = BirikimTheme.typography.cardTitle,
            color = BirikimTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (uiState.normalExpression.isEmpty()) "0" else uiState.normalExpression,
            style = BirikimTheme.typography.body,
            color = BirikimTheme.colors.textSecondary
        )

        Text(
            text = uiState.normalResultText,
            style = BirikimTheme.typography.balanceDisplay,
            color = BirikimTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        HNumberPad(
            onDigit = { viewModel.onNormalDigit(it) },
            onDecimal = { viewModel.onNormalDecimal() },
            onBackspace = { viewModel.onNormalBackspace() },
            onClear = { viewModel.onNormalClear() },
            showOperators = true,
            onOperator = { viewModel.onNormalOperator(it) }
        )
    }
}

@Composable
private fun ResultRow(label: String, amountKurus: Long, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = BirikimTheme.typography.body.copy(
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
            ),
            color = BirikimTheme.colors.textSecondary
        )
        Text(
            text = CurrencyUtils.formatCurrency(amountKurus),
            style = BirikimTheme.typography.numberTabular.copy(
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium
            ),
            color = BirikimTheme.colors.textPrimary
        )
    }
}
