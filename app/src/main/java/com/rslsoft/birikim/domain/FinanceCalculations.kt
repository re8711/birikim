package com.rslsoft.birikim.domain

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * Abonelik/fatura, taksit ve bileşik faiz hesaplama fonksiyonları.
 * Tüm para değerleri Long (kuruş) cinsindendir.
 * Saf fonksiyonlar — yan etkisiz, test edilebilir.
 */
object FinanceCalculations {

    private val mc = MathContext.DECIMAL128

    // ═══════════════════════════════════════
    //  ABONELİK / FATURA HESAPLAMA
    // ═══════════════════════════════════════

    data class SubscriptionResult(
        val monthlyCost: Long,
        val quarterlyCost: Long,
        val semiAnnualCost: Long,
        val yearlyCost: Long,
        val fiveYearCost: Long,
        val monthlyBreakdown: List<Long> // 12 aylık zamlı döküm
    )

    /**
     * Abonelik / fatura maliyetini hesaplar.
     *
     * @param amountKurus Tutar (kuruş cinsinden)
     * @param frequency Ödeme sıklığı
     * @param annualIncreasePercent Yıllık zam oranı (ör. 20.0 → %20)
     * @return Aylık, çeyreklik, yarı yıllık, yıllık ve 5 yıllık toplam
     */
    fun calculateSubscription(
        amountKurus: Long,
        frequency: SubscriptionFrequency,
        annualIncreasePercent: Double = 0.0
    ): SubscriptionResult {
        val monthlyBase = toMonthlyCost(amountKurus, frequency)

        if (annualIncreasePercent <= 0.0) {
            // Zam yok — basit çarpım
            return SubscriptionResult(
                monthlyCost = monthlyBase,
                quarterlyCost = monthlyBase * 3,
                semiAnnualCost = monthlyBase * 6,
                yearlyCost = monthlyBase * 12,
                fiveYearCost = monthlyBase * 60,
                monthlyBreakdown = List(12) { monthlyBase }
            )
        }

        // Zamlı hesaplama: her ay bir öncekinin üzerine aylık zam oranı eklenir
        val monthlyRate = BigDecimal(annualIncreasePercent)
            .divide(BigDecimal(1200), mc) // yıllık % → aylık oran

        val breakdown = mutableListOf<Long>()
        var current = BigDecimal(monthlyBase)
        for (month in 0 until 12) {
            breakdown.add(current.setScale(0, RoundingMode.HALF_UP).toLong())
            current = current.multiply(BigDecimal.ONE.add(monthlyRate), mc)
        }

        val yearlyTotal = breakdown.sum()

        // 5 yıllık hesaplama (zamlı)
        var fiveYearTotal = 0L
        var fiveYearCurrent = BigDecimal(monthlyBase)
        for (month in 0 until 60) {
            fiveYearTotal += fiveYearCurrent.setScale(0, RoundingMode.HALF_UP).toLong()
            fiveYearCurrent = fiveYearCurrent.multiply(BigDecimal.ONE.add(monthlyRate), mc)
        }

        return SubscriptionResult(
            monthlyCost = monthlyBase,
            quarterlyCost = breakdown.take(3).sum(),
            semiAnnualCost = breakdown.take(6).sum(),
            yearlyCost = yearlyTotal,
            fiveYearCost = fiveYearTotal,
            monthlyBreakdown = breakdown
        )
    }

    enum class SubscriptionFrequency {
        WEEKLY, MONTHLY, QUARTERLY, SEMI_ANNUALLY, YEARLY
    }

    private fun toMonthlyCost(amountKurus: Long, freq: SubscriptionFrequency): Long {
        val amount = BigDecimal(amountKurus)
        return when (freq) {
            SubscriptionFrequency.WEEKLY ->
                amount.multiply(BigDecimal("4.33")).setScale(0, RoundingMode.HALF_UP).toLong()
            SubscriptionFrequency.MONTHLY -> amountKurus
            SubscriptionFrequency.QUARTERLY ->
                amount.divide(BigDecimal(3), 0, RoundingMode.HALF_UP).toLong()
            SubscriptionFrequency.SEMI_ANNUALLY ->
                amount.divide(BigDecimal(6), 0, RoundingMode.HALF_UP).toLong()
            SubscriptionFrequency.YEARLY ->
                amount.divide(BigDecimal(12), 0, RoundingMode.HALF_UP).toLong()
        }
    }

