PS C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game> cd "C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game"
PS C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game>
PS C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game> $dmDir = (Resolve-Path ".\nx2android\nxnative\dm").Path
PS C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game> $bpy = Join-Path $dmDir "android\build.py"
PS C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game> $hmv = Join-Path $dmDir "host\hle_movie.c"
PS C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game>
PS C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game> # 1. Agregar los stubs completos de vpx (100% ASCII) en hle_movie.c
PS C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game> $c = Get-Content $hmv -Raw
PS C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game> $vpxStubs = @"
>> typedef struct { int dummy; } vpx_codec_ctx_t;
>> typedef struct { unsigned int d_w, d_h; unsigned char *planes[4]; int stride[4]; int range, cs, fmt; } vpx_image_t;
>> typedef struct { unsigned int threads, w, h; } vpx_codec_dec_cfg_t;
>> typedef int vpx_codec_err_t;
>> typedef const void *vpx_codec_iter_t;
>> enum { VPX_CR_FULL_RANGE = 1, VPX_CS_UNKNOWN = 0, VPX_CS_BT_709 = 2, VPX_IMG_FMT_I420 = 0x102, VP9D_SET_ROW_MT = 1 };
>> static inline const void *vpx_codec_vp9_dx(void) { return NULL; }
>> static inline vpx_codec_err_t vpx_codec_dec_init(vpx_codec_ctx_t *c, const void *i, const vpx_codec_dec_cfg_t *cfg, long f) { (void)c; (void)i; (void)cfg; (void)f; return -1; }
>> static inline const char *vpx_codec_err_to_string(vpx_codec_err_t e) { (void)e; return "no libvpx"; }
>> static inline int vpx_codec_control(vpx_codec_ctx_t *c, int k, int v) { (void)c; (void)k; (void)v; return 0; }
>> static inline const char *vpx_codec_version_str(void) { return "none"; }
>> static inline int vpx_codec_decode(vpx_codec_ctx_t *c, const uint8_t *d, unsigned int sz, void *u, long dl) { (void)c; (void)d; (void)sz; (void)u; (void)dl; return -1; }
>> static inline const char *vpx_codec_error(vpx_codec_ctx_t *c) { (void)c; return "no libvpx"; }
>> static inline const char *vpx_codec_error_detail(vpx_codec_ctx_t *c) { (void)c; return NULL; }
>> static inline vpx_image_t *vpx_codec_get_frame(vpx_codec_ctx_t *c, vpx_codec_iter_t *i) { (void)c; (void)i; return NULL; }
>> static inline void vpx_codec_destroy(vpx_codec_ctx_t *c) { (void)c; }
>> "@
PS C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game>
PS C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game> if ($c -notmatch "vpx_codec_dec_cfg_t") {
>>     $c = $c.Replace("typedef struct { int dummy; } vpx_codec_ctx_t;", $vpxStubs)
>>     [System.IO.File]::WriteAllText($hmv, $c)
>> }
PS C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game>
PS C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game> # 2. Compilar hle_movie.c, enlazar libdmhost.so y crear NieRAutomata.apk
PS C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game> $ndkDir = Get-ChildItem "$env:LOCALAPPDATA\Android\Sdk\ndk" -Directory | Sort-Object Name -Descending | Select-Object -First 1
PS C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game> python $bpy --game 0100B8E016F76000 --ndk $ndkDir.FullName --lib --out .\build_nier
--- host\hle_movie.c
C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game\nx2android\nxnative\dm\host\hle_movie.c:238:27: error: unknown type name 'vpx_image_t'
  238 | static void md_nv12(const vpx_image_t *im, uint8_t *dst, int w, int h) {
      |                           ^
C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game\nx2android\nxnative\dm\host\hle_movie.c:252:27: error: unknown type name 'vpx_image_t'
  252 | static void md_rgba(const vpx_image_t *im, uint8_t *dst, int w, int h) {
      |                           ^
C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game\nx2android\nxnative\dm\host\hle_movie.c:253:29: error: use of undeclared identifier 'VPX_CR_FULL_RANGE'
  253 |     int full = im->range == VPX_CR_FULL_RANGE, b709 = im->cs == VPX_CS_BT_709 || (im->cs == VPX_CS_UNKNOWN && h >= 720);
      |                             ^~~~~~~~~~~~~~~~~
C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game\nx2android\nxnative\dm\host\hle_movie.c:253:65: error: use of undeclared identifier 'VPX_CS_BT_709'
  253 |     int full = im->range == VPX_CR_FULL_RANGE, b709 = im->cs == VPX_CS_BT_709 || (im->cs == VPX_CS_UNKNOWN && h >= 720);
      |                                                                 ^~~~~~~~~~~~~
C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game\nx2android\nxnative\dm\host\hle_movie.c:253:93: error: use of undeclared identifier 'VPX_CS_UNKNOWN'
  253 |     int full = im->range == VPX_CR_FULL_RANGE, b709 = im->cs == VPX_CS_BT_709 || (im->cs == VPX_CS_UNKNOWN && h >= 720);
      |                                                                                             ^~~~~~~~~~~~~~
C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game\nx2android\nxnative\dm\host\hle_movie.c:274:9: error: unknown type name 'vpx_codec_dec_cfg_t'; did you mean 'vpx_codec_ctx_t'?
  274 |         vpx_codec_dec_cfg_t cfg = { .threads = (unsigned)md_threads(), .w = (unsigned)d->w, .h = (unsigned)d->h };
      |         ^~~~~~~~~~~~~~~~~~~
      |         vpx_codec_ctx_t
C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game\nx2android\nxnative\dm\host\hle_movie.c:16:31: note: 'vpx_codec_ctx_t' declared here
   16 | typedef struct { int dummy; } vpx_codec_ctx_t;
      |                               ^
C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game\nx2android\nxnative\dm\host\hle_movie.c:274:38: error: field designator 'threads' does not refer to any field in type 'vpx_codec_ctx_t'
  274 |         vpx_codec_dec_cfg_t cfg = { .threads = (unsigned)md_threads(), .w = (unsigned)d->w, .h = (unsigned)d->h };
      |                                     ~^~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game\nx2android\nxnative\dm\host\hle_movie.c:274:73: error: field designator 'w' does not refer to any field in type 'vpx_codec_ctx_t'
  274 |         vpx_codec_dec_cfg_t cfg = { .threads = (unsigned)md_threads(), .w = (unsigned)d->w, .h = (unsigned)d->h };
      |                                                                        ~^~~~~~~~~~~~~~~~~~
C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game\nx2android\nxnative\dm\host\hle_movie.c:274:94: error: field designator 'h' does not refer to any field in type 'vpx_codec_ctx_t'
  274 |         vpx_codec_dec_cfg_t cfg = { .threads = (unsigned)md_threads(), .w = (unsigned)d->w, .h = (unsigned)d->h };
      |                                                                                             ~^~~~~~~~~~~~~~~~~~
C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game\nx2android\nxnative\dm\host\hle_movie.c:275:9: error: unknown type name 'vpx_codec_err_t'; did you mean 'vpx_codec_ctx_t'?
  275 |         vpx_codec_err_t e = vpx_codec_dec_init(&d->vpx, vpx_codec_vp9_dx(), &cfg, 0);
      |         ^~~~~~~~~~~~~~~
      |         vpx_codec_ctx_t
C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game\nx2android\nxnative\dm\host\hle_movie.c:16:31: note: 'vpx_codec_ctx_t' declared here
   16 | typedef struct { int dummy; } vpx_codec_ctx_t;
      |                               ^
C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game\nx2android\nxnative\dm\host\hle_movie.c:275:29: error: call to undeclared function 'vpx_codec_dec_init'; ISO C99 and later do not support implicit function declarations [-Wimplicit-function-declaration]
  275 |         vpx_codec_err_t e = vpx_codec_dec_init(&d->vpx, vpx_codec_vp9_dx(), &cfg, 0);
      |                             ^
C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game\nx2android\nxnative\dm\host\hle_movie.c:275:57: error: call to undeclared function 'vpx_codec_vp9_dx'; ISO C99 and later do not support implicit function declarations [-Wimplicit-function-declaration]
  275 |         vpx_codec_err_t e = vpx_codec_dec_init(&d->vpx, vpx_codec_vp9_dx(), &cfg, 0);
      |                                                         ^
C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game\nx2android\nxnative\dm\host\hle_movie.c:275:25: error: initializing 'vpx_codec_ctx_t' with an expression of incompatible type 'int'
  275 |         vpx_codec_err_t e = vpx_codec_dec_init(&d->vpx, vpx_codec_vp9_dx(), &cfg, 0);
      |                         ^   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game\nx2android\nxnative\dm\host\hle_movie.c:276:9: error: statement requires expression of scalar type ('vpx_codec_ctx_t' invalid)
  276 |         if (e) { LOG("[RED] movie: libvpx failed to create the VP9 decoder: %s", vpx_codec_err_to_string(e)); return 0; }
      |         ^   ~
C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game\nx2android\nxnative\dm\host\hle_movie.c:276:82: error: call to undeclared function 'vpx_codec_err_to_string'; ISO C99 and later do not support implicit function declarations [-Wimplicit-function-declaration]
  276 |         if (e) { LOG("[RED] movie: libvpx failed to create the VP9 decoder: %s", vpx_codec_err_to_string(e)); return 0; }
      |                                                                                  ^
C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game\nx2android\nxnative\dm\host\hle_movie.c:277:9: error: call to undeclared function 'vpx_codec_control'; ISO C99 and later do not support implicit function declarations [-Wimplicit-function-declaration]
  277 |         vpx_codec_control(&d->vpx, VP9D_SET_ROW_MT, 1);
      |         ^
C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game\nx2android\nxnative\dm\host\hle_movie.c:277:36: error: use of undeclared identifier 'VP9D_SET_ROW_MT'
  277 |         vpx_codec_control(&d->vpx, VP9D_SET_ROW_MT, 1);
      |                                    ^~~~~~~~~~~~~~~
C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game\nx2android\nxnative\dm\host\hle_movie.c:279:59: error: call to undeclared function 'vpx_codec_version_str'; ISO C99 and later do not support implicit function declarations [-Wimplicit-function-declaration]
  279 |         LOG("[movie] libvpx %s: VP9 decoder, %d threads", vpx_codec_version_str(), cfg.threads);
      |                                                           ^
C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game\nx2android\nxnative\dm\host\hle_movie.c:279:88: error: no member named 'threads' in 'vpx_codec_ctx_t'
  279 |         LOG("[movie] libvpx %s: VP9 decoder, %d threads", vpx_codec_version_str(), cfg.threads);
      |                                                                                    ~~~ ^
fatal error: too many errors emitted, stopping now [-ferror-limit=]
20 errors generated.

compiled 67 sources, 1 failed: host\hle_movie.c
PS C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game> python (Join-Path $dmDir "android\apk\mkapk.py") .\build_nier\libdmhost.so .\NieRAutomata.apk --package org.nx2a.nierautomata --label "NieR: Automata" --lib dmhost --landscape
Traceback (most recent call last):
  File "C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game\nx2android\nxnative\dm\android\apk\mkapk.py", line 209, in <module>
    if __name__ == '__main__': main()
                               ~~~~^^
  File "C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game\nx2android\nxnative\dm\android\apk\mkapk.py", line 200, in main
    entries = [('AndroidManifest.xml', axml(MANIFEST), 4), (f'lib/arm64-v8a/lib{LIB}.so', open(so, 'rb').read(), 16384)]
                                                                                          ~~~~^^^^^^^^^^
FileNotFoundError: [Errno 2] No such file or directory: '.\\build_nier\\libdmhost.so'
PS C:\Users\jjose\OneDrive\Escritorio\Super-Mario-3D-World-Bowsers-Fury-Base-Game>
