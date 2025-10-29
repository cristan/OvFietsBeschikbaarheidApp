package nl.ovfietsbeschikbaarheid.ui.screen

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import java.net.URLEncoder

@Composable
actual fun onLocationClicked(): (String) -> Unit {
    val context = LocalContext.current
    val onLocationClicked: (String) -> Unit = { address ->
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = "http://maps.google.co.in/maps?q=${
            URLEncoder.encode(
                address,
                "UTF-8"
            )
        }".toUri()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }
    return onLocationClicked
}