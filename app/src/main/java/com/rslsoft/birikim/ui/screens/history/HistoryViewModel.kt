package com.rslsoft.birikim.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rslsoft.birikim.data.model.*
import com.rslsoft.birikim.data.repository.CategoryRepository
import com.rslsoft.birikim.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

enum class HistoryFilter {
    ALL, INCOME, EXPENSE
}

data class GroupedTransactions(
    val dateLabel: String,
    val transactions: List<TransactionWithCategory>
)

data class TransactionWithCategory(
    val transaction: Transaction,
    val category: Category?
)

data class HistoryUiState(
    val searchQuery: String = "",
    val selectedFilter: HistoryFilter = HistoryFilter.ALL,
    val groupedTransactions: List<GroupedTransactions> = emptyList(),
    val categories: List<Category> = emptyList(),
    val showAddEditSheet: Boolean = false,
    val editingTransaction: Transaction? = null,
    val inputTitle: String = "",
    val inputAmountText: String = "",
    val inputType: TransactionType = TransactionType.EXPENSE,
    val inputCategoryId: Long = 1,
    val inputRecurrence: Recurrence = Recurrence.ONCE,
    val showCategoryAddDialog: Boolean = false,
    val newCategoryName: String = "",
    val newCategoryColorHex: String = "#3B82F6"
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                transactionRepository.getAll(),
                categoryRepository.getAll(),
                _uiState.map { it.searchQuery }.distinctUntilChanged(),
                _uiState.map { it.selectedFilter }.distinctUntilChanged()
            ) { transactions, categories, query, filter ->
                val categoryMap = categories.associateBy { it.id }

                val filtered = transactions.filter { t ->
                    val matchesType = when (filter) {
                        HistoryFilter.ALL -> true
                        HistoryFilter.INCOME -> t.type == TransactionType.INCOME
                        HistoryFilter.EXPENSE -> t.type == TransactionType.EXPENSE
                    }
                    val matchesQuery = query.isBlank() ||
                            t.title.contains(query, ignoreCase = true) ||
                            (categoryMap[t.categoryId]?.name?.contains(query, ignoreCase = true) == true)

                    matchesType && matchesQuery
                }

                val grouped = groupTransactionsByDate(filtered, categoryMap)
                grouped to categories
            }.collect { (grouped, categories) ->
                _uiState.update {
                    it.copy(
                        groupedTransactions = grouped,
                        categories = categories
                    )
                }
            }
        }
    }

    private fun groupTransactionsByDate(
        transactions: List<Transaction>,
        categoryMap: Map<Long, Category>
    ): List<GroupedTransactions> {
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("tr", "TR"))
        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale("tr", "TR"))

        return transactions
            .sortedByDescending { it.createdAt }
            .groupBy { t ->
                val cal = Calendar.getInstance().apply { timeInMillis = t.createdAt }
                when {
                    isSameDay(cal, today) -> "Bugün"
                    isSameDay(cal, yesterday) -> "Dün"
                    else -> monthYearFormat.format(cal.time)
                }
            }
            .map { (dateLabel, txns) ->
                GroupedTransactions(
                    dateLabel = dateLabel,
                    transactions = txns.map { t -> TransactionWithCategory(t, categoryMap[t.categoryId]) }
                )
            }
    }

    private fun isSameDay(c1: Calendar, c2: Calendar): Boolean {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setFilter(filter: HistoryFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.delete(transaction)
        }
    }

    fun openAddSheet() {
        _uiState.update {
            it.copy(
                showAddEditSheet = true,
                editingTransaction = null,
                inputTitle = "",
                inputAmountText = "",
                inputType = TransactionType.EXPENSE,
                inputCategoryId = it.categories.firstOrNull()?.id ?: 1,
                inputRecurrence = Recurrence.ONCE
            )
        }
    }

    fun openEditSheet(transaction: Transaction) {
        _uiState.update {
            it.copy(
                showAddEditSheet = true,
                editingTransaction = transaction,
                inputTitle = transaction.title,
                inputAmountText = (transaction.amount / 100.0).toString(),
                inputType = transaction.type,
                inputCategoryId = transaction.categoryId,
                inputRecurrence = transaction.recurrence
            )
        }
    }

    fun closeAddEditSheet() {
        _uiState.update { it.copy(showAddEditSheet = false) }
    }

    fun setInputTitle(title: String) {
        _uiState.update { it.copy(inputTitle = title) }
    }

    fun setInputAmountText(amount: String) {
        _uiState.update { it.copy(inputAmountText = amount) }
    }

    fun setInputType(type: TransactionType) {
        _uiState.update { it.copy(inputType = type) }
    }

    fun setInputCategoryId(catId: Long) {
        _uiState.update { it.copy(inputCategoryId = catId) }
    }

    fun setInputRecurrence(recurrence: Recurrence) {
        _uiState.update { it.copy(inputRecurrence = recurrence) }
    }

    fun saveTransaction() {
        val state = _uiState.value
        if (state.inputTitle.isBlank()) return
        val amount = state.inputAmountText.replace(",", ".").toDoubleOrNull() ?: return
        val amountKurus = (amount * 100).toLong()

        viewModelScope.launch {
            val existing = state.editingTransaction
            if (existing != null) {
                transactionRepository.update(
                    existing.copy(
                        title = state.inputTitle,
                        amount = amountKurus,
                        type = state.inputType,
                        categoryId = state.inputCategoryId,
                        recurrence = state.inputRecurrence
                    )
                )
            } else {
                transactionRepository.insert(
                    Transaction(
                        title = state.inputTitle,
                        amount = amountKurus,
                        type = state.inputType,
                        categoryId = state.inputCategoryId,
                        recurrence = state.inputRecurrence,
                        startDate = System.currentTimeMillis()
                    )
                )
            }
            closeAddEditSheet()
        }
    }

    // New Category creation
    fun openAddCategoryDialog() {
        _uiState.update { it.copy(showCategoryAddDialog = true, newCategoryName = "") }
    }

    fun closeAddCategoryDialog() {
        _uiState.update { it.copy(showCategoryAddDialog = false) }
    }

    fun setNewCategoryName(name: String) {
        _uiState.update { it.copy(newCategoryName = name) }
    }

    fun setNewCategoryColor(colorHex: String) {
        _uiState.update { it.copy(newCategoryColorHex = colorHex) }
    }

    fun saveNewCategory() {
        val name = _uiState.value.newCategoryName
        val color = _uiState.value.newCategoryColorHex
        if (name.isBlank()) return

        viewModelScope.launch {
            val newId = categoryRepository.insert(
                Category(
                    name = name,
                    iconKey = "category",
                    colorHex = color
                )
            )
            _uiState.update { it.copy(inputCategoryId = newId) }
            closeAddCategoryDialog()
        }
    }
}
