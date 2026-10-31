package org.nx2a.app.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

@Composable
fun SetupScreen(
    uiState: NxUiState,
    viewModel: NxViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("setup_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 1: Requirements Table (porter doctor)
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
                            text = "Requirements (porter doctor)",
                            style = MaterialTheme.typography.titleMedium,
                            color = NxCyan
                        )
                        val allReady = uiState.requirements.all { it.ok || !it.required }
                        Surface(
                            color = if (allReady) NxEmerald.copy(alpha = 0.18f) else NxCrimson.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (allReady) "READY" else "ACTION NEEDED",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (allReady) NxEmerald else NxCrimson,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    uiState.requirements.forEachIndexed { idx, req ->
                        if (idx > 0) {
                            HorizontalDivider(color = NxOutline.copy(alpha = 0.35f))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val mark = when {
                                req.ok -> "✓"
                                req.required -> "✗"
                                else -> "–"
                            }
                            val markColor = when {
                                req.ok -> NxEmerald
                                req.required -> NxCrimson
                                else -> NxAmber
                            }
                            Text(
                                text = mark,
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                                color = markColor,
                                modifier = Modifier.width(18.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = req.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = req.version,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = req.detail + (if (!req.ok && req.fixHint.isNotBlank()) "  →  ${req.fixHint}" else ""),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (req.ok) MaterialTheme.colorScheme.onSurfaceVariant else markColor
                                )
                            }
                        }
                    }

                    Surface(
                        color = NxSurfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "A port needs the Android NDK r27c, the .NET SDK 10 and, to install from here, Android platform-tools. " +
                                "'Download missing tools' fetches them from Google and Microsoft (about 877 MB) into your nx2android folder; " +
                                "or install them yourself and set their paths below. Your prod.keys must come from your own Switch. " +
                                "Keys and game files never leave your device.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.downloadMissingTools() },
                            enabled = !uiState.isDownloadingTools,
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("download_tools_button")
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Download missing tools")
                        }

                        if (uiState.downloadStepText.isNotBlank()) {
                            Text(
                                text = uiState.downloadStepText,
                                style = MaterialTheme.typography.labelSmall,
                                color = NxCyan,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (uiState.isDownloadingTools) {
                        LinearProgressIndicator(
                            progress = { uiState.downloadProgress },
                            color = NxCyan,
                            trackColor = NxSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }

        // Section 2: Settings Form (porter.config)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NxSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NxOutline.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = NxCyan)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Settings (porter.config)",
                            style = MaterialTheme.typography.titleMedium,
                            color = NxCyan
                        )
                    }

                    OutlinedTextField(
                        value = uiState.config.keysPath,
                        onValueChange = { viewModel.updateConfigField("keys", it) },
                        label = { Text("prod.keys") },
                        placeholder = { Text("path to your Switch prod.keys") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("config_keys_input")
                    )

                    OutlinedTextField(
                        value = uiState.config.ndkPath,
                        onValueChange = { viewModel.updateConfigField("ndk", it) },
                        label = { Text("Android NDK folder") },
                        placeholder = { Text("found automatically") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("config_ndk_input")
                    )

                    OutlinedTextField(
                        value = uiState.config.dotnetPath,
                        onValueChange = { viewModel.updateConfigField("dotnet", it) },
                        label = { Text("dotnet") },
                        placeholder = { Text("found automatically") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("config_dotnet_input")
                    )

                    OutlinedTextField(
                        value = uiState.config.adbPath,
                        onValueChange = { viewModel.updateConfigField("adb", it) },
                        label = { Text("adb") },
                        placeholder = { Text("found automatically") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("config_adb_input")
                    )

                    OutlinedTextField(
                        value = uiState.config.outFolder,
                        onValueChange = { viewModel.updateConfigField("out", it) },
                        label = { Text("Default output folder") },
                        placeholder = { Text("the game folder") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("config_out_input")
                    )

                    OutlinedTextField(
                        value = uiState.config.deviceSerial,
                        onValueChange = { viewModel.updateConfigField("device", it) },
                        label = { Text("Device (adb serial)") },
                        placeholder = { Text("the only connected device") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("config_device_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { viewModel.saveAndCheckConfig() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NxCyan,
                                contentColor = Color(0xFF001F24)
                            ),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("save_and_check_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Save and check", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
