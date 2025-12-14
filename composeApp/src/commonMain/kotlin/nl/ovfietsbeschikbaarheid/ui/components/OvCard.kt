package nl.ovfietsbeschikbaarheid.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nl.ovfietsbeschikbaarheid.ui.navigation.LocalSpacing
import nl.ovfietsbeschikbaarheid.ui.theme.OVFietsBeschikbaarheidTheme

@Composable
fun OvCard(
    content: @Composable ColumnScope.() -> Unit
) {
    val spacing = LocalSpacing.current
    Card(
        modifier = Modifier
            .padding(top = spacing)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSystemInDarkTheme()) Color.Unspecified else Color.White,
        ),
    ) {
        Column(Modifier.padding(spacing), content = content)
    }
}

@Preview
@Composable
fun OvCardPreview() {
    OVFietsBeschikbaarheidTheme {
        OvCard {
            Text(text = "Test")
        }
    }
}