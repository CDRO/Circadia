package ch.circadia.tracker.core.domain

import ch.circadia.tracker.core.model.Interval
import ch.circadia.tracker.core.model.SleepState

class GetMainSleepEpisodeUseCase {
    operator fun invoke(intervals: List<Interval>, bucketStart: Long, bucketEnd: Long): SleepEpisodes {
        val sleepIntervals = intervals.filter { 
            it.state == SleepState.ASLEEP && it.startUtcMillis < bucketEnd && it.endUtcMillis > bucketStart
        }
        
        val mainEpisode = sleepIntervals.maxByOrNull { 
            val s = maxOf(it.startUtcMillis, bucketStart)
            val e = minOf(it.endUtcMillis, bucketEnd)
            e - s
        }
        
        val naps = sleepIntervals.filter { it != mainEpisode }
        
        return SleepEpisodes(mainEpisode, naps)
    }
}

data class SleepEpisodes(
    val main: Interval?,
    val naps: List<Interval>
)
