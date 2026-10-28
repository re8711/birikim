package com.rslsoft.birikim.domain

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Para birimi yardımcı fonksiyonları.
 * Dahili olarak tüm para değerleri Long (kuruş) olarak tutulur.
 */
object CurrencyUtils {

    private val turkishLocale = Locale("tr", "TR")

    private val currencyFormat = DecimalFormat("#,##0.00", DecimalFormatSymbols(turkishLocale).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    })

    private val currencyFormatNoDecimal = DecimalFormat("#,##0", DecimalFormatSymbols(turkishLocale).apply {
        groupingSeparator = '.'
    })

    /**
     * Kuruş cinsinden Long değeri "12.450,75 ₺" formatına çevirir.
     */
    fun formatCurrency(amountKurus: Long): String {
        val lira = amountKurus / 100.0
        return "${currencyFormat.format(lira)} ₺"
    }

    /**
     * Kuruş cinsinden Long değeri kuruş yoksa tam sayı olarak formatlar.
     */
    fun formatCurrencySmartDecimal(amountKurus: Long): String {
        return if (amountKurus % 100 == 0L) {
            "${currencyFormatNoDecimal.format(amountKurus / 100)} ₺"
        } else {
            formatCurrency(amountKurus)
        }
    }

    /**
     * Ondalıklı String'i (ör. "12450.75") kuruş Long'a çevirir.
     */
    fun parseTurkishCurrency(text: String): Long? {
        return try {
            val cleaned = text
                .replace("₺", "")
                .replace(" ", "")
                .replace(".", "")
                .replace(",", ".")
                .trim()
            val bd = BigDecimal(cleaned)
            bd.multiply(BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).toLong()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Kuruş değerini BigDecimal lira olarak döndürür.
     */
    fun kurusToLira(kurus: Long): BigDecimal {
        return BigDecimal(kurus).divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
    }

    /**
     * BigDecimal lira değerini kuruş Long'a çevirir.
     */
    fun liraToKurus(lira: BigDecimal): Long {
        return lira.multiply(BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).toLong()
    }

    /**
     * Sadece rakamları formatlar (currency sembolü olmadan).
     */
    fun formatNumber(amountKurus: Long): String {
        val lira = amountKurus / 100.0
        return currencyFormat.format(lira)
    }
}
