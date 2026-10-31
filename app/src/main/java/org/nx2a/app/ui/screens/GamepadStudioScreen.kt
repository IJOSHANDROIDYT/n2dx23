package org.nx2a.app.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.nx2a.app.ui.theme.JetBrainsMono
import org.nx2a.app.ui.theme.NxCyan
import org.nx2a.app.ui.theme.NxEmerald
import org.nx2a.app.ui.theme.NxOutline
import org.nx2a.app.ui.theme.NxSurface
import org.nx2a.app.viewmodel.NxUiState
import org.nx2a.app.viewmodel.NxViewModel
import kotlin.math.hypot
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GamepadStudioScreen(
    uiState: NxUiState,
    viewModel: NxViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTheme = remember(uiState.themePacks, uiState.selectedThemeId) {
        uiState.themePacks.find { it.id == uiState.selectedThemeId } ?: uiState.themePacks.firstOrNull()
    }
    val formFactor = uiState.selectedFormFactor
    val layoutMeta = currentTheme?.layouts?.get(formFactor)
    val controlsForForm = remember(uiState.overlayControls, formFactor) {
        uiState.overlayControls.filter { it.layout == formFactor }
    }

    val atlasBitmap = remember(currentTheme?.id, formFactor) {
        if (currentTheme == null) null
        else {
            val file = layoutMeta?.atlasFile ?: "$formFactor.png"
            try {
                context.assets.open("themes/${currentTheme.id}/$file").use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("gamepad_studio_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 1: Theme Pack Selector
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Themed Virtual Controller Studio",
                                style = MaterialTheme.typography.titleMedium,
                                color = NxCyan
                            )
                            Text(
                                text = "Renders nxnative/dm/android/themes/${currentTheme?.id ?: "ergo"}/$formFactor.png",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = formFactor == "phone",
                                onClick = { viewModel.selectFormFactor("phone") },
                                label = { Text("Phone (20:9)") },
                                modifier = Modifier.testTag("form_factor_phone")
                            )
                            FilterChip(
                                selected = formFactor == "tablet",
                                onClick = { viewModel.selectFormFactor("tablet") },
                                label = { Text("Tablet (16:10)") },
                                modifier = Modifier.testTag("form_factor_tablet")
                            )
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(uiState.themePacks, key = { it.id }) { pack ->
                            val selected = pack.id == currentTheme?.id
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.selectThemePack(pack.id) },
                                label = { Text("${pack.displayName} (${pack.id})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NxCyan.copy(alpha = 0.2f),
                                    selectedLabelColor = NxCyan
                                ),
                                modifier = Modifier.testTag("theme_chip_${pack.id}")
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Interactive Vulkan Overlay Canvas
        item {
            val aspect = if (formFactor == "phone") 915f / 412f else 1216f / 760f
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF06090E)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, NxCyan.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${currentTheme?.displayName ?: "Ergonomic"} · ${if (formFactor == "phone") "915×412 (3x atlas)" else "1216×760 (2x atlas)"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = NxCyan
                        )
                        val activeInputs = if (uiState.heldButtons.isEmpty()) "IDLE" else uiState.heldButtons.joinToString(", ").uppercase()
                        Text(
                            text = "HID: $activeInputs",
                            fontFamily = JetBrainsMono,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.heldButtons.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else NxEmerald
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(aspect)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF1A2639), Color(0xFF090D14))
                                )
                            )
                            .border(1.dp, NxOutline.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .testTag("gamepad_canvas")
                    ) {
                        val spritesForLayout = remember(currentTheme, formFactor) {
                            currentTheme?.sprites?.filter { it.layout == formFactor } ?: emptyList()
                        }

                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(controlsForForm, formFactor) {
                                    detectTapGestures(
                                        onPress = { tapOffset ->
                                            val w = size.width.toFloat()
                                            val h = size.height.toFloat()
                                            var hitControl: String? = null
                                            for (ctrl in controlsForForm) {
                                                val cx = ctrl.anchor * w + ctrl.x * h
                                                val cy = ctrl.y * h
                                                val cw = ctrl.w * h
                                                val ch = ctrl.h * h
                                                if (ctrl.kind == "dpad") {
                                                    val dx = tapOffset.x - cx
                                                    val dy = tapOffset.y - cy
                                                    if (hypot(dx, dy) <= cw * 0.65f) {
                                                        hitControl = if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                                                            if (dx < 0) "left" else "right"
                                                        } else {
                                                            if (dy < 0) "up" else "down"
                                                        }
                                                        break
                                                    }
                                                } else if (
                                                    tapOffset.x >= cx - cw * 0.55f &&
                                                    tapOffset.x <= cx + cw * 0.55f &&
                                                    tapOffset.y >= cy - ch * 0.55f &&
                                                    tapOffset.y <= cy + ch * 0.55f
                                                ) {
                                                    hitControl = ctrl.name
                                                    break
                                                }
                                            }
                                            if (hitControl != null) {
                                                viewModel.setButtonHeld(hitControl, true)
                                                tryAwaitRelease()
                                                viewModel.setButtonHeld(hitControl, false)
                                            }
                                        }
                                    )
                                }
                        ) {
                            val winW = size.width
                            val winH = size.height
                            val designH = (layoutMeta?.designH ?: if (formFactor == "phone") 412 else 760).toFloat()
                            val texelsPerCss = (layoutMeta?.texelsPerCssPx ?: if (formFactor == "phone") 3 else 2).toFloat()
                            val cssScale = winH / designH

                            // Subtle center screen viewport frame guide
                            drawRect(
                                color = Color.White.copy(alpha = 0.04f),
                                topLeft = Offset(winW * 0.15f, winH * 0.08f),
                                size = Size(winW * 0.70f, winH * 0.84f),
                                style = Stroke(width = 1.dp.toPx())
                            )

                            val controlCenters = mutableMapOf<String, Offset>()
                            for (ctrl in controlsForForm) {
                                val cx = ctrl.anchor * winW + ctrl.x * winH
                                val cy = ctrl.y * winH
                                controlCenters[ctrl.name] = Offset(cx, cy)

                                if (uiState.showHitboxes) {
                                    val cw = ctrl.w * winH
                                    val ch = ctrl.h * winH
                                    drawRect(
                                        color = NxCyan.copy(alpha = 0.35f),
                                        topLeft = Offset(cx - cw / 2f, cy - ch / 2f),
                                        size = Size(cw, ch),
                                        style = Stroke(width = 1.5f)
                                    )
                                }
                            }

                            if (atlasBitmap != null) {
                                // First draw decor sprites, then idle/held/knob sprites matching ov_theme_draw in gfx_vk_present.c
                                val orderedSprites = spritesForLayout.sortedBy { if (it.state == "decor") 0 else if (it.state == "knob") 2 else 1 }
                                for (sp in orderedSprites) {
                                    val center = controlCenters[sp.control] ?: continue
                                    val isHeld = when (sp.control) {
                                        "dpad" -> uiState.heldButtons.contains(sp.part)
                                        else -> uiState.heldButtons.contains(sp.control)
                                    }

                                    val shouldDraw = when (sp.state) {
                                        "decor" -> true
                                        "knob" -> true
                                        "held" -> isHeld
                                        "idle" -> !isHeld
                                        else -> false
                                    }
                                    if (!shouldDraw) continue

                                    val dstW = (sp.w / texelsPerCss) * cssScale
                                    val dstH = (sp.h / texelsPerCss) * cssScale
                                    val dstX = center.x + sp.offsetX * cssScale
                                    val dstY = center.y + sp.offsetY * cssScale

                                    drawImage(
                                        image = atlasBitmap,
                                        srcOffset = IntOffset(sp.atlasX, sp.atlasY),
                                        srcSize = IntSize(sp.w, sp.h),
                                        dstOffset = IntOffset(dstX.roundToInt(), dstY.roundToInt()),
                                        dstSize = IntSize(dstW.roundToInt().coerceAtLeast(1), dstH.roundToInt().coerceAtLeast(1)),
                                        alpha = uiState.overlayOpacity
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = "Tip: Tap or hold any button directly on the canvas above, or toggle held states below to inspect sprite transitions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Section 3: Interactive Button State Toggles & Opacity
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
                        text = "Overlay Settings & Held Button Inspector",
                        style = MaterialTheme.typography.titleMedium,
                        color = NxCyan
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Opacity: ${(uiState.overlayOpacity * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.weight(0.35f)
                        )
                        Slider(
                            value = uiState.overlayOpacity,
                            onValueChange = { viewModel.setOverlayOpacity(it) },
                            valueRange = 0.2f..1.0f,
                            modifier = Modifier
                                .weight(0.65f)
                                .testTag("opacity_slider")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Show Hitboxes (overlay_default.tsv)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = uiState.showHitboxes,
                            onCheckedChange = { viewModel.toggleShowHitboxes() },
                            modifier = Modifier.testTag("show_hitboxes_switch")
                        )
                    }

                    Text(
                        text = "Toggle Held Buttons (--held):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val allTestButtons = listOf("a", "b", "x", "y", "l", "r", "zl", "zr", "up", "down", "left", "right", "minus", "plus", "ls", "rs", "stick_l", "stick_r", "menu")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        allTestButtons.forEach { btn ->
                            val isHeld = uiState.heldButtons.contains(btn)
                            FilterChip(
                                selected = isHeld,
                                onClick = { viewModel.toggleButtonHeld(btn) },
                                label = { Text(btn.uppercase(), fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NxEmerald.copy(alpha = 0.25f),
                                    selectedLabelColor = NxEmerald
                                ),
                                modifier = Modifier.testTag("hold_btn_$btn")
                            )
                        }
                    }
                }
            }
        }
    }
}
