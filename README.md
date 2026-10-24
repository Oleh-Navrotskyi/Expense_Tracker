# 📊 Expense Tracker & Notification Interceptor

A modern, offline-first Android application designed to automatically track your expenses by intercepting finance/banking notifications (like Google Wallet), organizing them into credit card profiles, and offering optional Google Firebase cloud sync.

---

## ✨ Features

* **⚡ Automatic Expense Logging:** Reads incoming payment notifications (e.g., Google Wallet) and automatically records transaction details (amount, store name, card last 4 digits, and date).
* **🎛️ Notification Debugging & Tester:**
  * Test notification parser right inside the settings.
  * Live notification listener toggle to view all captured raw system notifications for easy troubleshooting and pattern matching.
* **💳 Multi-Card Management:**
  * Categorize expenses by specific card numbers.
  * Upload and custom card backgrounds/images.
  * Display overall expense totals per card or per month.
* **🗑️ Batch Deletion (Multi-Selection Mode):** Long-press any transaction to trigger Contextual Action Mode (`ActionMode`) on the toolbar to select and delete multiple transactions at once.
* **☁️ Cloud Sync (Firebase & Google Sign-In):**
  * One-tap Google Sign-In authentication.
  * Synchronize cards and transactions across multiple devices using Cloud Firestore.
  * Real-time deletion sync: Deleting cards or transactions locally reflects in your cloud storage.
* **🎨 Theme Adaptation (Dark/Gray & Light Modes):**
  * Automatically matches your device's system appearance.
  * Fully customizable gray/black and light palette foundations via standard Material Design token colors (`colors.xml`).

---

## 🛠️ Tech Stack & Libraries

* **Language:** Java 11 / Kotlin
* **UI & Navigation:** Android Jetpack (Fragments, ViewBinding, Navigation Component, ConstraintLayout, Material3 Components)
* **Local Database:** Room Database (ORM with LiveData and custom TypeConverters)
* **Cloud & Auth:** Firebase Authentication (Google Sign-In), Cloud Firestore
* **Background Processing:** Android `NotificationListenerService`

---

## 🚀 Getting Started & Configuration

### 1. Requirements
* Android Studio Jellyfish or newer (Recommended)
* Android SDK 26 (Android 8.0) or higher

### 2. Notification Interceptor Setup
To allow the app to capture payment messages:
1. Open the app and navigate to **Settings**.
2. Tap **Grant Permission** under "Notification Permission".
3. Enable **Expense Statistics** in the system settings screen that appears.
4. Set your target banking app's title/package name in settings (Default is `Google Wallet`).

### 3. Firebase Setup (Optional for Cloud Sync)
1. Register a project on the [Firebase Console](https://console.firebase.google.com/).
2. Enable **Google Auth** in Authentication settings.
3. Enable **Cloud Firestore** and apply the following Security Rules:
