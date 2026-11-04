package com.rslsoft.birikim.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Warning
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
import com.rslsoft.birikim.data.model.TransactionType
import com.rslsoft.birikim.data.model.getDisplayName
import com.rslsoft.birikim.domain.CurrencyUtils
import com.rslsoft.birikim.ui.components.*
import com.rslsoft.birikim.ui.theme.BirikimTheme
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var updateAmountInput by remember { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = BirikimTheme.dimens.screenPadding),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = BirikimTheme.dimens.bottomBarHeight + 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(BirikimTheme.dimens.cardSpacing)
        ) {
            // a) Üst Başlık
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.greeting_hello),
                            style = BirikimTheme.typography.label,
                            color = BirikimTheme.colors.textSecondary
                        )
                        Text(
                            text = stringResource(R.string.greeting_app_name),
                            style = BirikimTheme.typography.titleLarge,
                            color = BirikimTheme.colors.textPrimary
                        )
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.settings_title),
                            tint = BirikimTheme.colors.textPrimary
                        )
                    }
                }
            }

            // b) Bakiye Kartı
            item {
                HBalanceCard(
                    balanceKurus = uiState.balance,
                    isHidden = uiState.isBalanceHidden,
                    onToggleVisibility = { viewModel.toggleBalanceVisibility() },
                    onUpdateClick = { viewModel.showUpdateBalanceSheet() }
                )
            }

            // c) Dönem Segment Kontrolü
            item {
                HSegmentedControl(
                    items = listOf(stringResource(R.string.period_monthly), stringResource(R.string.period_yearly)),
                    selectedIndex = if (uiState.isYearly) 1 else 0,
                    onSelectionChanged = { index -> viewModel.setPeriod(index == 1) },
                    contentDescription = stringResource(R.string.period_selector_desc)
                )
            }

            // d) Özet Kartı
            item {
                HCard {
                    Text(
                        text = if (uiState.isYearly) stringResource(R.string.yearly_summary) else stringResource(R.string.monthly_summary),
                        style = BirikimTheme.typography.cardTitle,
                        color = BirikimTheme.colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.total_income),
                            style = BirikimTheme.typography.body,
                            color = BirikimTheme.colors.textSecondary
                        )
                        Text(
                            text = "+${CurrencyUtils.formatCurrency(uiState.totalIncome)}",
                            style = BirikimTheme.typography.numberTabular.copy(fontWeight = FontWeight.Medium),
                            color = BirikimTheme.colors.income
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.total_expense),
                            style = BirikimTheme.typography.body,
                            color = BirikimTheme.colors.textSecondary
                        )
                        Text(
                            text = "-${CurrencyUtils.formatCurrency(uiState.totalExpense)}",
                            style = BirikimTheme.typography.numberTabular.copy(fontWeight = FontWeight.Medium),
                            color = BirikimTheme.colors.expense
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BirikimTheme.colors.divider, thickness = BirikimTheme.dimens.dividerThickness)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.end_of_period_balance),
                        style = BirikimTheme.typography.label,
                        color = BirikimTheme.colors.textSecondary
                    )

                    val endBalance = uiState.endOfPeriodBalance
                    val isNegative = endBalance < 0

                    Text(
                        text = CurrencyUtils.formatCurrency(endBalance),
                        style = BirikimTheme.typography.titleLarge.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isNegative) BirikimTheme.colors.expense else BirikimTheme.colors.textPrimary
                    )

                    if (isNegative) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Warning,
                                contentDescription = null,
                                tint = BirikimTheme.colors.warning,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.deficit_warning),
                                style = BirikimTheme.typography.label,
                                color = BirikimTheme.colors.warning
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.spending_ratio, (uiState.expenseToIncomeRatio * 100).roundToInt()),
                        style = BirikimTheme.typography.label,
                        color = BirikimTheme.colors.textTertiary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    HProgressBar(
                        progress = uiState.expenseToIncomeRatio,
                        progressColor = if (uiState.expenseToIncomeRatio > 0.8f) BirikimTheme.colors.expense else BirikimTheme.colors.primary
                    )
                }
            }

            // e) Kategori Grafiği Kartı
            if (uiState.categoryExpenses.isNotEmpty()) {
                item {
                    HCard {
                        Text(
                            text = stringResource(R.string.category_distribution),
                            style = BirikimTheme.typography.cardTitle,
                            color = BirikimTheme.colors.textPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val donutSegments = uiState.categoryExpenses.map { catExp ->
                            val color = parseHexColor(catExp.category.colorHex, BirikimTheme.colors.primary)
                            DonutSegment(
                                label = catExp.category.getDisplayName(),
                                value = catExp.amount,
                                color = color
                            )
                        }

                        HDonutChart(
                            segments = donutSegments,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            strokeWidth = 24.dp,
                            centerSubText = stringResource(R.string.total_expense),
                            centerText = CurrencyUtils.formatCurrencySmartDecimal(uiState.totalExpense)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        uiState.categoryExpenses.take(4).forEach { catExp ->
                            val color = parseHexColor(catExp.category.colorHex, BirikimTheme.colors.primary)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Text(
                                        text = catExp.category.getDisplayName(),
                                        style = BirikimTheme.typography.body,
                                        color = BirikimTheme.colors.textPrimary
                                    )
                                }

                                Text(
                                    text = "%${(catExp.percentage * 100).toInt()} (${CurrencyUtils.formatCurrencySmartDecimal(catExp.amount)})",
                                    style = BirikimTheme.typography.label,
                                    color = BirikimTheme.colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            // f) Yaklaşan Ödemeler Kartı
            if (uiState.upcomingPayments.isNotEmpty()) {
                item {
                    HCard {
                        Text(
                            text = stringResource(R.string.upcoming_payments_title),
                            style = BirikimTheme.typography.cardTitle,
                            color = BirikimTheme.colors.textPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        uiState.upcomingPayments.forEach { payment ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = payment.transaction.title,
                                        style = BirikimTheme.typography.body.copy(fontWeight = FontWeight.Medium),
                                        color = BirikimTheme.colors.textPrimary
                                    )
                                    Text(
                                        text = CurrencyUtils.formatCurrency(payment.transaction.amount),
                                        style = BirikimTheme.typography.label,
                                        color = BirikimTheme.colors.expense
                                    )
                                }

                                HPillButton(
                                    text = stringResource(R.string.btn_paid),
                                    onClick = { viewModel.markPaymentAsPaid(payment.transaction) },
                                    backgroundColor = BirikimTheme.colors.primarySubtle,
                                    textColor = BirikimTheme.colors.primary,
                                    contentDescription = stringResource(R.string.marked_paid_desc, payment.transaction.title)
                                )
                            }
                        }
                    }
                }
            }

            // g) Son İşlemler Kartı
            item {
                HCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.recent_transactions_title),
                            style = BirikimTheme.typography.cardTitle,
                            color = BirikimTheme.colors.textPrimary
                        )

                        Text(
                            text = stringResource(R.string.see_all),
                            style = BirikimTheme.typography.label,
                            color = BirikimTheme.colors.primary,
                            modifier = Modifier.clickable { onNavigateToHistory() }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (uiState.recentTransactions.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_transactions_yet),
                            style = BirikimTheme.typography.body,
                            color = BirikimTheme.colors.textTertiary,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        uiState.recentTransactions.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    val catColor = parseHexColor(item.category?.colorHex ?: "#78909C", BirikimTheme.colors.primary)

                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(catColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(catColor)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = item.transaction.title,
                                            style = BirikimTheme.typography.body.copy(fontWeight = FontWeight.Medium),
                                            color = BirikimTheme.colors.textPrimary
                                        )
                                        Text(
                                            text = item.category?.getDisplayName() ?: stringResource(R.string.category_other),
                                            style = BirikimTheme.typography.label,
                                            color = BirikimTheme.colors.textSecondary
                                        )
                                    }
                                }

                                val isIncome = item.transaction.type == TransactionType.INCOME
                                Text(
                                    text = "${if (isIncome) "+" else "-"}${CurrencyUtils.formatCurrency(item.transaction.amount)}",
                                    style = BirikimTheme.typography.numberTabular.copy(fontWeight = FontWeight.Medium),
                                    color = if (isIncome) BirikimTheme.colors.income else BirikimTheme.colors.expense
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bakiye Güncelleme Bottom Sheet
        if (uiState.showUpdateBalanceSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.hideUpdateBalanceSheet() },
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
                        text = stringResource(R.string.btn_update_balance),
                        style = BirikimTheme.typography.titleLarge,
                        color = BirikimTheme.colors.textPrimary
                    )

                    OutlinedTextField(
                        value = updateAmountInput,
                        onValueChange = { updateAmountInput = it },
                        label = { Text(stringResource(R.string.amount_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = BirikimTheme.shapes.input,
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HPillButton(
                            text = stringResource(R.string.btn_set_balance),
                            onClick = {
                                val amount = CurrencyUtils.parseTurkishCurrency(updateAmountInput)
                                if (amount != null) {
                                    viewModel.setBalance(amount)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )

                        HPillButton(
                            text = stringResource(R.string.btn_add_amount),
                            onClick = {
                                val amount = CurrencyUtils.parseTurkishCurrency(updateAmountInput)
                                if (amount != null) {
                                    viewModel.adjustBalance(amount)
                                }
                            },
                            backgroundColor = BirikimTheme.colors.income,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

fun parseHexColor(hex: String, fallback: Color): Color {
    return try {
        val cleaned = if (hex.startsWith("#")) hex.substring(1) else hex
        val colorInt = android.graphics.Color.parseColor("#$cleaned")
        Color(colorInt)
    } catch (e: Exception) {
        fallback
    }
}
