package ch.circadia.tracker.feature.paywall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.circadia.tracker.core.domain.ResearchRepository
import ch.circadia.tracker.core.model.ResearchConsent
import ch.circadia.tracker.core.model.SurveyResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ResearchViewModel @Inject constructor(
    private val researchRepository: ResearchRepository,
    private val entitlementRepository: ch.circadia.tracker.core.domain.EntitlementRepository
) : ViewModel() {

    fun saveResearchParticipation(
        surveyResponse: SurveyResponse,
        consentSurvey: Boolean,
        consentData: Boolean
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val validUntil = now + (365L * 24 * 60 * 60 * 1000)
            val consent = ResearchConsent(
                id = UUID.randomUUID().toString(),
                grantedAtUtcMillis = now,
                surveyVersion = surveyResponse.surveyVersion.toString(),
                consentTextHash = "TODO_HASH",
                participantPseudonym = UUID.randomUUID().toString(),
                validUntilUtcMillis = validUntil,
                consentSurvey = consentSurvey,
                consentData = consentData
            )
            researchRepository.saveConsent(consent)
            
            // Update entitlement (T-806)
            entitlementRepository.updateEntitlement(
                ch.circadia.tracker.core.model.Entitlement(
                    source = ch.circadia.tracker.core.model.EntitlementSource.RESEARCH,
                    validUntilUtcMillis = validUntil
                )
            )
        }
    }
}
