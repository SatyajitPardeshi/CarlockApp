# CarlockApp
# Mobile-Controlled Vehicle Lock/Unlock System

This project implements a Wi-Fi-based vehicle locking system using a CC3200 LaunchPad and an Android application. It enables users to lock or unlock a vehicle remotely via mobile commands, providing real-time status feedback through relay activation and buzzer alerts.

## Components

- **CC3200 LaunchPad**: Operates in Wi-Fi Access Point mode (`CarLockSystem`), hosts HTTP endpoints `/lock` and `/unlock`.
- **Relay Module**: Simulates the vehicle door lock.
- **Buzzer**: Provides audible confirmation on each command.
- **Android App**: Developed using Jetpack Compose; connects directly to the CC3200, provides lock status display, connection monitoring, and light/dark theme toggle.

## How to Run

1. Open `LockControl.ino` in Energia and upload to CC3200.
2. Import the Android app into Android Studio.
3. Connect the phone to `CarLockSystem` Wi-Fi and launch the app.

## Features

- Remote lock/unlock via app
- Manual serial control (L/U commands)
- Real-time feedback (sound, vibration, status display)
- No internet required (local Wi-Fi AP mode)

## Documentation

- [Presentation.pdf](./Mobile-Controlled%20Vehicle%20Lock.pdf): Project overview
- [Use_Case_Report.pdf](./Use_case_Lock_Unlock_SatyajitSushantPardeshi.pdf): Use case analysis
- [Evaluation Form](https://forms.gle/AXKENWdk3NMMSnyU8)

## Limitations

- Wi-Fi AP disables mobile internet
- No persistent lock state
- Single-device connection

## Future Improvements

- Authentication (PIN/Fingerprint)
- Lock history logging
- Auto-lock scheduler
- Bluetooth fallback
