# CLAUDE.md: SkyBook Flight Booking App

## Stack
- Kotlin, Jetpack Compose, Material 3, MVVM, Hilt, Navigation Compose, Room, Coroutines/StateFlow, Coil.
- Gradle Kotlin DSL with version catalog (gradle/libs.versions.toml).

## Rules
- Follow the folder structure in FLIGHT_BOOKING_APP.md section 7.
- One screen = one Composable file + one ViewModel (booking flow shares BookingViewModel, scoped to the nested `booking/...` nav graph).
- ViewModels expose UI state as StateFlow; loading data uses `UiState` (Loading / Success / Error / Empty) from core/util.
- No business logic inside Composables. Pure rules live in `BookingRules` and `FlightFilters` (unit tested).
- Every reusable Composable gets a @Preview.
- All user-facing text goes in strings.xml.
- Format prices in INR with `Formatters.price`.
- Phase 1 uses mock data only (data/mock). Do not add real API calls unless asked.
- After each change, make sure `./gradlew assembleDebug testDebugUnitTest` succeeds.
- I'm a beginner: keep code simple and add short comments on non-obvious parts.

## Status
Milestones 1–11 from FLIGHT_BOOKING_APP.md section 9 are implemented. Milestone 12 (real API, login, payments) is not started.
