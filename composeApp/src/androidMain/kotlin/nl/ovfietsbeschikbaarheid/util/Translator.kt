package nl.ovfietsbeschikbaarheid.util

import android.content.Context
import androidx.annotation.StringRes

class Translator(val context: Context) {
    fun getString(@StringRes resId: Int, vararg formatArgs: Any?): String {
        return context.getString(resId, *formatArgs)
    }
}