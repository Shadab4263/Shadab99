package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.screens.CompatibilityScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GuideAntiBanScreen
import com.example.ui.screens.LatencyTouchScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

enum class AppScreen(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    DASHBOARD("120 FPS", Icons.Filled.Speed, Icons.Outlined.Speed, "nav_dashboard"),
    COMPATIBILITY("Whitelist", Icons.Filled.Assessment, Icons.Outlined.Assessment, "nav_compatibility"),
    TOUCH_LATENCY("Response", Icons.Filled.TouchApp, Icons.Outlined.TouchApp, "nav_touch_latency"),
    GUIDE("Safe Guide", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_guide")
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }

    fun applyWindowDisplayMode(modeId: Int) {
        window?.let { win ->
            val layoutParams = win.attributes
            layoutParams.preferredDisplayModeId = modeId
            win.attributes = layoutParams
        }
    }
}

@Composable
fun MainApp(viewModel: MainViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }

    LaunchedEffect(Unit) {
        viewModel.modeChangeRequests.collect { modeId ->
            (context as? MainActivity)?.applyWindowDisplayMode(modeId)
        }
    }

    // Intercept back presses on sub-screens to return to Dashboard
    if (currentScreen != AppScreen.DASHBOARD) {
        BackHandler {
            currentScreen = AppScreen.DASHBOARD
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        containerColor = DarkBackground,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .background(DarkSurfaceCard)
                    .border(width = 1.dp, color = DarkSurfaceBorder)
            ) {
                NavigationBar(
                    containerColor = DarkSurfaceCard,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    AppScreen.entries.forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DarkBackground,
                                unselectedIconColor = TextSecondary,
                                selectedTextColor = ElectricLime,
                                unselectedTextColor = TextMuted,
                                indicatorColor = ElectricLime
                            ),
                            modifier = Modifier.testTag(screen.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.DASHBOARD -> DashboardScreen(
                    state = uiState,
                    onToggle120HzLock = { viewModel.toggle120HzLock(it) },
                    onRunRamBoost = { viewModel.runRamBoost() },
                    onDecreaseFps = { viewModel.decreaseFps() },
                    onIncreaseFps = { viewModel.increaseFps() },
                    onSelectFps = { viewModel.setCustomFps(it) },
                    onApplyFpsTuning = { viewModel.applyFpsTuning() },
                    onRequestShizukuPermission = { activity -> viewModel.requestShizukuPermission(activity) },
                    onCheckShizukuStatus = { viewModel.checkShizukuStatus() },
                    availableFpsSteps = viewModel.availableFpsSteps
                )
                AppScreen.COMPATIBILITY -> CompatibilityScreen(
                    state = uiState,
                    onRefreshScan = { viewModel.refreshHardwareInfo() }
                )
                AppScreen.TOUCH_LATENCY -> LatencyTouchScreen(
                    touchMetrics = uiState.touchMetrics,
                    onRegisterTouch = { x, y -> viewModel.registerTouchEvent(x, y) },
                    onResetTouch = { viewModel.resetTouchMetrics() }
                )
                AppScreen.GUIDE -> GuideAntiBanScreen()
            }
        }
    }
}
