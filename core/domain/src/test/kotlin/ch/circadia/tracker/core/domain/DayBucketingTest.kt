package ch.circadia.tracker.core.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.*

class DayBucketingTest {

    private val bucketing = DayBucketing(LocalTime.of(18, 0))
    private val zoneId = "Europe/Zurich"

    @Test
    fun `Zuordnung vor Tagesgrenze geht zum Vortag`() {
        // 2024-09-08 02:00 Uhr < 18:00 Uhr -> gehört zum 07.09.
        val instant = ZonedDateTime.of(2024, 9, 8, 2, 0, 0, 0, ZoneId.of(zoneId)).toInstant().toEpochMilli()
        val bucketStart = bucketing.getDayBucketStart(instant, zoneId)
        
        val expected = ZonedDateTime.of(2024, 9, 7, 18, 0, 0, 0, ZoneId.of(zoneId)).toInstant().toEpochMilli()
        assertEquals(expected, bucketStart)
    }

    @Test
    fun `Zuordnung nach Tagesgrenze bleibt am selben Tag`() {
        // 2024-09-07 20:00 Uhr > 18:00 Uhr -> gehört zum 07.09.
        val instant = ZonedDateTime.of(2024, 9, 7, 20, 0, 0, 0, ZoneId.of(zoneId)).toInstant().toEpochMilli()
        val bucketStart = bucketing.getDayBucketStart(instant, zoneId)
        
        val expected = ZonedDateTime.of(2024, 9, 7, 18, 0, 0, 0, ZoneId.of(zoneId)).toInstant().toEpochMilli()
        assertEquals(expected, bucketStart)
    }

    @Test
    fun `Naechster Bucket-Start ueber Sommerzeitumstellung behaelt lokale Uhrzeit`() {
        // 2024-03-30 18:00 -> 2024-03-31 18:00 (Dauer 23h real)
        val start = ZonedDateTime.of(2024, 3, 30, 18, 0, 0, 0, ZoneId.of(zoneId)).toInstant().toEpochMilli()
        val next = bucketing.getNextBucketStart(start, zoneId)
        
        val expected = ZonedDateTime.of(2024, 3, 31, 18, 0, 0, 0, ZoneId.of(zoneId)).toInstant().toEpochMilli()
        assertEquals(expected, next)
        
        // Verifikation der realen Dauer
        assertEquals(23 * 3600 * 1000L, next - start)
    }

    @Test
    fun `Naechster Bucket-Start ueber Winterzeitumstellung behaelt lokale Uhrzeit`() {
        // 2024-10-26 18:00 -> 2024-10-27 18:00 (Dauer 25h real)
        val start = ZonedDateTime.of(2024, 10, 26, 18, 0, 0, 0, ZoneId.of(zoneId)).toInstant().toEpochMilli()
        val next = bucketing.getNextBucketStart(start, zoneId)
        
        val expected = ZonedDateTime.of(2024, 10, 27, 18, 0, 0, 0, ZoneId.of(zoneId)).toInstant().toEpochMilli()
        assertEquals(expected, next)
        
        // Verifikation der realen Dauer
        assertEquals(25 * 3600 * 1000L, next - start)
    }
}
