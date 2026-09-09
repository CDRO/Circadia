package ch.circadia.tracker.core.domain

import ch.circadia.tracker.core.model.Interval
import java.time.Instant
import java.time.ZoneId

class CalculateMSFscUseCase(
    private val calculateMidSleepUseCase: CalculateMidSleepUseCase
) {
    operator fun invoke(
        workDayIntervals: List<Interval>,
        freeDayIntervals: List<Interval>
    ): Long? {
        if (freeDayIntervals.isEmpty()) return null
        
        // 1. Average Sleep Duration on Work days (SDW)
        val sdw = if (workDayIntervals.isNotEmpty()) {
            workDayIntervals.map { it.endUtcMillis - it.startUtcMillis }.average().toLong()
        } else null
        
        // 2. Average Sleep Duration on Free days (SDF)
        val sdf = freeDayIntervals.map { it.endUtcMillis - it.startUtcMillis }.average().toLong()
        
        // 3. Mid-Sleep on Free days (MSF)
        val msfList = freeDayIntervals.map { calculateMidSleepUseCase(it) }
        val avgMsf = msfList.average().toLong()
        
        if (sdw == null || sdf <= sdw) {
            // No sleep debt correction if no work days or if sleeping more on work days
            return avgMsf
        }
        
        // 4. Correction: MSFsc = MSF - (SDF - SDW) / 2
        return avgMsf - (sdf - sdw) / 2
    }
}
