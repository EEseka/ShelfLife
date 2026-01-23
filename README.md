# ShelfLife 🍎

**ShelfLife** is a production-grade food inventory and expiry tracking application built with
*Kotlin Multiplatform (KMP)* and **Compose Multiplatform**.

It demonstrates a unified codebase running natively on **Android** and **iOS**, featuring
offline-first data synchronization, adaptive layouts for Tablets/Laptops, and platform-specific
hardware handling.

![Banner](art/app_logo_foreground.png)

## 🎥 Demo Reel

See ShelfLife in action on both platforms.

|                                              **iOS (iPhone 13 Pro)**                                               |                                   **Android (Tablet Split-View)**                                   |
|:------------------------------------------------------------------------------------------------------------------:|:---------------------------------------------------------------------------------------------------:|
| [![iOS Demo](docs/media/ios_list.png)](https://youtube.com/shorts/vyEKjGYQUwk?feature=share)<br>*(Click to watch)* | [![Android Demo](docs/media/android_split.png)](https://youtu.be/Gl_nvL1POxg)<br>*(Click to watch)* |

> *Note: On macOS (Apple Silicon), the Scanner feature is automatically disabled to prevent hardware
crashes, defaulting to a Gallery picker.*

## 📸 Screenshots

### Mobile Experience (iOS)

|                       Onboarding                       |                  Authentication                  |                   Pantry List                    |                    Item Detail                     |                       Insights                       |
|:------------------------------------------------------:|:------------------------------------------------:|:------------------------------------------------:|:--------------------------------------------------:|:----------------------------------------------------:|
| <img src="docs/media/ios_onboarding.png" width="180"/> | <img src="docs/media/ios_auth.png" width="180"/> | <img src="docs/media/ios_list.png" width="180"/> | <img src="docs/media/ios_detail.png" width="180"/> | <img src="docs/media/ios_insights.png" width="180"/> |

### Adaptive Experience (Android Tablet & ChromeOS)

|              Split View (List + Detail)               |                   Dashboard & Insights                   |                         Settings                         |
|:-----------------------------------------------------:|:--------------------------------------------------------:|:--------------------------------------------------------:|
| <img src="docs/media/android_split.png" width="350"/> | <img src="docs/media/android_insights.png" width="350"/> | <img src="docs/media/android_settings.png" width="350"/> |

## 🚀 Key Features

* **Cross-Platform Architecture:** 100% Shared UI and Logic using Compose Multiplatform.
* **Adaptive UI:** Responsive layouts that switch between **Vertical** (Phones), **Split-View** (
  Tablets), and **Constrained Center** (macOS/Chromebooks).
* **Smart Inventory:** Track expiry dates, quantities, and storage locations.
* **Product Data Integration:** fetches nutritional data via the **OpenFoodFacts API**.
* **Hardware Aware:**
    * *Mobile:* Uses `KScan` for barcode scanning.
    * *macOS (Apple Silicon):* Detects desktop environment to safely disable unsupported camera
      hardware and fallback to file pickers.
* **Offline-First Sync:** Built with **Room Database** (Local) and **Firebase Firestore** (Remote).
  Works seamlessly without internet.
* **Insights Engine:** Visual analytics of consumption habits using Donut charts.

## 🛠 Tech Stack

* **Language:** Kotlin (KMP)
* **UI:** Compose Multiplatform (Material 3)
* **Architecture:** MVI (Model-View-Intent) + Clean Architecture
* **Dependency Injection:** Koin
* **Navigation:** JetBrains Navigation Compose
* **Local DB:** Room (SQLite)
* **Remote DB & Auth:** Firebase Firestore & Auth (Google Sign-In)
* **API:** OpenFoodFacts API (Ktor)
* **Hardware:** KScan (Barcode), CameraX / AVFoundation

## 🏗️ Building the Project

1. **Clone the repo:** `git clone https://github.com/eeseka/ShelfLife.git`
2. **Secrets:**
    * Add `google-services.json` to `composeApp/` (Android).
    * Add `GoogleService-Info.plist` to `iosApp/iosApp/` (iOS).
3. **Build:**
    * Android: Run via Android Studio (Supports Phones, Tablets, ChromeOS).
    * iOS: Open `iosApp.xcodeproj` in Xcode or run via Android Studio (Supports iPhone, iPad, Mac).