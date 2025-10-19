package nl.ovfietsbeschikbaarheid.util

expect class DecimalFormatter {
    constructor()

    fun format(number: Double, numberOfDecimals: Int): String
}