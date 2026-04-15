package com.example.gunplaradar.ui.navigation

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.gunplaradar.GunplaRadarApplication
import com.example.gunplaradar.data.entity.GunplaItemEntity
import com.example.gunplaradar.data.repository.GunplaRepository
import com.example.gunplaradar.ui.calendar.CalendarScreen
import com.example.gunplaradar.ui.calendar.CalendarViewModel
import com.example.gunplaradar.ui.calendar.StockDiffScreen
import com.example.gunplaradar.ui.patrol.*
import com.example.gunplaradar.ui.settings.SettingsScreen
import com.example.gunplaradar.ui.settings.SettingsViewModel
import com.example.gunplaradar.ui.store.StoreListScreen
import com.example.gunplaradar.ui.store.StoreScreen
import com.example.gunplaradar.ui.store.StoreViewModel
import com.example.gunplaradar.ui.wishlist.GunplaFormScreen
import com.example.gunplaradar.ui.wishlist.WishlistScreen
import com.example.gunplaradar.ui.wishlist.WishlistViewModel
import kotlinx.coroutines.launch

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Wishlist : BottomNavItem("wishlist", "ほしい物", Icons.Default.List)
    object Calendar : BottomNavItem("calendar", "カレンダー", Icons.Default.DateRange)
    object Store : BottomNavItem("store", "店舗分析", Icons.Default.LocationOn)
    object Patrol : BottomNavItem("patrol", "巡回", Icons.Default.DirectionsWalk)
    object Settings : BottomNavItem("settings", "設定", Icons.Default.Settings)
}

