package nl.ovfietsbeschikbaarheid.ext

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.shimmer

@Composable
fun Modifier.shimmerable(
    shimmerInstance: Shimmer,
    shape: Shape = RoundedCornerShape(8.dp),
    color: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
): Modifier {
    return this
        .shimmer(shimmerInstance)
        .background(color = color, shape = shape)
        .drawWithContent {
            // Do not draw the actual content.
        }
}