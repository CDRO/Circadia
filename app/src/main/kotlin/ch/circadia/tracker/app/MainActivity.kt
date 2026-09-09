package ch.circadia.tracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ch.circadia.tracker.core.common.FeatureFlags
import ch.circadia.tracker.core.designsystem.CircadiaTheme
import ch.circadia.tracker.core.domain.DeleteAllDataUseCase
import ch.circadia.tracker.feature.export.ExportScreen
import ch.circadia.tracker.feature.export.SettingsScreen
import ch.circadia.tracker.feature.paywall.*
import ch.circadia.tracker.feature.persons.PersonsScreen
import ch.circadia.tracker.feature.timeline.AnalyticsScreen
import ch.circadia.tracker.feature.timeline.TimelineScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject lateinit var deleteAllDataUseCase: DeleteAllDataUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scope = rememberCoroutineScope()
            CircadiaTheme {
                val navController = rememberNavController()
                val screens = remember {
                    mutableListOf(
                        Screen.Timeline,
                        Screen.Analytics,
                        Screen.Persons,
                        Screen.Export,
                        Screen.Settings
                    ).apply {
                        if (FeatureFlags.researchMode) {
                            add(Screen.Research)
                        }
                    }
                }

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination
                            screens.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = null) },
                                    label = { Text(screen.label) },
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(modifier = Modifier.padding(innerPadding)) {
                        NavHost(
                            navController,
                            startDestination = Screen.Timeline.route
                        ) {
                            composable(Screen.Timeline.route) { TimelineScreen() }
                            composable(Screen.Analytics.route) { AnalyticsScreen() }
                            composable(Screen.Persons.route) { PersonsScreen() }
                            composable(Screen.Export.route) { ExportScreen() }
                            composable(Screen.Settings.route) { 
                                SettingsScreen(onDeleteAllData = {
                                    scope.launch { deleteAllDataUseCase() }
                                }) 
                            }
                            if (FeatureFlags.researchMode) {
                                composable(Screen.Research.route) {
                                    ResearchFlow(
                                        onComplete = { navController.navigate(Screen.Timeline.route) },
                                        onCancel = { navController.navigate(Screen.Timeline.route) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResearchFlow(
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    viewModel: ResearchViewModel = hiltViewModel()
) {
    var currentStep by remember { mutableStateOf(ResearchStep.ENLIGHTENMENT) }
    var surveyResponse by remember { mutableStateOf<ch.circadia.tracker.core.model.SurveyResponse?>(null) }

    when (currentStep) {
        ResearchStep.ENLIGHTENMENT -> EnlightenmentScreen(
            onContinue = { currentStep = ResearchStep.SURVEY },
            onCancel = onCancel
        )
        ResearchStep.SURVEY -> SurveyScreen(
            onComplete = { 
                surveyResponse = it
                currentStep = ResearchStep.CONSENT 
            },
            onCancel = onCancel
        )
        ResearchStep.CONSENT -> ConsentScreen(
            onComplete = { consentSurvey, consentData ->
                surveyResponse?.let {
                    viewModel.saveResearchParticipation(it, consentSurvey, consentData)
                }
                onComplete()
            },
            onCancel = onCancel
        )
    }
}

enum class ResearchStep { ENLIGHTENMENT, SURVEY, CONSENT }

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Timeline : Screen("timeline", "Auswertung", Icons.Default.Timeline)
    data object Analytics : Screen("analytics", "Analytik", Icons.Default.Analytics)
    data object Persons : Screen("persons", "Personen", Icons.Default.People)
    data object Research : Screen("research", "Forschung", Icons.Default.Science)
    data object Export : Screen("export", "Export", Icons.Default.Download)
    data object Settings : Screen("settings", "Einstellungen", Icons.Default.Settings)
}
