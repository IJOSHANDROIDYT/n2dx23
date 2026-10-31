# CONTEXTO DEL PROYECTO Y ESTADO ACTUAL (NIER: AUTOMATA EN NX2ANDROID)

Tienes conectado el repositorio `nx2android` (`nxnative`). Estoy ejecutando **NieR: Automata The End of YoRHa Edition** (`0100B8E016F76000` / perfil base `010025C0140F8000`, paquete `org.nx2a.nierautomata`) en un **Samsung Galaxy S23 Ultra** (Snapdragon 8 Gen 2, GPU Adreno 740, driver Vulkan 1.3 `0x802a4001`).

## 1. Lo que YA está instalado y funcionando en el teléfono (NO re-subir RomFS ni shaders)
- **Datos en el teléfono:** Los **10.95 GB de `RomFS`** (681 archivos `.cpk` / `.usm`) y el bundle **`shaders.dmsv` (40.6 MB, 1,225 shaders)** ya están instalados y verificados en `/sdcard/Android/data/org.nx2a.nierautomata/files/`.
- **Flujo de actualización:** Solo necesitamos modificar el código fuente C del host (`libdmhost.so`), recompilar con Android NDK r30 (`build.py`), re-empaquetar **`NieRAutomata.apk`** (`mkapk.py`) e instalarlo encima con `adb install -r NieRAutomata.apk` (tarda 5 segundos).
- **Rendimiento y estabilidad actual:**
  - El juego corre a **30 FPS estables (`33.3 ms`, `present_interval 2`)** sin calentarse.
  - Los 1,225 shaders cargan sin fallos: `skipped (no program 0, no target 0, no pipeline 0), pipelines 220`.
  - La interfaz (HUD, barra de vida `"Nv: 1"`, subtítulos, menú principal), los proyectiles enemigos, el cielo/nubes volumétricas de la primera misión y el fuego naranja emisivo de los propulsores de la nave se renderizan perfectamente.

---

## 2. EL PROBLEMA A RESOLVER: Geometría 3D opaca / G-Buffer / Iluminación en NEGRO y Crash al atacar

A pesar de que el cielo, las nubes, las partículas emisivas y el HUD se ven perfectos:
1. **En la fase de vuelo inicial:** El chasis metálico de la nave de 2B se ve como una **silueta completamente negra** (solo brillan las luces naranjas de las turbinas y las luces roja/verde de las alas, pero la malla 3D no recibe color difuso ni iluminación ambiental/especular).
2. **Al cambiar de zona (entrar a la fábrica) y cuando 2B baja de la nave al suelo:** Todo el escenario interior, el piso, 2B, 9S y los enemigos se ven **completamente negros**.
3. **Al presionar el botón de golpear (ataque con espada) en el suelo:** El juego sufre un crash inmediato.

---

## 3. Métricas reales extraídas de `/sdcard/Android/data/org.nx2a.nierautomata/files/log.txt`

```text
[dm] [vk] Adreno (TM) 740, Vulkan 1.3, driver 0x802a4001; BC yes, ASTC yes, geometry shaders yes, nullDescriptor yes, custom border colours yes, depth clip control yes, transform feedback yes
[dm] [nvn] WindowInitialize swap chain 3 textures, interval 1, first 1920x1080 format 0x25
[dm] [vk] shader bundle /storage/emulated/0/Android/data/org.nx2a.nierautomata/files/shaders.dmsv: 1225 shaders, 40.6 MB
[dm] [nvn] [RED] SetRenderEnableConditional: the host draws anyway for now
[dm] [nvn] timing up to frame 9900: frame time p50 33.48 ms; GPU p50 23.27 ms
[dm] [nvn] vertex bindings up to frame 9900: per frame address lookups 0 / reused 0, vertex table writes 0 / skipped 0
[dm] [nvn] packaged textures: first uploads 131..161, re-uploads on pool content change 0; write-protection faults 0, pages unprotected before host writes 0; missed 0
[dm] [vk] replay up to present 9600: draws 4055025, skipped (no program 0, no target 0, no pipeline 0), passes 382828, clears folded 68439, dispatches 53898, pipelines 220; presents shown 9599, no image 0, never drawn 0; uniform updates landed 0 (superseded 0, shadows 0, CPU reads racing a GPU write 0), same targets kept 25207, most draws in one pass 324
[dm] [vk] replay per frame (last 600): 729 draws, replay 7.49 ms, GPU busy 22.02 ms; texture uploads 0.1 (0.01 MB, 0.01 ms); descriptor sets reused 2501460, new 1334907
```

---

## 4. Cambios que ya se probaron en `nxnative/dm/android/` (Estado actual del código)

