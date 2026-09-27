# ✈️ Flight Booking App (Android): Requirements & Build Guide

> **Purpose of this file:** A single source of truth for building the app in VS Code with Claude Code.
> Keep it in the project root. You can also copy the "Instructions for Claude Code" section into a `CLAUDE.md` file so Claude Code follows these rules automatically.

---

## 1. Project Overview

| Item | Detail |
|---|---|
| App name | SkyBook (working name, change anytime) |
| Platform | Android (phones first, tablets later) |
| Min Android version | Android 8.0 (API 26) |
| Target | Latest stable Android SDK |
| Core idea | Users see available flights on the **Home screen**, search and filter them, and complete a **booking flow** from flight selection to confirmation. |
| Phase 1 data | Mock (fake) flight data stored locally in the app; no real airline API yet |
| Phase 2 data | Real flight API + real backend (optional, later) |

---

## 2. Tech Stack (Which Language & Tools)

### ✅ Recommended: Kotlin + Jetpack Compose (Native Android)

This is Google's official, modern way to build Android apps. Claude Code knows it very well, so it can write most of the code for you.

| Layer | Technology | Why |
|---|---|---|
| Language | **Kotlin** | Official Android language |
| UI | **Jetpack Compose** + **Material 3** | Modern declarative UI, similar in spirit to React components |
| Architecture | **MVVM** (Model-View-ViewModel) | Clean separation of UI and logic |
| Navigation | **Navigation Compose** | Screen-to-screen navigation |
| State | `ViewModel` + `StateFlow` | Screens react to data changes |
| Dependency Injection | **Hilt** | Wires classes together cleanly |
| Local Database | **Room** | Stores bookings on the device |
| Networking (Phase 2) | **Retrofit** + **OkHttp** + **Kotlinx Serialization** | Calls real APIs |
| Images | **Coil** | Loads airline logos |
| Async | **Kotlin Coroutines** | Background work |
| Build System | **Gradle (Kotlin DSL)** + Version Catalog (`libs.versions.toml`) | Standard Android build |
| Testing | JUnit, Compose UI Test | Basic tests |

### Alternatives (only if you prefer)
| Option | When to choose |
|---|---|
| **Flutter (Dart)** | You want Android + iOS from one codebase |
| **React Native (Expo)** | You're more comfortable with JavaScript/web skills |

> This guide assumes **Kotlin + Jetpack Compose**.

---

## 3. Development Setup (Mac + VS Code + Claude Code)

VS Code is great for editing with Claude Code, but Android needs the SDK and an emulator, which come with Android Studio.

### Step-by-step
1. **Install Android Studio** (free, from developer.android.com). You need it for the Android SDK and emulator, even if you write code in VS Code.
2. Open Android Studio → **SDK Manager** → install the latest Android SDK + Build Tools.
3. Open **Device Manager** → create an emulator (e.g., Pixel 8).
4. **Create the project** in Android Studio:
   `New Project → Empty Activity (Compose)` → Name: `SkyBook` → Language: Kotlin → Min SDK: API 26.
5. Close Android Studio and **open the same folder in VS Code**.
6. Install VS Code extensions:
   - *Kotlin* (language support)
   - *Gradle for Java* (optional)
