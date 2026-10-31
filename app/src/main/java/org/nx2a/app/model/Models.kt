package org.nx2a.app.model

data class GameBuild(
    val versionCode: String,
    val displayVersion: String,
    val mainBuildId: String,
    val sdkBuildId: String
)

data class GameProfile(
    val titleId: String,
    val programId: String,
    val name: String,
    val ipaName: String,
    val status: String, // playable, wip, boots, experimental, ios only
    val tested: String,
    val androidPackage: String,
    val orientation: String,
    val modules: List<String>,
    val romfsMarker: String,
    val zipStore: String,
    val memReserve: String,
    val memAlias: String,
    val memStack: String,
    val appPool: String,
    val fpsTarget: String,
    val presentInterval: String,
    val opMode: String,
    val padLayout: String,
    val shaderNote: String,
    val shaderGroups: String,
    val shaderCount: Int,
    val builds: List<GameBuild>,
    val defaultPackageVer: String,
    val hasPatches: Boolean,
    val patchesSource: String,
    val rawGameTsv: String,
    val rawAndroidTsv: String,
    val themePack: String,
    val imageAssetPath: String?,
    val releaseOutputSummary: String?,
    val releasePortTime: String?,
    val releaseBundle: String?
) {
    val isAndroidSupported: Boolean
        get() = status in listOf("playable", "wip", "boots")
}

data class StageProgress(
    val index: Int,
    val total: Int,
    val key: String,
    val name: String,
    val state: String, // "pending", "running", "done", "failed"
    val stepDetail: String = "",
    val summary: String = "",
    val elapsedSeconds: Int = 0
)

data class PortResult(
    val gameName: String,
    val titleId: String,
    val apkPath: String,
    val dataFolderPath: String,
    val apkSizeMb: Double,
    val dataSizeGb: Double,
    val shaderCount: Int,
    val packageName: String,
    val elapsedTotalSeconds: Int,
    val installedOnDevice: Boolean
)

data class ToolRequirement(
    val id: String,
    val name: String,
    val version: String,
    val sizeBytes: Long,
    val hostDomain: String,
    val licenseUrl: String,
    val required: Boolean,
    val ok: Boolean,
    val detail: String,
    val fixHint: String
)

data class PorterConfig(
    val keysPath: String = "/sdcard/SwitchKeys/prod.keys",
    val ndkPath: String = "/data/local/nx2android/toolchains/android-ndk-r27c",
    val dotnetPath: String = "/data/local/nx2android/toolchains/dotnet-sdk-10.0.100/dotnet",
    val adbPath: String = "/data/local/nx2android/toolchains/platform-tools/adb",
    val outFolder: String = "/sdcard/nx2android/ports",
    val deviceSerial: String = "HA1Z98P4"
)

data class ConnectedDevice(
    val serial: String,
    val modelInfo: String,
    val gpuInfo: String,
    val supportsBcn: Boolean
)

data class OverlayControl(
    val layout: String, // "tablet" or "phone"
    val name: String,
    val kind: String, // stick_l, stick_r, button, dpad, menu
    val anchor: Float,
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    val buttonBit: String,
    val label: String
)

data class ThemeSprite(
    val layout: String,
    val control: String,
    val part: String,
    val state: String, // idle, held, knob, decor
    val atlasX: Int,
    val atlasY: Int,
    val w: Int,
    val h: Int,
    val offsetX: Float,
    val offsetY: Float
)

data class ThemeLayoutMeta(
    val layout: String,
    val designW: Int,
    val designH: Int,
    val atlasFile: String,
    val texelsPerCssPx: Int,
    val knobTravelCssPx: Int
)

data class GamepadThemePack(
    val id: String,
    val displayName: String,
    val titleId: String,
    val alpha: Float,
    val layouts: Map<String, ThemeLayoutMeta>,
    val sprites: List<ThemeSprite>
)
