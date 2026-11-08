import SwiftUI
import Shared

@main
struct ZapSpikeApp: App {
    var body: some Scene {
        WindowGroup { ComposeHost().ignoresSafeArea() }
    }
}

/// Hosts the Kotlin Compose screen. "-autoplay <url>" at launch (UserDefaults argument domain) starts a stream at once.
struct ComposeHost: UIViewControllerRepresentable {
    private static let player = VlcPlayer()

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(bridge: ComposeHost.player, autoplay: UserDefaults.standard.string(forKey: "autoplay"))
    }

    func updateUIViewController(_ controller: UIViewController, context: Context) {}
}
