import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Self.Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    @State private var isStatusBarVisible = false

    var body: some View {
        ComposeView()
            .ignoresSafeArea()
            .statusBarHidden(!isStatusBarVisible)
            .onReceive(NotificationCenter.default.publisher(for: NSNotification.Name("SystemBarsVisibilityNotification"))) { notification in
                if let visible = notification.userInfo?["visible"] as? Bool {
                    withAnimation(.easeInOut(duration: 0.8)) {
                        self.isStatusBarVisible = visible
                    }
                }
            }
    }
}