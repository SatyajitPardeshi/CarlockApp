package com.example.carlockcontrol

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    // IP address of the embedded device in WiFi AP mode
    private val deviceIp = "http://192.168.1.1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up the Compose UI
        setContent {
            var isLocked by remember { mutableStateOf(true) }       // Tracks current lock state
            var darkTheme by remember { mutableStateOf(false) }     // Tracks theme preference
            var isConnected by remember { mutableStateOf(false) }   // Tracks connectivity status

            // Coroutine to check connection status every 5 seconds
            LaunchedEffect(Unit) {
                while (true) {
                    isConnected = checkConnection()
                    delay(5000)
                }
            }

            // Select appropriate color scheme based on user preference
            val colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()

            // Main UI surface with theme applied
            MaterialTheme(colorScheme = colorScheme) {
                Surface {
                    CarLockApp(
                        isLocked = isLocked,
                        onLock = {
                            isLocked = true
                            playFeedback(true)
                            sendCommand("/lock")
                        },
                        onUnlock = {
                            isLocked = false
                            playFeedback(false)
                            sendCommand("/unlock")
                        },
                        darkTheme = darkTheme,
                        onToggleTheme = { darkTheme = !darkTheme },
                        isConnected = isConnected
                    )
                }
            }
        }
    }

    // Sends a GET request to the embedded device with the specified path
    private fun sendCommand(path: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("http://192.168.1.1$path")
                with(url.openConnection() as HttpURLConnection) {
                    connectTimeout = 3000
                    requestMethod = "GET"
                    inputStream.bufferedReader().use { it.readText() }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Command sent: $path", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Verifies whether the embedded device is reachable by requesting the /sensor endpoint
    private suspend fun checkConnection(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("http://192.168.1.1/sensor")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 1500
                conn.requestMethod = "GET"
                conn.connect()
                conn.responseCode == 200
            } catch (e: Exception) {
                false
            }
        }
    }

    // Provides auditory and tactile feedback based on lock state
    private fun playFeedback(isLocked: Boolean) {
        val toneType = if (isLocked) ToneGenerator.TONE_PROP_BEEP else ToneGenerator.TONE_PROP_ACK
        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGen.startTone(toneType, 150)

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(200)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarLockApp(
    isLocked: Boolean,
    onLock: () -> Unit,
    onUnlock: () -> Unit,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    isConnected: Boolean
) {
    // Application layout using Scaffold and TopAppBar
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "  Car Lock Control",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 12.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_wifi),
                            contentDescription = "WiFi",
                            tint = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isConnected) "Connected" else "Disconnected",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            painter = painterResource(id = if (darkTheme) R.drawable.ic_sun else R.drawable.ic_moon),
                            contentDescription = "Toggle Theme"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()
        ) {
            CarControlUI(isLocked, onLock, onUnlock)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CarControlUI(
    isLocked: Boolean,
    onLock: () -> Unit,
    onUnlock: () -> Unit
) {
    // Text to show current status of the vehicle
    val statusText = if (isLocked) "Car is Locked 🔒" else "Car is Unlocked 🔓"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated image transition between locked and unlocked car state
        AnimatedContent(
            targetState = isLocked,
            transitionSpec = {
                fadeIn(tween(300)) with fadeOut(tween(300))
            },
            label = "Car Image Transition"
        ) { locked ->
            val imageRes = if (locked) R.drawable.car_locked else R.drawable.car_unlocked
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = if (locked) "Locked Car" else "Unlocked Car",
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp)
            )
        }

        // Display Lock/Unlock status text
        Text(statusText, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(32.dp))

        // Lock button
        Button(
            onClick = onLock,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Lock Car 🔒")
        }

        // Unlock button
        Button(
            onClick = onUnlock,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Unlock Car 🔓")
        }
    }
}


