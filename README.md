# Mobile Final Project

## Map-based order tracking

This project now includes a map-first delivery finder experience in `DriverFinderFragment`.

### Implemented features
- Order pickup/drop-off points rendered on an interactive map
- Device geolocation with runtime permission handling
- A distinct current-location indicator
- Marker selection opens a delivery detail bottom sheet
- Lightweight clustering behavior for dense map areas
- Unit + instrumentation test coverage for map behavior

## Verified commands

```bash
cd /home/razbro/Repos/mobile-final-project
./gradlew :app:assembleDebug --no-daemon
./gradlew :app:testDebugUnitTest --no-daemon
./gradlew :app:assembleAndroidTest --no-daemon
```

## Notes
- Instrumentation tests compile successfully in this environment.
- Running them on-device requires Android platform tooling and a connected emulator/device.

### Optional on-device instrumentation run

```bash
cd /home/razbro/Repos/mobile-final-project
./gradlew :app:connectedDebugAndroidTest --no-daemon
```

