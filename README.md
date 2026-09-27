# SkyBook ✈️

Android flight booking app (Kotlin + Jetpack Compose), built from `FLIGHT_BOOKING_APP.md`.
Phase 1: mock flight data, bookings saved on the device with Room, mock payment.

## Run it

1. Install **Android Studio** and open this folder (`File → Open`). It will set up the SDK path (`local.properties`) and sync Gradle.
2. Create an emulator in **Device Manager** (e.g. Pixel 8, API 35) and press ▶ Run.

From a terminal (after Android Studio has created `local.properties`):

```bash
./gradlew assembleDebug        # build
./gradlew installDebug         # install on a running emulator/phone
./gradlew testDebugUnitTest    # unit tests (price maths, validation, sort/filter)
```

## What's inside

| Screen | Where |
|---|---|
| Home: search panel (From/To/swap/date/passengers/class) + flight list with pull-to-refresh | `feature/home` |
| Search results: sort (cheapest/fastest/earliest), filters (stops/airline/time/price), result count | `feature/search` |
| Flight details: timings, aircraft, baggage, Saver/Flexi fares, fare rules | `feature/flightdetails` |
| Booking: passengers → seats → add-ons → review → payment → confirmation (PNR) | `feature/booking` |
| My Trips: upcoming/past, e-ticket, cancel | `feature/trips` |
| Profile: name/email/phone (pre-fills passenger 1), saved passengers | `feature/profile` |

Mock data: 26 daily routes between BLR, DEL, BOM, MAA, HYD, CCU and GOI (IndiGo, Air India, Akasa Air, SpiceJet, Air India Express), bookable for the next 30 days. Business class is available on Air India flights.

Pricing: `total = fare × passengers + seat charges + add-ons + 12% taxes` (see `BookingRules`).
