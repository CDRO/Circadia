package ch.circadia.tracker.core.domain

import ch.circadia.tracker.core.model.Interval
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

class CalculateSocialJetlagUseCase(
    private val calculateMidSleepUseCase: CalculateMidSleepUseCase
) {
    operator fun invoke(
        workDayIntervals: List<Interval>,
        freeDayIntervals: List<Interval>
    ): Long? {
        if (workDayIntervals.isEmpty() || freeDayIntervals.isEmpty()) return null
        
        val msf = freeDayIntervals.map { calculateMidSleepUseCase(it) }.average()
        val msw = workDayIntervals.map { calculateMidSleepUseCase(it) }.average()
        
        // Difference in local time. To handle wrap-around, we use local time?
        // Actually, the spec says absolute difference.
        // If we stay in UTC, it's just abs(msf - msw).
        // But the definition usually refers to the *clock time* difference.
        
        val msfLocal = Instant.ofEpochMilli(msf.toLong()).atZone(ZoneId.of("UTC")).toLocalTime()
        val mswLocal = Instant.ofEpochMilli(msw.toLong()).atZone(ZoneId.of("UTC")).toLocalTime()
        
        val diff = Duration.between(mswLocal, msfLocal).abs()
        val minDiff = if (diff.toHours() > 12) Duration.ofHours(24).minus(diff) else diff
        
        return minDiff.toMillis()
    }
}
