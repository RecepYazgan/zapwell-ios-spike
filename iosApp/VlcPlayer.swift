import UIKit
import MobileVLCKit
import Shared

/// The native player behind the Compose screen: VLCKit (the engine of VLC for iOS) draws into a plain UIView that the
/// host puts under the transparent Compose layer; Compose reports where the video area is.
final class VlcPlayer: NSObject, PlayerBridge {
    let view = UIView()
    private let player = VLCMediaPlayer()
    private var lastUrl = ""
    /// A URL asked for before the video area had a size. VLC's view inside ours keeps the size it had when playback
    /// began (autoresizing from 0x0 stays 0x0): a fast live TS stream went black on the iPad until the app was reopened.
    private var pendingUrl: String?

    override init() {
        super.init()
        view.backgroundColor = .black
        view.isUserInteractionEnabled = false
        player.drawable = view
    }

    func layoutVideo(x: Double, y: Double, width: Double, height: Double) {
        let f = CGRect(x: x, y: y, width: width, height: height)
        DispatchQueue.main.async {
            if self.view.frame != f { self.view.frame = f }
            if f.width > 0, f.height > 0, let u = self.pendingUrl { self.pendingUrl = nil; self.start(u) }
        }
    }

    func play(url: String) {
        DispatchQueue.main.async {
            if self.view.bounds.width > 0, self.view.bounds.height > 0 { self.start(url) } else { self.pendingUrl = url }
        }
    }

    private func start(_ url: String) {
        guard let u = URL(string: url) else { return }
        lastUrl = url
        let media = VLCMedia(url: u)
        media.addOptions(["network-caching": 1500, "http-reconnect": true])
        player.media = media
        player.play()
    }

    func stop() { pendingUrl = nil; player.stop() }

    func status() -> String {
        let state = VLCMediaPlayerStateToString(player.state) ?? "?"
        let size = player.videoSize
        let t = player.time.intValue / 1000
        return "\(state) | \(Int(size.width))x\(Int(size.height)) | \(t) s | \(lastUrl.hasPrefix("https") ? "https" : (lastUrl.isEmpty ? "-" : "http"))"
    }
}
