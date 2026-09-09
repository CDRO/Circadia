package ch.circadia.tracker.core.domain

class CalculateTrendsUseCase {
    fun calculateMovingAverage(data: List<Long>, windowSize: Int): List<Double> {
        if (data.size < windowSize) return emptyList()
        
        return (windowSize - 1 until data.size).map { i ->
            data.subList(i - windowSize + 1, i + 1).average()
        }
    }
}
