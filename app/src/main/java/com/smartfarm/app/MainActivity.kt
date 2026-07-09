package com.smartfarm.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smartfarm.app.ui.AppViewModel
import com.smartfarm.app.ui.components.AppNavigationScaffold
import com.smartfarm.app.ui.navigation.Screen
import com.smartfarm.app.ui.screens.admin.AdminGradesScreen
import com.smartfarm.app.ui.screens.admin.AdminPricingScreen
import com.smartfarm.app.ui.screens.admin.AdminUsersScreen
import com.smartfarm.app.ui.screens.community.CommunityScreen
import com.smartfarm.app.ui.screens.crops.CropsScreen
import com.smartfarm.app.ui.screens.dashboard.DashboardScreen
import com.smartfarm.app.ui.screens.diagnose.DiagnoseScreen
import com.smartfarm.app.ui.screens.inventory.InventoryScreen
import com.smartfarm.app.ui.screens.journal.JournalScreen
import com.smartfarm.app.ui.screens.links.LinksScreen
import com.smartfarm.app.ui.screens.login.LoginScreen
import com.smartfarm.app.ui.screens.market.MarketScreen
import com.smartfarm.app.ui.screens.more.MoreScreen
import com.smartfarm.app.ui.screens.pesticide.PesticideScreen
import com.smartfarm.app.ui.screens.pricing.PricingScreen
import com.smartfarm.app.ui.screens.profile.ProfileScreen
import com.smartfarm.app.ui.screens.rda.RdaScreen
import com.smartfarm.app.ui.screens.safety.SafetyScreen
import com.smartfarm.app.ui.screens.settings.SettingsScreen
import com.smartfarm.app.ui.screens.shipment.ShipmentScreen
import com.smartfarm.app.ui.screens.support.SupportScreen
import com.smartfarm.app.ui.screens.tasks.TasksScreen
import com.smartfarm.app.ui.screens.weather.WeatherScreen
import com.smartfarm.app.ui.theme.SmartFarmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as SmartFarmApp).container

        setContent {
            SmartFarmTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val appViewModel: AppViewModel = viewModel(factory = AppViewModel.factory(container))
                    val sessionUser by appViewModel.sessionUser.collectAsState()
                    val isLoading by appViewModel.isLoading.collectAsState()

                    when {
                        isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                        sessionUser == null -> LoginScreen(appViewModel = appViewModel)
                        else -> MainAppScaffold(appViewModel = appViewModel, isAdmin = sessionUser!!.isAdmin)
                    }
                }
            }
        }
    }
}

@Composable
private fun MainAppScaffold(appViewModel: AppViewModel, isAdmin: Boolean) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Screen.Dashboard.route

    AppNavigationScaffold(
        currentRoute = currentRoute,
        isAdmin = isAdmin,
        onNavigate = { screen ->
            navController.navigate(screen.route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        },
        onLogout = { appViewModel.logout() },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen(isAdmin = isAdmin) }
            composable(Screen.Crops.route) { CropsScreen() }
            composable(Screen.Journal.route) { JournalScreen() }
            composable(Screen.More.route) {
                MoreScreen(isAdmin = isAdmin, onNavigate = { screen -> navController.navigate(screen.route) })
            }
            composable(Screen.Tasks.route) { TasksScreen() }
            composable(Screen.Inventory.route) { InventoryScreen() }
            composable(Screen.Diagnose.route) { DiagnoseScreen() }
            composable(Screen.Market.route) { MarketScreen() }
            composable(Screen.Weather.route) { WeatherScreen() }
            composable(Screen.Pesticide.route) { PesticideScreen() }
            composable(Screen.Support.route) { SupportScreen() }
            composable(Screen.Rda.route) { RdaScreen() }
            composable(Screen.Community.route) { CommunityScreen() }
            composable(Screen.Shipment.route) { ShipmentScreen() }
            composable(Screen.Safety.route) { SafetyScreen() }
            composable(Screen.Pricing.route) { PricingScreen() }
            composable(Screen.Profile.route) { ProfileScreen(appViewModel = appViewModel) }
            composable(Screen.Links.route) { LinksScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
            composable(Screen.AdminUsers.route) { AdminUsersScreen() }
            composable(Screen.AdminPricing.route) { AdminPricingScreen() }
            composable(Screen.AdminGrades.route) { AdminGradesScreen() }
        }
    }
}
