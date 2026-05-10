package com.hastakala.shop

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.activity.compose.BackHandler
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.hastakala.shop.data.Product
import com.hastakala.shop.data.UserProfile
import com.hastakala.shop.ui.DashboardScreen
import com.hastakala.shop.ui.LowStockScreen
import com.hastakala.shop.ui.ProductAddScreen
import com.hastakala.shop.ui.ProductListScreen
import com.hastakala.shop.ui.ProfitInsightsScreen
import com.hastakala.shop.ui.SalesAnalyticsScreen
import com.hastakala.shop.ui.SalesEntryScreen
import com.hastakala.shop.ui.SettingsScreen
import com.hastakala.shop.ui.SplashScreen
import com.hastakala.shop.ui.WelcomeScreen
import com.hastakala.shop.ui.theme.MyApplicationTheme
import com.hastakala.shop.ui.components.ExportBottomSheet
import com.hastakala.shop.utils.ExportType
import com.hastakala.shop.utils.ExportFormat
import com.hastakala.shop.utils.ExportManager
import com.hastakala.shop.utils.StockNotificationHelper
import com.hastakala.shop.viewmodel.ShopViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

import androidx.fragment.app.FragmentActivity
import com.hastakala.shop.utils.BiometricHelper

@AndroidEntryPoint
class MainActivity : androidx.appcompat.app.AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check for 30-day inactivity for anonymous users
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && user.isAnonymous) {
            val lastSignIn = user.metadata?.lastSignInTimestamp ?: 0L
            val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000
            if (System.currentTimeMillis() - lastSignIn > thirtyDaysInMillis) {
                user.delete().addOnCompleteListener {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                return
            }
        }

        setContent {
            val vm: ShopViewModel = hiltViewModel()
            val isDarkMode by vm.darkMode.collectAsState()
            val themeName by vm.themeColor.collectAsState()
            val fontSize by vm.fontSize.collectAsState()
            val currentUser = FirebaseAuth.getInstance().currentUser
            val isAnonymous = currentUser?.isAnonymous == true

            val language by vm.language.collectAsState()
            val appLockEnabled by vm.appLock.collectAsState()
            val isAppLocked by vm.isAppLocked.collectAsState()
            val unlockMethod by vm.unlockMethod.collectAsState()
            val appPin by vm.appPin.collectAsState()

            LaunchedEffect(language) {
                com.hastakala.shop.utils.LocaleHelper.setLocale(language)
            }

            LaunchedEffect(appLockEnabled) {
                if (appLockEnabled) {
                    vm.setAppLocked(true)
                }
            }

            val activity = this
            
            DisposableEffect(lifecycle) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                        if (appLockEnabled) {
                            vm.setAppLocked(true)
                        }
                    }
                }
                lifecycle.addObserver(observer)
                onDispose {
                    lifecycle.removeObserver(observer)
                }
            }

            if (appLockEnabled && isAppLocked) {
                var pinInput by remember { mutableStateOf("") }
                var showPinError by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Text(
                            text = if (unlockMethod == "PIN") "Enter PIN" else stringResource(R.string.auth_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = if (unlockMethod == "PIN") "Please enter your 4-digit PIN to continue" else stringResource(R.string.auth_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(48.dp))
                        
                        if (unlockMethod == "PIN") {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(bottom = 24.dp)
                            ) {
                                repeat(4) { index ->
                                    val isFilled = index < pinInput.length
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isFilled) MaterialTheme.colorScheme.primary 
                                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                            )
                                    )
                                }
                            }

                            if (showPinError) {
                                Text(
                                    "Invalid PIN. Please try again.",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // Simple numeric keypad
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val keys = listOf(
                                    listOf("1", "2", "3"),
                                    listOf("4", "5", "6"),
                                    listOf("7", "8", "9"),
                                    listOf("C", "0", "⌫")
                                )
                                keys.forEach { row ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        row.forEach { key ->
                                            Box(
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                                    .clickable {
                                                        when (key) {
                                                            "C" -> {
                                                                pinInput = ""
                                                                showPinError = false
                                                            }
                                                            "⌫" -> if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1)
                                                            else -> {
                                                                if (pinInput.length < 4) {
                                                                    pinInput += key
                                                                    if (pinInput.length == 4) {
                                                                        if (pinInput == appPin) {
                                                                            vm.setAppLocked(false)
                                                                            showPinError = false
                                                                        } else {
                                                                            showPinError = true
                                                                            pinInput = ""
                                                                        }
                                                                    } else {
                                                                        showPinError = false
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = key,
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    BiometricHelper.authenticate(
                                        activity = activity,
                                        title = activity.getString(R.string.auth_title),
                                        subtitle = activity.getString(R.string.auth_subtitle),
                                        onSuccess = { vm.setAppLocked(false) },
                                        onError = { /* Handle error */ }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Fingerprint, null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    stringResource(R.string.btn_unlock_app),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "v1.0.0 • Developed by Bdriii",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                    }
                }

                LaunchedEffect(Unit) {
                    if (unlockMethod == "Biometric") {
                        BiometricHelper.authenticate(
                            activity = activity,
                            title = activity.getString(R.string.auth_title),
                            subtitle = activity.getString(R.string.auth_subtitle),
                            onSuccess = { vm.setAppLocked(false) },
                            onError = { /* Handle error */ }
                        )
                    }
                }
            } else {
                key(language) {
                    MyApplicationTheme(
                        darkTheme = isDarkMode,
                        themeName = themeName,
                        fontSize = fontSize
                    ) {
            val initialScreen = if (FirebaseAuth.getInstance().currentUser != null) "main" else "splash"
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = initialScreen) {
                            composable("splash") {
                                SplashScreen(onTimeout = {
                                    val next = if (FirebaseAuth.getInstance().currentUser != null) "main" else "welcome"
                                    navController.navigate(next) {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                })
                            }
                            composable("welcome") {
                                WelcomeScreen(onGetStarted = {
                                    val intent = Intent(this@MainActivity, ArtisanAuthActivity::class.java)
                                    startActivity(intent)
                                })
                            }
                            composable("main") {
                                HastaKalaApp(
                                    vm = vm,
                                    isDarkMode = isDarkMode,
                                    onToggleDarkMode = {
                                        vm.updatePreference(com.hastakala.shop.data.PreferenceManager.DARK_MODE, !isDarkMode)
                                    },
                                    isAnonymous = isAnonymous,
                                    onLogout = {
                                        vm.logout {
                                            logout()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HastaKalaApp(
    vm: ShopViewModel,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    isAnonymous: Boolean,
    onLogout: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val notificationHelper = remember { StockNotificationHelper(context) }
    val alreadyNotified = remember { mutableStateListOf<Int>() }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )

    val products by vm.products.collectAsState()
    val revenue by vm.revenue.collectAsState()
    val profit by vm.profit.collectAsState()

    val onLogoutClick = onLogout

    val filter by vm.selectedFilter.collectAsState()
    val selectedCategory by vm.selectedCategory.collectAsState()
    val topProducts by vm.topProducts.collectAsState()
    val colorBreakdown by vm.colorBreakdown.collectAsState()
    val categoryBreakdown by vm.categoryBreakdown.collectAsState()
    val slowMovingProducts by vm.slowMovingProducts.collectAsState()
    val isRefreshing by vm.isRefreshing.collectAsState()

    LaunchedEffect(Unit) {
        notificationHelper.ensureChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(products) {
        products.forEach { product ->
            val isLow = product.stock <= product.lowStockThreshold
            if (isLow && !alreadyNotified.contains(product.id)) {
                notificationHelper.showLowStockNotification(product.name, product.color, product.stock)
                alreadyNotified.add(product.id)
            } else if (!isLow) {
                alreadyNotified.remove(product.id)
            }
        }
    }

    LaunchedEffect(Unit) {
        vm.exportMessage.collectLatest { path ->
            Toast.makeText(context, context.getString(R.string.csv_exported, path), Toast.LENGTH_LONG).show()
        }
    }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var isAddingProduct by rememberSaveable { mutableStateOf(false) }
    var isShowingProfile by rememberSaveable { mutableStateOf(false) }
    var isShowingSettings by rememberSaveable { mutableStateOf(false) }
    var isShowingLowStock by rememberSaveable { mutableStateOf(false) }
    var isShowingSalesAnalytics by rememberSaveable { mutableStateOf(false) }
    var isShowingProfitInsights by rememberSaveable { mutableStateOf(false) }
    var preselectedCategoryForAdd by rememberSaveable { mutableStateOf<String?>(null) }

    // Back handlers for navigation state
    if (isShowingSettings) {
        BackHandler { isShowingSettings = false }
    } else if (isShowingLowStock) {
        BackHandler { isShowingLowStock = false }
    } else if (isShowingSalesAnalytics) {
        BackHandler { isShowingSalesAnalytics = false }
    } else if (isShowingProfitInsights) {
        BackHandler { isShowingProfitInsights = false }
    } else if (isShowingProfile) {
        BackHandler { isShowingProfile = false }
    } else if (isAddingProduct) {
        BackHandler { 
            isAddingProduct = false
            preselectedCategoryForAdd = null
        }
    } else if (selectedTab != 0) {
        BackHandler { selectedTab = 0 }
    }

    val navItems = listOf(
        Triple(stringResource(R.string.nav_home), Icons.Default.Home, "Dashboard"),
        Triple(stringResource(R.string.nav_products), Icons.Default.Inventory, "Inventory"),
        Triple(stringResource(R.string.nav_add_product), Icons.Default.AddBox, "Add New Product"),
        Triple(stringResource(R.string.nav_sales), Icons.Default.AddShoppingCart, "Add Sale"),
        Triple(stringResource(R.string.nav_profile), Icons.Default.Person, "User Profile")
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.menu_title),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                HorizontalDivider()
                navItems.forEachIndexed { idx, (label, icon, _) ->
                    val isSelected = when (idx) {
                        0 -> selectedTab == 0 && !isAddingProduct && !isShowingProfile && !isShowingSettings
                        1 -> selectedTab == 1 && !isAddingProduct && !isShowingProfile && !isShowingSettings
                        2 -> isAddingProduct
                        3 -> selectedTab == 2 && !isAddingProduct && !isShowingProfile && !isShowingSettings
                        4 -> isShowingProfile
                        else -> false
                    }
                    NavigationDrawerItem(
                        label = { Text(label) },
                        selected = isSelected,
                        onClick = {
                            scope.launch { drawerState.close() }
                            isShowingProfile = false
                            isShowingSettings = false
                            isAddingProduct = false
                            when (idx) {
                                0 -> selectedTab = 0
                                1 -> selectedTab = 1
                                2 -> {
                                    selectedTab = 1
                                    isAddingProduct = true
                                }
                                3 -> selectedTab = 2
                                4 -> isShowingProfile = true
                            }
                        },
                        icon = { Icon(icon, contentDescription = null) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Credits for bdriii
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.credit_crafted_by),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.logout), color = MaterialTheme.colorScheme.error) },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onLogoutClick()
                        }
                    },
                    icon = { Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                val title = when {
                    isShowingSettings -> stringResource(R.string.title_settings)
                    isShowingLowStock -> stringResource(R.string.title_low_stock)
                    isShowingSalesAnalytics -> stringResource(R.string.title_sales_analytics)
                    isShowingProfitInsights -> stringResource(R.string.title_profit_insights)
                    isShowingProfile -> stringResource(R.string.nav_profile)
                    isAddingProduct -> stringResource(R.string.title_add_product)
                    selectedTab == 1 -> stringResource(R.string.nav_products)
                    selectedTab == 2 -> stringResource(R.string.nav_sales)
                    else -> stringResource(R.string.app_name).uppercase()
                }

                val showBackArrow = isShowingSettings || isShowingLowStock || isShowingSalesAnalytics || 
                                 isShowingProfitInsights || isShowingProfile || isAddingProduct

                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = if (title == stringResource(R.string.app_name).uppercase()) 2.sp else 0.sp
                        )
                    },
                    navigationIcon = {
                        if (showBackArrow) {
                            IconButton(onClick = {
                                when {
                                    isShowingSettings -> isShowingSettings = false
                                    isShowingLowStock -> isShowingLowStock = false
                                    isShowingSalesAnalytics -> isShowingSalesAnalytics = false
                                    isShowingProfitInsights -> isShowingProfitInsights = false
                                    isShowingProfile -> isShowingProfile = false
                                    isAddingProduct -> {
                                        isAddingProduct = false
                                        preselectedCategoryForAdd = null
                                    }
                                }
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = onToggleDarkMode) {
                            Icon(
                                if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Dark Mode"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            floatingActionButton = {
                if (selectedTab == 1 && !isAddingProduct && !isShowingProfile && !isShowingSettings) {
                    FloatingActionButton(
                        onClick = { isAddingProduct = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Product")
                    }
                }
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = MaterialTheme.colorScheme.background
            ) {
                if (isShowingSettings) {
                    SettingsScreen(
                        viewModel = vm,
                        onNavigateBack = { isShowingSettings = false }
                    )
                } else if (isShowingLowStock) {
                    LowStockScreen(
                        products = products,
                        onNavigateBack = { isShowingLowStock = false },
                        onEditProduct = { product ->
                            isShowingLowStock = false
                            selectedTab = 1
                            // Note: In a real app we'd trigger the edit dialog here.
                        }
                    )
                } else if (isShowingSalesAnalytics) {
                    SalesAnalyticsScreen(
                        sales = vm.sales.collectAsState(initial = emptyList()).value,
                        onNavigateBack = { isShowingSalesAnalytics = false }
                    )
                } else if (isShowingProfitInsights) {
                    ProfitInsightsScreen(
                        products = products,
                        sales = vm.sales.collectAsState(initial = emptyList()).value,
                        totalProfit = profit,
                        onNavigateBack = { isShowingProfitInsights = false }
                    )
                } else if (isShowingProfile) {
                    ProfileScreen(
                        viewModel = vm,
                        isAnonymous = isAnonymous,
                        onLogout = onLogout,
                        onNavigateToSettings = { isShowingSettings = true }
                    )
                } else {
                    when (selectedTab) {
                        0 -> {
                            val lowStockCount = products.count { it.stock <= it.lowStockThreshold }
                            DashboardScreen(
                                totalSales = revenue,
                                totalProducts = products.size,
                                lowStockItems = lowStockCount,
                                profit = profit,
                                topProducts = topProducts,
                                categoryBreakdown = categoryBreakdown,
                                colorBreakdown = colorBreakdown,
                                slowMovingProducts = slowMovingProducts,
                                filter = filter,
                                selectedCategory = selectedCategory,
                                isDarkMode = isDarkMode,
                                isRefreshing = isRefreshing,
                                onFilterChange = vm::setFilter,
                                onCategoryChange = vm::setCategoryFilter,
                                onSalesClick = { isShowingSalesAnalytics = true },
                                onProfitClick = { isShowingProfitInsights = true },
                                onTotalProductsClick = { selectedTab = 1 },
                                onLowStockClick = { isShowingLowStock = true },
                                onRefresh = vm::refreshData,
                                onExportCsv = {
                                    val csv = vm.createCsv()
                                    val path = com.hastakala.shop.utils.CsvExporter.export(context, csv)
                                    // Share Intent
                                    val file = java.io.File(path)
                                    val uri = androidx.core.content.FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/csv"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Sales Log"))
                                    vm.notifyExported(path)
                                }
                            )
                        }
                        1 -> {
                            if (isAddingProduct) {
                                ProductAddScreen(
                                    initialCategoryName = preselectedCategoryForAdd,
                                    onAdd = { name, category, color, cost, sell, qty, threshold ->
                                        vm.addProduct(name, category, color, cost, sell, qty, threshold)
                                        isAddingProduct = false
                                        preselectedCategoryForAdd = null
                                    },
                                    onCancel = { 
                                        isAddingProduct = false
                                        preselectedCategoryForAdd = null
                                    }
                                )
                            } else {
                                ProductListScreen(
                                    products = products,
                                    selectedCategory = selectedCategory,
                                    onCategoryFilterChange = vm::setCategoryFilter,
                                    onDelete = vm::deleteProduct,
                                    onUpdate = vm::updateProductDetails,
                                    onAddProductInCategory = { category ->
                                        preselectedCategoryForAdd = category
                                        isAddingProduct = true
                                    }
                                )
                            }
                        }
                        2 -> {
                            SalesEntryScreen(
                                products = products,
                                onBill = vm::recordSale,
                                vm = vm
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    viewModel: ShopViewModel,
    isAnonymous: Boolean,
    onLogout: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val userProfile by viewModel.userProfile.collectAsState()
    val topProducts by viewModel.topProducts.collectAsState()
    val revenue by viewModel.revenue.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val filteredSales by viewModel.filteredSales.collectAsState()

    val products by viewModel.products.collectAsState()

    var showExportSheet by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showEditProfile by remember { mutableStateOf(false) }
    var showHelpSupport by remember { mutableStateOf(false) }
    var showAboutApp by remember { mutableStateOf(false) }
    var helpScreenType by remember { mutableIntStateOf(0) } // 0: Menu, 1: Email, 2: Guide, 3: Report

    val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
    val monthlySales = sales.filter {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = it.timestamp
        cal.get(java.util.Calendar.MONTH) == currentMonth
    }

    val bestSelling = topProducts.firstOrNull()?.productName ?: "N/A"

    if (showExportSheet) {
        ExportBottomSheet(
            onDismiss = { showExportSheet = false },
            onExportRequested = { type, format, start, end ->
                scope.launch {
                    val path = ExportManager.generateExport(
                        context, 
                        type, 
                        format, 
                        sales, 
                        products, 
                        start, 
                        end,
                        userProfile
                    )
                    if (path != null) {
                        ExportManager.shareFile(context, path)
                    } else {
                        Toast.makeText(context, context.getString(R.string.export_no_data), Toast.LENGTH_SHORT).show()
                    }
                    showExportSheet = false
                }
            }
        )
    }

    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text(stringResource(R.string.title_backup_restore)) },
            text = {
                Column {
                    Text(stringResource(R.string.label_last_backup, com.hastakala.shop.utils.BackupManager.getLastBackupTime(context)))
                    Text(stringResource(R.string.label_sync_status, stringResource(R.string.sync_ready)))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.backup_confirm))
                }
            },
            confirmButton = {
                Button(onClick = {
                    val success = com.hastakala.shop.utils.BackupManager.backupDatabase(context)
                    Toast.makeText(context, if (success) R.string.backup_success else R.string.backup_failed, Toast.LENGTH_SHORT).show()
                    showBackupDialog = false
                }) { Text(stringResource(R.string.btn_backup_now)) }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) { Text(stringResource(R.string.btn_cancel)) }
            }
        )
    }

    if (showEditProfile) {
        var name by remember { mutableStateOf(userProfile?.fullName ?: "") }
        var shopName by remember { mutableStateOf(userProfile?.shopName ?: "") }
        var contact by remember { mutableStateOf(userProfile?.contact ?: "") }
        var location by remember { mutableStateOf(userProfile?.location ?: "") }

        AlertDialog(
            onDismissRequest = { showEditProfile = false },
            title = { Text(stringResource(R.string.title_edit_profile_screen)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.hint_artisan_name)) }, shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = shopName, onValueChange = { shopName = it }, label = { Text(stringResource(R.string.hint_shop_name)) }, shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text(stringResource(R.string.hint_phone)) }, shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text(stringResource(R.string.hint_location)) }, shape = RoundedCornerShape(12.dp))
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank() && shopName.isNotBlank()) {
                        viewModel.updateUserProfile(name, shopName, contact, location)
                        Toast.makeText(context, R.string.profile_updated, Toast.LENGTH_SHORT).show()
                        showEditProfile = false
                    }
                }) { Text(stringResource(R.string.btn_save_changes)) }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfile = false }) { Text(stringResource(R.string.btn_cancel)) }
            }
        )
    }

    if (showHelpSupport) {
        AlertDialog(
            onDismissRequest = { 
                showHelpSupport = false 
                helpScreenType = 0
            },
            title = { 
                Text(
                    when(helpScreenType) {
                        1 -> stringResource(R.string.support_email)
                        2 -> stringResource(R.string.app_guide)
                        3 -> stringResource(R.string.report_problem)
                        else -> stringResource(R.string.title_help_support)
                    }
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    when (helpScreenType) {
                        0 -> {
                            HelpItem(Icons.Default.Email, stringResource(R.string.support_email)) { helpScreenType = 1 }
                            HelpItem(Icons.AutoMirrored.Filled.MenuBook, stringResource(R.string.app_guide)) { helpScreenType = 2 }
                            HelpItem(Icons.Default.BugReport, stringResource(R.string.report_problem)) { helpScreenType = 3 }
                        }
                        1 -> {
                            Text(stringResource(R.string.support_email_desc))
                        }
                        2 -> {
                            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                                item { Text(stringResource(R.string.guide_content)) }
                            }
                        }
                        3 -> {
                            var title by remember { mutableStateOf("") }
                            var description by remember { mutableStateOf("") }
                            
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(stringResource(R.string.report_problem_desc))
                                OutlinedTextField(
                                    value = title,
                                    onValueChange = { title = it },
                                    label = { Text(stringResource(R.string.report_title_hint)) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = description,
                                    onValueChange = { description = it },
                                    label = { Text(stringResource(R.string.report_desc_hint)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3
                                )
                                TextButton(onClick = { /* Screenshot logic */ }) {
                                    Icon(Icons.Default.AttachFile, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.report_attach_screenshot))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (helpScreenType == 0) {
                    TextButton(onClick = { showHelpSupport = false }) { Text(stringResource(R.string.btn_close)) }
                } else {
                    Button(onClick = {
                        if (helpScreenType == 1 || helpScreenType == 3) {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = android.net.Uri.parse("mailto:")
                                putExtra(Intent.EXTRA_EMAIL, arrayOf("contactbdriii@gmail.com"))
                                if (helpScreenType == 3) {
                                    putExtra(Intent.EXTRA_SUBJECT, "App Report: Hasta-Kala Shop")
                                } else {
                                    putExtra(Intent.EXTRA_SUBJECT, "Support Request: Hasta-Kala Shop")
                                }
                            }
                            try {
                                context.startActivity(Intent.createChooser(intent, "Send Email"))
                                if (helpScreenType == 3) {
                                    Toast.makeText(context, R.string.report_success, Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                            }
                        }
                        helpScreenType = 0
                    }) { 
                        Text(if (helpScreenType == 3) stringResource(R.string.btn_send_report) else if (helpScreenType == 1) "Open Email" else "Back") 
                    }
                }
            },
            dismissButton = {
                if (helpScreenType != 0) {
                    TextButton(onClick = { helpScreenType = 0 }) { Text("Back") }
                }
            }
        )
    }

    if (showAboutApp) {
        AlertDialog(
            onDismissRequest = { showAboutApp = false },
            title = { Text(stringResource(R.string.about_title)) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.about_app_content), textAlign = TextAlign.Center)
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.developer_label), fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.developer_name))
                    }
                    
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.purpose_label), fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.purpose_content), style = MaterialTheme.typography.bodySmall)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.credit_powered_by_full), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutApp = false }) { Text(stringResource(R.string.btn_close)) }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Identity Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userProfile?.fullName ?: (if (isAnonymous) stringResource(R.string.guest_profile) else "Artisan"),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.label_artisan),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        Text(
                            text = userProfile?.shopName ?: "Hasta-Kala Artisan Shop",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = userProfile?.contact ?: (FirebaseAuth.getInstance().currentUser?.email ?: ""),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (userProfile?.location?.isNotEmpty() == true) {
                            Text(
                                text = userProfile?.location!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Business Summary
        item {
            Text(
                text = stringResource(R.string.label_business_summary),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = stringResource(R.string.stat_total_sales_month),
                    value = "Rs. ${monthlySales.sumOf { it.subtotal }.toInt()}",
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = stringResource(R.string.stat_best_selling),
                    value = bestSelling,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = stringResource(R.string.stat_total_bills),
                    value = sales.size.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Actions
        item {
            ProfileSectionHeader(stringResource(R.string.section_quick_actions))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileActionButton(Icons.Default.FileDownload, stringResource(R.string.action_export_data), onClick = { showExportSheet = true })
                ProfileActionButton(Icons.Default.Sync, stringResource(R.string.action_backup_sync), onClick = { showBackupDialog = true })
                ProfileActionButton(Icons.Default.Share, stringResource(R.string.action_share_app), onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_app_text))
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                })
                ProfileActionButton(Icons.Default.Edit, stringResource(R.string.action_edit_profile), onClick = { showEditProfile = true })
            }
        }

        // Support & App
        item {
            ProfileSectionHeader(stringResource(R.string.section_support))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileActionButton(Icons.Default.HelpOutline, stringResource(R.string.action_help_support), onClick = { showHelpSupport = true })
                ProfileActionButton(Icons.Default.Info, stringResource(R.string.action_about_app), onClick = { showAboutApp = true })
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.label_app_version),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "1.0.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // Account
        item {
            ProfileSectionHeader(stringResource(R.string.section_account))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileActionButton(Icons.Default.Settings, stringResource(R.string.action_settings), onClick = onNavigateToSettings)
                ProfileActionButton(
                    Icons.Default.Logout, 
                    stringResource(R.string.logout), 
                    tint = MaterialTheme.colorScheme.error,
                    onClick = onLogout
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.credit_powered_by_full),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun HelpItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ProfileSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun ProfileActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = tint)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = tint)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
        }
    }
}
