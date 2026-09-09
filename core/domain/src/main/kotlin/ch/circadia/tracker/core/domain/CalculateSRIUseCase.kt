package ch.circadia.tracker.core.domain

import ch.circadia.tracker.core.model.Interval
import ch.circadia.tracker.core.model.SleepState

class CalculateSRIUseCase {
    operator fun invoke(intervals: List<Interval>, startUtc: Long, dayCount: Int): Int? {
        if (dayCount < 7) return null
        
        val minuteRaster = 1440 // Minutes in a day
        val totalMinutes = dayCount * minuteRaster
        val states = IntArray(totalMinutes) { 0 } // 0 = AWAKE, 1 = ASLEEP

        intervals.forEach { interval ->
            val startIdx = ((interval.startUtcMillis - startUtc) / 60000).toInt().coerceIn(0, totalMinutes - 1)
            val endIdx = ((interval.endUtcMillis - startUtc) / 60000).toInt().coerceIn(0, totalMinutes)
            
            if (interval.state == SleepState.ASLEEP) {
                for (i in startIdx until endIdx) {
                    states[i] = 1
                }
            }
        }

        var matchCount = 0L
        val comparisons = (dayCount - 1) * minuteRaster
        
        for (day in 0 until dayCount - 1) {
            for (min in 0 until minuteRaster) {
                val current = states[day * minuteRaster + min]
                val nextDay = states[(day + 1) * minuteRaster + min]
                if (current == nextDay) {
                    matchCount++
                }
            }
        }

        return if (comparisons > 0) {
            // SRI = (Fraction of matching states) * 200 - 100
            val fraction = matchCount.toDouble() / comparisons
            (fraction * 200 - 100).toInt().coerceIn(0, 100)
        } else null
    }
}
