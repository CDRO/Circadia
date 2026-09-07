package ch.circadia.tracker.feature.timeline

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import ch.circadia.tracker.core.designsystem.ChartColors
import ch.circadia.tracker.core.model.Interval
import ch.circadia.tracker.core.model.SleepState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ActogramDay(
    val date: LocalDate,
    val startTimeUtc: Long,
    val endTimeUtc: Long,
    val intervals: List<Interval>
)

@Composable
fun ActogramRenderer(
    days: List<ActogramDay>,
    modifier: Modifier = Modifier
) {
    val rowHeight = 48.dp
    val labelWidth = 60.dp
    val totalHeight = rowHeight * days.size

    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.")

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight)
    ) {
        val width = size.width
        val rowHeightPx = rowHeight.toPx()
        val labelWidthPx = labelWidth.toPx()
        val chartWidth = width - labelWidthPx

        days.forEachIndexed { index, day ->
            val top = index * rowHeightPx
            
            // Draw day label
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 12.dp.toPx()
                }
                canvas.nativeCanvas.drawText(
                    day.date.format(dateFormatter),
                    8.dp.toPx(),
                    top + rowHeightPx / 2 + 6.dp.toPx(),
                    paint
                )
            }

            // Draw background line
            drawLine(
                color = ChartColors.Axis,
                start = Offset(labelWidthPx, top + rowHeightPx),
                end = Offset(width, top + rowHeightPx),
                strokeWidth = 1f
            )

            // Draw intervals
            day.intervals.filter { it.state == SleepState.ASLEEP }.forEach { interval ->
                val startRatio = (interval.startUtcMillis - day.startTimeUtc).toFloat() / (day.endTimeUtc - day.startTimeUtc)
                val endRatio = (interval.endUtcMillis - day.startTimeUtc).toFloat() / (day.endTimeUtc - day.startTimeUtc)
                
                val left = labelWidthPx + startRatio.coerceIn(0f, 1f) * chartWidth
                val right = labelWidthPx + endRatio.coerceIn(0f, 1f) * chartWidth
                
                if (right > left) {
                    drawRect(
                        color = Color.Black, // TODO: Use person color
                        topLeft = Offset(left, top + 8.dp.toPx()),
                        size = Size(right - left, rowHeightPx - 16.dp.toPx())
                    )
                }
            }
        }
        
        // Draw vertical time markers (6h, 12h, 18h)
        listOf(0.25f, 0.5f, 0.75f).forEach { ratio ->
            val x = labelWidthPx + ratio * chartWidth
            drawLine(
                color = ChartColors.Axis,
                start = Offset(x, 0f),
                end = Offset(x, totalHeight.toPx()),
                strokeWidth = 1f
            )
        }
    }
}
