package nl.ovfietsbeschikbaarheid.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import nl.ovfietsbeschikbaarheid.ext.withStyledLink
import nl.ovfietsbeschikbaarheid.resources.Res
import nl.ovfietsbeschikbaarheid.resources.about_privacy_text_1
import nl.ovfietsbeschikbaarheid.resources.about_privacy_text_2
import nl.ovfietsbeschikbaarheid.resources.about_privacy_text_3
import nl.ovfietsbeschikbaarheid.resources.about_privacy_title
import nl.ovfietsbeschikbaarheid.resources.about_privacy_usage_statistics_switch
import org.jetbrains.compose.resources.stringResource

@Composable
fun AboutPrivacySection(
    isUsageStatisticsEnabled: Boolean,
    onUsageStatisticsSwitched: (Boolean) -> Unit,
) {
    Column {
        Text(
            text = stringResource(Res.string.about_privacy_title),
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = buildAnnotatedString {
                append(stringResource(Res.string.about_privacy_text_1))
                withStyledLink(
                    url = "https://github.com/cristan/OvFietsBeschikbaarheidApp/blob/main/PRIVACY_POLICY.md",
                    text = stringResource(Res.string.about_privacy_text_2)
                )
                append(stringResource(Res.string.about_privacy_text_3))
            },
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyLarge
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .toggleable(
                    value = isUsageStatisticsEnabled,
                    role = Role.Switch,
                    onValueChange = onUsageStatisticsSwitched
                )
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.about_privacy_usage_statistics_switch),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = isUsageStatisticsEnabled,
                onCheckedChange = null
            )
        }
    }
}