    // ═══════════════════════════════════════
    //  TAKSİT HESAPLAMA
    // ═══════════════════════════════════════

    data class InstallmentResult(
        val monthlyPayment: Long,
        val totalPayment: Long,
        val totalInterest: Long
    )

    /**
     * Taksit hesaplama (sabit aylık taksit — annüite formülü).
     *
     * @param totalAmountKurus Toplam borç tutarı (kuruş)
     * @param installmentCount Taksit sayısı
     * @param monthlyInterestPercent Aylık faiz oranı (ör. 3.5 → %3.5)
     * @return Aylık taksit, toplam ödeme, toplam faiz
     */
    fun calculateInstallment(
        totalAmountKurus: Long,
        installmentCount: Int,
        monthlyInterestPercent: Double
    ): InstallmentResult {
        require(installmentCount > 0) { "Taksit sayısı 0'dan büyük olmalı" }
        require(totalAmountKurus > 0) { "Tutar 0'dan büyük olmalı" }

        if (monthlyInterestPercent <= 0.0) {
            // Faizsiz taksit
            val monthly = BigDecimal(totalAmountKurus)
                .divide(BigDecimal(installmentCount), 0, RoundingMode.HALF_UP).toLong()
            val total = monthly * installmentCount
            return InstallmentResult(
                monthlyPayment = monthly,
                totalPayment = total,
                totalInterest = total - totalAmountKurus
            )
        }

        val principal = BigDecimal(totalAmountKurus)
        val rate = BigDecimal(monthlyInterestPercent).divide(BigDecimal(100), mc)
        val n = installmentCount

        // Annüite formülü: M = P * [r(1+r)^n] / [(1+r)^n - 1]
        val onePlusR = BigDecimal.ONE.add(rate, mc)
        val onePlusRpowN = onePlusR.pow(n, mc)

        val numerator = principal.multiply(rate, mc).multiply(onePlusRpowN, mc)
        val denominator = onePlusRpowN.subtract(BigDecimal.ONE, mc)

        val monthlyPayment = numerator.divide(denominator, 0, RoundingMode.HALF_UP).toLong()
        val totalPayment = monthlyPayment * n
        val totalInterest = totalPayment - totalAmountKurus

        return InstallmentResult(
            monthlyPayment = monthlyPayment,
            totalPayment = totalPayment,
            totalInterest = totalInterest
        )
    }

    // ═══════════════════════════════════════
    //  BİLEŞİK FAİZ / BİRİKİM HESAPLAMA
    // ═══════════════════════════════════════

    data class CompoundInterestResult(
        val totalAmount: Long,
        val totalDeposited: Long,
        val totalInterestEarned: Long
    )

    /**
     * Bileşik faiz ile birikim hesaplama.
     *
     * @param monthlyDepositKurus Aylık yatırılan tutar (kuruş)
     * @param annualInterestPercent Yıllık faiz oranı (ör. 45.0 → %45)
     * @param months Süre (ay olarak)
     * @return Vade sonu toplam, yatırılan, kazanılan faiz
     */
    fun calculateCompoundInterest(
        monthlyDepositKurus: Long,
        annualInterestPercent: Double,
        months: Int
    ): CompoundInterestResult {
        require(months > 0) { "Süre 0'dan büyük olmalı" }
        require(monthlyDepositKurus > 0) { "Aylık birikim 0'dan büyük olmalı" }

        val totalDeposited = monthlyDepositKurus * months

        if (annualInterestPercent <= 0.0) {
            return CompoundInterestResult(
                totalAmount = totalDeposited,
                totalDeposited = totalDeposited,
                totalInterestEarned = 0
            )
        }

        val monthlyRate = BigDecimal(annualInterestPercent)
            .divide(BigDecimal(1200), mc)
        val deposit = BigDecimal(monthlyDepositKurus)

        // FV = P × [(1+r)^n - 1] / r  (her ay başında yatırım)
        val onePlusR = BigDecimal.ONE.add(monthlyRate, mc)
        val onePlusRpowN = onePlusR.pow(months, mc)

        val futureValue = deposit
            .multiply(onePlusRpowN.subtract(BigDecimal.ONE, mc), mc)
            .divide(monthlyRate, 0, RoundingMode.HALF_UP)

        val totalAmount = futureValue.toLong()
        val interestEarned = totalAmount - totalDeposited

        return CompoundInterestResult(
            totalAmount = totalAmount,
            totalDeposited = totalDeposited,
            totalInterestEarned = interestEarned
        )
    }

