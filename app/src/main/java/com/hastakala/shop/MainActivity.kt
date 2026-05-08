package com.hastakala.shop

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import com.hastakala.shop.ui.DashboardScreen
import com.hastakala.shop.ui.ProductAddScreen
import com.hastakala.shop.ui.ProductListScreen
import com.hastakala.shop.ui.SalesEntryScreen
import com.hastakala.shop.ui.SplashScreen
import com.hastakala.shop.ui.WelcomeScreen
import com.hastakala.shop.ui.theme.MyApplicationTheme
import com.hastakala.shop.utils.StockNotificationHelper
import com.hastakala.shop.viewmodel.ShopViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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
            val currentUser = FirebaseAuth.getInstance().currentUser
            val isAnonymous = currentUser?.isAnonymous == true

            var isDarkMode by rememberSaveable { mutableStateOf(false) }
            MyApplicationTheme(darkTheme = isDarkMode) {
                val vm: ShopViewModel = hiltViewModel()
                HastaKalaApp(
                    vm = vm,
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { isDarkMode = !isDarkMode },
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

    private fun logout() {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut()
        
        // Sign out from Google
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
    var preselectedCategoryForAdd by rememberSaveable { mutableStateOf<String?>(null) }
    
    var currentScreen by rememberSaveable { 
        val initialScreen = if (FirebaseAuth.getInstance().currentUser != null) "main" else "splash"
        mutableStateOf(initialScreen) 
    }

    if (currentScreen == "splash") {
        SplashScreen(onTimeout = { 
            val nextScreen = if (FirebaseAuth.getInstance().currentUser != null) "main" else "welcome"
            currentScreen = nextScreen 
        })
        return
    }

    if (currentScreen == "welcome") {
        WelcomeScreen(onGetStarted = { currentScreen = "main" })
        return
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
                        0 -> selectedTab == 0 && !isAddingProduct && !isShowingProfile
                        1 -> selectedTab == 1 && !isAddingProduct && !isShowingProfile
                        2 -> isAddingProduct
                        3 -> selectedTab == 2 && !isAddingProduct && !isShowingProfile
                        4 -> isShowingProfile
                        else -> false
                    }
                    NavigationDrawerItem(
                        label = { Text(label) },
                        selected = isSelected,
                        onClick = {
                            scope.launch { drawerState.close() }
                            isShowingProfile = false
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
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.app_name).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
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
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            floatingActionButton = {
                if (selectedTab == 1 && !isAddingProduct && !isShowingProfile) {
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
                if (isShowingProfile) {
                    ProfileScreen(isAnonymous = isAnonymous, onLogout = onLogout)
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
                                filter = filter,
                                selectedCategory = selectedCategory,
                                isDarkMode = isDarkMode,
                                isRefreshing = isRefreshing,
                                onFilterChange = vm::setFilter,
                                onCategoryChange = vm::setCategoryFilter,
                                onRefresh = vm::refreshData,
                                onExportCsv = {
                                    val csv = vm.createCsv()
                                    val path = com.hastakala.shop.utils.CsvExporter.export(context, csv)
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
                                onBill = vm::recordSale
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(isAnonymous: Boolean, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        val currentUser = FirebaseAuth.getInstance().currentUser
        Text(
            text = if (isAnonymous) stringResource(R.string.guest_profile) else (currentUser?.email ?: stringResource(R.string.user_profile)),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (isAnonymous) {
            Text(
                text = stringResource(R.string.guest_hint),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        } else {
            Text(
                text = stringResource(R.string.welcome_back),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.logout))
        }
    }
}
