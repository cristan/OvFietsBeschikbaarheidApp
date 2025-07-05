package nl.ovfietsbeschikbaarheid.util

import java.util.Locale

@Suppress("DEPRECATION")
// The alternative is Locale.of, but that's not available with the current minSdkVersion
val dutchLocale = Locale("NL", "nl")