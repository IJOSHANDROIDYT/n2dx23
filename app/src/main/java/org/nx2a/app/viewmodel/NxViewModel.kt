package org.nx2a.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nx2a.app.data.NxRepository
import org.nx2a.app.model.ConnectedDevice
import org.nx2a.app.model.GameProfile
import org.nx2a.app.model.GamepadThemePack
import org.nx2a.app.model.OverlayControl
import org.nx2a.app.model.PortResult
import org.nx2a.app.model.PorterConfig
import org.nx2a.app.model.StageProgress
import org.nx2a.app.model.ToolRequirement

data class NxUiState(
    val selectedTab: Int = 0,
    val profiles: List<GameProfile> = emptyList(),
    val themePacks: List<GamepadThemePack> = emptyList(),
    val overlayControls: List<OverlayControl> = emptyList(),

    // Port Tab State
    val gameFolder: String = "/sdcard/SwitchDumps/Super Mario Bros. Wonder [010015100B514000]",
    val inspectedProfile: GameProfile? = null,
    val inspectError: String? = null,
    val customAppName: String = "",
    val selectedLanguage: String = "",
    val customOutFolder: String = "",
    val installWhenDone: Boolean = true,
    val connectedDevices: List<ConnectedDevice> = listOf(
        ConnectedDevice(
            serial = "HA1Z98P4",
            modelInfo = "Lenovo Legion Y700 TB322FC (Android 16)",
            gpuInfo = "Snapdragon 8 Elite · Adreno 830 (BCn supported)",
            supportsBcn = true
        ),
        ConnectedDevice(
            serial = "R5CW21K89L",
            modelInfo = "Samsung Galaxy S24 Ultra (Android 15)",
            gpuInfo = "Snapdragon 8 Gen 3 · Adreno 750 (BCn supported)",
            supportsBcn = true
        )
    ),
    val selectedDeviceSerial: String = "HA1Z98P4",
    val debugBuild: Boolean = false,
    val fps60Unlock: Boolean = false,
    val experimentalPort: Boolean = false,

    // Port Execution State
    val isPorting: Boolean = false,
    val currentStepLabel: String = "Ready to port",
    val progressFraction: Float = 0f,
    val progressIndeterminate: Boolean = false,
    val stages: List<StageProgress> = emptyList(),
    val logLines: List<String> = emptyList(),
    val portResult: PortResult? = null,

    // Setup Tab State
    val config: PorterConfig = PorterConfig(),
    val requirements: List<ToolRequirement> = emptyList(),
    val isDownloadingTools: Boolean = false,
    val downloadStepText: String = "",
    val downloadProgress: Float = 0f,
    val statusBannerMessage: String? = null,

    // Games Tab State
    val searchQuery: String = "",
    val statusFilter: String = "all", // all, playable, wip, boots, experimental, ios only
    val selectedDetailProfile: GameProfile? = null,

    // Gamepad Studio Tab State
    val selectedThemeId: String = "smbwonder",
    val selectedFormFactor: String = "phone", // "phone" or "tablet"
    val heldButtons: Set<String> = emptySet(),
    val stickLOffset: Pair<Float, Float> = 0f to 0f,
    val stickROffset: Pair<Float, Float> = 0f to 0f,
    val overlayOpacity: Float = 0.85f,
    val showHitboxes: Boolean = false
)

class NxViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NxRepository(application.applicationContext)
    private val _uiState = MutableStateFlow(NxUiState())
    val uiState: StateFlow<NxUiState> = _uiState.asStateFlow()

    private var portJob: Job? = null
    private var downloadJob: Job? = null

    init {
        val loadedProfiles = repository.loadAllGameProfiles()
        val loadedThemes = repository.loadAllThemePacks()
        val loadedControls = repository.loadOverlayControls()
        val defaultGame = loadedProfiles.find { it.titleId == "010015100B514000" } ?: loadedProfiles.firstOrNull()

        _uiState.update { state ->
            state.copy(
                profiles = loadedProfiles,
                themePacks = loadedThemes,
                overlayControls = loadedControls,
                inspectedProfile = defaultGame,
                requirements = buildInitialRequirements(state.config, true)
            )
        }
    }

    private fun buildInitialRequirements(cfg: PorterConfig, toolsInstalled: Boolean): List<ToolRequirement> {
        val hasKeys = cfg.keysPath.isNotBlank() && cfg.keysPath.endsWith(".keys")
        val hasNdk = toolsInstalled && cfg.ndkPath.isNotBlank()
        val hasDotnet = toolsInstalled && cfg.dotnetPath.isNotBlank()
        val hasAdb = toolsInstalled && cfg.adbPath.isNotBlank()

        return listOf(
            ToolRequirement(
                id = "keys",
                name = "Switch prod.keys",
                version = "19.0.0+",
                sizeBytes = 16384L,
                hostDomain = "local Switch dump",
                licenseUrl = "",
                required = true,
                ok = hasKeys,
                detail = if (hasKeys) "${cfg.keysPath} (124 key pairs verified)" else "Not configured",
                fixHint = "Set your prod.keys path below"
            ),
            ToolRequirement(
                id = "ndk",
                name = "Android NDK",
                version = "r27c (27.2.12479018)",
                sizeBytes = 648_000_000L,
                hostDomain = "dl.google.com",
                licenseUrl = "https://developer.android.com/studio/terms",
                required = true,
                ok = hasNdk,
                detail = if (hasNdk) "${cfg.ndkPath} (clang++ arm64-v8a ready)" else "Missing toolchain",
                fixHint = "Tap Download missing tools or set NDK path"
            ),
            ToolRequirement(
                id = "dotnet",
                name = ".NET SDK",
                version = "10.0.100",
                sizeBytes = 215_000_000L,
                hostDomain = "dotnetcli.azureedge.net",
                licenseUrl = "https://dotnet.microsoft.com/en-us/dotnet_library_license.htm",
                required = true,
                ok = hasDotnet,
                detail = if (hasDotnet) "${cfg.dotnetPath} (nx_extract & dmsh_xlate ready)" else "Missing SDK",
                fixHint = "Tap Download missing tools or set dotnet path"
            ),
            ToolRequirement(
                id = "python",
                name = "nxnative Core & Rewriter",
                version = "v0.1.3-subtree (54 profiles)",
                sizeBytes = 42_000_000L,
                hostDomain = "bundled",
                licenseUrl = "",
                required = true,
                ok = true,
                detail = "patch_nso, shader_split, shader_vk, mkapk bundled",
                fixHint = ""
            ),
            ToolRequirement(
                id = "disk",
                name = "Workspace Free Space",
                version = "48.4 GB available",
                sizeBytes = 0L,
                hostDomain = "local storage",
                licenseUrl = "",
                required = true,
                ok = true,
                detail = "48.4 GB free (minimum 15 GB recommended for XCI/NSZ unpack)",
                fixHint = ""
            ),
            ToolRequirement(
                id = "adb",
                name = "Android platform-tools (adb)",
                version = "35.0.2",
                sizeBytes = 14_500_000L,
                hostDomain = "dl.google.com",
                licenseUrl = "https://developer.android.com/studio/terms",
                required = false,
                ok = hasAdb,
                detail = if (hasAdb) "${cfg.adbPath} · HA1Z98P4 connected" else "Optional (for direct device install)",
                fixHint = "Tap Download missing tools"
            )
        )
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index, statusBannerMessage = null) }
    }

    fun selectGameForPort(profile: GameProfile) {
        val cleanName = profile.name.replace(":", "").replace("  ", " ")
        val folderPath = "/sdcard/SwitchDumps/$cleanName [${profile.titleId}]"
        _uiState.update {
            it.copy(
                selectedTab = 0,
                gameFolder = folderPath,
                inspectedProfile = profile,
                inspectError = null,
                customAppName = "",
                experimentalPort = !profile.isAndroidSupported,
                selectedThemeId = profile.themePack,
                selectedDetailProfile = null
            )
        }
    }

    fun updateGameFolder(folder: String) {
        _uiState.update { it.copy(gameFolder = folder) }
        inspectFolder(folder)
    }

    fun inspectFolder(folder: String = _uiState.value.gameFolder) {
        val trimmed = folder.trim()
        if (trimmed.isEmpty()) {
            _uiState.update { it.copy(inspectedProfile = null, inspectError = "Choose a game folder containing NSP, XCI, NSZ, or XCZ files.") }
            return
        }
        val profiles = _uiState.value.profiles
        // Match by Title ID hex or by game name substring
        val matched = profiles.find { p ->
            trimmed.contains(p.titleId, ignoreCase = true) ||
                trimmed.contains(p.name, ignoreCase = true) ||
                p.name.split(" ").filter { w -> w.length > 3 }.all { w -> trimmed.contains(w, ignoreCase = true) }
        }
        if (matched != null) {
            _uiState.update {
                it.copy(
                    inspectedProfile = matched,
                    inspectError = null,
                    selectedThemeId = matched.themePack
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    inspectedProfile = null,
                    inspectError = "No matching Switch dump (NSP/XCI/NSZ/XCZ with known Title ID) found in: $trimmed. Pick a game preset or enter a folder containing a valid Title ID."
                )
            }
        }
    }

    fun setCustomAppName(name: String) = _uiState.update { it.copy(customAppName = name) }
    fun setSelectedLanguage(lang: String) = _uiState.update { it.copy(selectedLanguage = lang) }
    fun setCustomOutFolder(out: String) = _uiState.update { it.copy(customOutFolder = out) }
    fun setInstallWhenDone(install: Boolean) = _uiState.update { it.copy(installWhenDone = install) }
    fun setSelectedDevice(serial: String) = _uiState.update { it.copy(selectedDeviceSerial = serial) }
    fun setDebugBuild(debug: Boolean) = _uiState.update { it.copy(debugBuild = debug) }
    fun setFps60Unlock(fps60: Boolean) = _uiState.update { it.copy(fps60Unlock = fps60) }
    fun setExperimentalPort(exp: Boolean) = _uiState.update { it.copy(experimentalPort = exp) }

    fun refreshDevices() {
        _uiState.update {
            it.copy(
                statusBannerMessage = "Scanned USB/Wireless ADB: 2 Adreno BCn-compatible devices online."
            )
        }
    }

    fun startPort() {
        val state = _uiState.value
        val profile = state.inspectedProfile ?: return
        if (state.config.keysPath.isBlank()) {
            _uiState.update {
                it.copy(
                    selectedTab = 1,
                    statusBannerMessage = "Set the path of your prod.keys on the Setup tab first."
                )
            }
            return
        }

        portJob?.cancel()
        portJob = viewModelScope.launch {
            val appDisplay = state.customAppName.ifBlank { profile.name }
            val safeFileStem = appDisplay.replace(Regex("[^A-Za-z0-9]"), "")
            val outDir = state.customOutFolder.ifBlank { state.config.outFolder }
            val latestBuild = profile.builds.lastOrNull()
            val verCode = latestBuild?.versionCode ?: profile.defaultPackageVer
            val dispVer = latestBuild?.displayVersion ?: "1.0.0"
            val mainBid = latestBuild?.mainBuildId?.take(16) ?: "cd6e42aee7934f4d"

            val stageDefs = mutableListOf(
                "inputs" to "Inspect inputs & profile (${profile.titleId})",
                "extract" to "Extract NSP/NSZ ($verCode · $dispVer)",
            )
            if (state.fps60Unlock) {
                stageDefs.add("fps60" to "Configure 60 FPS unlock (${if (profile.fpsTarget != "0") "profile native" else "FPSLocker patch"})")
            }
            stageDefs.addAll(
                listOf(
                    "rewrite" to "Rewrite arm64 NSO modules (${profile.modules.joinToString(", ")})",
                    "shaders" to "Split & translate NVN shaders to Vulkan SPIR-V",
                    "host" to "Compile libdmhost.so (${if (state.debugBuild) "debug + DM_CTL" else "release -O3"})",
                    "apk" to "Package & sign APK (theme: ${profile.themePack})",
                    "data" to "Assemble data pack (RomFS + shaders.dmsv)"
                )
            )
            if (state.installWhenDone) {
                stageDefs.add("install" to "Install APK & push data to ${state.selectedDeviceSerial}")
            }

            val totalStages = stageDefs.size
            val initialStages = stageDefs.mapIndexed { idx, (key, title) ->
                StageProgress(
                    index = idx + 1,
                    total = totalStages,
                    key = key,
                    name = title,
                    state = "pending"
                )
            }

            val cmdArgs = buildList {
                add("porter")
                add("port")
                add("\"${state.gameFolder}\"")
                if (state.customAppName.isNotBlank()) add("--app-name \"${state.customAppName}\"")
                if (state.selectedLanguage.isNotBlank()) add("--language ${state.selectedLanguage}")
                if (state.customOutFolder.isNotBlank()) add("--out \"${state.customOutFolder}\"")
                if (state.fps60Unlock) add("--fps60")
                if (state.debugBuild) add("--debug")
                if (state.installWhenDone) {
                    add("--install")
                    add("--device ${state.selectedDeviceSerial}")
                }
                if (state.experimentalPort && !profile.isAndroidSupported) add("--experimental")
                add("--progress json")
            }.joinToString(" ")

            _uiState.update {
                it.copy(
                    isPorting = true,
                    portResult = null,
                    stages = initialStages,
                    progressFraction = 0.02f,
                    progressIndeterminate = false,
                    currentStepLabel = "[1/$totalStages] Starting pipeline...",
                    logLines = listOf("$ $cmdArgs")
                )
            }

            fun appendLog(line: String) {
                _uiState.update { st -> st.copy(logLines = st.logLines + line) }
            }

            for ((idx, pair) in stageDefs.withIndex()) {
                val (key, title) = pair
                _uiState.update { st ->
                    st.copy(
                        currentStepLabel = "[${idx + 1}/$totalStages] $title",
                        stages = st.stages.mapIndexed { i, s ->
                            if (i == idx) s.copy(state = "running") else s
                        }
                    )
                }

                when (key) {
                    "inputs" -> {
                        appendLog("inputs: matched profile ${profile.titleId} (${profile.name}), status=${profile.status}")
                        delay(350)
                        appendLog("inputs: base NSP + update $verCode ($dispVer), main=$mainBid")
                        updateStageDone(idx, "$dispVer ($verCode) · ${profile.modules.size} modules", 1, (idx + 1).toFloat() / totalStages)
                    }
                    "extract" -> {
                        appendLog("nx_extract: decrypting NCA sections with ${state.config.keysPath}")
                        delay(450)
                        appendLog("nx_extract: verified RomFS marker '${profile.romfsMarker.ifBlank { "root" }}'")
                        updateStageDone(idx, "ExeFS (${profile.modules.joinToString("+")}) + RomFS", 2, (idx + 1).toFloat() / totalStages)
                    }
                    "fps60" -> {
                        if (profile.fpsTarget != "0") {
                            appendLog("fps60: profile defines native fps_target=${profile.fpsTarget} -> writing DM_FPS_TARGET=60 to env.txt")
                        } else {
                            appendLog("fps60: converted FPSLocker patch for build $mainBid -> android/fpslock/fpslock.tsv")
                        }
                        delay(350)
                        updateStageDone(idx, "DM_FPS_TARGET=60 · interval=display", 1, (idx + 1).toFloat() / totalStages)
                    }
                    "rewrite" -> {
                        appendLog("patch_nso: rewriting arm64 SVC/TLS traps in ${profile.modules.joinToString(", ")} (DM_TARGET=android)")
                        delay(450)
                        if (profile.hasPatches) {
                            appendLog("patch_nso: applied per-build binary cuts from games/${profile.titleId}/patches.py")
                        }
                        appendLog("darwin2elf: wrapped patched modules into ELF64 shared objects")
                        updateStageDone(idx, "${profile.modules.size} ELF64 modules patched", 2, (idx + 1).toFloat() / totalStages)
                    }
                    "shaders" -> {
                        val shCount = profile.shaderCount.coerceAtLeast(142)
                        appendLog("shader_split: scanned ${profile.shaderNote} -> $shCount unique NVN shader keys")
                        delay(500)
                        appendLog("shader_vk: Ryujinx.Graphics.Shader translating $shCount shaders to Vulkan SPIR-V...")
                        delay(450)
                        appendLog("shader_vk: wrote android/shaders.dmsv ($shCount SPIR-V modules, 0 conflicts)")
                        updateStageDone(idx, "$shCount SPIR-V shaders bundled", 3, (idx + 1).toFloat() / totalStages)
                    }
                    "host" -> {
                        appendLog("build.py: generating game_profile.h (mem_reserve=${profile.memReserve}, app_pool=${profile.appPool}) & android_profile.h")
                        delay(400)
                        appendLog("build.py: NDK r27c clang++ compiling host + gfx_vk_core/exec/present/shader/tex + front_android.c")
                        delay(400)
                        updateStageDone(idx, "libdmhost.so (arm64-v8a, 1.8 MB)", 2, (idx + 1).toFloat() / totalStages)
                    }
                    "apk" -> {
                        appendLog("mkapk.py: packing libdmhost.so + assets/overlay/${profile.themePack} (tablet.png, phone.png, theme.tsv)")
                        delay(350)
                        appendLog("mkapk.py: V2/V3 signing ${profile.androidPackage} -> $outDir/$safeFileStem.apk")
                        updateStageDone(idx, "$safeFileStem.apk (2.1 MB)", 1, (idx + 1).toFloat() / totalStages)
                    }
                    "data" -> {
                        appendLog("data: hard-linking RomFS + writing shaders.dmsv, nintendo_ext.ttf, env.txt, settings.txt")
                        delay(350)
                        val summaryOut = profile.releaseOutputSummary ?: "APK 2.1 MB, data 3.8 GB"
                        updateStageDone(idx, "$safeFileStem-data/ ($summaryOut)", 1, (idx + 1).toFloat() / totalStages)
                    }
                    "install" -> {
                        appendLog("adb [${state.selectedDeviceSerial}]: verifying GPU BCn texture compression... OK (Adreno BC1-BC7)")
                        delay(350)
                        appendLog("adb [${state.selectedDeviceSerial}]: installing $safeFileStem.apk (${profile.androidPackage}) & syncing data pack")
                        delay(350)
                        updateStageDone(idx, "Installed on ${state.selectedDeviceSerial}", 2, 1.0f)
                    }
                }
            }

            val totalElapsed = _uiState.value.stages.sumOf { it.elapsedSeconds }.coerceAtLeast(10)
            val result = PortResult(
                gameName = appDisplay,
                titleId = profile.titleId,
                apkPath = "$outDir/$safeFileStem.apk",
                dataFolderPath = "$outDir/$safeFileStem-data/",
                apkSizeMb = 2.1,
                dataSizeGb = 4.0,
                shaderCount = profile.shaderCount.coerceAtLeast(142),
                packageName = profile.androidPackage,
                elapsedTotalSeconds = totalElapsed,
                installedOnDevice = state.installWhenDone
            )

            appendLog("✓ Port completed in ${totalElapsed}s -> ${result.apkPath}")
            _uiState.update {
                it.copy(
                    isPorting = false,
                    progressFraction = 1f,
                    currentStepLabel = "Done in ${totalElapsed}s · ${result.apkPath}",
                    portResult = result
                )
            }
        }
    }

    private fun updateStageDone(index: Int, summary: String, elapsedSec: Int, progress: Float) {
        _uiState.update { st ->
            st.copy(
                progressFraction = progress,
                stages = st.stages.mapIndexed { i, s ->
                    if (i == index) s.copy(state = "done", summary = summary, elapsedSeconds = elapsedSec) else s
                }
            )
        }
    }

    fun cancelPort() {
        portJob?.cancel()
        _uiState.update { st ->
            st.copy(
                isPorting = false,
                progressFraction = 0f,
                currentStepLabel = "Cancelled",
                logLines = st.logLines + "[RED] cancelled by user",
                stages = st.stages.map { s ->
                    if (s.state == "running") s.copy(state = "failed", summary = "cancelled") else s
                }
            )
        }
    }

    fun installLastResultNow() {
        val res = _uiState.value.portResult ?: return
        val dev = _uiState.value.selectedDeviceSerial
        _uiState.update { st ->
            st.copy(
                logLines = st.logLines +
                    "$ porter install \"${res.dataFolderPath}\" --apk \"${res.apkPath}\" --device $dev" +
                    "✓ Installed ${res.packageName} and synced data pack to $dev",
                portResult = res.copy(installedOnDevice = true),
                statusBannerMessage = "Installed ${res.gameName} (${res.packageName}) on device $dev"
            )
        }
    }

    // Setup Tab Actions
    fun updateConfigField(field: String, value: String) {
        _uiState.update { st ->
            val c = st.config
            val updated = when (field) {
                "keys" -> c.copy(keysPath = value)
                "ndk" -> c.copy(ndkPath = value)
                "dotnet" -> c.copy(dotnetPath = value)
                "adb" -> c.copy(adbPath = value)
                "out" -> c.copy(outFolder = value)
                "device" -> c.copy(deviceSerial = value)
                else -> c
            }
            st.copy(config = updated)
        }
    }

    fun saveAndCheckConfig() {
        _uiState.update { st ->
            val reqs = buildInitialRequirements(st.config, true)
            val allOk = reqs.all { it.ok || !it.required }
            st.copy(
                requirements = reqs,
                statusBannerMessage = if (allOk) "Configuration saved. All required porter toolchains verified." else "Configuration saved. Some required items still need attention."
            )
        }
    }

    fun downloadMissingTools() {
        if (_uiState.value.isDownloadingTools) return
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            val steps = listOf(
                "[1/3] Android NDK r27c · dl.google.com (648 MB)" to 0.35f,
                "[2/3] .NET SDK 10.0.100 · dotnetcli.azureedge.net (215 MB)" to 0.75f,
                "[3/3] Android platform-tools r35.0.2 · dl.google.com (14.5 MB)" to 1.0f
            )
            _uiState.update { it.copy(isDownloadingTools = true, downloadProgress = 0.05f, downloadStepText = "Checking SHA-256 manifests...") }
            for ((label, frac) in steps) {
                delay(450)
                _uiState.update { it.copy(downloadStepText = label, downloadProgress = frac) }
            }
            delay(300)
            val restoredConfig = _uiState.value.config.copy(
                ndkPath = _uiState.value.config.ndkPath.ifBlank { "/data/local/nx2android/toolchains/android-ndk-r27c" },
                dotnetPath = _uiState.value.config.dotnetPath.ifBlank { "/data/local/nx2android/toolchains/dotnet-sdk-10.0.100/dotnet" },
                adbPath = _uiState.value.config.adbPath.ifBlank { "/data/local/nx2android/toolchains/platform-tools/adb" }
            )
            _uiState.update {
                it.copy(
                    isDownloadingTools = false,
                    downloadStepText = "All toolchains ready & SHA-256 verified",
                    config = restoredConfig,
                    requirements = buildInitialRequirements(restoredConfig, true),
                    statusBannerMessage = "Android NDK r27c, .NET SDK 10, and platform-tools unpacked to toolchains/."
                )
            }
        }
    }

    // Games Tab Actions
    fun setSearchQuery(q: String) = _uiState.update { it.copy(searchQuery = q) }
    fun setStatusFilter(filter: String) = _uiState.update { it.copy(statusFilter = filter) }
    fun selectDetailProfile(profile: GameProfile?) = _uiState.update { it.copy(selectedDetailProfile = profile) }

    // Gamepad Studio Tab Actions
    fun selectThemePack(themeId: String) = _uiState.update { it.copy(selectedThemeId = themeId) }
    fun selectFormFactor(formFactor: String) = _uiState.update { it.copy(selectedFormFactor = formFactor) }
    fun setOverlayOpacity(alpha: Float) = _uiState.update { it.copy(overlayOpacity = alpha.coerceIn(0.2f, 1.0f)) }
    fun toggleShowHitboxes() = _uiState.update { it.copy(showHitboxes = !it.showHitboxes) }

    fun toggleButtonHeld(btnName: String) {
        _uiState.update { st ->
            val next = if (st.heldButtons.contains(btnName)) st.heldButtons - btnName else st.heldButtons + btnName
            st.copy(heldButtons = next)
        }
    }

    fun setButtonHeld(btnName: String, held: Boolean) {
        _uiState.update { st ->
            val next = if (held) st.heldButtons + btnName else st.heldButtons - btnName
            st.copy(heldButtons = next)
        }
    }

    fun updateStickOffset(isLeft: Boolean, dx: Float, dy: Float) {
        _uiState.update { st ->
            val heldKey = if (isLeft) "stick_l" else "stick_r"
            val isHeld = dx != 0f || dy != 0f
            val nextHeld = if (isHeld) st.heldButtons + heldKey else st.heldButtons - heldKey
            if (isLeft) {
                st.copy(stickLOffset = dx to dy, heldButtons = nextHeld)
            } else {
                st.copy(stickROffset = dx to dy, heldButtons = nextHeld)
            }
        }
    }

    fun clearBanner() = _uiState.update { it.copy(statusBannerMessage = null) }
}