    // ═══════════════════════════════════════
    //  DÖNEM PROJEKSİYONU
    // ═══════════════════════════════════════

    data class PeriodProjection(
        val totalIncome: Long,
        val totalExpense: Long,
        val netAmount: Long, // gelir - gider
        val endBalance: Long, // mevcut bakiye + net
        val expenseToIncomeRatio: Float // gider/gelir oranı (0-1)
    )

    /**
     * Belirli bir dönem için gelir-gider projeksiyonu yapar.
     *
     * @param currentBalanceKurus Mevcut bakiye
     * @param transactions Tüm işlemler
     * @param isYearly true ise yıllık, false ise aylık projeksiyon
     */
    fun calculatePeriodProjection(
        currentBalanceKurus: Long,
        transactions: List<TransactionData>,
        isYearly: Boolean
    ): PeriodProjection {
        var totalIncome = 0L
        var totalExpense = 0L

        for (t in transactions) {
            val periodAmount = getPeriodAmount(t.amountKurus, t.recurrence, isYearly)
            when (t.type) {
                TransactionDataType.INCOME -> totalIncome += periodAmount
                TransactionDataType.EXPENSE -> totalExpense += periodAmount
            }
        }

        val net = totalIncome - totalExpense
        val endBalance = currentBalanceKurus + net
        val ratio = if (totalIncome > 0) {
            totalExpense.toFloat() / totalIncome.toFloat()
        } else if (currentBalanceKurus > 0) {
            totalExpense.toFloat() / currentBalanceKurus.toFloat()
        } else if (totalExpense > 0) {
            1f
        } else {
            0f
        }

        return PeriodProjection(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netAmount = net,
            endBalance = endBalance,
            expenseToIncomeRatio = ratio.coerceAtLeast(0f)
        )
    }

    data class TransactionData(
        val amountKurus: Long,
        val type: TransactionDataType,
        val recurrence: TransactionRecurrence
    )

    enum class TransactionDataType { INCOME, EXPENSE }

    enum class TransactionRecurrence {
        ONCE, WEEKLY, MONTHLY, QUARTERLY, SEMI_ANNUALLY, YEARLY
    }

    private fun getPeriodAmount(
        amountKurus: Long,
        recurrence: TransactionRecurrence,
        isYearly: Boolean
    ): Long {
        return if (isYearly) {
            when (recurrence) {
                TransactionRecurrence.ONCE -> amountKurus
                TransactionRecurrence.WEEKLY -> amountKurus * 52
                TransactionRecurrence.MONTHLY -> amountKurus * 12
                TransactionRecurrence.QUARTERLY -> amountKurus * 4
                TransactionRecurrence.SEMI_ANNUALLY -> amountKurus * 2
                TransactionRecurrence.YEARLY -> amountKurus
            }
        } else {
            // Aylık
            when (recurrence) {
                TransactionRecurrence.ONCE -> amountKurus
                TransactionRecurrence.WEEKLY -> {
                    val amount = BigDecimal(amountKurus)
                    amount.multiply(BigDecimal("4.33")).setScale(0, RoundingMode.HALF_UP).toLong()
                }
                TransactionRecurrence.MONTHLY -> amountKurus
                TransactionRecurrence.QUARTERLY -> {
                    BigDecimal(amountKurus)
                        .divide(BigDecimal(3), 0, RoundingMode.HALF_UP).toLong()
                }
                TransactionRecurrence.SEMI_ANNUALLY -> {
                    BigDecimal(amountKurus)
                        .divide(BigDecimal(6), 0, RoundingMode.HALF_UP).toLong()
                }
                TransactionRecurrence.YEARLY -> {
                    BigDecimal(amountKurus)
                        .divide(BigDecimal(12), 0, RoundingMode.HALF_UP).toLong()
                }
            }
        }
    }

    // ═══════════════════════════════════════
    //  HEDEF HESAPLAMA
    // ═══════════════════════════════════════

    data class GoalCalculation(
        val remainingAmount: Long,
        val progressPercent: Float,
        val monthlyRequired: Long,
        val estimatedMonths: Int
    )

