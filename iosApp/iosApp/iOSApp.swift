import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        MainViewControllerKt.doInitKoin()
        MainViewControllerKt.onAppStarted()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}