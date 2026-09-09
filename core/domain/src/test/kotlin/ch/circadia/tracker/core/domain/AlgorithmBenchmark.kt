package ch.circadia.tracker.core.domain

import ch.circadia.tracker.core.model.*
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

class AlgorithmBenchmark {
    private val calculateSRI = CalculateSRIUseCase()
    private val calculateSV = CalculateStabilityVariabilityUseCase()

    @Test
    fun benchmarkAlgorithms() {
        val personId = PersonId("p1")
        val dayCount = 5 * 365 // 5 years
        val startUtc = 0L
        
        // Generate synthetic intervals: 8 hours sleep, 16 hours awake per day
        val intervals = mutableListOf<Interval>()
        for (i in 0 until dayCount) {
            val dayStart = i * 86400000L
            intervals.add(Interval(personId, SleepState.ASLEEP, dayStart, dayStart + 28800000L, false, "UTC", "UTC", "e$i"))
            intervals.add(Interval(personId, SleepState.AWAKE, dayStart + 28800000L, dayStart + 86400000L, false, "UTC", "UTC", "w$i"))
        }

        val sriTime = measureTimeMillis {
            calculateSRI(intervals, startUtc, dayCount)
        }
        
        val svTime = measureTimeMillis {
            calculateSV(intervals, startUtc, dayCount)
        }

        assert(sriTime >= 0)
        assert(svTime >= 0)
    }
}
