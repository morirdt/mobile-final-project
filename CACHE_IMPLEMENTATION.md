# Cache Implementation (Room + Image Disk Cache)

This project now includes an offline-first cache layer using Room for objects and persistent disk caching for images.

## What Was Added

- Room database: `app/src/main/java/com/example/mobilefinalproject/db/AppDatabase.kt`
- Entities:
  - `OrderEntity`
  - `UserEntity`
  - `DriverProfileEntity`
  - `ConversationEntity`
  - `ChatMessageEntity`
  - `CachedImageEntity`
- DAOs:
  - `OrderDao`, `UserDao`, `DriverProfileDao`
  - `ConversationDao`, `ChatMessageDao`, `CachedImageDao`
- DTO/entity mappers: `app/src/main/java/com/example/mobilefinalproject/db/Mappers.kt`
- Offline-first repositories updated:
  - `OrderRepository`
  - `UserRepository`
  - `DriverRepository`
  - `ChatRepository`
- ViewModels migrated to Room-backed reactive streams (`Flow` -> `LiveData`):
  - `OrderViewModel`
  - `CustomerViewModel`
  - `DriverViewModel`
- Application-level image HTTP cache with Picasso + OkHttp disk cache:
  - `app/src/main/java/com/example/mobilefinalproject/AppApplication.kt`
- Room-tracked image file cache manager:
  - `app/src/main/java/com/example/mobilefinalproject/cache/ImageCacheManager.kt`
- Profile screens now load remote images through `ImageCacheManager`:
  - `CustomerProfileFragment`
  - `DriverProfileFragment`

## Behavior

- UI reads from Room cache first.
- Network refresh updates Room tables.
- Room emits updates to UI automatically.
- Profile images are persisted on disk and indexed in Room (`cached_images`).

## Build Notes

Room uses KSP and Kotlin Android plugin now.

### Added Gradle dependencies/plugins

- Plugin: `com.google.devtools.ksp`
- Libraries: `androidx.room:room-runtime`, `androidx.room:room-ktx`, `androidx.room:room-compiler`

## Quick Verify Commands

```bash
cd /home/razbro/Repos/mobile-final-project
./gradlew :app:assembleDebug
```

```bash
cd /home/razbro/Repos/mobile-final-project
./gradlew :app:lintDebug
```

## Next Recommended Steps

1. Add DB migrations before release (currently `fallbackToDestructiveMigration()` for dev speed).
2. Add periodic stale-image cleanup scheduling (`ImageCacheManager.evictStale`).
3. Extend `ImageCacheManager` usage to order/item list adapters if desired.