7. Add this `.md` file to the project root.
8. Run from the VS Code terminal:
   ```bash
   ./gradlew assembleDebug      # build the app
   ./gradlew installDebug       # install on the running emulator
   ```
   (Start the emulator first from Android Studio's Device Manager, or with `emulator -avd <name>`.)

> **Tip:** Running and debugging the app visually is easiest in Android Studio. Use VS Code + Claude Code for writing code, and Android Studio for running and previewing. Both can have the same folder open.

---

## 4. Features & Requirements

### 4.1 Functional Requirements

#### A. Home Screen: Flight List
- FR-01: Show a list of available flights on app launch.
- FR-02: Each flight card shows: airline logo + name, flight number, departure → arrival city (codes like BLR → DEL), departure & arrival time, duration, stops (Non-stop / 1 stop), price.
- FR-03: Search bar / search panel at the top: **From**, **To**, **Date**, **Passengers**, **Class** (Economy / Business).
- FR-04: Swap button to switch From ↔ To.
- FR-05: Pull-to-refresh on the list.
- FR-06: Show loading state, empty state ("No flights found"), and error state.

#### B. Search Results, Filter & Sort
- FR-07: Sort by: Cheapest, Fastest, Earliest departure.
- FR-08: Filter by: stops, airline, departure time range, price range.
- FR-09: Show result count ("24 flights found").

#### C. Flight Details
- FR-10: Full details: timings, duration, aircraft, baggage allowance, fare rules (refundable / non-refundable).
- FR-11: Fare options (e.g., Saver / Flexi) with price difference.
- FR-12: "Book Now" button.

#### D. Booking Flow
| Step | Screen | Requirements |
|---|---|---|
| 1 | **Passenger Details** | Name, age/DOB, gender, email, phone. One form per passenger. Validation on all fields. |
| 2 | **Seat Selection** (optional step) | Seat map grid, available / booked / selected states, extra charge for premium seats. "Skip" option. |
| 3 | **Add-ons** (optional) | Extra baggage, meals. |
| 4 | **Review Booking** | Summary of flight, passengers, seats, add-ons, price breakdown (base fare + taxes + add-ons = total). |
| 5 | **Payment** | Mock payment screen (Card / UPI options). Phase 1: always succeeds after a short fake delay. |
| 6 | **Confirmation** | Success animation, booking reference (PNR like `SKY7X2K9`), trip summary, "Go to My Trips" button. |

- FR-13: Show a progress indicator across booking steps (Step 2 of 5).
- FR-14: Back navigation keeps entered data.
- FR-15: Price total updates live as seats/add-ons change.

#### E. My Trips (Bookings)
- FR-16: List of upcoming and past bookings (saved in Room database).
- FR-17: Tap a booking to view the e-ticket / booking details.
- FR-18: Cancel booking (mock), status changes to "Cancelled".

#### F. Profile (simple)
- FR-19: Name, email, saved passengers (optional).
- FR-20: Phase 1: no real login. Phase 2: login with email/OTP.

### 4.2 Non-Functional Requirements
- NFR-01: Smooth scrolling (use `LazyColumn`).
- NFR-02: Support light and dark mode.
- NFR-03: Works on screen sizes from 5" to 7" phones.
- NFR-04: All text in `strings.xml` (ready for translation later).
- NFR-05: Accessibility: content descriptions on icons, minimum 48dp touch targets.
- NFR-06: Prices formatted in INR (₹) using proper currency formatting.
- NFR-07: No crashes on rotation (state saved in ViewModel).

---

## 5. Screens & Navigation

```
Splash
  └── Home (Flight List + Search)
        ├── Search Results (Filter / Sort)
        │     └── Flight Details
        │           └── Passenger Details
        │                 └── Seat Selection
        │                       └── Add-ons
        │                             └── Review Booking
        │                                   └── Payment
        │                                         └── Confirmation → My Trips
        ├── My Trips (bottom nav)
        │     └── Booking Details (e-ticket)
        └── Profile (bottom nav)
```

**Bottom Navigation:** Home · My Trips · Profile

---

## 6. Data Models

```kotlin
data class Airport(
    val code: String,        // "BLR"
    val city: String,        // "Bengaluru"
    val name: String         // "Kempegowda International Airport"
)

data class Flight(
    val id: String,
    val airlineName: String,
    val airlineLogoUrl: String,
    val flightNumber: String,     // "6E 2134"
    val from: Airport,
    val to: Airport,
    val departureTime: String,    // ISO date-time
    val arrivalTime: String,
    val durationMinutes: Int,
    val stops: Int,
    val price: Double,
    val cabinClass: CabinClass,
    val seatsAvailable: Int,
    val isRefundable: Boolean,
    val baggageKg: Int
)

enum class CabinClass { ECONOMY, BUSINESS }

data class Passenger(
    val fullName: String,
    val age: Int,
    val gender: String,
    val email: String?,
    val phone: String?,
    val seatNumber: String? = null
)

data class Seat(
    val number: String,        // "12A"
    val isAvailable: Boolean,
    val isPremium: Boolean,
    val extraPrice: Double
)

data class Booking(          // Room @Entity
    val bookingId: String,
    val pnr: String,
    val flight: Flight,
    val passengers: List<Passenger>,
    val addOnsTotal: Double,
    val totalPrice: Double,
    val status: BookingStatus,
    val bookedAt: Long
)

enum class BookingStatus { CONFIRMED, CANCELLED, COMPLETED }
```

---

## 7. Project Folder Structure

```
app/src/main/java/com/skybook/
├── MainActivity.kt
├── SkyBookApp.kt                 // @HiltAndroidApp
├── core/
│   ├── designsystem/             // Theme, colors, typography, reusable components
│   │   ├── theme/
│   │   └── components/           // PrimaryButton, FlightCard, PriceText...
│   └── util/                     // date/price formatters
├── data/
│   ├── model/                    // data classes above
│   ├── mock/                     // MockFlightData.kt (fake JSON/list)
│   ├── local/                    // Room DB, DAOs, entities
│   ├── remote/                   // Retrofit API (Phase 2)
│   └── repository/               // FlightRepository, BookingRepository
├── di/                           // Hilt modules
├── navigation/
│   └── AppNavGraph.kt
└── feature/
    ├── home/                     // HomeScreen + HomeViewModel
    ├── search/                   // SearchResultsScreen + ViewModel
    ├── flightdetails/
    ├── booking/                  // passenger, seats, addons, review, payment, confirmation
    │   └── BookingViewModel.kt   // shared across booking steps
    ├── trips/
    └── profile/
```

---

## 8. Design Guidelines

- **Design system:** Material 3
- **Primary color:** Deep blue (e.g., `#1E40AF`) · **Accent:** Orange (e.g., `#F97316`) for price and CTA highlights
- **Font:** Inter or Poppins (Google Fonts)
- **Spacing:** 4 / 8 / 12 / 16 / 24 dp scale
- **Corner radius:** 12–16dp for cards
- **Flight card layout:**
  ```
  [Logo] IndiGo · 6E 2134                  ₹4,599
  06:10 ──────── 2h 45m ──────── 08:55
  BLR            Non-stop            DEL
  ```
- Use skeleton / shimmer loaders instead of plain spinners.
- If you have Figma designs, share screenshots with Claude Code and ask it to match them screen by screen.

---

## 9. Build Plan (Phases & Milestones)

Build in small steps. Ask Claude Code to do **one milestone at a time**, then run the app and check it before moving on.

| # | Milestone | Done when |
|---|---|---|
| 1 | Project setup: dependencies (Hilt, Navigation, Room, Coil), theme, folder structure | App runs showing an empty themed screen |
| 2 | Design system: colors, typography, reusable components (FlightCard, buttons) | Components show in `@Preview` |
| 3 | Mock data + FlightRepository (20–30 fake flights across Indian cities) | Data loads in ViewModel |
| 4 | Home screen: search panel + flight list | Flights visible on launch |
| 5 | Search results: filter + sort | Sort/filter change the list |
| 6 | Flight details screen | Tap card → details open |
| 7 | Booking flow: passenger → seats → add-ons → review | Data carries across steps, total is correct |
| 8 | Mock payment + confirmation + PNR generation | Booking saved to Room |
| 9 | My Trips + booking details + cancel | Saved bookings appear after restart |
| 10 | Bottom nav, profile, dark mode, empty/error states | Full app navigable |
| 11 | Polish: animations, accessibility, testing | Ready for demo |
| 12 | *(Phase 2)* Real API + login + real payment gateway | Live data |

### Phase 2 options (later)
- **Flight data API:** a flight search API such as Amadeus Self-Service or Duffel (check current pricing and availability before choosing).
- **Backend:** Firebase (Auth + Firestore) is the easiest for beginners.
- **Payments:** Razorpay Android SDK (common in India) in test mode first.

---

## 10. Instructions for Claude Code

> Copy this section into `CLAUDE.md` in the project root.

```markdown
# CLAUDE.md: SkyBook Flight Booking App

## Stack
- Kotlin, Jetpack Compose, Material 3, MVVM, Hilt, Navigation Compose, Room, Coroutines/StateFlow, Coil.
- Gradle Kotlin DSL with version catalog (gradle/libs.versions.toml). Use latest stable versions.

## Rules
- Follow the folder structure in FLIGHT_BOOKING_APP.md section 7.
- One screen = one Composable file + one ViewModel (booking flow shares BookingViewModel).
- ViewModels expose UI state as StateFlow<UiState> (sealed class: Loading / Success / Error / Empty).
- No business logic inside Composables.
- Every reusable Composable gets a @Preview.
- All user-facing text goes in strings.xml.
- Format prices in INR with NumberFormat.
- Phase 1 uses mock data only (data/mock). Do not add real API calls unless asked.
- After each change, make sure `./gradlew assembleDebug` succeeds.
- Work one milestone at a time (see section 9) and explain briefly what was built and how to test it.
- I'm a beginner: keep code simple and add short comments on non-obvious parts.
```

---

## 11. Example Prompts to Use in Claude Code

1. *"Read FLIGHT_BOOKING_APP.md. Do Milestone 1: add all dependencies, set up Hilt, the theme, and the folder structure. Make sure it builds."*
2. *"Do Milestone 3: create MockFlightData with 25 realistic flights between BLR, DEL, BOM, MAA, HYD, CCU, GOI using airlines like IndiGo, Air India, Akasa Air, SpiceJet."*
3. *"Do Milestone 4: build HomeScreen with the search panel and flight list using FlightCard, as described in sections 4.1-A and 8."*
4. *"Here's a screenshot of my Figma design for the flight card. Update FlightCard to match it exactly."*
5. *"The build fails with this error: [paste error]. Fix it."*
6. *"Review the booking flow for bugs: check that the total price is correct and back navigation keeps the data."*

---

## 12. Testing Checklist

- [ ] App launches and shows flights
- [ ] Search by From/To/Date filters the list
- [ ] Swap From ↔ To works
- [ ] Sort and filters work together
- [ ] Passenger form validates (empty name, invalid email/phone)
- [ ] Seat selection limits seats to number of passengers
- [ ] Total price = base fare × passengers + seats + add-ons + taxes
- [ ] Booking appears in My Trips after app restart
- [ ] Cancel booking updates status
- [ ] Dark mode looks correct
- [ ] Rotating the screen doesn't lose data
- [ ] No crash with no flights / empty search

---

## 13. Future Enhancements
- Round-trip and multi-city search
- Price alerts & push notifications (Firebase Cloud Messaging)
- Download e-ticket as PDF
- Multi-language support (Hindi, Kannada)
- iOS version (consider Kotlin Multiplatform or Flutter)
