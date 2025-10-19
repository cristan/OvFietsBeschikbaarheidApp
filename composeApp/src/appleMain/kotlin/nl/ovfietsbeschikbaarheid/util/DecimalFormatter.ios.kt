package nl.ovfietsbeschikbaarheid.util

import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle
import platform.Foundation.numberWithDouble

actual class DecimalFormatter {
    actual fun format(number: Double, numberOfDecimals: Int): String {
        val formatter = NSNumberFormatter().apply {
            numberStyle = NSNumberFormatterDecimalStyle
            minimumFractionDigits = numberOfDecimals.toULong()
            maximumFractionDigits = numberOfDecimals.toULong()
            // Dutch locale (comma as decimal separator)
            locale = NSLocale(localeIdentifier = "nl_NL")
        }

        // Convert the Kotlin Double to an NSNumber
        val nsNumber = NSNumber.numberWithDouble(number)

        // Return the formatted string (or fallback)
        return formatter.stringFromNumber(nsNumber) ?: number.toString()
    }
}