# Sai Ireland — Sathya Sai Organisation Ireland App

Native Android app for the Sathya Sai Organisation Ireland community.

## Features

| Feature | Status |
|---|---|
| Google Sign-In (Credential Manager API) + GDPR consent | ✅ |
| Events — list, detail, admin create/edit/delete, reminders | ✅ |
| Polls — create, vote (one-per-user enforced), live results | ✅ |
| WhatsApp group join request flow | ✅ |
| Contact admin (subject-categorised messages) | ✅ |
| Bhajan Library — search, lyrics, audio link | ✅ |
| Home — personalised greeting, daily quote, announcements | ✅ |
| Push notifications (FCM topics) + per-preference toggles | ✅ |
| Settings — dark mode, notifications, analytics opt-in, account deletion | ✅ |
| Profile screen | ✅ |
| Gallery, Seva sign-up, Meditation Timer | Stub (coming soon) |

## Tech stack

- **Language**: Kotlin 2.2.10
- **UI**: Jetpack Compose + Material 3 (saffron orange seed colour)
- **DI**: Hilt 2.52.0 (KSP)
- **Navigation**: Navigation Compose (single back-stack)
- **Backend**: Firebase Auth · Firestore · FCM · Analytics · Crashlytics
- **Local cache**: Room 2.7 + DataStore Preferences
- **Images**: Coil 2.7
- **Min SDK**: 24 (Android 7.0) · **Target/Compile SDK**: 36 (Android 16)

## Project setup

### Prerequisites

- Android Studio Meerkat (2024.3) or later
- JDK 11+
- A Firebase project with **Authentication**, **Firestore**, **Cloud Messaging**, **Analytics**, and **Crashlytics** enabled

### 1. Clone and open

```bash
git clone https://github.com/shayshankr/sai-ireland-app.git
cd sai-ireland-app
```

Open in Android Studio: **File → Open** → select the repo root.

### 2. Add `google-services.json`

Download `google-services.json` from the Firebase Console (**Project Settings → Your apps → Android**) and place it at:

```
app/google-services.json
```

This file is in `.gitignore` and must **never** be committed.

### 3. Enable Google Sign-In

In the Firebase Console → **Authentication → Sign-in method → Google**, ensure it is enabled.  
Add your debug and release SHA-1 fingerprints under **Project Settings → Your apps**.

Get your debug SHA-1:
```bash
./gradlew signingReport
```

### 4. Deploy Firestore security rules

```bash
firebase deploy --only firestore:rules
```

The rules file is `firestore.rules` in the repo root.

### 5. Build and run

```bash
./gradlew assembleDebug          # debug APK
./gradlew installDebug           # install on connected device / emulator
./gradlew test                   # unit tests
```

## Building a release AAB

1. **Create a keystore** (one-time, store it securely — do not commit):
   ```bash
   keytool -genkey -v -keystore sai-ireland-release.jks \
     -alias sai-ireland -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Add signing config to `app/build.gradle.kts`:
   ```kotlin
   signingConfigs {
       create("release") {
           storeFile = file("../sai-ireland-release.jks")
           storePassword = System.getenv("KEYSTORE_PASSWORD")
           keyAlias = "sai-ireland"
           keyPassword = System.getenv("KEY_PASSWORD")
       }
   }
   buildTypes {
       release { signingConfig = signingConfigs.getByName("release") }
   }
   ```

3. Build the AAB:
   ```bash
   KEYSTORE_PASSWORD=xxx KEY_PASSWORD=xxx ./gradlew bundleRelease
   # Output: app/build/outputs/bundle/release/app-release.aab
   ```

4. Upload to **Google Play Console → Internal Testing** or **Firebase App Distribution**.

## Firestore collections

| Collection | Description |
|---|---|
| `users/{uid}` | User profile; role field (`member` / `admin` / `super_admin`) |
| `events/{id}` | Community events; admin-write, signed-in-read |
| `polls/{id}` | Polls; `votes/{uid}` sub-collection (one per user, immutable) |
| `announcements/{id}` | Active announcements shown on Home |
| `quotes/{yyyy-MM-dd}` | Daily Sai Baba quote (optional — fallback to built-in 14 quotes) |
| `bhajans/{id}` | Bhajan songs with lyrics and optional audio URL |
| `joinRequests/{uid}` | WhatsApp group join requests |
| `messages/{id}` | Contact-admin messages; admin-read only |

## Setting the first admin

In the Firebase Console → **Firestore → users → {uid}**, set:

```json
{ "role": "admin" }
```

Only a `super_admin` can promote other admins programmatically.

## Privacy & GDPR

- Analytics and Crashlytics are **off by default**; users opt in from Settings.
- No PII is written to analytics events or logs.
- Account deletion removes the user document; votes/messages are retained anonymously per the in-app disclosure (deletable within 30 days via a Cloud Function — see roadmap).

## CI / CD

GitHub Actions runs on every push and pull request to `master`.  
See [`.github/workflows/ci.yml`](.github/workflows/ci.yml).

Required repository secrets:

| Secret | Value |
|---|---|
| `GOOGLE_SERVICES_JSON` | Base64-encoded contents of `google-services.json` |

## Roadmap

- [ ] Announcements admin screen (create / deactivate)
- [ ] Gallery (Firebase Storage grid)
- [ ] Seva sign-up with RSVP
- [ ] Meditation timer (countdown + bell)
- [ ] Cloud Function for scheduled account deletion cleanup
- [ ] Admin dashboard
- [ ] iPad / tablet adaptive layout

## License

Private — © Sathya Sai Organisation Ireland. All rights reserved.
