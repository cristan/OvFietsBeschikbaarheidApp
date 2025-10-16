@file:OptIn(ExperimentalTime::class)

package nl.ovfietsbeschikbaarheid.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import nl.ovfietsbeschikbaarheid.ext.atEndOfDay
import nl.ovfietsbeschikbaarheid.ext.dutchTimeZone
import nl.ovfietsbeschikbaarheid.ext.millisecondsUntil
import nl.ovfietsbeschikbaarheid.ext.truncateToDay
import nl.ovfietsbeschikbaarheid.ext.truncateToHour
import nl.ovfietsbeschikbaarheid.model.CapacityModel
import nl.ovfietsbeschikbaarheid.model.GraphDayModel
import nl.ovfietsbeschikbaarheid.resources.Res
import nl.ovfietsbeschikbaarheid.resources.capacity_graph_title
import nl.ovfietsbeschikbaarheid.resources.graph_last_week_day
import nl.ovfietsbeschikbaarheid.resources.graph_this_week_day
import nl.ovfietsbeschikbaarheid.resources.graph_today
import nl.ovfietsbeschikbaarheid.ui.theme.Grey10
import nl.ovfietsbeschikbaarheid.ui.theme.Grey40
import nl.ovfietsbeschikbaarheid.ui.theme.Grey80
import nl.ovfietsbeschikbaarheid.ui.theme.OVFietsBeschikbaarheidTheme
import org.jetbrains.compose.resources.stringResource
import kotlin.math.ceil
import kotlin.math.max
import kotlin.time.ExperimentalTime

@Composable
fun CapacityGraph(
    graphDays: List<GraphDayModel>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    OvCard {
        Text(
            text = stringResource(Res.string.capacity_graph_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        var selectedIndex by rememberSaveable { mutableIntStateOf(graphDays.indexOfFirst { it.isToday }) }

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
                .semantics {
                    contentDescription = shownGraphDay.contentDescription
                }
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
                history[0].createTime.toLocalDateTime(dutchTimeZone).truncateToHour()
            else
                prediction[0].createTime.toLocalDateTime(dutchTimeZone).truncateToDay()

            val endTime = startTime.atEndOfDay()
            val duration = startTime.millisecondsUntil(endTime).toFloat()
            if (history.isNotEmpty()) {
                val points = history.map { model ->
                    val x = leftPadding + ((model.createTime - startTime.toInstant(dutchTimeZone)).inWholeMilliseconds / duration) * graphWidth
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
                val startTimePrediction = prediction[0].createTime.toLocalDateTime(dutchTimeZone).truncateToDay().toInstant(dutchTimeZone)
                val predictionPoints = prediction.map { model ->
                    val x = leftPadding + ((model.createTime - startTimePrediction).inWholeMilliseconds / duration) * graphWidth
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
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength, gapLength))
                    )
                )
            }

            // ----- Draw X-axis hour labels (on top, outside plot area) -----
            for (i in 0..6) {
                val hourTime = startTime.toInstant(dutchTimeZone).plus(i * 4, DateTimeUnit.HOUR).toLocalDateTime(dutchTimeZone)
                val x = leftPadding + (startTime.millisecondsUntil(hourTime) / duration) * graphWidth
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

        Column(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        ) {
            // Primary color line
            if(history.isNotEmpty()) {
                LegendItem(
                    color = MaterialTheme.colorScheme.primary,
                    text = when {
                        shownGraphDay.isToday -> stringResource(Res.string.graph_today)
                        else -> stringResource(Res.string.graph_this_week_day, shownGraphDay.dayFullName.lowercase())
                    }
                )
            }

            // Prediction (dotted line). Only show when more than 1 item (otherwise, you can't see the line)
            if(prediction.size > 1) {
                LegendItem(
                    color = Grey40,
                    isDashed = true,
                    text = stringResource(Res.string.graph_last_week_day, shownGraphDay.dayFullName.lowercase())
                )
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, text: String, isDashed: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Canvas(
            modifier = Modifier
                .size(width = 24.dp, height = 4.dp)
                .padding(end = 8.dp)
        ) {
            val stroke = Stroke(
                width = size.height,
                cap = StrokeCap.Round,
                pathEffect = if (isDashed) PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 8.dp.toPx())) else null
            )
            drawLine(
                color = color,
                start = Offset.Zero,
                end = Offset(size.width, 0f),
                strokeWidth = size.height,
                cap = StrokeCap.Round,
                pathEffect = stroke.pathEffect
            )
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark mode")
@Composable
fun CapacityGraphPreview() {
    val now = LocalDateTime(2025, 7, 12, 11, 35, 30, 500).toInstant(dutchTimeZone)
    val start = LocalDateTime(2025, 7, 12, 0, 1, 25, 250).toInstant(dutchTimeZone)

    val startPrediction = LocalDateTime(2025, 7, 5, 12, 2, 15, 150).toInstant(dutchTimeZone)

    val data = listOf(
        CapacityModel(20, start),
        CapacityModel(19, start.plus(1, DateTimeUnit.HOUR)),
        CapacityModel(18, start.plus(2, DateTimeUnit.HOUR)),
        CapacityModel(16, start.plus(3, DateTimeUnit.HOUR)),
        CapacityModel(16, start.plus(4, DateTimeUnit.HOUR)),
        CapacityModel(13, start.plus(5, DateTimeUnit.HOUR)),
        CapacityModel(0, start.plus(6, DateTimeUnit.HOUR)),
        CapacityModel(2, start.plus(7, DateTimeUnit.HOUR)),
        CapacityModel(22, start.plus(8, DateTimeUnit.HOUR)),
        CapacityModel(18, start.plus(9, DateTimeUnit.HOUR)),
        CapacityModel(14, start.plus(10, DateTimeUnit.HOUR)),
        CapacityModel(15, start.plus(11, DateTimeUnit.HOUR)),
        CapacityModel(14, now)
    )
    val prediction = listOf(
        CapacityModel(25, startPrediction),
        CapacityModel(27, startPrediction.plus(1, DateTimeUnit.HOUR)),
        CapacityModel(28, startPrediction.plus(2, DateTimeUnit.HOUR)),
        CapacityModel(29, startPrediction.plus(3, DateTimeUnit.HOUR)),
        CapacityModel(30, startPrediction.plus(4, DateTimeUnit.HOUR)),
        CapacityModel(29, startPrediction.plus(5, DateTimeUnit.HOUR)),
        CapacityModel(31, startPrediction.plus(6, DateTimeUnit.HOUR)),
        CapacityModel(32, startPrediction.plus(7, DateTimeUnit.HOUR)),
        CapacityModel(31, startPrediction.plus(8, DateTimeUnit.HOUR)),
        CapacityModel(31, startPrediction.plus(9, DateTimeUnit.HOUR)),
        CapacityModel(32, startPrediction.plus(10, DateTimeUnit.HOUR)),
        CapacityModel(32, startPrediction.plus(11, DateTimeUnit.HOUR)),
    )
    val graphDays = GraphDayModel(
        isToday = true,
        dayFullName = "Maandag",
        dayShortName = "M",
        capacityHistory = data,
        capacityPrediction = prediction,
        contentDescription = "",
    )

    OVFietsBeschikbaarheidTheme {
        Surface {
            CapacityGraph(listOf(graphDays))
        }
    }
}