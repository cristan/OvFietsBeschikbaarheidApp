package nl.ovfietsbeschikbaarheid.ui.components

import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
import kotlin.math.hypot

class CubicBezierSegment(val control1: Offset, val control2: Offset, val end: Offset)

/**
 * The cubic Bézier segments of a smooth curve that starts at the first point and runs through the rest,
 * each segment starting where the previous one ends.
 * Between two neighbouring points, the curve never rises above or dips below them.
 * See the Fritsch-Carlson method on https://en.wikipedia.org/wiki/Monotone_cubic_interpolation
 */
fun monotoneCubicSegments(points: List<Offset>): List<CubicBezierSegment> {
    if (points.size < 2) return emptyList()

    val secants = points.zipWithNext { start, end -> (end.y - start.y) / (end.x - start.x) }
    val tangents = FloatArray(points.size) { index ->
        when (index) {
            0 -> secants.first()
            points.lastIndex -> secants.last()
            else -> {
                val secantBefore = secants[index - 1]
                val secantAfter = secants[index]
                if (secantBefore * secantAfter <= 0f) 0f else (secantBefore + secantAfter) / 2
            }
        }
    }
    secants.forEachIndexed { index, secant ->
        val tangentPairLength = hypot(tangents[index], tangents[index + 1])
        val maxTangentPairLength = 3 * abs(secant)
        if (tangentPairLength > maxTangentPairLength) {
            tangents[index] *= maxTangentPairLength / tangentPairLength
            tangents[index + 1] *= maxTangentPairLength / tangentPairLength
        }
    }

    return points.zipWithNext().mapIndexed { index, (start, end) ->
        val thirdOfWidth = (end.x - start.x) / 3
        CubicBezierSegment(
            control1 = Offset(start.x + thirdOfWidth, start.y + tangents[index] * thirdOfWidth),
            control2 = Offset(end.x - thirdOfWidth, end.y - tangents[index + 1] * thirdOfWidth),
            end = end,
        )
    }
}
