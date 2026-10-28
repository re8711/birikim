package com.rslsoft.birikim.ui.screens.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rslsoft.birikim.data.model.*
import com.rslsoft.birikim.data.repository.CalculationHistoryRepository
import com.rslsoft.birikim.data.repository.CategoryRepository
import com.rslsoft.birikim.data.repository.TransactionRepository
import com.rslsoft.birikim.domain.CurrencyUtils
import com.rslsoft.birikim.domain.FinanceCalculations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CalculatorMode {
    SUBSCRIPTION,
    INSTALLMENT,
    COMPOUND_INTEREST,
    NORMAL
}

data class CalculatorUiState(
    val selectedMode: CalculatorMode = CalculatorMode.SUBSCRIPTION,

    // Subscription mode
    val subAmountText: String = "",
    val subFrequency: FinanceCalculations.SubscriptionFrequency = FinanceCalculations.SubscriptionFrequency.MONTHLY,
    val subIncreasePercentText: String = "",
    val subResult: FinanceCalculations.SubscriptionResult? = null,

    // Installment mode
    val instTotalAmountText: String = "",
    val instCountText: String = "12",
    val instInterestPercentText: String = "0",
    val instResult: FinanceCalculations.InstallmentResult? = null,

    // Compound Interest mode
    val compMonthlyDepositText: String = "",
    val compInterestPercentText: String = "",
    val compMonthsText: String = "12",
    val compResult: FinanceCalculations.CompoundInterestResult? = null,

    // Normal mode
    val normalExpression: String = "",
    val normalResultText: String = "0",

    // History
    val historyList: List<CalculationHistory> = emptyList(),

    // Save as Transaction Sheet state
    val showSaveSheet: Boolean = false,
    val saveType: TransactionType = TransactionType.EXPENSE,
    val saveTitle: String = "",
    val saveCategoryId: Long = 1,
    val saveAmountKurus: Long = 0L,
    val saveRecurrence: Recurrence = Recurrence.MONTHLY,

    val categories: List<Category> = emptyList()
)

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val calculationHistoryRepository: CalculationHistoryRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            calculationHistoryRepository.getAll().collect { history ->
                _uiState.update { it.copy(historyList = history) }
            }
        }

        viewModelScope.launch {
            categoryRepository.getAll().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    fun setMode(mode: CalculatorMode) {
        _uiState.update { it.copy(selectedMode = mode) }
    }

    // --- Subscription ---
    fun onSubDigit(digit: String) {
        val current = _uiState.value.subAmountText
        val updated = current + digit
        _uiState.update { it.copy(subAmountText = updated) }
        recalculateSub()
    }

    fun onSubBackspace() {
        val current = _uiState.value.subAmountText
        if (current.isNotEmpty()) {
            val updated = current.dropLast(1)
            _uiState.update { it.copy(subAmountText = updated) }
            recalculateSub()
        }
    }

    fun onSubClear() {
        _uiState.update { it.copy(subAmountText = "", subResult = null) }
    }

    fun setSubFrequency(freq: FinanceCalculations.SubscriptionFrequency) {
        _uiState.update { it.copy(subFrequency = freq) }
        recalculateSub()
    }

    fun setSubIncrease(percentText: String) {
        _uiState.update { it.copy(subIncreasePercentText = percentText) }
        recalculateSub()
    }

    private fun recalculateSub() {
        val amount = CurrencyUtils.parseTurkishCurrency(_uiState.value.subAmountText) ?: 0L
        val freq = _uiState.value.subFrequency
        val increase = _uiState.value.subIncreasePercentText.toDoubleOrNull() ?: 0.0

        if (amount > 0) {
            val result = FinanceCalculations.calculateSubscription(amount, freq, increase)
            _uiState.update { it.copy(subResult = result) }
            logCalculation(
                type = CalculationType.SUBSCRIPTION,
                inputsJson = "amount=$amount, freq=$freq, increase=$increase",
                resultSummary = "Aylık: ${CurrencyUtils.formatCurrency(result.monthlyCost)}, Yıllık: ${CurrencyUtils.formatCurrency(result.yearlyCost)}"
            )
        } else {
            _uiState.update { it.copy(subResult = null) }
        }
    }

    // --- Installment ---
    fun setInstAmount(text: String) {
        _uiState.update { it.copy(instTotalAmountText = text) }
        recalculateInst()
    }

    fun setInstCount(countText: String) {
        _uiState.update { it.copy(instCountText = countText) }
        recalculateInst()
    }

    fun setInstInterest(interestText: String) {
        _uiState.update { it.copy(instInterestPercentText = interestText) }
        recalculateInst()
    }

    private fun recalculateInst() {
        val amount = CurrencyUtils.parseTurkishCurrency(_uiState.value.instTotalAmountText) ?: 0L
        val count = _uiState.value.instCountText.toIntOrNull() ?: 0
        val rate = _uiState.value.instInterestPercentText.toDoubleOrNull() ?: 0.0

        if (amount > 0 && count > 0) {
            val result = FinanceCalculations.calculateInstallment(amount, count, rate)
            _uiState.update { it.copy(instResult = result) }
            logCalculation(
                type = CalculationType.INSTALLMENT,
                inputsJson = "amount=$amount, count=$count, rate=$rate",
                resultSummary = "$count Taksit, Aylık: ${CurrencyUtils.formatCurrency(result.monthlyPayment)}"
            )
        } else {
            _uiState.update { it.copy(instResult = null) }
        }
    }

    // --- Compound Interest ---
    fun setCompMonthlyDeposit(text: String) {
        _uiState.update { it.copy(compMonthlyDepositText = text) }
        recalculateComp()
    }

    fun setCompInterest(interestText: String) {
        _uiState.update { it.copy(compInterestPercentText = interestText) }
        recalculateComp()
    }

    fun setCompMonths(monthsText: String) {
        _uiState.update { it.copy(compMonthsText = monthsText) }
        recalculateComp()
    }

    private fun recalculateComp() {
        val deposit = CurrencyUtils.parseTurkishCurrency(_uiState.value.compMonthlyDepositText) ?: 0L
        val rate = _uiState.value.compInterestPercentText.toDoubleOrNull() ?: 0.0
        val months = _uiState.value.compMonthsText.toIntOrNull() ?: 0

        if (deposit > 0 && months > 0) {
            val result = FinanceCalculations.calculateCompoundInterest(deposit, rate, months)
            _uiState.update { it.copy(compResult = result) }
            logCalculation(
                type = CalculationType.COMPOUND_INTEREST,
                inputsJson = "deposit=$deposit, rate=$rate, months=$months",
                resultSummary = "$months Ay Sonunda: ${CurrencyUtils.formatCurrency(result.totalAmount)}"
            )
        } else {
            _uiState.update { it.copy(compResult = null) }
        }
    }

    // --- Normal ---
    fun onNormalDigit(digit: String) {
        val current = _uiState.value.normalExpression
        val updated = current + digit
        _uiState.update { it.copy(normalExpression = updated) }
        evaluateNormal()
    }

    fun onNormalOperator(op: String) {
        val current = _uiState.value.normalExpression
        val updated = current + op
        _uiState.update { it.copy(normalExpression = updated) }
    }

    fun onNormalDecimal() {
        val current = _uiState.value.normalExpression
        val updated = current + ","
        _uiState.update { it.copy(normalExpression = updated) }
    }

    fun onNormalBackspace() {
        val current = _uiState.value.normalExpression
        if (current.isNotEmpty()) {
            val updated = current.dropLast(1)
            _uiState.update { it.copy(normalExpression = updated) }
            evaluateNormal()
        }
    }

    fun onNormalClear() {
        _uiState.update { it.copy(normalExpression = "", normalResultText = "0") }
    }

    private fun evaluateNormal() {
        val expr = _uiState.value.normalExpression
        if (expr.isNotEmpty()) {
            val res = FinanceCalculations.evaluateExpression(expr)
            if (res != null) {
                _uiState.update { it.copy(normalResultText = res.toPlainString()) }
            }
        }
    }

    private fun logCalculation(type: CalculationType, inputsJson: String, resultSummary: String) {
        viewModelScope.launch {
            calculationHistoryRepository.insert(
                CalculationHistory(
                    type = type,
                    inputsJson = inputsJson,
                    resultSummary = resultSummary,
                    resultsJson = ""
                )
            )
        }
    }

    // --- Save as Transaction Sheet ---
    fun openSaveSheet(type: TransactionType, amountKurus: Long, defaultTitle: String = "") {
        _uiState.update {
            it.copy(
                showSaveSheet = true,
                saveType = type,
                saveAmountKurus = amountKurus,
                saveTitle = defaultTitle
            )
        }
    }

    fun closeSaveSheet() {
        _uiState.update { it.copy(showSaveSheet = false) }
    }

    fun setSaveTitle(title: String) {
        _uiState.update { it.copy(saveTitle = title) }
    }

    fun setSaveCategory(catId: Long) {
        _uiState.update { it.copy(saveCategoryId = catId) }
    }

    fun setSaveRecurrence(rec: Recurrence) {
        _uiState.update { it.copy(saveRecurrence = rec) }
    }

    fun confirmSaveTransaction() {
        val state = _uiState.value
        if (state.saveTitle.isBlank() || state.saveAmountKurus <= 0) return

        viewModelScope.launch {
            transactionRepository.insert(
                Transaction(
                    title = state.saveTitle,
                    amount = state.saveAmountKurus,
                    type = state.saveType,
                    categoryId = state.saveCategoryId,
                    recurrence = state.saveRecurrence,
                    startDate = System.currentTimeMillis()
                )
            )
            closeSaveSheet()
        }
    }

    fun deleteHistoryItem(history: CalculationHistory) {
        viewModelScope.launch {
            calculationHistoryRepository.delete(history)
        }
    }
}
