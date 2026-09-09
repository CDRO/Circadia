package ch.circadia.tracker.core.common

import org.junit.Test
import java.io.File

class PlaceholderGuardTest {

    @Test
    fun `Testfall 35 - Keine Platzhalter im Forschungsmodus`() {
        if (!FeatureFlags.researchMode) return

        val userDir = System.getProperty("user.dir") ?: "."
        val rootDir = File(userDir).parentFile.parentFile
        
        // 1. Check consent draft
        val consentFile = File(rootDir, "feature/paywall/src/main/res/raw/consent_draft_0.md")
        org.junit.Assert.assertTrue("Consent file should exist at ${consentFile.absolutePath}", consentFile.exists())
        val consentContent = consentFile.readText()
        org.junit.Assert.assertFalse("Consent text carries 'status: draft'", consentContent.contains("status: draft"))

        // 2. Check survey drafts
        val surveyDir = File(rootDir, "feature/paywall/src/main/assets/surveys")
        org.junit.Assert.assertTrue("Survey directory should exist at ${surveyDir.absolutePath}", surveyDir.exists())
        surveyDir.listFiles()?.forEach { file ->
            val content = file.readText()
            org.junit.Assert.assertFalse("Survey ${file.name} carries 'status: draft'", content.contains("\"status\": \"draft\""))
        }
    }
}
