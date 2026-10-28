package com.rslsoft.birikim.ui.screens.history

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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rslsoft.birikim.data.model.Recurrence
import com.rslsoft.birikim.data.model.TransactionType
import com.rslsoft.birikim.domain.CurrencyUtils
import com.rslsoft.birikim.ui.components.*
import com.rslsoft.birikim.ui.screens.home.parseHexColor
import com.rslsoft.birikim.ui.theme.BirikimTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
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
            // Header + Add Button
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "İşlem Geçmişi",
                        style = BirikimTheme.typography.titleLarge,
                        color = BirikimTheme.colors.textPrimary
                    )

                    HPillButton(
                        text = "+ Yeni İşlem",
                        onClick = { viewModel.openAddSheet() },
                        contentDescription = "Yeni işlem ekle"
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("İşlem veya kategori ara...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = BirikimTheme.colors.textSecondary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = BirikimTheme.shapes.input,
                    singleLine = true
                )
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        HistoryFilter.ALL to "Tümü",
                        HistoryFilter.INCOME to "Gelir",
                        HistoryFilter.EXPENSE to "Gider"
                    )

                    filters.forEach { (filter, label) ->
                        val isSelected = uiState.selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) BirikimTheme.colors.primary else BirikimTheme.colors.surface)
                                .clickable { viewModel.setFilter(filter) }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
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

            // Empty state
            if (uiState.groupedTransactions.isEmpty()) {
                item {
                    HCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Henüz işlem yok. Sağ üstteki '+ Yeni İşlem' butonundan ekleyebilirsin.",
                            style = BirikimTheme.typography.body,
                            color = BirikimTheme.colors.textTertiary,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }
            }

            // Grouped List
            uiState.groupedTransactions.forEach { group ->
                item {
                    Text(
                        text = group.dateLabel,
                        style = BirikimTheme.typography.label.copy(fontWeight = FontWeight.Bold),
                        color = BirikimTheme.colors.textSecondary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(group.transactions, key = { it.transaction.id }) { item ->
                    HCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
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
                                        text = item.category?.name ?: "Diğer",
                                        style = BirikimTheme.typography.label,
                                        color = BirikimTheme.colors.textSecondary
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val isIncome = item.transaction.type == TransactionType.INCOME
                                Text(
                                    text = "${if (isIncome) "+" else "-"}${CurrencyUtils.formatCurrency(item.transaction.amount)}",
                                    style = BirikimTheme.typography.numberTabular.copy(fontWeight = FontWeight.Bold),
                                    color = if (isIncome) BirikimTheme.colors.income else BirikimTheme.colors.expense
                                )

                                IconButton(
                                    onClick = { viewModel.openEditSheet(item.transaction) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = "Düzenle",
                                        tint = BirikimTheme.colors.textTertiary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.deleteTransaction(item.transaction) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Sil",
                                        tint = BirikimTheme.colors.textTertiary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add/Edit Transaction Bottom Sheet
        if (uiState.showAddEditSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeAddEditSheet() },
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
                        text = if (uiState.editingTransaction != null) "İşlemi Düzenle" else "Yeni İşlem Ekle",
                        style = BirikimTheme.typography.titleLarge,
                        color = BirikimTheme.colors.textPrimary
                    )

                    // Income / Expense selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HPillButton(
                            text = "Gider",
                            onClick = { viewModel.setInputType(TransactionType.EXPENSE) },
                            backgroundColor = if (uiState.inputType == TransactionType.EXPENSE) BirikimTheme.colors.expense else BirikimTheme.colors.background,
                            textColor = if (uiState.inputType == TransactionType.EXPENSE) Color.White else BirikimTheme.colors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )

                        HPillButton(
                            text = "Gelir",
                            onClick = { viewModel.setInputType(TransactionType.INCOME) },
                            backgroundColor = if (uiState.inputType == TransactionType.INCOME) BirikimTheme.colors.income else BirikimTheme.colors.background,
                            textColor = if (uiState.inputType == TransactionType.INCOME) Color.White else BirikimTheme.colors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = uiState.inputTitle,
                        onValueChange = { viewModel.setInputTitle(it) },
                        label = { Text("Başlık") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = BirikimTheme.shapes.input,
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uiState.inputAmountText,
                        onValueChange = { viewModel.setInputAmountText(it) },
                        label = { Text("Tutar (₺)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = BirikimTheme.shapes.input,
                        singleLine = true
                    )

                    // Category selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kategori",
                            style = BirikimTheme.typography.label,
                            color = BirikimTheme.colors.textSecondary
                        )

                        Text(
                            text = "+ Yeni Kategori",
                            style = BirikimTheme.typography.label,
                            color = BirikimTheme.colors.primary,
                            modifier = Modifier.clickable { viewModel.openAddCategoryDialog() }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.categories.forEach { category ->
                            val isSelected = category.id == uiState.inputCategoryId
                            val catColor = parseHexColor(category.colorHex, BirikimTheme.colors.primary)

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isSelected) catColor else BirikimTheme.colors.background)
                                    .clickable { viewModel.setInputCategoryId(category.id) }
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

                    HPillButton(
                        text = "Kaydet",
                        onClick = { viewModel.saveTransaction() },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Add Category Dialog
        if (uiState.showCategoryAddDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.closeAddCategoryDialog() },
                title = { Text("Yeni Kategori Ekle", style = BirikimTheme.typography.titleLarge) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = uiState.newCategoryName,
                            onValueChange = { viewModel.setNewCategoryName(it) },
                            label = { Text("Kategori Adı") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = BirikimTheme.shapes.input,
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    HPillButton(
                        text = "Ekle",
                        onClick = { viewModel.saveNewCategory() }
                    )
                },
                dismissButton = {
                    HPillButton(
                        text = "İptal",
                        onClick = { viewModel.closeAddCategoryDialog() },
                        backgroundColor = BirikimTheme.colors.background,
                        textColor = BirikimTheme.colors.textPrimary
                    )
                }
            )
        }
    }
}
