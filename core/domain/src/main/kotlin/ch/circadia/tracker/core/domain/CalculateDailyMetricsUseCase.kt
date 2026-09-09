package ch.circadia.tracker.core.domain

import ch.circadia.tracker.core.model.*
import java.time.Instant
import java.time.ZoneId

class CalculateDailyMetricsUseCase {
    operator fun invoke(intervals: List<Interval>, bucketStart: Long, bucketEnd: Long): DailyMetrics? {
        if (intervals.isEmpty()) return null
        
        val personId = intervals.first().personId
        val date = Instant.ofEpochMilli(bucketStart).atZone(ZoneId.systemDefault()).toLocalDate().toString()
        
        val sleepIntervals = intervals.filter { 
            it.state == SleepState.ASLEEP && it.startUtcMillis < bucketEnd && it.endUtcMillis > bucketStart
        }
        
        val awakeIntervals = intervals.filter {
            it.state == SleepState.AWAKE && it.startUtcMillis < bucketEnd && it.endUtcMillis > bucketStart
        }
        
        val totalSleep = sleepIntervals.sumOf { 
            val s = maxOf(it.startUtcMillis, bucketStart)
            val e = minOf(it.endUtcMillis, bucketEnd)
            maxOf(0L, e - s)
        }
        
        val maxAwake = awakeIntervals.maxOfOrNull {
            val s = maxOf(it.startUtcMillis, bucketStart)
            val e = minOf(it.endUtcMillis, bucketEnd)
            maxOf(0L, e - s)
        } ?: 0L
        
        // Hauptschlafepisode bestimmen (längste)
        val mainEpisode = sleepIntervals.maxByOrNull { it.endUtcMillis - it.startUtcMillis }
        
        return DailyMetrics(
            personId = personId,
            date = date,
            totalSleepMillis = totalSleep,
            sleepEpisodes = sleepIntervals.size,
            maxAwakeMillis = maxAwake,
            sleepStartUtcMillis = mainEpisode?.startUtcMillis,
            sleepEndUtcMillis = mainEpisode?.endUtcMillis
        )
    }
}
