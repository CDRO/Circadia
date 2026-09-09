package ch.circadia.tracker.core.domain

class DeleteAllDataUseCase(
    private val personRepository: PersonRepository,
    private val stateEventRepository: StateEventRepository,
    private val researchRepository: ResearchRepository,
    private val widgetBindingRepository: WidgetBindingRepository
) {
    suspend operator fun invoke() {
        personRepository.deleteAll()
        stateEventRepository.deleteAll()
        researchRepository.revokeConsent()
        // We don't have a deleteAll for widgets yet, but person delete cascades in DB
    }
}
