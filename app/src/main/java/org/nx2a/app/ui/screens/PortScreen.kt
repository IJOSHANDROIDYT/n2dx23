package org.nx2a.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.nx2a.app.model.GameProfile
import org.nx2a.app.ui.theme.JetBrainsMono
import org.nx2a.app.ui.theme.NxAmber
import org.nx2a.app.ui.theme.NxCrimson
import org.nx2a.app.ui.theme.NxCyan
import org.nx2a.app.ui.theme.NxEmerald
import org.nx2a.app.ui.theme.NxOutline
import org.nx2a.app.ui.theme.NxSurface
import org.nx2a.app.ui.theme.NxSurfaceVariant
import org.nx2a.app.viewmodel.NxUiState
import org.nx2a.app.viewmodel.NxViewModel

val LANGUAGES = listOf(
    "" to "English (default)",
    "ja" to "Japanese",
    "fr" to "French",
    "de" to "German",
    "it" to "Italian",
    "es" to "Spanish",
    "nl" to "Dutch",
    "pt" to "Portuguese",
    "ru" to "Russian",
    "ko" to "Korean",
    "zh-Hans" to "Chinese (Simplified)",
    "zh-Hant" to "Chinese (Traditional)"
)

fun statusBadgeColor(status: String): Color = when (status.lowercase()) {
    "playable" -> Color(0xFF2E7D32)
    "wip", "boots", "experimental" -> Color(0xFFEF6C00)
    else -> Color(0xFF475569)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PortScreen(
    uiState: NxUiState,
    viewModel: NxViewModel,
    modifier: Modifier = Modifier
) {
    val androidGames = remember(uiState.profiles) {
        uiState.profiles.filter { it.status != "ios only" }
    }
    var langExpanded by remember { mutableStateOf(false) }
    var deviceExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("port_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 1: Game Folder & Quick Dump Presets
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NxSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NxOutline.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Game Folder (porter inspect)",
                            style = MaterialTheme.typography.titleMedium,
                            color = NxCyan
                        )
                        Text(
                            text = "NSP / XCI / NSZ / XCZ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedTextField(
                        value = uiState.gameFolder,
                        onValueChange = { viewModel.updateGameFolder(it) },
                        label = { Text("Folder with base game, update & DLC") },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(
                                onClick = { viewModel.inspectFolder() },
                                modifier = Modifier.testTag("inspect_folder_button")
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = "Inspect folder")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("game_folder_input")
                    )

                    Text(
                        text = "Quick Select Verified Dump:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(androidGames, key = { it.titleId }) { game ->
                            val isSelected = uiState.inspectedProfile?.titleId == game.titleId
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectGameForPort(game) },
                                label = {
                                    Text(
                                        text = game.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NxCyan.copy(alpha = 0.2f),
                                    selectedLabelColor = NxCyan
                                ),
                                modifier = Modifier.testTag("preset_chip_${game.titleId}")
                            )
                        }
                    }

                    // Inspected Game Card
                    val inspected = uiState.inspectedProfile
                    if (inspected != null) {
                        InspectedGameCard(
                            profile = inspected,
                            onPreviewGamepad = {
                                viewModel.selectThemePack(inspected.themePack)
                                viewModel.selectTab(3)
                            }
                        )
                    } else if (uiState.inspectError != null) {
                        Surface(
                            color = NxCrimson.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = uiState.inspectError,
                                color = Color(0xFFFF8A80),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Options
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NxSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NxOutline.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Options",
                        style = MaterialTheme.typography.titleMedium,
                        color = NxCyan
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.customAppName,
                            onValueChange = { viewModel.setCustomAppName(it) },
                            label = { Text("App name") },
                            placeholder = { Text(uiState.inspectedProfile?.name ?: "the game's name") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("app_name_input")
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            val currentLangName = LANGUAGES.find { it.first == uiState.selectedLanguage }?.second ?: "English (default)"
                            OutlinedTextField(
                                value = currentLangName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Game language") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { langExpanded = true }
                                    .testTag("language_selector")
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { langExpanded = true }
                            )
                            DropdownMenu(
                                expanded = langExpanded,
                                onDismissRequest = { langExpanded = false }
                            ) {
                                LANGUAGES.forEach { (code, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            viewModel.setSelectedLanguage(code)
                                            langExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = uiState.customOutFolder,
                        onValueChange = { viewModel.setCustomOutFolder(it) },
                        label = { Text("Output folder") },
                        placeholder = { Text(uiState.config.outFolder) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("output_folder_input")
                    )

                    // Device selector + refresh
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            val dev = uiState.connectedDevices.find { it.serial == uiState.selectedDeviceSerial }
                            val devText = if (dev != null) "${dev.serial} · ${dev.modelInfo}" else "no device connected"
                            OutlinedTextField(
                                value = devText,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Target Device (ADB)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { deviceExpanded = true }
                            )
                            DropdownMenu(
                                expanded = deviceExpanded,
                                onDismissRequest = { deviceExpanded = false }
                            ) {
                                uiState.connectedDevices.forEach { d ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text("${d.serial} — ${d.modelInfo}", fontWeight = FontWeight.Bold)
                                                Text(d.gpuInfo, style = MaterialTheme.typography.labelSmall)
                                            }
                                        },
                                        onClick = {
                                            viewModel.setSelectedDevice(d.serial)
                                            deviceExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { viewModel.refreshDevices() },
                            modifier = Modifier.testTag("refresh_devices_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh devices")
                        }
                    }

                    // Checkboxes
                    OptionCheckRow(
                        checked = uiState.installWhenDone,
                        onCheckedChange = { viewModel.setInstallWhenDone(it) },
                        label = "Install on the device when done (checks BCn texture support)",
                        tag = "install_when_done_checkbox"
                    )
                    OptionCheckRow(
                        checked = uiState.fps60Unlock,
                        onCheckedChange = { viewModel.setFps60Unlock(it) },
                        label = "60 FPS unlock (--fps60: profile frame step or FPSLocker patch)",
                        tag = "fps60_checkbox"
                    )
                    OptionCheckRow(
                        checked = uiState.debugBuild,
                        onCheckedChange = { viewModel.setDebugBuild(it) },
                        label = "Development build (--debug: debuggable, debug info, DM_CTL key presses)",
                        tag = "debug_build_checkbox"
                    )
                    OptionCheckRow(
                        checked = uiState.experimentalPort,
                        onCheckedChange = { viewModel.setExperimentalPort(it) },
                        label = "Experimental: try a game that is not ported to Android yet (--experimental)",
                        tag = "experimental_checkbox"
                    )
                }
            }
        }

        // Section 3: Action Bar (Port, Cancel, Install)
        item {
            val inspected = uiState.inspectedProfile
            val canPort = inspected != null &&
                (inspected.isAndroidSupported || (uiState.experimentalPort && inspected.status != "playable")) &&
                !uiState.isPorting

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.startPort() },
                    enabled = canPort,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NxCyan,
                        contentColor = Color(0xFF001F24)
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("port_action_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Port", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.cancelPort() },
                    enabled = uiState.isPorting,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("cancel_port_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Cancel")
                }

                if (uiState.portResult != null) {
                    OutlinedButton(
                        onClick = { viewModel.installLastResultNow() },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("install_result_button")
                    ) {
                        Icon(Icons.Default.InstallMobile, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(if (uiState.portResult.installedOnDevice) "Re-install on device" else "Install on device")
                    }
                }
            }
        }

        // Section 4: Progress Bar & Stage Tree
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NxSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NxOutline.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.currentStepLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (uiState.isPorting) NxCyan else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${(uiState.progressFraction * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = NxCyan
                        )
                    }

                    LinearProgressIndicator(
                        progress = { uiState.progressFraction },
                        color = NxCyan,
                        trackColor = NxSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .testTag("port_progress_bar")
                    )

                    if (uiState.stages.isNotEmpty()) {
                        HorizontalDivider(color = NxOutline.copy(alpha = 0.4f))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            uiState.stages.forEach { st ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val mark = when (st.state) {
                                        "done" -> "✓"
                                        "running" -> "▶"
                                        "failed" -> "✗"
                                        else -> "…"
                                    }
                                    val markColor = when (st.state) {
                                        "done" -> NxEmerald
                                        "running" -> NxCyan
                                        "failed" -> NxCrimson
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                    Text(
                                        text = mark,
                                        fontFamily = JetBrainsMono,
                                        fontWeight = FontWeight.Bold,
                                        color = markColor,
                                        modifier = Modifier.width(18.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = st.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (st.state == "running") FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (st.summary.isNotBlank()) {
                                            Text(
                                                text = st.summary,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (st.elapsedSeconds > 0) {
                                        Text(
                                            text = "0:${st.elapsedSeconds.toString().padStart(2, '0')}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Result Summary Box if finished
                    uiState.portResult?.let { res ->
                        Surface(
                            color = NxEmerald.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, NxEmerald.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NxEmerald, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Port Output Ready (${res.packageName})",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = NxEmerald
                                    )
                                }
                                Text(
                                    text = "APK: ${res.apkPath} (${res.apkSizeMb} MB)",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = "Data Pack: ${res.dataFolderPath} (${res.shaderCount} Vulkan SPIR-V shaders)",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 5: Live Console Log
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF070A0F)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NxOutline.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Terminal, contentDescription = null, tint = NxCyan, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "porter --progress json log",
                            style = MaterialTheme.typography.labelSmall,
                            color = NxCyan
                        )
                    }
                    if (uiState.logLines.isEmpty()) {
                        Text(
                            text = "$ Ready. Pick a game folder and tap Port to run the pipeline.",
                            fontFamily = JetBrainsMono,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            uiState.logLines.forEach { line ->
                                val isRed = line.startsWith("[RED]")
                                val isCmd = line.startsWith("$")
                                val color = when {
                                    isRed -> NxCrimson
                                    isCmd -> NxCyan
                                    line.startsWith("✓") -> NxEmerald
                                    else -> Color(0xFFCBD5E1)
                                }
                                Text(
                                    text = line.removePrefix("[RED] "),
                                    fontFamily = JetBrainsMono,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = color
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InspectedGameCard(
    profile: GameProfile,
    onPreviewGamepad: () -> Unit
) {
    Surface(
        color = NxSurfaceVariant.copy(alpha = 0.65f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NxOutline, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (profile.imageAssetPath != null) {
                AsyncImage(
                    model = "file:///android_asset/${profile.imageAssetPath}",
                    contentDescription = profile.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(145.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Title ID: ${profile.titleId} · Package: ${profile.androidPackage}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = statusBadgeColor(profile.status),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = profile.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            val updatesText = if (profile.builds.isNotEmpty()) {
                profile.builds.joinToString(", ") { "${it.versionCode} (${it.displayVersion})" }
            } else {
                profile.defaultPackageVer
            }

            Text(
                text = "In the folder: base game, builds [$updatesText] — packs ${profile.defaultPackageVer}",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Memory, contentDescription = null, tint = NxCyan, modifier = Modifier.size(16.dp))
                Text(
                    text = "${profile.shaderCount} NVN shaders · Modules: ${profile.modules.joinToString("+")} · Theme: ${profile.themePack}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NxCyan,
                    modifier = Modifier.weight(1f)
                )
            }

            if (profile.tested.isNotBlank()) {
                Text(
                    text = "Tested: ${profile.tested}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!profile.isAndroidSupported) {
                Text(
                    text = "Upstream iOS/Experimental profile: enable 'Experimental' below to port with provisional Android settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NxAmber
                )
            }

            OutlinedButton(
                onClick = onPreviewGamepad,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("preview_theme_button")
            ) {
                Icon(Icons.Default.SportsEsports, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Preview '${profile.themePack}' On-Screen Gamepad Theme")
            }
        }
    }
}

@Composable
private fun OptionCheckRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 2.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}
