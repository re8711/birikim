package com.rslsoft.birikim.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rslsoft.birikim.data.local.PreferencesDataStore
import com.rslsoft.birikim.data.model.Category
import com.rslsoft.birikim.data.model.Recurrence
import com.rslsoft.birikim.data.model.Transaction
import com.rslsoft.birikim.data.model.TransactionType
import com.rslsoft.birikim.data.repository.CategoryRepository
import com.rslsoft.birikim.data.repository.TransactionRepository
import com.rslsoft.birikim.domain.FinanceCalculations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val balance: Long = 0L,
    val isBalanceHidden: Boolean = false,
    val isYearly: Boolean = false,
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val endOfPeriodBalance: Long = 0L,
    val expenseToIncomeRatio: Float = 0f,
    val categoryExpenses: List<CategoryExpense> = emptyList(),
    val upcomingPayments: List<UpcomingPayment> = emptyList(),
    val recentTransactions: List<TransactionItem> = emptyList(),
    val showUpdateBalanceSheet: Boolean = false
)

data class CategoryExpense(
    val category: Category,
    val amount: Long,
    val percentage: Float
)

data class UpcomingPayment(
    val transaction: Transaction,
    val categoryName: String
)

data class TransactionItem(
    val transaction: Transaction,
    val category: Category?
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Bakiye ve gizleme tercihi
        viewModelScope.launch {
            combine(
                preferencesDataStore.balance,
                preferencesDataStore.hideBalance
            ) { balance, hidden ->
                balance to hidden
            }.collect { (balance, hidden) ->
                _uiState.update { it.copy(balance = balance, isBalanceHidden = hidden) }
            }
        }

        // İşlemler, kategori verileri ve bakiye
        viewModelScope.launch {
            combine(
                transactionRepository.getAll(),
                categoryRepository.getAll(),
                preferencesDataStore.balance,
                _uiState.map { it.isYearly }.distinctUntilChanged()
            ) { transactions, categories, balance, isYearly ->
                updateProjection(transactions, categories, balance, isYearly)
            }.collect()
        }

        // Yaklaşan ödemeler
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val sevenDaysLater = now + 7 * 24 * 60 * 60 * 1000L

            combine(
                transactionRepository.getUpcomingPayments(now, sevenDaysLater),
                categoryRepository.getAll()
            ) { payments, categories ->
                payments to categories
            }.collect { (payments, categories) ->
                val categoryMap = categories.associateBy { it.id }
                val upcoming = payments.map { t ->
                    UpcomingPayment(t, categoryMap[t.categoryId]?.name ?: "")
                }
                _uiState.update { it.copy(upcomingPayments = upcoming) }
            }
        }

        // Son işlemler
        viewModelScope.launch {
            combine(
                transactionRepository.getAll(),
                categoryRepository.getAll()
            ) { transactions, categories ->
                transactions to categories
            }.collect { (transactions, categories) ->
                val categoryMap = categories.associateBy { it.id }
                val recent = transactions
                    .sortedByDescending { it.createdAt }
                    .take(5)
                    .map { t -> TransactionItem(t, categoryMap[t.categoryId]) }
                _uiState.update { it.copy(recentTransactions = recent) }
            }
        }
    }

    private fun updateProjection(
        transactions: List<Transaction>,
        categories: List<Category>,
        balance: Long,
        isYearly: Boolean
    ) {
        val now = java.util.Calendar.getInstance()
        val currentYear = now.get(java.util.Calendar.YEAR)
        val currentMonth = now.get(java.util.Calendar.MONTH)

        // Tek seferlik işlemleri içinde bulunulan ay/yıl ile filtrele
        val relevantTransactions = transactions.filter { t ->
            if (t.recurrence == Recurrence.ONCE) {
                val cal = java.util.Calendar.getInstance().apply { timeInMillis = t.createdAt }
                val txnYear = cal.get(java.util.Calendar.YEAR)
                val txnMonth = cal.get(java.util.Calendar.MONTH)
                if (isYearly) {
                    txnYear == currentYear
                } else {
                    txnYear == currentYear && txnMonth == currentMonth
                }
            } else {
                true
            }
        }

        val transactionData = relevantTransactions.map { t ->
            FinanceCalculations.TransactionData(
                amountKurus = t.amount,
                type = when (t.type) {
                    TransactionType.INCOME -> FinanceCalculations.TransactionDataType.INCOME
                    TransactionType.EXPENSE -> FinanceCalculations.TransactionDataType.EXPENSE
                },
                recurrence = when (t.recurrence) {
                    Recurrence.ONCE -> FinanceCalculations.TransactionRecurrence.ONCE
                    Recurrence.WEEKLY -> FinanceCalculations.TransactionRecurrence.WEEKLY
                    Recurrence.MONTHLY -> FinanceCalculations.TransactionRecurrence.MONTHLY
                    Recurrence.QUARTERLY -> FinanceCalculations.TransactionRecurrence.QUARTERLY
                    Recurrence.SEMI_ANNUALLY -> FinanceCalculations.TransactionRecurrence.SEMI_ANNUALLY
                    Recurrence.YEARLY -> FinanceCalculations.TransactionRecurrence.YEARLY
                }
            )
        }

        val projection = FinanceCalculations.calculatePeriodProjection(
            currentBalanceKurus = balance,
            transactions = transactionData,
            isYearly = isYearly
        )

        // Kategori bazlı gider dağılımı
        val categoryMap = categories.associateBy { it.id }
        val expensesByCategory = relevantTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryId }
            .map { (catId, txns) ->
                val cat = categoryMap[catId] ?: Category(id = 11, name = "Diğer", iconKey = "more_horiz", colorHex = "#78909C")
                val totalForCategory = txns.sumOf { t ->
                    val rd = when (t.recurrence) {
                        Recurrence.ONCE -> FinanceCalculations.TransactionRecurrence.ONCE
                        Recurrence.WEEKLY -> FinanceCalculations.TransactionRecurrence.WEEKLY
                        Recurrence.MONTHLY -> FinanceCalculations.TransactionRecurrence.MONTHLY
                        Recurrence.QUARTERLY -> FinanceCalculations.TransactionRecurrence.QUARTERLY
                        Recurrence.SEMI_ANNUALLY -> FinanceCalculations.TransactionRecurrence.SEMI_ANNUALLY
                        Recurrence.YEARLY -> FinanceCalculations.TransactionRecurrence.YEARLY
                    }
                    val td = FinanceCalculations.TransactionData(t.amount, FinanceCalculations.TransactionDataType.EXPENSE, rd)
                    FinanceCalculations.calculatePeriodProjection(0, listOf(td), isYearly).totalExpense
                }
                CategoryExpense(
                    category = cat,
                    amount = totalForCategory,
                    percentage = if (projection.totalExpense > 0) totalForCategory.toFloat() / projection.totalExpense.toFloat() else 0f
                )
            }
            .sortedByDescending { it.amount }

        _uiState.update {
            it.copy(
                totalIncome = projection.totalIncome,
                totalExpense = projection.totalExpense,
                endOfPeriodBalance = projection.endBalance,
                expenseToIncomeRatio = projection.expenseToIncomeRatio,
                categoryExpenses = expensesByCategory
            )
        }
    }

    fun toggleBalanceVisibility() {
        viewModelScope.launch {
            val newValue = !_uiState.value.isBalanceHidden
            preferencesDataStore.setHideBalance(newValue)
        }
    }

    fun setPeriod(isYearly: Boolean) {
        _uiState.update { it.copy(isYearly = isYearly) }
    }

    fun showUpdateBalanceSheet() {
        _uiState.update { it.copy(showUpdateBalanceSheet = true) }
    }

    fun hideUpdateBalanceSheet() {
        _uiState.update { it.copy(showUpdateBalanceSheet = false) }
    }

    fun setBalance(newBalanceKurus: Long) {
        viewModelScope.launch {
            preferencesDataStore.setBalance(newBalanceKurus)
            hideUpdateBalanceSheet()
        }
    }

    fun adjustBalance(amountKurus: Long) {
        viewModelScope.launch {
            val current = _uiState.value.balance
            preferencesDataStore.setBalance(current + amountKurus)
            hideUpdateBalanceSheet()
        }
    }

    fun markPaymentAsPaid(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.update(transaction.copy(isPaid = true))
        }
    }
}
