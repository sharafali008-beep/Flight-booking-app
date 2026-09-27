package com.skybook.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.skybook.R
import com.skybook.feature.booking.AddOnsScreen
import com.skybook.feature.booking.BookingViewModel
import com.skybook.feature.booking.ConfirmationScreen
import com.skybook.feature.booking.PassengerDetailsScreen
import com.skybook.feature.booking.PaymentScreen
import com.skybook.feature.booking.ReviewBookingScreen
import com.skybook.feature.booking.SeatSelectionScreen
import com.skybook.feature.flightdetails.FlightDetailsScreen
import com.skybook.feature.home.HomeScreen
import com.skybook.feature.profile.ProfileScreen
import com.skybook.feature.search.SearchResultsScreen
import com.skybook.feature.trips.BookingDetailsScreen
import com.skybook.feature.trips.TripsScreen

private enum class TopLevel(
    val route: String,
    @StringRes val label: Int,
    val selectedIcon: ImageVector,
    val icon: ImageVector,
) {
    HOME(Routes.HOME, R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
    TRIPS(Routes.TRIPS, R.string.nav_trips, Icons.Filled.Luggage, Icons.Outlined.Luggage),
    PROFILE(Routes.PROFILE, R.string.nav_profile, Icons.Filled.Person, Icons.Outlined.Person),
}

/** App shell: bottom navigation (Home · My Trips · Profile) + all screens. */
@Composable
fun SkyBookRoot() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentDestination = backStack?.destination
    val showBottomBar = TopLevel.entries.any { it.route == currentDestination?.route }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevel.entries.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = { navController.navigateTopLevel(item.route) },
                            icon = { Icon(if (selected) item.selectedIcon else item.icon, contentDescription = null) },
                            label = { Text(stringResource(item.label)) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        AppNavGraph(navController, Modifier.padding(padding))
    }
}

/** Switch bottom-nav tabs without piling up copies of screens on the back stack. */
private fun NavController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController, startDestination = Routes.HOME, modifier = modifier) {
        composable(Routes.HOME) {
            HomeScreen(
                onSearch = { from, to, date, pax, cabin ->
                    navController.navigate(Routes.search(from, to, date, pax, cabin))
                },
                onFlightClick = { flight, pax -> navController.navigate(Routes.flightDetails(flight.id, pax)) }
            )
        }
        composable(Routes.TRIPS) {
            TripsScreen(
                onBookingClick = { navController.navigate(Routes.bookingDetails(it)) },
                onBookFlight = { navController.navigateTopLevel(Routes.HOME) }
            )
        }
        composable(Routes.PROFILE) { ProfileScreen() }

        composable(Routes.SEARCH) {
            SearchResultsScreen(
                onBack = navController::popBackStack,
                onFlightClick = { flight, pax -> navController.navigate(Routes.flightDetails(flight.id, pax)) }
            )
        }
        composable(Routes.FLIGHT_DETAILS) {
            FlightDetailsScreen(
                onBack = navController::popBackStack,
                onBookNow = { id, fare, pax -> navController.navigate(Routes.booking(id, fare, pax)) }
            )
        }
        composable(Routes.BOOKING_DETAILS) {
            BookingDetailsScreen(onBack = navController::popBackStack)
        }

        // ---- Booking flow (nested graph sharing one BookingViewModel) ----
        navigation(startDestination = Routes.PASSENGERS, route = Routes.BOOKING_GRAPH) {
            composable(Routes.PASSENGERS) { entry ->
                PassengerDetailsScreen(
                    viewModel = entry.bookingViewModel(navController),
                    onBack = navController::popBackStack,
                    onContinue = { navController.navigate(Routes.SEATS) }
                )
            }
            composable(Routes.SEATS) { entry ->
                SeatSelectionScreen(
                    viewModel = entry.bookingViewModel(navController),
                    onBack = navController::popBackStack,
                    onContinue = { navController.navigate(Routes.ADD_ONS) }
                )
            }
            composable(Routes.ADD_ONS) { entry ->
                AddOnsScreen(
                    viewModel = entry.bookingViewModel(navController),
                    onBack = navController::popBackStack,
                    onContinue = { navController.navigate(Routes.REVIEW) }
                )
            }
            composable(Routes.REVIEW) { entry ->
                ReviewBookingScreen(
                    viewModel = entry.bookingViewModel(navController),
                    onBack = navController::popBackStack,
                    onContinue = { navController.navigate(Routes.PAYMENT) }
                )
            }
            composable(Routes.PAYMENT) { entry ->
                PaymentScreen(
                    viewModel = entry.bookingViewModel(navController),
                    onBack = navController::popBackStack,
                    onPaid = {
                        // Payment is done: remove the form steps so "back" can't return to them.
                        navController.navigate(Routes.CONFIRMATION) {
                            popUpTo(Routes.PASSENGERS) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.CONFIRMATION) { entry ->
                ConfirmationScreen(
                    viewModel = entry.bookingViewModel(navController),
                    onGoToTrips = {
                        navController.navigate(Routes.TRIPS) {
                            popUpTo(Routes.HOME) { saveState = false }
                            launchSingleTop = true
                        }
                    },
                    onGoHome = { navController.popBackStack(Routes.HOME, inclusive = false) }
                )
            }
        }
    }
}

/**
 * Returns the BookingViewModel owned by the booking graph (not by the single screen),
 * so every step sees the same data.
 */
@Composable
private fun NavBackStackEntry.bookingViewModel(navController: NavController): BookingViewModel {
    val graphId = destination.parent!!.id
    val parentEntry = remember(this) { navController.getBackStackEntry(graphId) }
    return hiltViewModel(parentEntry)
}
