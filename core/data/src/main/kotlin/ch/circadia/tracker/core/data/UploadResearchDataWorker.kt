package ch.circadia.tracker.core.data

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ch.circadia.tracker.core.database.StateEventDao
import ch.circadia.tracker.core.domain.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class UploadResearchDataWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val researchRepository: ResearchRepository,
    private val stateEventDao: StateEventDao,
    private val settingsRepository: SettingsRepository,
    private val anonymizeUseCase: AnonymizeUseCase,
    private val uploadEndpoint: ResearchUploadEndpoint
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val consent = researchRepository.getConsent().first()
        if (consent == null || consent.revokedAtUtcMillis != null || !consent.consentData) {
            return Result.success()
        }

        if (System.currentTimeMillis() > consent.validUntilUtcMillis) {
            return Result.success()
        }

        val lastUploaded = settingsRepository.getLastUploadedEventRecordedAt().first()
        val pendingEntities = stateEventDao.getEventsRecordedAfter(lastUploaded)
        
        if (pendingEntities.isEmpty()) return Result.success()

        val pendingEvents = pendingEntities.map { it.toDomain() }
        val anonymized = anonymizeUseCase.anonymizeEvents(
            events = pendingEvents,
            pseudonym = consent.participantPseudonym,
            studyStartUtcMillis = consent.grantedAtUtcMillis
        )

        val success = uploadEndpoint.uploadEvents(anonymized)
        
        return if (success) {
            val maxRecordedAt = pendingEntities.maxOf { it.recordedAt }
            settingsRepository.setLastUploadedEventRecordedAt(maxRecordedAt)
            Result.success()
        } else {
            Result.retry()
        }
    }
}
