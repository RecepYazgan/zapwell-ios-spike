package spike

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.delay
import platform.UIKit.UIView
import platform.UIKit.UIViewController

/** The native player, written in Swift (iosApp/VlcPlayer.swift); Compose only places its view and asks its state. */
interface PlayerBridge {
    fun videoView(): UIView
    fun play(url: String)
    fun stop()
    fun status(): String
}

/** Apple's public HLS test stream: technical test only, not content. */
private const val HLS_TEST = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8"

/** [autoplay]: a URL given at launch (the CI run passes its local TS stream) that starts at once. */
fun MainViewController(bridge: PlayerBridge, autoplay: String?): UIViewController = ComposeUIViewController {
    MaterialTheme(colorScheme = darkColorScheme()) { Spike(bridge, autoplay) }
}

@Composable
private fun Spike(bridge: PlayerBridge, autoplay: String?) {
    var url by remember { mutableStateOf(autoplay ?: "") }
    var status by remember { mutableStateOf("idle") }
    LaunchedEffect(Unit) {
        if (!autoplay.isNullOrBlank()) bridge.play(autoplay)
        while (true) {
            val s = bridge.status()
            if (s != status) println("ZAPSPIKE status: $s") // the CI log reads these lines
            status = s
            delay(1000)
        }
    }
    Column(
        Modifier.fillMaxSize().background(Color(0xFF0E1116)).safeDrawingPadding().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("ZapWell iOS deneme", color = Color.White, fontSize = 22.sp)
        Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(Color.Black)) {
            UIKitView(factory = { bridge.videoView() }, modifier = Modifier.fillMaxSize())
        }
        Text(status, color = Color(0xFFB8C0CC), fontSize = 14.sp)
        OutlinedTextField(
            value = url, onValueChange = { url = it }, singleLine = true, modifier = Modifier.fillMaxWidth(),
            label = { Text("Yayin adresi (http/https)") },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { if (url.isNotBlank()) bridge.play(url.trim()) }, shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
            ) { Text("Oynat") }
            OutlinedButton(onClick = { url = HLS_TEST; bridge.play(HLS_TEST) }, shape = RoundedCornerShape(12.dp)) { Text("Apple HLS testi") }
            OutlinedButton(onClick = { bridge.stop() }, shape = RoundedCornerShape(12.dp)) { Text("Durdur") }
        }
    }
}
