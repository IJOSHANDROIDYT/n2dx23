package org.nx2a.app.data

import android.content.Context
import org.nx2a.app.model.GameBuild
import org.nx2a.app.model.GameProfile
import org.nx2a.app.model.GamepadThemePack
import org.nx2a.app.model.OverlayControl
import org.nx2a.app.model.ThemeLayoutMeta
import org.nx2a.app.model.ThemeSprite

class NxRepository(private val context: Context) {

    private val imageMapping = mapOf(
        "01003FC01670C000" to "images/13-sentinels.jpg",
        "0100300012F2A000" to "images/advance-wars-1-2-re-boot-camp.jpg",
        "010040801A4C8000" to "images/blasphemous-2.jpg",
        "01009BF0072D4000" to "images/captain-toad-treasure-tracker.jpg",
        "01002B30028F6000" to "images/celeste.jpg",
        "01009D901BC56000" to "images/donkey-kong-country-returns-hd.jpg",
        "0100005027A18000" to "images/graveyard-keeper-2.jpg",
        "0100A00019DE0000" to "images/hades-ii.jpg",
        "010013C00E930000" to "images/hollow-knight-silksong.jpg",
        "0100B99019412000" to "images/mario-vs-donkey-kong.jpg",
        "010043B013C5C000" to "images/neo-the-world-ends-with-you.jpg",
        "010015100B514000" to "images/super-mario-bros-wonder.jpg",
        "010006E0230AC000" to "images/tales-of-eternia-remastered.jpg",
        "010069401ADB8000" to "images/unicorn-overlord.jpg",
        "010054B01AD92000" to "images/unicorn-overlord.jpg",
        "0100701015846000" to "images/unpacking.jpg",
        "0100FD501F95A000" to "images/viractal.jpg",
        "010074F013262000" to "images/xenoblade-chronicles-3.jpg"
    )

    data class ReleaseCheckMeta(val output: String, val time: String, val bundle: String)

    private val releaseCheckMap = mapOf(
        "01003FC01670C000" to ReleaseCheckMeta("APK 2 MB, data 4.4 GB", "1 min (from scratch)", "v0.1.2-71-g27ea47f"),
        "0100300012F2A000" to ReleaseCheckMeta("APK 2 MB, data 7.2 GB", "4 min (from scratch)", "v0.1.2-71-g27ea47f"),
        "010040801A4C8000" to ReleaseCheckMeta("APK 2 MB, data 1.9 GB, 1 DLC", "3 min (from scratch)", "v0.1.2-71-g27ea47f"),
        "01009BF0072D4000" to ReleaseCheckMeta("APK 2 MB, data 1.9 GB, 2 DLC", "2 min (from scratch)", "v0.1.2-71-g27ea47f"),
        "01002B30028F6000" to ReleaseCheckMeta("APK 2 MB, data 1.2 GB", "2 min (from scratch)", "v0.1.2-71-g27ea47f"),
        "01009D901BC56000" to ReleaseCheckMeta("APK 2 MB, data 13.1 GB", "6 min (from scratch)", "v0.1.2-71-g27ea47f"),
        "0100005027A18000" to ReleaseCheckMeta("APK 1 MB, data 1.4 GB, 1 DLC", "4 min (from scratch)", "7d2b374"),
        "0100A00019DE0000" to ReleaseCheckMeta("APK 2 MB, data 4.7 GB", "1 min (from scratch)", "v0.1.0-5-gbd68d8b"),
        "010013C00E930000" to ReleaseCheckMeta("APK 2 MB, data 6.5 GB", "5 min (from scratch)", "v0.1.2-71-g27ea47f"),
        "0100B99019412000" to ReleaseCheckMeta("APK 2 MB, data 1.6 GB", "4 min (from scratch)", "v0.1.2-71-g27ea47f"),
        "010043B013C5C000" to ReleaseCheckMeta("APK 2 MB, data 6.7 GB, 2 DLC", "3 min (from scratch)", "e89aecb"),
        "010015100B514000" to ReleaseCheckMeta("APK 2 MB, data 4.0 GB", "4 min (from scratch)", "v0.1.2-71-g27ea47f"),
        "010006E0230AC000" to ReleaseCheckMeta("APK 2 MB, data 5.6 GB", "4 min (from scratch)", "v0.1.1-1-g8372959"),
        "010069401ADB8000" to ReleaseCheckMeta("APK 2 MB, data 3.5 GB, 2 DLC", "1 min (from scratch)", "v0.1.2-71-g27ea47f"),
        "0100701015846000" to ReleaseCheckMeta("APK 2 MB, data 1.0 GB", "2 min (from scratch)", "7d2b374"),
        "0100FD501F95A000" to ReleaseCheckMeta("APK 2 MB, data 2.3 GB", "5 min (from scratch)", "7d2b374"),
        "010074F013262000" to ReleaseCheckMeta("APK 2 MB, data 19.2 GB, 4 DLC", "4 min (from scratch)", "7d2b374")
    )

