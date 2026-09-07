package ch.circadia.tracker.feature.timeline

import org.junit.Test
import kotlin.system.measureTimeMillis

class TimelineBenchmark {

    @Test
    fun benchmarkDataPreparation() {
        val personCount = 4
        val dayCount = 3 * 365 // 3 years
        
        val time = measureTimeMillis {
            var sum = 0L
            for (i in 0 until dayCount) {
                for (p in 0 until personCount) {
                    sum += (i + p).toLong()
                }
            }
        }
        
        // Result can be checked via profiler or logs in debug
    }
}
