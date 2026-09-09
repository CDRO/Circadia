package ch.circadia.tracker.core.domain

import ch.circadia.tracker.core.model.Interval
import ch.circadia.tracker.core.model.SleepState
import kotlin.math.pow

class CalculateStabilityVariabilityUseCase {
    operator fun invoke(intervals: List<Interval>, startUtc: Long, dayCount: Int): StabilityVariability? {
        if (dayCount < 7) return null
        
        val hourlyRaster = 24
        val totalHours = dayCount * hourlyRaster
        val hourlyStates = DoubleArray(totalHours) { 0.0 }

        intervals.forEach { interval ->
            val startIdx = ((interval.startUtcMillis - startUtc) / 3600000).toInt().coerceIn(0, totalHours - 1)
            val endIdx = ((interval.endUtcMillis - startUtc) / 3600000).toInt().coerceIn(0, totalHours)
            
            if (interval.state == SleepState.ASLEEP) {
                for (i in startIdx until endIdx) {
                    hourlyStates[i] = 1.0
                }
            }
        }

        val overallMean = hourlyStates.average()
        
        // 1. Interdaily Stability (IS)
        // IS = [N * sum(Xh - X)^2] / [p * sum(Xi - X)^2]
        // N = total hours, p = hours per day (24), Xh = hourly means across days, Xi = each hour, X = overall mean
        
        val hourlyMeans = DoubleArray(hourlyRaster) { h ->
            (0 until dayCount).map { d -> hourlyStates[d * hourlyRaster + h] }.average()
        }
        
        val numeratorIS = dayCount * hourlyMeans.sumOf { (it - overallMean).pow(2) }
        val denominatorIS = hourlyStates.sumOf { (it - overallMean).pow(2) }
        
        val isMetric = if (denominatorIS > 0) numeratorIS / denominatorIS else 0.0

        // 2. Intradaily Variability (IV)
        // IV = [N * sum(Xi - Xi-1)^2] / [(N-1) * sum(Xi - X)^2]
        
        var sumDiffSquared = 0.0
        for (i in 1 until totalHours) {
            sumDiffSquared += (hourlyStates[i] - hourlyStates[i - 1]).pow(2)
        }
        
        val numeratorIV = totalHours * sumDiffSquared
        val denominatorIV = (totalHours - 1) * denominatorIS
        
        val ivMetric = if (denominatorIV > 0) numeratorIV / denominatorIV else 0.0

        return StabilityVariability(isMetric, ivMetric)
    }
}

data class StabilityVariability(
    val isMetric: Double,
    val ivMetric: Double
)