    private fun readAssetText(path: String): String? {
        return try {
            context.assets.open(path).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            null
        }
    }

    fun loadThemeTable(): Map<String, Pair<String, String>> {
        // returns titleId -> (packName, gameLabel)
        val result = mutableMapOf<String, Pair<String, String>>()
        val content = readAssetText("themes/themes.tsv") ?: return result
        for (line in content.lineSequence()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue
            val cols = line.split("\t")
            if (cols.size >= 2) {
                val tid = cols[0].trim()
                val pack = cols[1].trim()
                val label = if (cols.size >= 3) cols[2].trim() else pack
                result[tid] = pack to label
            }
        }
        return result
    }

    fun loadAllGameProfiles(): List<GameProfile> {
        val themesByTitle = loadThemeTable()
        val gameDirs = try {
            context.assets.list("games")?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        val profiles = mutableListOf<GameProfile>()
        for (gid in gameDirs.sorted()) {
            val gameTsv = readAssetText("games/$gid/game.tsv") ?: continue
            val androidTsv = readAssetText("games/$gid/android.tsv") ?: ""
            val patchesSource = readAssetText("games/$gid/patches.py") ?: ""
            val shaderCountStr = readAssetText("games/$gid/shader_count.txt")?.trim() ?: "0"
            val shaderCount = (shaderCountStr.toIntOrNull() ?: 1).coerceAtLeast(1) - 1

            val gameMap = mutableMapOf<String, String>()
            val builds = mutableListOf<GameBuild>()

            for (line in gameTsv.lineSequence()) {
                val t = line.trim()
                if (t.isEmpty() || t.startsWith("#")) continue
                val parts = line.split("\t")
                if (parts.isEmpty()) continue
                val key = parts[0].trim()
                if (key == "build" && parts.size >= 3) {
                    val ver = parts[1].trim()
                    val disp = parts[2].trim()
                    var mainBid = ""
                    var sdkBid = ""
                    for (i in 3 until parts.size) {
                        val kv = parts[i].trim().split("=", limit = 2)
                        if (kv.size == 2) {
                            if (kv[0] == "main") mainBid = kv[1]
                            if (kv[0] == "sdk") sdkBid = kv[1]
                        }
                    }
                    builds.add(GameBuild(ver, disp, mainBid, sdkBid))
                } else if (parts.size >= 2) {
                    gameMap[key] = parts.drop(1).joinToString(" ") { it.trim() }
                }
            }

            val androidMap = mutableMapOf<String, String>()
            for (line in androidTsv.lineSequence()) {
                val t = line.trim()
                if (t.isEmpty() || t.startsWith("#")) continue
                val parts = line.split("\t")
                if (parts.size >= 2) {
                    androidMap[parts[0].trim()] = parts.drop(1).joinToString(" ") { it.trim() }
                }
            }

            val titleId = gameMap["title_id"] ?: gid
            val name = gameMap["name"] ?: titleId
            val status = androidMap["status"] ?: "ios only"
            val tested = androidMap["tested"] ?: ""
            val pkg = androidMap["package"] ?: "org.nx2a.t${titleId.lowercase()}"
            val orient = androidMap["orientation"] ?: "landscape"
            val themePack = themesByTitle[titleId]?.first ?: "ergo"
            val relCheck = releaseCheckMap[titleId]

            profiles.add(
                GameProfile(
                    titleId = titleId,
                    programId = gameMap["program_id"] ?: titleId,
                    name = name,
                    ipaName = gameMap["ipa"] ?: "${titleId}.ipa",
                    status = status,
                    tested = tested,
                    androidPackage = pkg,
                    orientation = orient,
                    modules = (gameMap["modules"] ?: "main sdk").split(" ").filter { it.isNotBlank() },
                    romfsMarker = gameMap["romfs_marker"] ?: "",
                    zipStore = gameMap["zip_store"] ?: "",
                    memReserve = gameMap["mem_reserve"] ?: "0x140000000",
                    memAlias = gameMap["mem_alias"] ?: "0xB8000000",
                    memStack = gameMap["mem_stack"] ?: "0x10000000",
                    appPool = gameMap["app_pool"] ?: "0xCD500000",
                    fpsTarget = gameMap["fps_target"] ?: "0",
                    presentInterval = gameMap["present_interval"] ?: "game",
                    opMode = gameMap["op_mode"] ?: "console",
                    padLayout = gameMap["pad_layout"] ?: "simple",
                    shaderNote = gameMap["shader_note"] ?: "$name shaders",
                    shaderGroups = gameMap["shader_groups"] ?: "",
                    shaderCount = shaderCount.coerceAtLeast(0),
                    builds = builds,
                    defaultPackageVer = gameMap["package"] ?: (builds.lastOrNull()?.versionCode ?: "v0"),
                    hasPatches = patchesSource.isNotBlank(),
                    patchesSource = patchesSource,
                    rawGameTsv = gameTsv,
                    rawAndroidTsv = androidTsv,
                    themePack = themePack,
                    imageAssetPath = imageMapping[titleId],
                    releaseOutputSummary = relCheck?.output,
                    releasePortTime = relCheck?.time,
                    releaseBundle = relCheck?.bundle
                )
            )
        }

        // Sort with Android supported games first, then alphabetically
        return profiles.sortedWith(
            compareByDescending<GameProfile> { it.isAndroidSupported }
                .thenByDescending { it.status == "experimental" }
                .thenBy { it.name.lowercase() }
        )
    }

    fun loadOverlayControls(): List<OverlayControl> {
        val controls = mutableListOf<OverlayControl>()
        val content = readAssetText("overlay_default.tsv") ?: return controls
        for (line in content.lineSequence()) {
            val t = line.trim()
            if (t.isEmpty() || t.startsWith("#")) continue
            val cols = line.split("\t")
            if (cols.size >= 10) {
                controls.add(
                    OverlayControl(
                        layout = cols[0].trim(),
                        name = cols[1].trim(),
                        kind = cols[2].trim(),
                        anchor = cols[3].trim().toFloatOrNull() ?: 0.5f,
                        x = cols[4].trim().toFloatOrNull() ?: 0f,
                        y = cols[5].trim().toFloatOrNull() ?: 0f,
                        w = cols[6].trim().toFloatOrNull() ?: 0.1f,
                        h = cols[7].trim().toFloatOrNull() ?: 0.1f,
                        buttonBit = cols[8].trim(),
                        label = cols[9].trim()
                    )
                )
            }
        }
        return controls
    }

    fun loadAllThemePacks(): List<GamepadThemePack> {
        val themesTable = loadThemeTable()
        val packToGame = mutableMapOf<String, Pair<String, String>>()
        for ((tid, pair) in themesTable) {
            packToGame[pair.first] = tid to pair.second
        }
        packToGame["ergo"] = "DEFAULT" to "Ergonomic Default (All Other Games)"

        val themeDirs = try {
            context.assets.list("themes")?.filter { it != "themes.tsv" } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        val packs = mutableListOf<GamepadThemePack>()
        for (packId in themeDirs.sorted()) {
            val tsv = readAssetText("themes/$packId/theme.tsv") ?: continue
            var alpha = 0.9f
            val layouts = mutableMapOf<String, ThemeLayoutMeta>()
            val sprites = mutableListOf<ThemeSprite>()

            for (line in tsv.lineSequence()) {
                val t = line.trim()
                if (t.isEmpty() || t.startsWith("#")) continue
                val c = line.split("\t")
                when (c[0].trim()) {
                    "alpha" -> if (c.size >= 2) alpha = c[1].trim().toFloatOrNull() ?: 0.9f
                    "layout" -> if (c.size >= 7) {
                        val lay = c[1].trim()
                        layouts[lay] = ThemeLayoutMeta(
                            layout = lay,
                            designW = c[2].trim().toIntOrNull() ?: 1216,
                            designH = c[3].trim().toIntOrNull() ?: 760,
                            atlasFile = c[4].trim(),
                            texelsPerCssPx = c[5].trim().toIntOrNull() ?: 2,
                            knobTravelCssPx = c[6].trim().toIntOrNull() ?: 34
                        )
                    }
                    "sprite" -> if (c.size >= 11) {
                        sprites.add(
                            ThemeSprite(
                                layout = c[1].trim(),
                                control = c[2].trim(),
                                part = c[3].trim(),
                                state = c[4].trim(),
                                atlasX = c[5].trim().toIntOrNull() ?: 0,
                                atlasY = c[6].trim().toIntOrNull() ?: 0,
                                w = c[7].trim().toIntOrNull() ?: 0,
                                h = c[8].trim().toIntOrNull() ?: 0,
                                offsetX = c[9].trim().toFloatOrNull() ?: 0f,
                                offsetY = c[10].trim().toFloatOrNull() ?: 0f
                            )
                        )
                    }
                }
            }

            val meta = packToGame[packId]
            packs.add(
                GamepadThemePack(
                    id = packId,
                    displayName = meta?.second ?: packId,
                    titleId = meta?.first ?: "",
                    alpha = alpha,
                    layouts = layouts,
                    sprites = sprites
                )
            )
        }

        return packs.sortedWith(
            compareBy<GamepadThemePack> { it.id != "smbwonder" && it.id != "neotwewy" && it.id != "hades2" }
                .thenBy { it.displayName }
        )
    }
}
