package org.nx2a.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import org.nx2a.app.ui.theme.NxCyan
import org.nx2a.app.ui.theme.NxEmerald
import org.nx2a.app.ui.theme.NxOutline
import org.nx2a.app.ui.theme.NxSurface
import org.nx2a.app.ui.theme.NxSurfaceVariant
import org.nx2a.app.viewmodel.NxUiState
import org.nx2a.app.viewmodel.NxViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    uiState: NxUiState,
    viewModel: NxViewModel,
    modifier: Modifier = Modifier
) {
    val filters = listOf(
        "all" to "All (${uiState.profiles.size})",
        "playable" to "Playable (${uiState.profiles.count { it.status == "playable" }})",
        "wip" to "WIP (${uiState.profiles.count { it.status == "wip" }})",
        "boots" to "Boots (${uiState.profiles.count { it.status == "boots" }})",
        "experimental" to "Experimental (${uiState.profiles.count { it.status == "experimental" }})",
        "ios only" to "iOS Only (${uiState.profiles.count { it.status == "ios only" }})"
    )

    val filteredProfiles = remember(uiState.profiles, uiState.searchQuery, uiState.statusFilter) {
        uiState.profiles.filter { p ->
            val matchesFilter = uiState.statusFilter == "all" || p.status == uiState.statusFilter
            val q = uiState.searchQuery.trim()
            val matchesQuery = q.isEmpty() ||
                p.name.contains(q, ignoreCase = true) ||
                p.titleId.contains(q, ignoreCase = true) ||
                p.androidPackage.contains(q, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }

    if (uiState.selectedDetailProfile != null) {
        BackHandler {
            viewModel.selectDetailProfile(null)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("games_screen_container")
    ) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Games ported to Android. playable, wip (plays, with parts missing) and boots ones can be ported; any other game can still be tried with Experimental on the Port tab.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search by game title, Title ID (0100...), or package") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("games_search_input")
        )

        Spacer(Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filters, key = { it.first }) { (key, label) ->
                val selected = uiState.statusFilter == key
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.setStatusFilter(key) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NxCyan.copy(alpha = 0.2f),
                        selectedLabelColor = NxCyan
                    ),
                    modifier = Modifier.testTag("filter_chip_$key")
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("games_list"),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredProfiles, key = { it.titleId }) { profile ->
                GameProfileRowCard(
                    profile = profile,
                    onClick = { viewModel.selectDetailProfile(profile) },
                    onQuickPort = { viewModel.selectGameForPort(profile) }
                )
            }
        }
    }

    // Detail Modal Sheet
    val detailProfile = uiState.selectedDetailProfile
    if (detailProfile != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.selectDetailProfile(null) },
            sheetState = sheetState,
            containerColor = NxSurface
        ) {
            GameProfileDetailSheet(
                profile = detailProfile,
                onPortThisGame = { viewModel.selectGameForPort(detailProfile) },
                onPreviewTheme = {
                    viewModel.selectThemePack(detailProfile.themePack)
                    viewModel.selectDetailProfile(null)
                    viewModel.selectTab(3)
                },
                onClose = { viewModel.selectDetailProfile(null) }
            )
        }
    }
}

