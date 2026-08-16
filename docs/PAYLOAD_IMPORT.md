Payload import and format for Root-My-Galaxy

Overview
- A local debug payload is provided under app/src/debug/assets/payloads/sample-a53
- This is intended for manual testing and import into the app's runtime files directory

Schema (manifest.json)
- profileId: string (directory/id used under filesDir/payloads)
- displayName: human string
- model: device model matching (e.g., SM-A536B)
- kernelVersion: kernel version string used by the app's matching logic
- exploit: filename of the exploit artifact inside the payload folder
- kernelSu: filename of the KernelSU artifact inside the payload folder

Importer
- Debug-only Activity: dev.busung.s25uroot.LocalPayloadImporterActivity
- How to run: install debug APK and run via adb:
  adb shell am start -n dev.busung.s25uroot/.LocalPayloadImporterActivity
- The importer copies all files from assets/payloads/sample-a53 into the app filesDir/payloads/sample-a53 and sets executable bits where possible.

Build & target
- app/build.gradle.kts already sets ndk.abiFilters += "arm64-v8a" which targets modern Samsung A53 (arm64)
- compileSdk=37, minSdk=33, targetSdk=36. These choices match Android 13/14 device compatibility. No further changes required for debug builds.

APK location after build
- app/build/outputs/apk/debug/app-debug.apk

Manual test steps
1. Build and install debug APK: .\gradlew.bat :app:assembleDebug then adb install -r app/build/outputs/apk/debug/app-debug.apk
2. Run importer via ADB (see command above). The app will toast a success message.
3. Verify files exist on device in /data/data/dev.busung.s25uroot/files/payloads/sample-a53 (requires root or run-as to inspect):
   adb shell run-as dev.busung.s25uroot ls -l files/payloads/sample-a53

Notes
- Payload files here are placeholders and contain no exploit code. Do NOT include real binaries in this test payload.
- For production usage, maintain payloads in the Root-My-Galaxy-Payloads repository and let the app download them per existing logic.
