package nl.ovfietsbeschikbaarheid.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.ovfietsbeschikbaarheid.R
import nl.ovfietsbeschikbaarheid.ext.atEndOfDay
import nl.ovfietsbeschikbaarheid.model.CapacityModel
import nl.ovfietsbeschikbaarheid.model.GraphDayModel
import nl.ovfietsbeschikbaarheid.ui.theme.Grey10
import nl.ovfietsbeschikbaarheid.ui.theme.Grey40
import nl.ovfietsbeschikbaarheid.ui.theme.Grey80
import nl.ovfietsbeschikbaarheid.ui.theme.OVFietsBeschikbaarheidTheme
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
import kotlin.math.max

// TODO: skeleton screens

@Composable
fun CapacityGraph(
    graphDays: List<GraphDayModel>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    OvCard {
        Text(
            text = stringResource(R.string.prediction_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        var selectedIndex by remember { mutableIntStateOf(graphDays.indexOfFirst { it.isToday }) }

        // Use the same max capacity for all days for consistency's sake
        val maxCapacity = graphDays.maxOf { graphDay ->
            max(graphDay.capacityHistory.maxOfOrNull { it.capacity } ?: 0, graphDay.capacityPrediction.maxOfOrNull { it.capacity } ?: 0)
        }

        SingleChoiceSegmentedButtonRow(modifier = Modifier.padding(bottom = 24.dp).fillMaxWidth()) {
            graphDays.forEachIndexed { index, graphDay ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = graphDays.size
                    ),
                    onClick = { selectedIndex = index },
                    selected = index == selectedIndex,
                    label = { Text(text = graphDay.dayShortName) },
                    modifier = Modifier.semantics { contentDescription = graphDay.dayFullName }
                )
            }
        }

        val shownGraphDay = graphDays[selectedIndex]

        val history = shownGraphDay.capacityHistory
        val prediction = shownGraphDay.capacityPrediction

        val primaryColor = MaterialTheme.colorScheme.primary
        val labelColor = MaterialTheme.colorScheme.onBackground
        val gridLineColor = if(isSystemInDarkTheme()) Grey80 else Grey10

        Canvas(
            modifier = modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val leftPadding = 24.dp.toPx()
            val bottomPadding = 16.dp.toPx()

            val graphWidth = size.width - leftPadding
            val graphHeight = size.height - bottomPadding

            val niceStep = when {
                maxCapacity <= 10 -> 1
                maxCapacity <= 25 -> 5
                maxCapacity <= 50 -> 10
                maxCapacity <= 100 -> 20
                maxCapacity <= 200 -> 50
                maxCapacity <= 500 -> 100
                else -> 200
            }
            val roundedMax = ceil(maxCapacity.toFloat() / niceStep) * niceStep

            // ----- Draw Y grid lines and labels -----
            for (i in 0..(roundedMax / niceStep).toInt()) {
                val yVal = i * niceStep
                val y = graphHeight - (yVal / roundedMax) * graphHeight

                drawLine(
                    color = if (i == 0) Color.LightGray else gridLineColor,
                    start = Offset(leftPadding, y),
                    end = Offset(leftPadding + graphWidth, y),
                    strokeWidth = if (i == 0) 1.dp.toPx() else 2.dp.toPx()
                )

                val label = yVal.toString()
                val textLayout = textMeasurer.measure(
                    text = AnnotatedString(label),
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = labelColor,
                        textAlign = TextAlign.Right
                    )
                )

                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(
                        x = leftPadding - 8.dp.toPx() - textLayout.size.width,
                        y = y - textLayout.size.height / 2
                    )
                )
            }

            // ----- Draw the capacity line -----
            val startTime = if (history.isNotEmpty())
                history[0].dateTime.truncatedTo(ChronoUnit.HOURS)
            else
                prediction[0].dateTime.truncatedTo(ChronoUnit.DAYS)

            val endTime = startTime.atEndOfDay()
            val duration = Duration.between(startTime, endTime).toMillis().toFloat()
            if (history.isNotEmpty()) {
                val points = history.map { model ->
                    val x = leftPadding + (Duration.between(startTime, model.dateTime).toMillis() / duration) * graphWidth
                    val y = graphHeight - (model.capacity / roundedMax) * graphHeight
                    Offset(x, y)
                }

                val path = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (pt in points) {
                        lineTo(pt.x, pt.y)
                    }
                }

                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // ----- Draw the prediction -----
            if (prediction.isNotEmpty()) {
                val startTimePrediction = prediction[0].dateTime.truncatedTo(ChronoUnit.DAYS)
                val predictionPoints = prediction.map { model ->
                    val x = leftPadding + (Duration.between(startTimePrediction, model.dateTime).toMillis() / duration) * graphWidth
                    val y = graphHeight - (model.capacity / roundedMax) * graphHeight
                    Offset(x, y)
                }

                val predictionPath = Path().apply {
                    moveTo(predictionPoints.first().x, predictionPoints.first().y)
                    for (pt in predictionPoints) {
                        lineTo(pt.x, pt.y)
                    }
                }

                val dashLength = 4.dp.toPx()
                val gapLength = 12.dp.toPx()

                drawPath(
                    path = predictionPath,
                    color = Grey40,
                    style = Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(dashLength, gapLength),
                            phase = 0f
                        )
                    )
                )
            }

            // ----- Draw X-axis hour labels (on top, outside plot area) -----
            for (i in 0..6) {
                val hourTime = startTime.plus(i * 4L, ChronoUnit.HOURS)
                val x = leftPadding + (Duration.between(startTime, hourTime).toMillis() / duration) * graphWidth
                val label = hourTime.hour.toString().padStart(2, '0') + ":00"

                val textLayoutResult = textMeasurer.measure(
                    text = AnnotatedString(label),
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = labelColor,
                        textAlign = TextAlign.Center
                    )
                )

                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        x = x - textLayoutResult.size.width / 2f,
                        y = graphHeight + 8.dp.toPx()
                    )
                )
            }
        }

        // TODO: show a legend instead with the applicable colors.
        Text(
            text = stringResource(R.string.details_prediction_explanation),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark mode")
@Composable
fun CapacityGraphPreview() {
    val amsterdamZoneId = ZoneId.of("Europe/Amsterdam")
    val now = ZonedDateTime.of(2025, 7, 12, 11, 35, 30, 500, amsterdamZoneId)
    val start = ZonedDateTime.of(2025, 7, 12, 0, 1, 25, 250, amsterdamZoneId)
    val startPrediction = ZonedDateTime.of(2025, 7, 5, 12, 2, 15, 150, amsterdamZoneId)

    val data = listOf(
        CapacityModel(20, start),
        CapacityModel(19, start.plusHours(1)),
        CapacityModel(18, start.plusHours(2)),
        CapacityModel(16, start.plusHours(3)),
        CapacityModel(16, start.plusHours(4)),
        CapacityModel(13, start.plusHours(5)),
        CapacityModel(0, start.plusHours(6)),
        CapacityModel(2, start.plusHours(7)),
        CapacityModel(22, start.plusHours(8)),
        CapacityModel(18, start.plusHours(9)),
        CapacityModel(14, start.plusHours(10)),
        CapacityModel(15, start.plusHours(11)),
        CapacityModel(14, now)
    )
    val prediction = listOf(
        CapacityModel(25, startPrediction),
        CapacityModel(27, startPrediction.plusHours(1)),
        CapacityModel(28, startPrediction.plusHours(2)),
        CapacityModel(29, startPrediction.plusHours(3)),
        CapacityModel(30, startPrediction.plusHours(4)),
        CapacityModel(29, startPrediction.plusHours(5)),
        CapacityModel(31, startPrediction.plusHours(6)),
        CapacityModel(32, startPrediction.plusHours(7)),
        CapacityModel(31, startPrediction.plusHours(8)),
        CapacityModel(31, startPrediction.plusHours(9)),
        CapacityModel(32, startPrediction.plusHours(10)),
        CapacityModel(32, startPrediction.plusHours(11)),
    )
    val graphDays = GraphDayModel(
        isToday = true,
        dayFullName = "Maandag",
        dayShortName = "M",
        capacityHistory = data,
        capacityPrediction = prediction
    )

    OVFietsBeschikbaarheidTheme {
        Surface {
            CapacityGraph(listOf(graphDays))
        }
    }
}