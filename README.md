# Mobile Final Project

## Map-based order tracking

This project now includes a map-first delivery finder experience in `DriverFinderFragment`.

### Implemented features
- Order pickup/drop-off points rendered on an interactive map
- Device geolocation with runtime permission handling
- A distinct current-location indicator
- Marker selection opens a delivery detail bottom sheet
- Lightweight clustering behavior for dense map areas
- Customer dashboard with order counters and quick actions
- Customer My Orders list with status filtering
- Customer New Order flow with address/date/time/description/budget validation
- Places autocomplete support with manual-address fallback when no Maps API key is set
- Unit + instrumentation test coverage for map behavior and customer data flow

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

