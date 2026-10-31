package org.nx2a.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.nx2a.app.ui.screens.GamepadStudioScreen
import org.nx2a.app.ui.screens.GamesScreen
import org.nx2a.app.ui.screens.PortScreen
import org.nx2a.app.ui.screens.SetupScreen
import org.nx2a.app.ui.theme.JetBrainsMono
import org.nx2a.app.ui.theme.Nx2AndroidTheme
import org.nx2a.app.ui.theme.NxCrimson
import org.nx2a.app.ui.theme.NxCyan
import org.nx2a.app.ui.theme.NxEmerald
import org.nx2a.app.ui.theme.NxSlateBg
import org.nx2a.app.ui.theme.NxSurface
import org.nx2a.app.ui.theme.NxSurfaceVariant
import org.nx2a.app.viewmodel.NxViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Nx2AndroidTheme {
                Nx2AndroidApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Nx2AndroidApp(viewModel: NxViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.selectedTab != 0) {
        BackHandler {
            viewModel.selectTab(0)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = NxSlateBg,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NxSurface
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = NxCrimson,
                            shape = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp),
                            modifier = Modifier.size(width = 10.dp, height = 22.dp)
                        ) {}
                        Surface(
                            color = NxCyan,
                            shape = RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp),
                            modifier = Modifier.size(width = 10.dp, height = 22.dp)
                        ) {}
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "nx2android",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "v0.1.3 · arm64 NSO + Vulkan NVN Replay",
                                fontFamily = JetBrainsMono,
                                style = MaterialTheme.typography.labelSmall,
                                color = NxCyan
                            )
                        }
                    }
                },
                actions = {
                    val playableCount = uiState.profiles.count { it.status != "ios only" }
                    Surface(
                        color = NxSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "$playableCount Android / ${uiState.profiles.size} Profiles",
                            style = MaterialTheme.typography.labelSmall,
                            color = NxEmerald,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = NxSurface
            ) {
                val tabs = listOf(
                    Triple(0, "Port", Icons.Default.RocketLaunch),
                    Triple(1, "Setup", Icons.Default.Build),
                    Triple(2, "Games", Icons.Default.SportsEsports),
                    Triple(3, "Gamepad", Icons.Default.Gamepad)
                )
                tabs.forEach { (idx, title, icon) ->
                    NavigationBarItem(
                        selected = uiState.selectedTab == idx,
                        onClick = { viewModel.selectTab(idx) },
                        icon = { Icon(icon, contentDescription = title) },
                        label = { Text(title, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF001F24),
                            selectedTextColor = NxCyan,
                            indicatorColor = NxCyan,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_${title.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            uiState.statusBannerMessage?.let { msg ->
                Surface(
                    color = NxCyan.copy(alpha = 0.16f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = NxCyan,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearBanner() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = NxCyan)
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NxSlateBg)
            ) {
                when (uiState.selectedTab) {
                    0 -> PortScreen(uiState = uiState, viewModel = viewModel)
                    1 -> SetupScreen(uiState = uiState, viewModel = viewModel)
                    2 -> GamesScreen(uiState = uiState, viewModel = viewModel)
                    3 -> GamepadStudioScreen(uiState = uiState, viewModel = viewModel)
                }
            }
        }
    }
}