1. **`gfx_vk_exec.c` (MRT Write Mask):** Se dejó la regla estándar `int written = !fsh || ((fsh->out_locs >> i) & 1);` en `prepare_draw` para que los attachments no escritos por el fragment shader tengan `wmask = 0`.
2. **`gfx_vk_tex.c` (`packaged_tex` y `vk_tex`):**
   - Originalmente `first uploads` daba `0` porque NieR carga las texturas desde los `.cpk` a memoria tiled sin llamar a `nvnTextureBuilderSetPackagedTextureLayout` (`t->has_layout == 0`).
   - Se modificó `packaged_tex()` y `vk_tex()` para subir con `pkg_upload()` toda textura con `t->mem != NULL` que no sea Render Target (`!im->gpu_w && !f->depth && !f->stencil`). Con eso aparecieron las nubes, el cielo y las fuentes, subiendo `first uploads` a `131–161`, pero `re-uploads` sigue en `0` y los modelos 3D / interiores siguen negros.
   - Se desactivó `tex_dirty_upload` (`mprotect`) porque causaba 5,318 page faults (`SIGSEGV`) y tumbaba el framerate a 15 FPS.

---

## 5. TU TAREA: Diagnosticar en el repositorio por qué el G-Buffer / Iluminación de los modelos 3D sale negro

Por favor analiza a fondo los archivos del repositorio (`nxnative/dm/android/gfx_vk_tex.c`, `gfx_vk_exec.c`, `gfx_vk_shader.c`, `nxnative/dm/gfx/nvn/hle_nvn.c`, y los perfiles en `nxnative/dm/games/`) e investiga estas 5 hipótesis principales:

1. **Aliasing de memoria entre Render Targets / Storage Images / Sampled Textures (`akey` en `vk_tex` y `alias_check` en `gfx_vk_tex.c`):**
   - En `vk_tex()`, la llave `akey` de `g_by_key` incluye `k.d = t->d` y para formatos de color conserva `NVN_TEXTURE_FLAGS_IMAGE` en `k.d.flags`.
   - Si NieR: Automata calcula la iluminación diferida, el Light Buffer, SSAO o mapas IBL con Compute Shaders (`dispatches 53898`) sobre un descriptor `tex_t` con `NVN_TEXTURE_FLAGS_IMAGE` (o con distinto formato `UNORM` vs `SRGB`, o distinto número de `levels`/vistas 2D vs Cube) y luego el pase de iluminación/materiales lo muestrea a través de otro `tex_t` sobre el mismo `pool + off` sin `NVN_TEXTURE_FLAGS_IMAGE`, `vk_tex()` crea **dos `VkImage` separadas** y la vista muestreada lee una imagen vacía (negra).
   - Revisa cómo se resolvió esto para *Xenoblade Chronicles 3* o *Bayonetta 3* (`01004A4010FEA000`, mismo motor PlatinumGames) en `gfx_vk_tex.c`.

2. **`pkg_upload` sobrescribiendo Render Targets o texturas llenadas por GPU (`im->gpu_w`):**
   - En `vk_tex_view()`, `vk_tex(t)` se llama **antes** de asignar `if (as_attachment) im->gpu_w = 1;`.
   - Además, verifica si `OP_COPY_BUF_TO_TEX`, `OP_COPY_TEX_TO_TEX`, `OP_CLEAR_COLOR` o los Compute Shaders que escriben en Storage Images marcan `im->gpu_w = 1` antes de llamar a `vk_tex(t)`. Si `im->gpu_w` es `0` la primera vez que se crea la `VkImage`, nuestro cambio en `vk_tex()` podría estar llamando a `pkg_upload()` sobre un Render Target / G-Buffer / Cubemap de iluminación y pisándolo con datos basura o ceros de `t->mem`.

3. **Streaming de texturas desde `.cpk` sobre el mismo pool y caché de Descriptor Sets (`g_tvc` / `g_smemo` / `g_tex_gen`):**
   - Cuando NieR cambia de zona (del vuelo al interior de la fábrica) o descomprime texturas de modelos en hilos secundarios, `re-uploads on pool content change` da `0`.
   - En `gfx_vk_exec.c`, `g_tvc` y `set_memo_get` cachean las `VkImageView` y los `VkDescriptorSet` mientras `g_tex_gen` no cambie, por lo que `vk_tex()` ni siquiera vuelve a ejecutarse para texturas ya vinculadas.

4. **Lectura del Depth/Stencil Buffer o Constant Buffers (`uniform updates landed 0`) en el pase de iluminación diferida:**
   - Nota que el log reporta `uniform updates landed 0 (superseded 0, shadows 0)` y `vertex bindings: address lookups 0`. Verifica cómo actualiza PlatinumGames sus Uniform Buffers / SSBOs / Texture Buffers (`NVN_TEXTURE_TARGET_BUFFER`) para las matrices de iluminación y materiales de modelos 3D, y si los lee desde un pool `zero-copy` que requiere invalidación/coherencia o un `VkBufferView` que esté cayendo en `vk_dummy_view`.

5. **Crash al golpear con la espada en el suelo:**
   - Identifica qué subsistema se dispara al presionar el botón de ataque de 2B (Compute Shader de partículas/trail de espada, lectura de buffer en CPU, vibración o audio) y cómo protegerlo.

---

## 6. Formato de respuesta esperado
1. Explícame exactamente qué encontraste en el código del repositorio que causa que las mallas 3D y las zonas nuevas se vean negras en NieR: Automata.
2. Entrégame los bloques de código C exactos (o el patch/script de PowerShell) listos para aplicar sobre `nxnative`, seguidos del comando exacto para recompilar `libdmhost.so`, re-empaquetar `NieRAutomata.apk` e instalarlo con `adb install -r NieRAutomata.apk`.