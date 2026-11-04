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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rslsoft.birikim.R
import com.rslsoft.birikim.data.model.CalculationType
import com.rslsoft.birikim.data.model.Recurrence
import com.rslsoft.birikim.data.model.TransactionType
import com.rslsoft.birikim.data.model.getDisplayName
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
                    text = stringResource(R.string.nav_calculator),
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
                        CalculatorMode.SUBSCRIPTION to stringResource(R.string.calc_mode_sub),
                        CalculatorMode.INSTALLMENT to stringResource(R.string.calc_mode_inst),
                        CalculatorMode.COMPOUND_INTEREST to stringResource(R.string.calc_mode_comp),
                        CalculatorMode.NORMAL to stringResource(R.string.calc_mode_norm)
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
                            text = stringResource(R.string.calc_history_title),
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
                                                CalculationType.SUBSCRIPTION -> stringResource(R.string.sub_title)
                                                CalculationType.INSTALLMENT -> stringResource(R.string.calc_mode_inst)
                                                CalculationType.COMPOUND_INTEREST -> stringResource(R.string.calc_mode_comp)
                                                CalculationType.NORMAL -> stringResource(R.string.normal_calc_title)
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
                                            contentDescription = stringResource(R.string.clear_history),
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
                        text = if (uiState.saveType == TransactionType.EXPENSE) stringResource(R.string.save_sheet_expense_title) else stringResource(R.string.save_sheet_income_title),
                        style = BirikimTheme.typography.titleLarge,
                        color = BirikimTheme.colors.textPrimary
                    )

                    OutlinedTextField(
                        value = uiState.saveTitle,
                        onValueChange = { viewModel.setSaveTitle(it) },
                        label = { Text(stringResource(R.string.title_label_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = BirikimTheme.shapes.input,
                        singleLine = true
                    )

                    Text(
                        text = stringResource(R.string.select_category),
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
                                    text = category.getDisplayName(),
                                    style = BirikimTheme.typography.label,
                                    color = if (isSelected) Color.White else BirikimTheme.colors.textPrimary
                                )
                            }
                        }
                    }

                    Text(
                        text = stringResource(R.string.amount_display, CurrencyUtils.formatCurrency(uiState.saveAmountKurus)),
                        style = BirikimTheme.typography.cardTitle,
                        color = if (uiState.saveType == TransactionType.EXPENSE) BirikimTheme.colors.expense else BirikimTheme.colors.income
                    )

                    HPillButton(
                        text = stringResource(R.string.btn_save),
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
    val defaultSubTitle = stringResource(R.string.default_title_sub)
    val defaultIncTitle = stringResource(R.string.default_title_inc)

    HCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.sub_title),
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
                FinanceCalculations.SubscriptionFrequency.WEEKLY to stringResource(R.string.sub_freq_weekly),
                FinanceCalculations.SubscriptionFrequency.MONTHLY to stringResource(R.string.sub_freq_monthly),
                FinanceCalculations.SubscriptionFrequency.QUARTERLY to stringResource(R.string.sub_freq_quarterly),
                FinanceCalculations.SubscriptionFrequency.SEMI_ANNUALLY to stringResource(R.string.sub_freq_semi_annual),
                FinanceCalculations.SubscriptionFrequency.YEARLY to stringResource(R.string.sub_freq_yearly)
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
            label = { Text(stringResource(R.string.annual_increase)) },
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
                ResultRow(stringResource(R.string.monthly_cost), result.monthlyCost)
                ResultRow(stringResource(R.string.quarterly_total), result.quarterlyCost)
                ResultRow(stringResource(R.string.semi_annual_total), result.semiAnnualCost)
                ResultRow(stringResource(R.string.yearly_total), result.yearlyCost, isBold = true)
                ResultRow(stringResource(R.string.five_year_total), result.fiveYearCost, isBold = true)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HPillButton(
                    text = stringResource(R.string.save_as_expense),
                    onClick = { viewModel.openSaveSheet(TransactionType.EXPENSE, result.monthlyCost, defaultSubTitle) },
                    backgroundColor = BirikimTheme.colors.expense,
                    modifier = Modifier.weight(1f)
                )
                HPillButton(
                    text = stringResource(R.string.save_as_income),
                    onClick = { viewModel.openSaveSheet(TransactionType.INCOME, result.monthlyCost, defaultIncTitle) },
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
    val defaultInstTitle = stringResource(R.string.default_title_inst)

    HCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.inst_title),
            style = BirikimTheme.typography.cardTitle,
            color = BirikimTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.instTotalAmountText,
            onValueChange = { viewModel.setInstAmount(it) },
            label = { Text(stringResource(R.string.total_debt)) },
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
                label = { Text(stringResource(R.string.inst_count)) },
                modifier = Modifier.weight(1f),
                shape = BirikimTheme.shapes.input,
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.instInterestPercentText,
                onValueChange = { viewModel.setInstInterest(it) },
                label = { Text(stringResource(R.string.monthly_interest)) },
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
                ResultRow(stringResource(R.string.monthly_installment), result.monthlyPayment, isBold = true)
                ResultRow(stringResource(R.string.total_payment), result.totalPayment)
                ResultRow(stringResource(R.string.total_interest), result.totalInterest)
            }

            Spacer(modifier = Modifier.height(16.dp))

            HPillButton(
                text = stringResource(R.string.save_sheet_expense_title),
                onClick = { viewModel.openSaveSheet(TransactionType.EXPENSE, result.monthlyPayment, defaultInstTitle) },
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
            text = stringResource(R.string.comp_title),
            style = BirikimTheme.typography.cardTitle,
            color = BirikimTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.compMonthlyDepositText,
            onValueChange = { viewModel.setCompMonthlyDeposit(it) },
            label = { Text(stringResource(R.string.monthly_deposit)) },
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
                label = { Text(stringResource(R.string.annual_interest)) },
                modifier = Modifier.weight(1f),
                shape = BirikimTheme.shapes.input,
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.compMonthsText,
                onValueChange = { viewModel.setCompMonths(it) },
                label = { Text(stringResource(R.string.duration_months)) },
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
                ResultRow(stringResource(R.string.amount_at_end), result.totalAmount, isBold = true)
                ResultRow(stringResource(R.string.total_deposited), result.totalDeposited)
                ResultRow(stringResource(R.string.total_interest_earned), result.totalInterestEarned)
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
            text = stringResource(R.string.normal_calc_title),
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
