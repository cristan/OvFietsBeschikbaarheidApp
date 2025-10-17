package nl.ovfietsbeschikbaarheid.util

expect class DecimalFormatter {
    fun format(number: Double, numberOfDecimals: Int): String
}