@Composable
private fun GameProfileRowCard(
    profile: GameProfile,
    onClick: () -> Unit,
    onQuickPort: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NxSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NxOutline.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("game_card_${profile.titleId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (profile.imageAssetPath != null) {
                AsyncImage(
                    model = "file:///android_asset/${profile.imageAssetPath}",
                    contentDescription = profile.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 88.dp, height = 56.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(width = 88.dp, height = 56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(NxSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.titleId.takeLast(6),
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        color = NxCyan
                    )
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        color = statusBadgeColor(profile.status),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = profile.status,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "${profile.titleId} · ${profile.shaderCount} shaders · theme: ${profile.themePack}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NxCyan
                )

                if (profile.tested.isNotBlank()) {
                    Text(
                        text = profile.tested,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "Modules: ${profile.modules.joinToString(", ")} · Default build: ${profile.defaultPackageVer}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun GameProfileDetailSheet(
    profile: GameProfile,
    onPortThisGame: () -> Unit,
    onPreviewTheme: () -> Unit,
    onClose: () -> Unit
) {
    var selectedSourceTab by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "nxnative/dm/games/${profile.titleId}/",
                        style = MaterialTheme.typography.labelSmall,
                        color = NxCyan
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        }

        if (profile.imageAssetPath != null) {
            item {
                AsyncImage(
                    model = "file:///android_asset/${profile.imageAssetPath}",
                    contentDescription = profile.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onPortThisGame,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NxCyan,
                        contentColor = Color(0xFF001F24)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("sheet_port_game_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Load in Port Tab", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onPreviewTheme,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("sheet_preview_theme_button")
                ) {
                    Icon(Icons.Default.SportsEsports, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Theme: ${profile.themePack}")
                }
            }
        }

        // Key Constants Grid
        item {
            Surface(
                color = NxSurfaceVariant.copy(alpha = 0.55f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = NxCyan, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Horizon Host & Memory Partition", style = MaterialTheme.typography.titleMedium, color = NxCyan)
                    }
                    HorizontalDivider(color = NxOutline.copy(alpha = 0.4f))
                    Text("Package: ${profile.androidPackage} (${profile.orientation})", style = MaterialTheme.typography.labelSmall)
                    Text("Memory: reserve=${profile.memReserve} · alias=${profile.memAlias} · stack=${profile.memStack} · pool=${profile.appPool}", style = MaterialTheme.typography.labelSmall)
                    Text("Modules: ${profile.modules.joinToString(" -> ")} · RomFS marker: ${profile.romfsMarker.ifBlank { "(root)" }}", style = MaterialTheme.typography.labelSmall)
                    Text("Pacing: present_interval=${profile.presentInterval} · fps_target=${profile.fpsTarget} · op_mode=${profile.opMode}", style = MaterialTheme.typography.labelSmall)
                    Text("Shaders: ${profile.shaderCount} in shader_map.tsv (${profile.shaderNote})", style = MaterialTheme.typography.labelSmall)
                    if (profile.releaseOutputSummary != null) {
                        Text(
                            text = "Release Check (${profile.releaseBundle}): ${profile.releaseOutputSummary} in ${profile.releasePortTime}",
                            style = MaterialTheme.typography.labelSmall,
                            color = NxEmerald
                        )
                    }
                }
            }
        }

        // Source Viewer Tabs (game.tsv, android.tsv, patches.py)
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedSourceTab == 0,
                    onClick = { selectedSourceTab = 0 },
                    label = { Text("game.tsv") }
                )
                if (profile.rawAndroidTsv.isNotBlank()) {
                    FilterChip(
                        selected = selectedSourceTab == 1,
                        onClick = { selectedSourceTab = 1 },
                        label = { Text("android.tsv") }
                    )
                }
                if (profile.hasPatches) {
                    FilterChip(
                        selected = selectedSourceTab == 2,
                        onClick = { selectedSourceTab = 2 },
                        label = { Text("patches.py") }
                    )
                }
            }
        }

        item {
            val codeText = when (selectedSourceTab) {
                1 -> profile.rawAndroidTsv
                2 -> profile.patchesSource
                else -> profile.rawGameTsv
            }
            Surface(
                color = Color(0xFF070A0F),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NxOutline.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = NxCyan, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = when (selectedSourceTab) {
                                1 -> "nxnative/dm/games/${profile.titleId}/android.tsv"
                                2 -> "nxnative/dm/games/${profile.titleId}/patches.py"
                                else -> "nxnative/dm/games/${profile.titleId}/game.tsv"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = NxCyan
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = codeText,
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = Color(0xFFE2E8F0),
                        modifier = Modifier.heightIn(min = 120.dp)
                    )
                }
            }
        }
    }
}
