package com.rslsoft.birikim.ui.screens.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rslsoft.birikim.data.local.PreferencesDataStore
import com.rslsoft.birikim.data.model.Goal
import com.rslsoft.birikim.data.repository.GoalRepository
import com.rslsoft.birikim.domain.FinanceCalculations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class GoalItem(
    val goal: Goal,
    val calculation: FinanceCalculations.GoalCalculation
)

data class GoalsUiState(
    val goalItems: List<GoalItem> = emptyList(),
    val showAddGoalSheet: Boolean = false,
    val inputName: String = "",
    val inputTargetAmountText: String = "",
    val inputTargetMonthsText: String = "12",

    val showAddMoneySheet: Boolean = false,
    val selectedGoal: Goal? = null,
    val inputAddMoneyAmountText: String = "",
    val deductFromBalance: Boolean = true
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            goalRepository.getAll().collect { goals ->
                val items = goals.map { goal ->
                    val months = calculateMonthsToDate(goal.targetDate)
                    val calc = FinanceCalculations.calculateGoalProgress(
                        targetAmountKurus = goal.targetAmount,
                        savedAmountKurus = goal.savedAmount,
                        remainingMonths = months
                    )
                    GoalItem(goal, calc)
                }
                _uiState.update { it.copy(goalItems = items) }
            }
        }
    }

    private fun calculateMonthsToDate(targetDate: Long): Int {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = targetDate }

        val yearsDiff = target.get(Calendar.YEAR) - now.get(Calendar.YEAR)
        val monthsDiff = target.get(Calendar.MONTH) - now.get(Calendar.MONTH)

        val totalMonths = yearsDiff * 12 + monthsDiff
        return totalMonths.coerceAtLeast(1)
    }

    // --- Add Goal Sheet ---
    fun openAddGoalSheet() {
        _uiState.update {
            it.copy(
                showAddGoalSheet = true,
                inputName = "",
                inputTargetAmountText = "",
                inputTargetMonthsText = "12"
            )
        }
    }

    fun closeAddGoalSheet() {
        _uiState.update { it.copy(showAddGoalSheet = false) }
    }

    fun setInputName(name: String) {
        _uiState.update { it.copy(inputName = name) }
    }

    fun setInputTargetAmount(amount: String) {
        _uiState.update { it.copy(inputTargetAmountText = amount) }
    }

    fun setInputTargetMonths(months: String) {
        _uiState.update { it.copy(inputTargetMonthsText = months) }
    }

    fun saveGoal() {
        val state = _uiState.value
        if (state.inputName.isBlank()) return
        val targetLira = state.inputTargetAmountText.replace(",", ".").toDoubleOrNull() ?: return
        val months = state.inputTargetMonthsText.toIntOrNull() ?: 12
        val targetKurus = (targetLira * 100).toLong()

        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, months)

        viewModelScope.launch {
            goalRepository.insert(
                Goal(
                    name = state.inputName,
                    targetAmount = targetKurus,
                    savedAmount = 0L,
                    targetDate = cal.timeInMillis
                )
            )
            closeAddGoalSheet()
        }
    }

    // --- Add Money Sheet ---
    fun openAddMoneySheet(goal: Goal) {
        _uiState.update {
            it.copy(
                showAddMoneySheet = true,
                selectedGoal = goal,
                inputAddMoneyAmountText = "",
                deductFromBalance = true
            )
        }
    }

    fun closeAddMoneySheet() {
        _uiState.update { it.copy(showAddMoneySheet = false) }
    }

    fun setAddMoneyAmount(amount: String) {
        _uiState.update { it.copy(inputAddMoneyAmountText = amount) }
    }

    fun setDeductFromBalance(deduct: Boolean) {
        _uiState.update { it.copy(deductFromBalance = deduct) }
    }

    fun saveAddMoney() {
        val state = _uiState.value
        val goal = state.selectedGoal ?: return
        val addLira = state.inputAddMoneyAmountText.replace(",", ".").toDoubleOrNull() ?: return
        val addKurus = (addLira * 100).toLong()

        viewModelScope.launch {
            // Update goal
            val newSaved = goal.savedAmount + addKurus
            goalRepository.update(goal.copy(savedAmount = newSaved))

            // Deduct from balance if selected
            if (state.deductFromBalance) {
                val currentBalance = preferencesDataStore.balance.first()
                preferencesDataStore.setBalance(currentBalance - addKurus)
            }

            closeAddMoneySheet()
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            goalRepository.delete(goal)
        }
    }
}
