import UIKit
import MobileVLCKit
import Shared

/// The native player behind the Compose screen: VLCKit (the engine of VLC for iOS) draws into a plain UIView.
final class VlcPlayer: NSObject, PlayerBridge {
    private let view = UIView()
    private let player = VLCMediaPlayer()
    private var lastUrl = ""

    override init() {
        super.init()
        view.backgroundColor = .black
        player.drawable = view
    }

    func videoView() -> UIView { view }

    func play(url: String) {
        guard let u = URL(string: url) else { return }
        lastUrl = url
        let media = VLCMedia(url: u)
        media.addOptions(["network-caching": 1500, "http-reconnect": true])
        player.media = media
        player.play()
    }

    func stop() { player.stop() }

    func status() -> String {
        let state = VLCMediaPlayerStateToString(player.state) ?? "?"
        let size = player.videoSize
        let t = player.time.intValue / 1000
        return "\(state) | \(Int(size.width))x\(Int(size.height)) | \(t) s | \(lastUrl.hasPrefix("https") ? "https" : (lastUrl.isEmpty ? "-" : "http"))"
    }
}
