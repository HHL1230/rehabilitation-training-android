# Android App Development Skill

This skill summarizes the repeatable workflow used to build the rehabilitation-training Android app. Use it as a checklist for future Android app MVPs, especially Kotlin + Jetpack Compose apps that need local storage, reminders, sharing, emulator validation, and GitHub publishing.

## When to use this skill

Use this workflow when creating or extending an Android app that needs:

1. A clear product spec.
2. Native Android Kotlin development.
3. Jetpack Compose UI.
4. Room local persistence.
5. Notifications or reminders.
6. Text summary sharing through Android Sharesheet.
7. Emulator and physical-device validation.
8. GitHub repository setup and push.

## High-level workflow

1. Start from a short `spec.md`.
2. Convert the spec into an implementation plan.
3. Confirm core product choices with the user.
4. Create or update the Android Gradle project.
5. Implement data, domain, UI, background work, and sharing in separate packages.
6. Add unit tests for validation, calculations, export formatting, and scheduling.
7. Build and test with Gradle.
8. Install the debug APK on an emulator.
9. Update documentation.
10. Commit and push to GitHub when requested.

## Recommended project stack

| Area | Recommendation |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| State | ViewModel + StateFlow |
| Local DB | Room |
| Background reminders | WorkManager |
| Sharing | Android Sharesheet + plain-text summary |
| Tests | JUnit + Kotlin coroutines test |
| Build | Gradle Wrapper |

## Repository setup checklist

1. Add Gradle wrapper files.
2. Add `.gitignore` for Android build artifacts:
   - `.gradle/`
   - `.kotlin/`
   - `.idea/`
   - `build/`
   - `app/build/`
   - `local.properties`
   - generated Room schemas unless intentionally committed
3. Keep `local.properties` untracked.
4. Keep the app package structure explicit:
   - `data`
   - `domain`
   - `ui`
   - `reminder`
   - `sharing`

## Implementation pattern

### 1. Data layer

Use Room for local persistence.

Common files:

```text
data/
├── AppDatabase.kt
├── Entity.kt
├── Dao.kt
├── Repository.kt
└── TypeConverter.kt
```

When changing persisted data:

1. Add the new entity field.
2. Bump Room database version.
3. Add a migration.
4. Update DAO sort/query behavior if the field affects display order.
5. Add or update tests for formatting and validation.

### 2. Domain layer

Keep business rules outside Compose UI.

Typical responsibilities:

1. Input validation.
2. Data conversion from draft input to entity.
3. Statistics calculations.
4. Pure functions that can be unit tested.

### 3. UI layer

Use Compose screens with high readability:

1. Large labels and buttons.
2. Simple tab structure.
3. Clear status and validation messages.
4. Avoid dense medical or technical wording.
5. Keep input state in a ViewModel.

For high-age users:

1. Prefer direct visible fields over hidden menus.
2. Use large tap targets.
3. Avoid low-contrast UI.
4. Explain what each setting does.

### 4. Reminder layer

Use WorkManager for daily reminders unless exact alarms are required.

Pattern:

1. Store reminder settings in SharedPreferences or DataStore.
2. Use unique work names per reminder type.
3. Include identifying data in `WorkRequest` input data.
4. Re-schedule the next reminder after the worker runs.
5. Re-apply enabled schedules after boot.
6. Handle Android 13+ notification permission.

### 5. Sharing layer

Use Android Sharesheet for general compatibility.

Pattern:

1. Generate readable summary text.
2. Prefer a plain-text summary table for LINE and messaging apps.
3. Share via `ACTION_SEND`.
4. Avoid file attachments unless the product specifically needs spreadsheet export.
5. Avoid app-specific SDKs unless the product requires deep integration.

## Validation commands

Use the smallest command that validates the changed behavior:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug --no-daemon --console=plain
```

If Android Studio uses its bundled JBR:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = "C:\Users\Vivobook S16\AppData\Local\Android\Sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
.\gradlew.bat testDebugUnitTest assembleDebug --no-daemon --console=plain
```

## Emulator workflow

1. Install Android Studio.
2. Install required SDK platform and build tools.
3. Create an AVD in Device Manager.
4. Enable physical keyboard input if typing does not work:

```text
hw.keyboard=yes
```

5. Install the debug APK:

```powershell
adb install -r "app\build\outputs\apk\debug\app-debug.apk"
```

6. Launch the app:

```powershell
adb shell monkey -p <applicationId> 1
```

## Documentation checklist

Keep these files current:

1. `spec.md`
   - Product goal.
   - Current implemented behavior.
   - Technical stack.
   - Limits and future roadmap.
2. `README.md`
   - What the app does.
   - How to build.
   - How to run in Android Studio.
   - How to install to a phone.
   - Safety or domain-specific notes.

## GitHub publishing checklist

Before the first push:

```powershell
git init --initial-branch=main
git add .
git commit -m "Initial Android app"
git remote add origin <repo-url>
git push -u origin main
```

Before later pushes:

1. Check `git status --short`.
2. Verify no local SDK, build output, or secrets are staged.
3. Run Gradle validation if code changed.
4. Commit with a concise message.
5. Push.

## Lessons from the rehabilitation-training app

1. Keep reminder settings flexible early; per-item reminders are often more useful than one global reminder.
2. Record date and `HH:mm` time separately enough that users can correct backfilled entries.
3. Plain-text summary tables are easier to read in LINE than spreadsheet attachments.
4. Android emulator typing may fail if `hw.keyboard=no`; fix AVD config and restart the emulator.
5. `local.properties` should point to the user's official Android SDK, not a temporary session SDK.
6. Build and install to the emulator after meaningful UI or database changes.
