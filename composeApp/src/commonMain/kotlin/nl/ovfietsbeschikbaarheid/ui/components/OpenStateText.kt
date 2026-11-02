package nl.ovfietsbeschikbaarheid.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.ovfietsbeschikbaarheid.model.OpenState
import nl.ovfietsbeschikbaarheid.resources.Res
import nl.ovfietsbeschikbaarheid.resources.open_state_closed
import nl.ovfietsbeschikbaarheid.resources.open_state_closes_at
import nl.ovfietsbeschikbaarheid.resources.open_state_closing_soon
import nl.ovfietsbeschikbaarheid.resources.open_state_open_247
import nl.ovfietsbeschikbaarheid.resources.open_state_opens_later_at
import nl.ovfietsbeschikbaarheid.resources.open_state_opens_today_at
import nl.ovfietsbeschikbaarheid.resources.open_until
import nl.ovfietsbeschikbaarheid.ui.theme.Orange50
import nl.ovfietsbeschikbaarheid.ui.theme.Red50
import org.jetbrains.compose.resources.stringResource

@Composable
fun OpenStateText(openState: OpenState, modifier: Modifier = Modifier) {
    Row(modifier) {
        when (openState) {
            is OpenState.Closed -> {
                Text(text = stringResource(Res.string.open_state_closed), color = Red50)
                if (openState.openDay != null) {
                    val openDay = stringResource(openState.openDay).lowercase()
                    Text(text = " " + stringResource(Res.string.open_state_opens_later_at, openDay, openState.openTime))
                } else {
                    Text(text = " " + stringResource(Res.string.open_state_opens_today_at, openState.openTime))
                }
            }

            is OpenState.Closing -> {
                Text(text = stringResource(Res.string.open_state_closing_soon), color = Orange50)
                Text(" " + stringResource(Res.string.open_state_closes_at, openState.closingTime))
            }

            is OpenState.Open -> {
                Text(stringResource(Res.string.open_until, openState.closingTime))
            }

            OpenState.Open247 -> Text(stringResource(Res.string.open_state_open_247))
        }
    }
}
