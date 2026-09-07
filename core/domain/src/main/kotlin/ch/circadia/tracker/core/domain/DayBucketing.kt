package ch.circadia.tracker.core.domain

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class DayBucketing(private val dayBoundary: LocalTime = LocalTime.of(18, 0)) {

    fun getDayBucketStart(instant: Long, zoneId: String): Long {
        val zone = ZoneId.of(zoneId)
        val zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(instant), zone)
        
        val bucketDate = if (zdt.toLocalTime().isBefore(dayBoundary)) {
            zdt.toLocalDate().minusDays(1)
        } else {
            zdt.toLocalDate()
        }
        
        return bucketDate.atTime(dayBoundary).atZone(zone).toInstant().toEpochMilli()
    }

    fun getNextBucketStart(currentBucketStart: Long, zoneId: String): Long {
        val zone = ZoneId.of(zoneId)
        val zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentBucketStart), zone)
        // plusDays(1) on ZonedDateTime correctly handles DST by keeping the same local time.
        return zdt.plusDays(1).toInstant().toEpochMilli()
    }
}
