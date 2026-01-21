import SwiftUI
import GoogleSignIn
import Firebase
import FirebaseAppCheck
import UserNotifications

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            ContentView().onOpenURL(perform: { url in
                GIDSignIn.sharedInstance.handle(url)
            })
        }
    }
}

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    
    func application(
                _ application: UIApplication,
                didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
            ) -> Bool {
                let providerFactory: AppCheckProviderFactory

                #if DEBUG
                // This generates a "Debug Token" in the Xcode Console.
                // You must copy that token to Firebase Console -> App Check -> Apps -> iOS -> Debug Tokens
                providerFactory = AppCheckDebugProviderFactory()
                #else
                providerFactory = AppCheckAppAttestProviderFactory()
                #endif

                AppCheck.setAppCheckProviderFactory(providerFactory)
                
                FirebaseApp.configure()
                
                UNUserNotificationCenter.current().delegate = self
                
                return true
            }

    // This tells iOS: "If a notification arrives while the app is OPEN, show it anyway."
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        // Show the banner, play the sound, and list it in the tray
        completionHandler([.banner, .sound, .list])
    }
}
