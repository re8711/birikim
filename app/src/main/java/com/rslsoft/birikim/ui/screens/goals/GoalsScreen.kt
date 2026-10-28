package com.rslsoft.birikim.ui.screens.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rslsoft.birikim.domain.CurrencyUtils
import com.rslsoft.birikim.ui.components.*
import com.rslsoft.birikim.ui.theme.BirikimTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel,
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
            // Header + New Goal Button
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Birikim Hedefleri",
                        style = BirikimTheme.typography.titleLarge,
                        color = BirikimTheme.colors.textPrimary
                    )

                    HPillButton(
                        text = "+ Yeni Hedef",
                        onClick = { viewModel.openAddGoalSheet() },
                        contentDescription = "Yeni hedef ekle"
                    )
                }
            }

            // Empty state
            if (uiState.goalItems.isEmpty()) {
                item {
                    HCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Henüz birikim hedefi eklemedin. Tatil, araba veya acil durum fonu için ilk hedefini ekle!",
                            style = BirikimTheme.typography.body,
                            color = BirikimTheme.colors.textTertiary,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }
            }

            // Goal Cards
            items(uiState.goalItems, key = { it.goal.id }) { item ->
                HCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.goal.name,
                                style = BirikimTheme.typography.titleLarge.copy(fontSize = 20.sp),
                                color = BirikimTheme.colors.textPrimary
                            )
                            Text(
                                text = "Hedef: ${CurrencyUtils.formatCurrency(item.goal.targetAmount)}",
                                style = BirikimTheme.typography.label,
                                color = BirikimTheme.colors.textSecondary
                            )
                        }

                        // Circular Progress
                        HCircularProgress(
                            progress = item.calculation.progressPercent,
                            modifier = Modifier.size(64.dp),
                            strokeWidth = 6.dp
                        ) {
                            Text(
                                text = "%${(item.calculation.progressPercent * 100).toInt()}",
                                style = BirikimTheme.typography.label.copy(fontWeight = FontWeight.Bold),
                                color = BirikimTheme.colors.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BirikimTheme.colors.divider)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Biriken: ${CurrencyUtils.formatCurrency(item.goal.savedAmount)}",
                            style = BirikimTheme.typography.body.copy(fontWeight = FontWeight.Medium),
                            color = BirikimTheme.colors.income
                        )
                        Text(
                            text = "Kalan: ${CurrencyUtils.formatCurrency(item.calculation.remainingAmount)}",
                            style = BirikimTheme.typography.body,
                            color = BirikimTheme.colors.textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (item.calculation.remainingAmount > 0) {
                        Text(
                            text = "Bu hedefe ulaşmak için ayda ${CurrencyUtils.formatCurrency(item.calculation.monthlyRequired)} biriktirmelisin. (Tahmini ${item.calculation.estimatedMonths} ay)",
                            style = BirikimTheme.typography.label,
                            color = BirikimTheme.colors.primary
                        )
                    } else {
                        Text(
                            text = "Tebrikler! Bu hedefe ulaştın 🎉",
                            style = BirikimTheme.typography.label.copy(fontWeight = FontWeight.Bold),
                            color = BirikimTheme.colors.income
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HPillButton(
                            text = "Para Ekle",
                            onClick = { viewModel.openAddMoneySheet(item.goal) },
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = { viewModel.deleteGoal(item.goal) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Hedefi sil",
                                tint = BirikimTheme.colors.textTertiary
                            )
                        }
                    }
                }
            }
        }

        // Add Goal Bottom Sheet
        if (uiState.showAddGoalSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeAddGoalSheet() },
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
                        text = "Yeni Birikim Hedefi",
                        style = BirikimTheme.typography.titleLarge,
                        color = BirikimTheme.colors.textPrimary
                    )

                    OutlinedTextField(
                        value = uiState.inputName,
                        onValueChange = { viewModel.setInputName(it) },
                        label = { Text("Hedef Adı (ör. Tatil, Araba)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = BirikimTheme.shapes.input,
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uiState.inputTargetAmountText,
                        onValueChange = { viewModel.setInputTargetAmount(it) },
                        label = { Text("Hedef Tutar (₺)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = BirikimTheme.shapes.input,
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uiState.inputTargetMonthsText,
                        onValueChange = { viewModel.setInputTargetMonths(it) },
                        label = { Text("Hedef Süre (Ay)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = BirikimTheme.shapes.input,
                        singleLine = true
                    )

                    HPillButton(
                        text = "Hedef Oluştur",
                        onClick = { viewModel.saveGoal() },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Add Money Bottom Sheet
        if (uiState.showAddMoneySheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeAddMoneySheet() },
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
                        text = "${uiState.selectedGoal?.name ?: ""} Hedefine Para Ekle",
                        style = BirikimTheme.typography.titleLarge,
                        color = BirikimTheme.colors.textPrimary
                    )

                    OutlinedTextField(
                        value = uiState.inputAddMoneyAmountText,
                        onValueChange = { viewModel.setAddMoneyAmount(it) },
                        label = { Text("Eklenecek Tutar (₺)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = BirikimTheme.shapes.input,
                        singleLine = true
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = uiState.deductFromBalance,
                            onCheckedChange = { viewModel.setDeductFromBalance(it) }
                        )
                        Text(
                            text = "Mevcut bakiyeden düşülsün mü?",
                            style = BirikimTheme.typography.body,
                            color = BirikimTheme.colors.textPrimary
                        )
                    }

                    HPillButton(
                        text = "Birikime Ekle",
                        onClick = { viewModel.saveAddMoney() },
                        backgroundColor = BirikimTheme.colors.income,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
