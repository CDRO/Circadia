package ch.circadia.tracker.feature.persons

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ch.circadia.tracker.core.designsystem.CircadiaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PersonSetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CircadiaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SetupScreen(onFinished = { finish() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(onFinished: () -> Unit) {
    var name by remember { mutableStateOf("") }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Willkommen bei Circadia") }) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Dein Name") }
            )
            Button(
                onClick = onFinished,
                enabled = name.isNotBlank(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Text("Starten")
            }
        }
    }
}