object Routes {
    const val WISHLIST = "wishlist"
    const val GUNPLA_FORM_ADD = "gunpla_form_add"
    const val GUNPLA_FORM_EDIT = "gunpla_form_edit/{itemId}"
    const val CALENDAR = "calendar"
    const val STOCK_DIFF = "stock_diff"
    const val STORE = "store"
    const val STORE_LIST = "store_list"
    const val PATROL = "patrol"
    const val PATROL_FORM = "patrol_form"
    const val PATROL_DETAIL = "patrol_detail/{planId}"
    const val SETTINGS = "settings"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavGraph() {
    val context = LocalContext.current
    val app = context.applicationContext as GunplaRadarApplication
    val repository = remember {
        GunplaRepository(
            app.database.gunplaItemDao(),
            app.database.storeDao(),
            app.database.stockDelayRecordDao(),
            app.database.patrolPlanDao()
        )
    }

    val navController = rememberNavController()
    val bottomNavItems = listOf(
        BottomNavItem.Wishlist,
        BottomNavItem.Calendar,
        BottomNavItem.Store,
        BottomNavItem.Patrol,
        BottomNavItem.Settings
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val topLevelRoutes = bottomNavItems.map { it.route }
            val currentRoute = currentDestination?.route
            if (topLevelRoutes.any { currentRoute == it || currentRoute?.startsWith(it) == true }) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.WISHLIST,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Wishlist
            composable(Routes.WISHLIST) {
                val viewModel: WishlistViewModel = viewModel(
                    factory = WishlistViewModel.Factory(repository)
                )
                val uiState by viewModel.uiState.collectAsState()
                WishlistScreen(
                    uiState = uiState,
                    onSearchQueryChange = viewModel::setSearchQuery,
                    onSortOrderChange = viewModel::setSortOrder,
                    onItemClick = { item ->
                        navController.navigate("gunpla_form_edit/${item.id}")
                    },
                    onAddClick = { navController.navigate(Routes.GUNPLA_FORM_ADD) },
                    onDeleteItem = viewModel::deleteItem
                )
            }

            composable(Routes.GUNPLA_FORM_ADD) {
                val viewModel: WishlistViewModel = viewModel(
                    factory = WishlistViewModel.Factory(repository)
                )
                val coroutineScope = rememberCoroutineScope()
                GunplaFormScreen(
                    existingItem = null,
                    onSave = { item ->
                        coroutineScope.launch {
                            repository.insertItem(item)
                            navController.popBackStack()
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.GUNPLA_FORM_EDIT,
                arguments = listOf(navArgument("itemId") { type = NavType.StringType })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
                var item by remember { mutableStateOf<GunplaItemEntity?>(null) }
                val coroutineScope = rememberCoroutineScope()
                LaunchedEffect(itemId) {
                    item = repository.getItemById(itemId)
                }
                item?.let { existingItem ->
                    GunplaFormScreen(
                        existingItem = existingItem,
                        onSave = { updatedItem ->
                            coroutineScope.launch {
                                repository.updateItem(updatedItem)
                                navController.popBackStack()
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            // Calendar
            composable(Routes.CALENDAR) {
                val viewModel: CalendarViewModel = viewModel(
                    factory = CalendarViewModel.Factory(repository)
                )
                val uiState by viewModel.uiState.collectAsState()
                CalendarScreen(
                    uiState = uiState,
                    onPreviousMonth = viewModel::previousMonth,
                    onNextMonth = viewModel::nextMonth,
                    onDayClick = {}
                )
            }

            composable(Routes.STOCK_DIFF) {
                val calendarViewModel: CalendarViewModel = viewModel(
                    factory = CalendarViewModel.Factory(repository)
                )
                val uiState by calendarViewModel.uiState.collectAsState()
                val storeViewModel: StoreViewModel = viewModel(
                    factory = StoreViewModel.Factory(repository)
                )
                val storeUiState by storeViewModel.uiState.collectAsState()
                StockDiffScreen(
                    items = uiState.itemsWithRestock,
                    stores = storeUiState.stores,
                    onSave = { record ->
                        calendarViewModel.insertStockDelayRecord(record)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // Store
            composable(Routes.STORE) {
                val viewModel: StoreViewModel = viewModel(
                    factory = StoreViewModel.Factory(repository)
                )
                val uiState by viewModel.uiState.collectAsState()
                StoreScreen(
                    uiState = uiState,
                    onSearchQueryChange = viewModel::setSearchQuery,
                    onAddStore = { navController.navigate(Routes.STORE_LIST) },
                    onListClick = { navController.navigate(Routes.STORE_LIST) },
                    onMarkerClick = {}
                )
            }

            composable(Routes.STORE_LIST) {
                val viewModel: StoreViewModel = viewModel(
                    factory = StoreViewModel.Factory(repository)
                )
                val uiState by viewModel.uiState.collectAsState()
                StoreListScreen(
                    stores = uiState.stores,
                    onBack = { navController.popBackStack() },
                    onToggleFavorite = viewModel::toggleFavorite,
                    onDeleteStore = viewModel::deleteStore,
                    onAddStore = viewModel::insertStore
                )
            }

            // Patrol
            composable(Routes.PATROL) {
                val viewModel: PatrolViewModel = viewModel(
                    factory = PatrolViewModel.Factory(repository, context)
                )
                val uiState by viewModel.uiState.collectAsState()
                PatrolScreen(
                    uiState = uiState,
                    onAddClick = { navController.navigate(Routes.PATROL_FORM) },
                    onPlanClick = { plan -> navController.navigate("patrol_detail/${plan.id}") },
                    onDeletePlan = viewModel::deletePlan
                )
            }

            composable(Routes.PATROL_FORM) {
                val viewModel: PatrolViewModel = viewModel(
                    factory = PatrolViewModel.Factory(repository, context)
                )
                val uiState by viewModel.uiState.collectAsState()
                PatrolFormScreen(
                    stores = uiState.stores,
                    items = uiState.items,
                    onSave = { plan ->
                        viewModel.insertPlan(plan)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.PATROL_DETAIL,
                arguments = listOf(navArgument("planId") { type = NavType.StringType })
            ) { backStackEntry ->
                val planId = backStackEntry.arguments?.getString("planId") ?: return@composable
                val viewModel: PatrolViewModel = viewModel(
                    factory = PatrolViewModel.Factory(repository, context)
                )
                val uiState by viewModel.uiState.collectAsState()
                var plan by remember { mutableStateOf<com.example.gunplaradar.data.entity.PatrolPlanEntity?>(null) }
                val coroutineScope = rememberCoroutineScope()
                LaunchedEffect(planId) {
                    plan = viewModel.getPlanById(planId)
                }
                plan?.let { currentPlan ->
                    val store = uiState.stores.find { it.id == currentPlan.storeId }
                    val targetItemIds = currentPlan.targetItemIds.split(",").filter { it.isNotBlank() }
                    val targetItems = uiState.items.filter { it.id in targetItemIds }
                    PatrolDetailScreen(
                        plan = currentPlan,
                        store = store,
                        targetItems = targetItems,
                        onBack = { navController.popBackStack() },
                        onDelete = {
                            viewModel.deletePlan(currentPlan)
                            navController.popBackStack()
                        }
                    )
                }
            }

            // Settings
            composable(Routes.SETTINGS) {
                val viewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(repository, context)
                )
                val uiState by viewModel.uiState.collectAsState()
                SettingsScreen(
                    uiState = uiState,
                    onNotificationsToggle = viewModel::setNotificationsEnabled,
                    onDeleteAllData = {
                        viewModel.deleteAllData {}
                    }
                )
            }
        }
    }
}
