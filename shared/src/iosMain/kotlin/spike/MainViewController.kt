@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class) // ComposeUIViewController opaque = false

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
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.delay
import platform.UIKit.UIViewController

/**
 * The native player, written in Swift (iosApp/VlcPlayer.swift). Its video view lies natively BEHIND the transparent
 * Compose layer (a UIKitView inside Compose went fully black on the iPad once VLC drew); Compose only tells it where.
 */
interface PlayerBridge {
    /** The video area in points, in the Compose view's coordinates. */
    fun layoutVideo(x: Double, y: Double, width: Double, height: Double)
    fun play(url: String)
    fun stop()
    fun status(): String
}

/** Apple's public HLS test stream: technical test only, not content. */
private const val HLS_TEST = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8"

/** [autoplay]: a URL given at launch (the CI run passes its local TS stream) that starts at once. */
fun MainViewController(bridge: PlayerBridge, autoplay: String?): UIViewController = ComposeUIViewController(configure = { opaque = false }) {
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
            if (s != status) println("ZAPSPIKE status: $s") // stdout: simctl --stdout in CI, pymobiledevice3 dvt launch --stream on a device (NSLog with a Kotlin String crashed)
            status = s
            delay(1000)
        }
    }
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp), // no background: the host view paints it
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("ZapWell iOS deneme", color = Color.White, fontSize = 22.sp)
        // A transparent hole: the native video view is placed under it.
        val density = LocalDensity.current.density
        Box(Modifier.fillMaxWidth(0.62f).aspectRatio(16f / 9f).onGloballyPositioned { c ->
            val r = c.boundsInWindow()
            bridge.layoutVideo((r.left / density).toDouble(), (r.top / density).toDouble(), (r.width / density).toDouble(), (r.height / density).toDouble())
        })
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
