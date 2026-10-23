@file:OptIn(ExperimentalTime::class)

package nl.ovfietsbeschikbaarheid.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.format.char
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
import nl.ovfietsbeschikbaarheid.resources.graph_selected_capacity
import nl.ovfietsbeschikbaarheid.resources.graph_selected_capacity_last_week
import nl.ovfietsbeschikbaarheid.resources.graph_this_week_day
import nl.ovfietsbeschikbaarheid.resources.graph_today
import nl.ovfietsbeschikbaarheid.ui.theme.Grey10
import nl.ovfietsbeschikbaarheid.ui.theme.Grey40
import nl.ovfietsbeschikbaarheid.ui.theme.Grey80
import nl.ovfietsbeschikbaarheid.ui.theme.OVFietsBeschikbaarheidTheme
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
fun CapacityGraph(
    graphDays: List<GraphDayModel>,
    chartHeight: Dp = 140.dp,
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

        val startTime = if (history.isNotEmpty())
            history[0].createTime.toLocalDateTime(dutchTimeZone).truncateToHour()
        else
            prediction[0].createTime.toLocalDateTime(dutchTimeZone).truncateToDay()

        val startInstant = startTime.toInstant(dutchTimeZone)
        val endTime = startTime.atEndOfDay()
        val duration = startTime.millisecondsUntil(endTime, dutchTimeZone).toFloat()

        val historyPoints = history.toGraphPoints(startInstant, duration, isPrediction = false)
        val predictionPoints = prediction.firstOrNull()?.let { firstPrediction ->
            val startTimePrediction = firstPrediction.createTime.toLocalDateTime(dutchTimeZone).truncateToDay().toInstant(dutchTimeZone)
            prediction.toGraphPoints(startTimePrediction, duration, isPrediction = true)
        }.orEmpty()
        val graphPoints = historyPoints + predictionPoints

        var selectedPoint by remember(graphPoints) { mutableStateOf<GraphPoint?>(null) }
        val selectedPointAndText = selectedPoint?.let { it to it.selectedCapacityText() }

        val primaryColor = MaterialTheme.colorScheme.primary
        val labelColor = MaterialTheme.colorScheme.onBackground
        val gridLineColor = if(isSystemInDarkTheme()) Grey80 else Grey10
        val selectionLineColor = MaterialTheme.colorScheme.outline
        val tooltipBackgroundColor = MaterialTheme.colorScheme.inverseSurface
        val tooltipTextStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.inverseOnSurface)

        Canvas(
            modifier = modifier
                .semantics {
                    contentDescription = shownGraphDay.contentDescription
                }
                .pointerInput(graphPoints) {
                    val leftPadding = graphLeftPadding.toPx()
                    fun pointNearest(canvasX: Float) = graphPoints.nearestTo(fractionOfGraphWidth = (canvasX - leftPadding) / (size.width - leftPadding))

                    awaitEachGesture {
                        val down = awaitFirstDown()
                        selectedPoint = pointNearest(down.position.x)
                        val horizontalDragStart = awaitHorizontalTouchSlopOrCancellation(down.id) { change, _ ->
                            change.consume()
                            selectedPoint = pointNearest(change.position.x)
                        }
                        if (horizontalDragStart != null) {
                            horizontalDrag(horizontalDragStart.id) { change ->
                                change.consume()
                                selectedPoint = pointNearest(change.position.x)
                            }
                        }
                        selectedPoint = null
                    }
                }
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            val leftPadding = graphLeftPadding.toPx()
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

            fun GraphPoint.toOffset() = Offset(
                x = leftPadding + fractionOfGraphWidth * graphWidth,
                y = graphHeight - (capacity / roundedMax) * graphHeight
            )

            // ----- Draw the capacity line -----
            if (historyPoints.isNotEmpty()) {
                drawPath(
                    path = smoothPathThrough(historyPoints.map { it.toOffset() }),
                    color = primaryColor,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // ----- Draw the prediction -----
            if (predictionPoints.isNotEmpty()) {
                val dashLength = 4.dp.toPx()
                val gapLength = 12.dp.toPx()

                drawPath(
                    path = smoothPathThrough(predictionPoints.map { it.toOffset() }),
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
                val hourTime = startInstant.plus(i * 4, DateTimeUnit.HOUR).toLocalDateTime(dutchTimeZone)
                val x = leftPadding + (startTime.millisecondsUntil(hourTime, dutchTimeZone) / duration) * graphWidth
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

            selectedPointAndText?.let { (point, selectedCapacityText) ->
                val markerCenter = point.toOffset()
                val markerRadius = 6.dp.toPx()
                drawLine(
                    color = selectionLineColor,
                    start = Offset(markerCenter.x, 0f),
                    end = Offset(markerCenter.x, graphHeight),
                    strokeWidth = 1.dp.toPx()
                )
                drawCircle(
                    color = if (point.isPrediction) Grey40 else primaryColor,
                    radius = markerRadius,
                    center = markerCenter
                )

                val tooltipHorizontalPadding = 8.dp.toPx()
                val tooltipVerticalPadding = 4.dp.toPx()
                val tooltipTextLayout = textMeasurer.measure(
                    text = selectedCapacityText,
                    style = tooltipTextStyle,
                    constraints = Constraints(maxWidth = (size.width - 2 * tooltipHorizontalPadding).toInt())
                )
                val tooltipSize = Size(
                    width = tooltipTextLayout.size.width + 2 * tooltipHorizontalPadding,
                    height = tooltipTextLayout.size.height + 2 * tooltipVerticalPadding
                )
                val tooltipLeft = (markerCenter.x - tooltipSize.width / 2).coerceIn(0f, size.width - tooltipSize.width)
                val tooltipMarkerGap = 4.dp.toPx()
                val tooltipAtTopClearsMarker = tooltipSize.height + tooltipMarkerGap <= markerCenter.y - markerRadius
                val tooltipTop = if (tooltipAtTopClearsMarker) 0f else (markerCenter.y + markerRadius + tooltipMarkerGap).coerceAtMost(size.height - tooltipSize.height)
                drawRoundRect(
                    color = tooltipBackgroundColor,
                    topLeft = Offset(tooltipLeft, tooltipTop),
                    size = tooltipSize,
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
                drawText(
                    textLayoutResult = tooltipTextLayout,
                    topLeft = Offset(tooltipLeft + tooltipHorizontalPadding, tooltipTop + tooltipVerticalPadding)
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

private val graphLeftPadding = 24.dp

private val hourMinuteFormat = LocalTime.Format {
    hour()
    char(':')
    minute()
}

private data class GraphPoint(
    val capacity: Int,
    val createTime: Instant,
    val fractionOfGraphWidth: Float,
    val isPrediction: Boolean,
)

private fun List<CapacityModel>.toGraphPoints(graphStart: Instant, graphDurationMillis: Float, isPrediction: Boolean) = map { model ->
    GraphPoint(model.capacity, model.createTime, (model.createTime - graphStart).inWholeMilliseconds / graphDurationMillis, isPrediction)
}

private fun List<GraphPoint>.nearestTo(fractionOfGraphWidth: Float) = minBy { abs(it.fractionOfGraphWidth - fractionOfGraphWidth) }

@Composable
private fun GraphPoint.selectedCapacityText(): String {
    val time = createTime.toLocalDateTime(dutchTimeZone).time.format(hourMinuteFormat)
    val resource = if (isPrediction) Res.plurals.graph_selected_capacity_last_week else Res.plurals.graph_selected_capacity
    return pluralStringResource(resource, capacity, capacity, time)
}

private fun smoothPathThrough(points: List<Offset>) = Path().apply {
    moveTo(points.first().x, points.first().y)
    // A zero-length line, so the round stroke cap draws a lone point as a dot
    lineTo(points.first().x, points.first().y)
    monotoneCubicSegments(points).forEach { segment ->
        cubicTo(segment.control1.x, segment.control1.y, segment.control2.x, segment.control2.y, segment.end.x, segment.end.y)
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