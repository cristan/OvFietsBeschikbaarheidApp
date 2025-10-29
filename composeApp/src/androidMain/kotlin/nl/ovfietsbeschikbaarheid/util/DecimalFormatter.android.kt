package nl.ovfietsbeschikbaarheid.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

actual class DecimalFormatter {

    @Suppress("DEPRECATION")
    // The alternative is Locale.of, but that's not available with the current minSdkVersion
    private val dutchLocale = Locale("NL", "nl")

    actual fun format(number: Double, numberOfDecimals: Int): String {
        val symbols = DecimalFormatSymbols(dutchLocale)

        val kmFormat = DecimalFormat().apply {
            minimumFractionDigits = numberOfDecimals
            maximumFractionDigits = numberOfDecimals
            decimalFormatSymbols = symbols
        }

        return kmFormat.format(number)
    }
}