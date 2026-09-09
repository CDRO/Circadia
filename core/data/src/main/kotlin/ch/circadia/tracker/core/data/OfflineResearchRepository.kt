package ch.circadia.tracker.core.data

import ch.circadia.tracker.core.database.ResearchDao
import ch.circadia.tracker.core.model.*
import ch.circadia.tracker.core.domain.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineResearchRepository(
    private val researchDao: ResearchDao,
    private val entitlementRepository: EntitlementRepository,
    private val settingsRepository: SettingsRepository
) : ResearchRepository {
    override fun getConsent(): Flow<ResearchConsent?> = 
        researchDao.getLatestConsent().map { it?.toDomain() }

    override suspend fun saveConsent(consent: ResearchConsent) {
        researchDao.insertConsent(consent.toEntity())
    }

    override suspend fun revokeConsent() {
        val now = System.currentTimeMillis()
        researchDao.revokeAllConsents(now)
        
        // Fallback to FREE
        entitlementRepository.updateEntitlement(
            Entitlement(source = EntitlementSource.FREE, validUntilUtcMillis = null)
        )
        
        // Mark revocation as pending (T-807)
        settingsRepository.setRevocationPending(true)
    }
}