    /**
     * Hedefe ulaşmak için gereken aylık birikim ve tahmini süre hesabı.
     *
     * @param targetAmountKurus Hedef tutar
     * @param savedAmountKurus Biriken tutar
     * @param remainingMonths Kalan ay sayısı (hedef tarihine)
     */
    fun calculateGoalProgress(
        targetAmountKurus: Long,
        savedAmountKurus: Long,
        remainingMonths: Int
    ): GoalCalculation {
        val remaining = (targetAmountKurus - savedAmountKurus).coerceAtLeast(0)
        val progress = if (targetAmountKurus > 0) {
            (savedAmountKurus.toFloat() / targetAmountKurus.toFloat()).coerceIn(0f, 1f)
        } else 0f

        val monthlyRequired = if (remainingMonths > 0) {
            BigDecimal(remaining)
                .divide(BigDecimal(remainingMonths), 0, RoundingMode.CEILING).toLong()
        } else {
            remaining
        }

        val estimatedMonths = if (monthlyRequired > 0) {
            BigDecimal(remaining)
                .divide(BigDecimal(monthlyRequired), 0, RoundingMode.CEILING).toInt()
        } else 0

        return GoalCalculation(
            remainingAmount = remaining,
            progressPercent = progress,
            monthlyRequired = monthlyRequired,
            estimatedMonths = estimatedMonths
        )
    }

    // ═══════════════════════════════════════
    //  TEMEL HESAP MAKİNESİ
    // ═══════════════════════════════════════

    /**
     * Basit matematiksel ifade değerlendirici.
     * +, -, ×, ÷, %, parantez destekler.
     * Sonuç kuruş olarak döner (2 ondalık hassasiyet).
     */
    fun evaluateExpression(expression: String): BigDecimal? {
        return try {
            val sanitized = expression
                .replace("×", "*")
                .replace("÷", "/")
                .replace(",", ".")
                .replace(" ", "")
            ExpressionParser(sanitized).parse()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Recursive descent parser for basic arithmetic expressions.
     * Grammar:
     *   expression = term (('+' | '-') term)*
     *   term       = factor (('*' | '/') factor)*
     *   factor     = ('+'|'-') factor | '(' expression ')' | number ('%')?
     */
    private class ExpressionParser(private val input: String) {
        private var pos = 0

        fun parse(): BigDecimal {
            val result = parseExpression()
            if (pos < input.length) throw IllegalArgumentException("Unexpected: ${input[pos]}")
            return result.setScale(2, RoundingMode.HALF_UP)
        }

        private fun parseExpression(): BigDecimal {
            var result = parseTerm()
            while (pos < input.length) {
                when (input[pos]) {
                    '+' -> { pos++; result = result.add(parseTerm(), mc) }
                    '-' -> { pos++; result = result.subtract(parseTerm(), mc) }
                    else -> break
                }
            }
            return result
        }

        private fun parseTerm(): BigDecimal {
            var result = parseFactor()
            while (pos < input.length) {
                when (input[pos]) {
                    '*' -> { pos++; result = result.multiply(parseFactor(), mc) }
                    '/' -> {
                        pos++
                        val divisor = parseFactor()
                        if (divisor.compareTo(BigDecimal.ZERO) == 0) throw ArithmeticException("Division by zero")
                        result = result.divide(divisor, mc)
                    }
                    else -> break
                }
            }
            return result
        }

        private fun parseFactor(): BigDecimal {
            if (pos < input.length && input[pos] == '+') { pos++; return parseFactor() }
            if (pos < input.length && input[pos] == '-') { pos++; return parseFactor().negate() }

            var result: BigDecimal

            if (pos < input.length && input[pos] == '(') {
                pos++ // skip '('
                result = parseExpression()
                if (pos < input.length && input[pos] == ')') pos++ // skip ')'
            } else {
                result = parseNumber()
            }

            // Handle percent
            if (pos < input.length && input[pos] == '%') {
                pos++
                result = result.divide(BigDecimal(100), mc)
            }

            return result
        }

        private fun parseNumber(): BigDecimal {
            val start = pos
            while (pos < input.length && (input[pos].isDigit() || input[pos] == '.')) {
                pos++
            }
            if (start == pos) throw IllegalArgumentException("Expected number at position $pos")
            return BigDecimal(input.substring(start, pos))
        }
    }
}
