package com.rslsoft.birikim.domain

import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class FinanceCalculationsTest {

    // ═══ ABONELİK / FATURA TESTLERİ ═══

    @Test
    fun `subscription monthly no increase`() {
        val result = FinanceCalculations.calculateSubscription(
            amountKurus = 30000L, // 300 TL
            frequency = FinanceCalculations.SubscriptionFrequency.MONTHLY,
            annualIncreasePercent = 0.0
        )
        assertEquals(30000L, result.monthlyCost)
        assertEquals(90000L, result.quarterlyCost)
        assertEquals(180000L, result.semiAnnualCost)
        assertEquals(360000L, result.yearlyCost) // 3.600 TL
        assertEquals(1800000L, result.fiveYearCost) // 18.000 TL
    }

    @Test
    fun `subscription weekly to monthly conversion`() {
        val result = FinanceCalculations.calculateSubscription(
            amountKurus = 10000L, // 100 TL haftalık
            frequency = FinanceCalculations.SubscriptionFrequency.WEEKLY
        )
        // 100 × 4.33 = 433 TL aylık
        assertEquals(43300L, result.monthlyCost)
    }

    @Test
    fun `subscription yearly to monthly conversion`() {
        val result = FinanceCalculations.calculateSubscription(
            amountKurus = 120000L, // 1.200 TL yıllık
            frequency = FinanceCalculations.SubscriptionFrequency.YEARLY
        )
        assertEquals(10000L, result.monthlyCost) // 100 TL aylık
    }

    @Test
    fun `subscription quarterly to monthly conversion`() {
        val result = FinanceCalculations.calculateSubscription(
            amountKurus = 90000L, // 900 TL 3 aylık
            frequency = FinanceCalculations.SubscriptionFrequency.QUARTERLY
        )
        assertEquals(30000L, result.monthlyCost)
    }

    @Test
    fun `subscription with annual increase`() {
        val result = FinanceCalculations.calculateSubscription(
            amountKurus = 30000L,
            frequency = FinanceCalculations.SubscriptionFrequency.MONTHLY,
            annualIncreasePercent = 12.0 // %12 yıllık = %1 aylık
        )
        assertEquals(30000L, result.monthlyCost)
        // İlk ay 300 TL, zamlı yıllık toplam 300'den büyük olmalı × 12
        assertTrue(result.yearlyCost > 360000L)
        assertEquals(12, result.monthlyBreakdown.size)
        // Her ay bir öncekinden büyük
        for (i in 1 until result.monthlyBreakdown.size) {
            assertTrue(result.monthlyBreakdown[i] >= result.monthlyBreakdown[i - 1])
        }
    }

    // ═══ TAKSİT TESTLERİ ═══

    @Test
    fun `installment no interest`() {
        val result = FinanceCalculations.calculateInstallment(
            totalAmountKurus = 1200000L, // 12.000 TL
            installmentCount = 12,
            monthlyInterestPercent = 0.0
        )
        assertEquals(100000L, result.monthlyPayment) // 1.000 TL
        assertEquals(1200000L, result.totalPayment)
        assertEquals(0L, result.totalInterest)
    }

    @Test
    fun `installment with interest`() {
        val result = FinanceCalculations.calculateInstallment(
            totalAmountKurus = 1000000L, // 10.000 TL
            installmentCount = 12,
            monthlyInterestPercent = 2.0 // %2 aylık
        )
        // Aylık taksit 945.60 TL civarı olmalı
        assertTrue(result.monthlyPayment > 90000L)
        assertTrue(result.totalInterest > 0)
        assertEquals(result.totalPayment, result.monthlyPayment * 12)
        assertEquals(result.totalInterest, result.totalPayment - 1000000L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `installment zero count throws`() {
        FinanceCalculations.calculateInstallment(100000L, 0, 1.0)
    }

    // ═══ BİLEŞİK FAİZ TESTLERİ ═══

    @Test
    fun `compound interest no interest`() {
        val result = FinanceCalculations.calculateCompoundInterest(
            monthlyDepositKurus = 100000L, // 1.000 TL
            annualInterestPercent = 0.0,
            months = 12
        )
        assertEquals(1200000L, result.totalAmount)
        assertEquals(1200000L, result.totalDeposited)
        assertEquals(0L, result.totalInterestEarned)
    }

    @Test
    fun `compound interest with rate`() {
        val result = FinanceCalculations.calculateCompoundInterest(
            monthlyDepositKurus = 100000L, // 1.000 TL
            annualInterestPercent = 12.0, // %12 yıllık
            months = 12
        )
        assertEquals(1200000L, result.totalDeposited)
        assertTrue(result.totalAmount > result.totalDeposited)
        assertTrue(result.totalInterestEarned > 0)
        assertEquals(result.totalInterestEarned, result.totalAmount - result.totalDeposited)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `compound interest zero months throws`() {
        FinanceCalculations.calculateCompoundInterest(100000L, 10.0, 0)
    }

    // ═══ DÖNEM PROJEKSİYONU TESTLERİ ═══

    @Test
    fun `period projection monthly`() {
        val transactions = listOf(
            FinanceCalculations.TransactionData(
                500000L, // 5.000 TL maaş
                FinanceCalculations.TransactionDataType.INCOME,
                FinanceCalculations.TransactionRecurrence.MONTHLY
            ),
            FinanceCalculations.TransactionData(
                200000L, // 2.000 TL kira
                FinanceCalculations.TransactionDataType.EXPENSE,
                FinanceCalculations.TransactionRecurrence.MONTHLY
            ),
            FinanceCalculations.TransactionData(
                30000L, // 300 TL abonelik
                FinanceCalculations.TransactionDataType.EXPENSE,
                FinanceCalculations.TransactionRecurrence.MONTHLY
            )
        )
        val result = FinanceCalculations.calculatePeriodProjection(
            currentBalanceKurus = 1000000L, // 10.000 TL
            transactions = transactions,
            isYearly = false
        )
        assertEquals(500000L, result.totalIncome)
        assertEquals(230000L, result.totalExpense) // 2.000 + 300
        assertEquals(270000L, result.netAmount) // 5.000 - 2.300
        assertEquals(1270000L, result.endBalance) // 10.000 + 2.700
        assertTrue(result.expenseToIncomeRatio < 1f)
    }

    @Test
    fun `period projection yearly multiplies monthly`() {
        val transactions = listOf(
            FinanceCalculations.TransactionData(
                500000L,
                FinanceCalculations.TransactionDataType.INCOME,
                FinanceCalculations.TransactionRecurrence.MONTHLY
            )
        )
        val result = FinanceCalculations.calculatePeriodProjection(
            currentBalanceKurus = 0L,
            transactions = transactions,
            isYearly = true
        )
        assertEquals(6000000L, result.totalIncome) // 5.000 × 12
    }

    @Test
    fun `period projection once not multiplied`() {
        val transactions = listOf(
            FinanceCalculations.TransactionData(
                100000L,
                FinanceCalculations.TransactionDataType.EXPENSE,
                FinanceCalculations.TransactionRecurrence.ONCE
            )
        )
        val resultMonthly = FinanceCalculations.calculatePeriodProjection(0L, transactions, false)
        val resultYearly = FinanceCalculations.calculatePeriodProjection(0L, transactions, true)
        assertEquals(100000L, resultMonthly.totalExpense)
        assertEquals(100000L, resultYearly.totalExpense)
    }

    @Test
    fun `period projection negative balance shows deficit`() {
        val transactions = listOf(
            FinanceCalculations.TransactionData(
                300000L,
                FinanceCalculations.TransactionDataType.INCOME,
                FinanceCalculations.TransactionRecurrence.MONTHLY
            ),
            FinanceCalculations.TransactionData(
                500000L,
                FinanceCalculations.TransactionDataType.EXPENSE,
                FinanceCalculations.TransactionRecurrence.MONTHLY
            )
        )
        val result = FinanceCalculations.calculatePeriodProjection(100000L, transactions, false)
        assertTrue(result.netAmount < 0)
        assertTrue(result.endBalance < 100000L)
    }

    @Test
    fun `period projection expense exceeds income ratio over 1`() {
        val transactions = listOf(
            FinanceCalculations.TransactionData(
                100000L, // 1.000 TL gelir
                FinanceCalculations.TransactionDataType.INCOME,
                FinanceCalculations.TransactionRecurrence.MONTHLY
            ),
            FinanceCalculations.TransactionData(
                150000L, // 1.500 TL gider
                FinanceCalculations.TransactionDataType.EXPENSE,
                FinanceCalculations.TransactionRecurrence.MONTHLY
            )
        )
        val result = FinanceCalculations.calculatePeriodProjection(0L, transactions, false)
        assertEquals(1.5f, result.expenseToIncomeRatio, 0.001f)
    }

    @Test
    fun `period projection zero income uses balance for ratio`() {
        val transactions = listOf(
            FinanceCalculations.TransactionData(
                200000L, // 2.000 TL gider
                FinanceCalculations.TransactionDataType.EXPENSE,
                FinanceCalculations.TransactionRecurrence.MONTHLY
            )
        )
        // 10.000 TL bakiye var, gelir 0 -> 2.000 / 10.000 = 0.2 (%20)
        val result = FinanceCalculations.calculatePeriodProjection(1000000L, transactions, false)
        assertEquals(0.2f, result.expenseToIncomeRatio, 0.001f)
    }

    // ═══ HEDEF HESAPLAMA TESTLERİ ═══

    @Test
    fun `goal progress calculation`() {
        val result = FinanceCalculations.calculateGoalProgress(
            targetAmountKurus = 5000000L, // 50.000 TL
            savedAmountKurus = 1000000L,  // 10.000 TL
            remainingMonths = 10
        )
        assertEquals(4000000L, result.remainingAmount)
        assertEquals(0.2f, result.progressPercent, 0.01f)
        assertEquals(400000L, result.monthlyRequired) // 4.000 TL/ay
        assertEquals(10, result.estimatedMonths)
    }

    @Test
    fun `goal fully achieved`() {
        val result = FinanceCalculations.calculateGoalProgress(
            targetAmountKurus = 100000L,
            savedAmountKurus = 100000L,
            remainingMonths = 5
        )
        assertEquals(0L, result.remainingAmount)
        assertEquals(1f, result.progressPercent, 0.01f)
    }

    @Test
    fun `goal overfunded`() {
        val result = FinanceCalculations.calculateGoalProgress(
            targetAmountKurus = 100000L,
            savedAmountKurus = 150000L,
            remainingMonths = 5
        )
        assertEquals(0L, result.remainingAmount)
        assertEquals(1f, result.progressPercent, 0.01f)
    }

    // ═══ HESAP MAKİNESİ TESTLERİ ═══

    @Test
    fun `basic addition`() {
        val result = FinanceCalculations.evaluateExpression("2+3")
        assertNotNull(result)
        assertEquals(BigDecimal("5.00"), result)
    }

    @Test
    fun `multiplication and addition precedence`() {
        val result = FinanceCalculations.evaluateExpression("2+3×4")
        assertNotNull(result)
        assertEquals(BigDecimal("14.00"), result)
    }

    @Test
    fun `parentheses`() {
        val result = FinanceCalculations.evaluateExpression("(2+3)×4")
        assertNotNull(result)
        assertEquals(BigDecimal("20.00"), result)
    }

    @Test
    fun `division`() {
        val result = FinanceCalculations.evaluateExpression("10÷4")
        assertNotNull(result)
        assertEquals(BigDecimal("2.50"), result)
    }

    @Test
    fun `percentage`() {
        val result = FinanceCalculations.evaluateExpression("200×10%")
        assertNotNull(result)
        assertEquals(BigDecimal("20.00"), result)
    }

    @Test
    fun `negative number`() {
        val result = FinanceCalculations.evaluateExpression("-5+3")
        assertNotNull(result)
        assertEquals(BigDecimal("-2.00"), result)
    }

    @Test
    fun `complex expression`() {
        val result = FinanceCalculations.evaluateExpression("(100+200)×1.18")
        assertNotNull(result)
        assertEquals(BigDecimal("354.00"), result)
    }

    @Test
    fun `division by zero returns null`() {
        val result = FinanceCalculations.evaluateExpression("5÷0")
        assertNull(result)
    }

    @Test
    fun `invalid expression returns null`() {
        val result = FinanceCalculations.evaluateExpression("abc")
        assertNull(result)
    }

    // ═══ PARA BİRİMİ FORMATLAMA TESTLERİ ═══

    @Test
    fun `format currency basic`() {
        assertEquals("300,00 ₺", CurrencyUtils.formatCurrency(30000L))
    }

    @Test
    fun `format currency thousands`() {
        assertEquals("12.450,75 ₺", CurrencyUtils.formatCurrency(1245075L))
    }

    @Test
    fun `format currency zero`() {
        assertEquals("0,00 ₺", CurrencyUtils.formatCurrency(0L))
    }

    @Test
    fun `format currency smart decimal no kurus`() {
        assertEquals("300 ₺", CurrencyUtils.formatCurrencySmartDecimal(30000L))
    }

    @Test
    fun `format currency smart decimal with kurus`() {
        assertEquals("300,50 ₺", CurrencyUtils.formatCurrencySmartDecimal(30050L))
    }

    @Test
    fun `parse turkish currency`() {
        assertEquals(1245075L, CurrencyUtils.parseTurkishCurrency("12.450,75 ₺"))
    }

    @Test
    fun `parse turkish currency simple`() {
        assertEquals(30000L, CurrencyUtils.parseTurkishCurrency("300"))
    }

    @Test
    fun `parse invalid returns null`() {
        assertNull(CurrencyUtils.parseTurkishCurrency("abc"))
    }
}
