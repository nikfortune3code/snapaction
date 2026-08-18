package com.snapaction.ui.screens

import android.Manifest
 import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.snapaction.data.model.IntentCategory
import com.snapaction.data.model.SnapActionCard
import com.snapaction.ui.FeedTab
import com.snapaction.ui.SnapViewModel
import com.snapaction.ui.components.ActionCardItem
import com.snapaction.ui.components.EditActionSheet
import com.snapaction.ui.components.UploadHub
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: SnapViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var isSearchExpanded by remember { mutableStateOf(false) }
    var showOfflineExpenseDialog by remember { mutableStateOf(false) }
    var selectedMonthFilter by remember { mutableStateOf("All Months") }
    var showSmsPermissionDialog by remember { mutableStateOf(false) }
    var activeExpenseTab by remember { mutableStateOf("Debited") }

    // ── Cart: camera capture URI holder ──────────────────────────────────────
    var cartCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Cart gallery picker — picks an image, copies it to a temp file, sends to backend
    val cartGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val tmpFile = uriToTempFile(context, it) ?: return@let
            viewModel.analyzeCartImage(tmpFile, it.toString())
        }
    }

    // Cart camera capture launcher
    val cartCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cartCameraUri?.let { uri ->
                val tmpFile = uriToTempFile(context, uri) ?: return@let
                viewModel.analyzeCartImage(tmpFile, uri.toString())
            }
        }
    }

    // Helper to create a fresh camera URI
    fun createCartCameraUri(): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "cart_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    // Runtime SMS Permission launcher
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.scanDeviceSmsMessages(context)
        } else {
            showSmsPermissionDialog = true
        }
    }

    // Helper to trigger SMS scan (checks permission first)
    val onScanMessages = {
        val hasPerm = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPerm) {
            viewModel.scanDeviceSmsMessages(context)
        } else {
            smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchExpanded) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Search all tabs...", fontSize = 14.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "SnapAction Logo",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SnapAction",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                },
                actions = {
                    // Global Search Button
                    IconButton(onClick = { 
                        isSearchExpanded = !isSearchExpanded
                        if (!isSearchExpanded) viewModel.updateSearchQuery("")
                    }) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search All Tabs",
                            tint = if (isSearchExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { viewModel.toggleDarkMode() }) {
                        Icon(
                            imageVector = if (uiState.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = uiState.selectedTab == FeedTab.GROCERIES,
                    onClick = { viewModel.selectTab(FeedTab.GROCERIES) },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart") },
                    label = { Text("Cart") }
                )
                NavigationBarItem(
                    selected = uiState.selectedTab == FeedTab.EXPENSES,
                    onClick = { viewModel.selectTab(FeedTab.EXPENSES) },
                    icon = { Icon(Icons.Default.Receipt, contentDescription = "Expenses") },
                    label = { Text("Expenses") }
                )
                NavigationBarItem(
                    selected = uiState.selectedTab == FeedTab.REMINDERS,
                    onClick = { viewModel.selectTab(FeedTab.REMINDERS) },
                    icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Reminders") },
                    label = { Text("Reminders") }
                )
                NavigationBarItem(
                    selected = uiState.selectedTab == FeedTab.BOOKMARKS,
                    onClick = { viewModel.selectTab(FeedTab.BOOKMARKS) },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = "Bookmark") },
                    label = { Text("Bookmark") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.selectedTab != FeedTab.EXPENSES) {
                UploadHub(
                    processingState = uiState.processingState,
                    onPickImage = { uri -> viewModel.uploadScreenshot(uri, context) }
                )
            }

            // Centered "Add Event" Button in Reminders Tab
            if (uiState.selectedTab == FeedTab.REMINDERS) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { viewModel.openAddEventModal() },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Event", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            } else if (uiState.selectedTab == FeedTab.EXPENSES) {
                // Expenses Tab: Scan Messages + Manual SMS entry
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Primary: Scan native SMS button
                    Button(
                        onClick = { onScanMessages() },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isSmsScanning
                    ) {
                        if (uiState.isSmsScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scanning Messages...", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scan Messages", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    // Secondary: Enter Offline Expense button
                    OutlinedButton(
                        onClick = { showOfflineExpenseDialog = true },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enter offline Expenses", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    // Show count badge after scanning
                    if (uiState.smsScannedCount > 0) {
                        LaunchedEffect(uiState.smsScannedCount) {
                            kotlinx.coroutines.delay(4000)
                            viewModel.clearSmsScannedCount()
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(0.85f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Found ${uiState.smsScannedCount} new transaction${if (uiState.smsScannedCount != 1) "s" else ""} from Messages",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            } else if (uiState.selectedTab == FeedTab.GROCERIES) {
                // Cart Tab: AI Image Upload buttons + loading indicator
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.isCartAnalyzing) {
                        // ── Loading state ──
                        Card(
                            modifier = Modifier.fillMaxWidth(0.85f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Analysing image — detecting product & brand...",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    } else {
                        // ── Primary: Take Photo ──
                        Button(
                            onClick = {
                                val uri = createCartCameraUri()
                                cartCameraUri = uri
                                uri?.let { cartCameraLauncher.launch(it) }
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Take Photo", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        // ── Secondary: Choose from Gallery ──
                        OutlinedButton(
                            onClick = { cartGalleryLauncher.launch("image/*") },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Choose from Gallery", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Error toast card
                    uiState.cartAnalysisError?.let { err ->
                        LaunchedEffect(err) {
                            kotlinx.coroutines.delay(4000)
                            viewModel.clearCartAnalysisError()
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(0.85f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "⚠ $err",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            // Permission Denied Explanation Dialog
            if (showSmsPermissionDialog) {
                AlertDialog(
                    onDismissRequest = { showSmsPermissionDialog = false },
                    icon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    title = { Text("SMS Permission Required") },
                    text = {
                        Text(
                            "SnapAction needs access to your Messages to automatically detect bank " +
                            "and UPI transaction SMS messages.\n\n" +
                            "Go to Settings → Apps → SnapAction → Permissions → SMS and enable it."
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showSmsPermissionDialog = false
                            // Open app permission settings
                            val intent = android.content.Intent(
                                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                android.net.Uri.fromParts("package", context.packageName, null)
                            )
                            context.startActivity(intent)
                        }) {
                            Text("Open Settings")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSmsPermissionDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Filtering Logic per Tab (Global Search searches all fields when active)
            val filteredCards = remember(uiState.cards, uiState.selectedTab, uiState.searchQuery) {
                uiState.cards.filter { card ->
                    val matchesTab = when (uiState.selectedTab) {
                        FeedTab.GROCERIES -> card.category == IntentCategory.GROCERY
                        FeedTab.EXPENSES -> card.category == IntentCategory.EXPENSE
                        FeedTab.REMINDERS -> card.category == IntentCategory.EVENT
                        FeedTab.BOOKMARKS -> card.category == IntentCategory.BOOKMARK
                    }
                    val matchesSearch = if (uiState.searchQuery.isBlank()) true else {
                        val title = card.event?.title ?: card.grocery?.dishName ?: card.expense?.vendor ?: card.bookmark?.headline ?: ""
                        val details = card.event?.details ?: card.expense?.category ?: card.bookmark?.summary ?: ""
                        title.contains(uiState.searchQuery, ignoreCase = true) || details.contains(uiState.searchQuery, ignoreCase = true)
                    }
                    matchesTab && matchesSearch
                }
            }

            // EXPENSES TAB: Monthly Categorization & Spend Analysis
            if (uiState.selectedTab == FeedTab.EXPENSES && filteredCards.isNotEmpty()) {
                val expenseCards = filteredCards.filter { it.expense != null }
                val availableMonths = remember(expenseCards) {
                    listOf("All Months") + expenseCards.map { it.getMonthYearString() }.distinct()
                }

                // Selected Month Filtered List
                val monthFilteredExpenses = remember(expenseCards, selectedMonthFilter) {
                    if (selectedMonthFilter == "All Months") expenseCards else expenseCards.filter { it.getMonthYearString() == selectedMonthFilter }
                }

                // Separate Debited (spent) vs Credited (refunds) vs Credit Card Payments (Cred club, Bureaus, etc.)
                val spentExpenses = remember(monthFilteredExpenses) {
                    monthFilteredExpenses.filter { card ->
                        val exp = card.expense
                        if (exp == null) return@filter false
                        val isCredit = exp.category == "Credit / Refund" || exp.vendor.contains("Credited", ignoreCase = true)
                        val payee = exp.vendor.lowercase()
                        val sms = exp.rawSmsText?.lowercase() ?: ""
                        val isCC = listOf("cred", "bureau", "credit card", "cc payment", "sbi card", "onecard", "slice").any { payee.contains(it) || sms.contains(it) }
                        !isCredit && !isCC
                    }
                }
                val creditedExpenses = remember(monthFilteredExpenses) {
                    monthFilteredExpenses.filter { card ->
                        val exp = card.expense
                        if (exp == null) return@filter false
                        val isCredit = exp.category == "Credit / Refund" || exp.vendor.contains("Credited", ignoreCase = true)
                        isCredit
                    }
                }
                val ccPayments = remember(monthFilteredExpenses) {
                    monthFilteredExpenses.filter { card ->
                        val exp = card.expense
                        if (exp == null) return@filter false
                        val isCredit = exp.category == "Credit / Refund" || exp.vendor.contains("Credited", ignoreCase = true)
                        val payee = exp.vendor.lowercase()
                        val sms = exp.rawSmsText?.lowercase() ?: ""
                        val isCC = listOf("cred", "bureau", "credit card", "cc payment", "sbi card", "onecard", "slice").any { payee.contains(it) || sms.contains(it) }
                        !isCredit && isCC
                    }
                }

                val totalSpend = spentExpenses.sumOf { it.expense?.totalAmount ?: 0.0 }
                val paidSpend = spentExpenses.filter { it.expense?.isPaid == true }.sumOf { it.expense?.totalAmount ?: 0.0 }
                val pendingSpend = spentExpenses.filter { it.expense?.isPaid == false }.sumOf { it.expense?.totalAmount ?: 0.0 }

                // Monthly Summary Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📊 Monthly Spend Analysis",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${spentExpenses.size} Expenses",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Spend", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = String.format(Locale.US, "₹%.2f", totalSpend),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column {
                                Text("Paid Amount", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = String.format(Locale.US, "₹%.2f", paidSpend),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Column {
                                Text("Pending Bills", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = String.format(Locale.US, "₹%.2f", pendingSpend),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                // Month Selector Filter Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableMonths) { month ->
                        FilterChip(
                            selected = selectedMonthFilter == month,
                            onClick = { selectedMonthFilter = month },
                            label = { Text(month, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = if (selectedMonthFilter == month) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                // Debited / Credited / Credit Card Payment Tabs just below months chips
                TabRow(
                    selectedTabIndex = when (activeExpenseTab) {
                        "Debited" -> 0
                        "Credited" -> 1
                        else -> 2
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = activeExpenseTab == "Debited",
                        onClick = { activeExpenseTab = "Debited" },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (activeExpenseTab == "Debited") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Debited (${spentExpenses.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    )
                    Tab(
                        selected = activeExpenseTab == "Credited",
                        onClick = { activeExpenseTab = "Credited" },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (activeExpenseTab == "Credited") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Credited (${creditedExpenses.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    )
                    Tab(
                        selected = activeExpenseTab == "Credit Card Payment",
                        onClick = { activeExpenseTab = "Credit Card Payment" },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (activeExpenseTab == "Credit Card Payment") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("CC Pay (${ccPayments.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Group Expenses by Month
                val displayExpenses = when (activeExpenseTab) {
                    "Debited" -> spentExpenses
                    "Credited" -> creditedExpenses
                    else -> ccPayments
                }

                if (displayExpenses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No ${activeExpenseTab.lowercase()} transactions found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    val groupedByMonth = displayExpenses.groupBy { it.getMonthYearString() }

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        groupedByMonth.forEach { (monthName, cardsInMonth) ->
                            val monthTotal = cardsInMonth.sumOf { it.expense?.totalAmount ?: 0.0 }
                            item {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "📅 $monthName",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = String.format(Locale.US, "Total: ₹%.2f", monthTotal),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            items(cardsInMonth, key = { it.id }) { card ->
                                ActionCardItem(
                                    card = card,
                                    onToggleGrocery = { cardId, itemId -> viewModel.toggleGroceryItem(cardId, itemId) },
                                    onTogglePaid = { cardId -> viewModel.toggleExpensePaid(cardId) },
                                    onEditCard = { editCard -> viewModel.openEditCard(editCard) },
                                    onDeleteCard = { delId -> viewModel.deleteCard(delId) }
                                )
                            }
                        }
                    }
                }
            } else if (uiState.selectedTab == FeedTab.GROCERIES && uiState.cartItems.isNotEmpty()) {
                // Cart tab: show saved cart items
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.cartItems, key = { it.id }) { cartItem ->
                        CartItemCard(item = cartItem)
                    }
                }
            } else if (filteredCards.isEmpty() && uiState.selectedTab != FeedTab.GROCERIES) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (uiState.selectedTab == FeedTab.REMINDERS) "No Reminders or Events yet" else "No items in this tab",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredCards, key = { it.id }) { card ->
                        ActionCardItem(
                            card = card,
                            onToggleGrocery = { cardId, itemId -> viewModel.toggleGroceryItem(cardId, itemId) },
                            onTogglePaid = { cardId -> viewModel.toggleExpensePaid(cardId) },
                            onEditCard = { editCard -> viewModel.openEditCard(editCard) },
                            onDeleteCard = { delId -> viewModel.deleteCard(delId) }
                        )
                    }
                }
            }
        }
    }

    // Modal Edit Sheet
    uiState.activeEditCard?.let { activeCard ->
        EditActionSheet(
            card = activeCard,
            onDismiss = { viewModel.closeEditCard() },
            onSave = { updated -> viewModel.saveEditedCard(updated) }
        )
    }

    // ── Cart Analysis Result Dialog ──────────────────────────────────────────
    uiState.pendingCartAnalysis?.let { analysis ->
        CartItemFormDialog(
            productName  = analysis.productName,
            brandName    = analysis.brandName,
            onDismiss    = { viewModel.dismissCartAnalysis() },
            onSave       = { product, brand, qty ->
                viewModel.saveCartItem(product, brand, qty)
            }
        )
    }

    // ── Manual Event Creation Dialog ─────────────────────────────────────────
    if (uiState.showAddEventModal) {
        AddEventDialog(
            onDismiss = { viewModel.closeAddEventModal() },
            onAddEvent = { title, startDate, startTime, location, details ->
                viewModel.addManualEvent(title, startDate, startTime, location, details)
            }
        )
    }

    // Enter Offline Expense Dialog
    if (showOfflineExpenseDialog) {
        AddOfflineExpenseDialog(
            onDismiss = { showOfflineExpenseDialog = false },
            onAddExpense = { vendor, amount, category, isPaid ->
                viewModel.addOfflineExpense(vendor, amount, category, isPaid)
                showOfflineExpenseDialog = false
            }
        )
    }
}

@Composable
fun AddOfflineExpenseDialog(
    onDismiss: () -> Unit,
    onAddExpense: (vendor: String, amount: Double, category: String, isPaid: Boolean) -> Unit
) {
    var vendor by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Cash / Offline") }
    var isPaid by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Enter Offline Expense", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Add a manual cash or offline transaction to your expenses.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = vendor,
                    onValueChange = { vendor = it },
                    label = { Text("Paid To / Store Name *") },
                    placeholder = { Text("e.g. Lucky Traders, Tea Shop") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Total Amount (₹) *") },
                    placeholder = { Text("e.g. 250") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    placeholder = { Text("e.g. Cash, Food & Dining, Retail") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isPaid,
                        onCheckedChange = { isPaid = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPaid) "Marked as Paid" else "Mark as Pending",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (vendor.isNotBlank() && amount > 0) {
                        onAddExpense(vendor, amount, category, isPaid)
                    }
                },
                enabled = vendor.isNotBlank() && (amountStr.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Add Expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ──────────────────────────────────────────────────────────────────────────────
// Cart: Pre-filled form dialog after AI analysis
// ──────────────────────────────────────────────────────────────────────────────

@Composable
fun CartItemFormDialog(
    productName: String,
    brandName: String?,
    onDismiss: () -> Unit,
    onSave: (productName: String, brandName: String?, quantity: Int) -> Unit
) {
    var product  by remember { mutableStateOf(productName) }
    var brand    by remember { mutableStateOf(brandName ?: "") }
    var qtyStr   by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add to Cart", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "AI detected the following product. Edit any field before saving.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Product Name — always pre-filled
                OutlinedTextField(
                    value = product,
                    onValueChange = { product = it },
                    label = { Text("Product Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                )

                // Brand Name — pre-filled or blank with hint placeholder
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand") },
                    placeholder = {
                        Text(
                            "Brand not detected — tap to enter",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Sell, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                )

                // Quantity
                OutlinedTextField(
                    value = qtyStr,
                    onValueChange = { if (it.all(Char::isDigit)) qtyStr = it },
                    label = { Text("Quantity") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Numbers, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (product.isNotBlank()) {
                        val qty = qtyStr.toIntOrNull()?.coerceAtLeast(1) ?: 1
                        onSave(product, brand.ifBlank { null }, qty)
                    }
                },
                enabled = product.isNotBlank()
            ) {
                Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add to Cart")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ──────────────────────────────────────────────────────────────────────────────
// Cart: Individual item card displayed in the Cart list
// ──────────────────────────────────────────────────────────────────────────────

@Composable
fun CartItemCard(item: com.snapaction.data.model.CartItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product icon placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                item.brandName?.let { brand ->
                    Text(
                        text = brand,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // Quantity badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = "×${item.quantity}",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Utility: copy a content URI to a temp file OkHttp can read as a File
// ──────────────────────────────────────────────────────────────────────────────

fun uriToTempFile(context: android.content.Context, uri: android.net.Uri): java.io.File? {
    return try {
        val ext = when (context.contentResolver.getType(uri)) {
            "image/png"  -> ".png"
            "image/webp" -> ".webp"
            else         -> ".jpg"
        }
        val tmp = java.io.File(context.cacheDir, "cart_upload_${System.currentTimeMillis()}$ext")
        context.contentResolver.openInputStream(uri)?.use { input ->
            tmp.outputStream().use { output -> input.copyTo(output) }
        }
        tmp
    } catch (e: Exception) {
        null
    }
}
@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onAddEvent: (String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("12:00") }
    var location by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Event / Reminder", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Date (YYYY-MM-DD) *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Time (HH:MM)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Details & Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && startDate.isNotBlank()) {
                        onAddEvent(title, startDate, startTime, location, details)
                    }
                },
                enabled = title.isNotBlank() && startDate.isNotBlank()
            ) {
                Text("Add Event")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
