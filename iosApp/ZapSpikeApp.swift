import SwiftUI
import Shared

@main
struct ZapSpikeApp: App {
    var body: some Scene {
        WindowGroup { ComposeHost().ignoresSafeArea() }
    }
}

/// "-autoplay <url>" at launch (UserDefaults argument domain) starts a stream at once.
struct ComposeHost: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController { HostController() }
    func updateUIViewController(_ controller: UIViewController, context: Context) {}
}

/// Layers, back to front: page color, the native video view, the transparent Compose screen.
final class HostController: UIViewController {
    private let player = VlcPlayer()

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor(red: 0x0E / 255.0, green: 0x11 / 255.0, blue: 0x16 / 255.0, alpha: 1)
        view.addSubview(player.view)

        let compose = MainViewControllerKt.MainViewController(bridge: player, autoplay: UserDefaults.standard.string(forKey: "autoplay"))
        addChild(compose)
        compose.view.frame = view.bounds
        compose.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        compose.view.backgroundColor = .clear
        compose.view.isOpaque = false
        view.addSubview(compose.view)
        compose.didMove(toParent: self)
    }
}
