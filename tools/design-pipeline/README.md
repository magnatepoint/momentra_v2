# Momentra design pipeline (Personal v1.1)

One source of truth for Personal v1.1 design tokens, the life taxonomy and the Personal
icons/artwork, generated into iOS, Android and the backend. **Personal mode only**: Group,
Business and Circle keep their current themes.

## Sources (edit these)

| File | What it holds |
|---|---|
| `design/tokens/personal.tokens.json` | DTCG tokens: colors (Personal theme `#6C4EF2` on `#14121B`, life-area tints, mood/toast aliases), radius, space, size, shadow, Plus Jakarta Sans faces, type scale |
| `design/taxonomy/personal-taxonomy.v1.json` | 2 layers (NOW/BUILD), 8 life areas P1–P8, 48 subcategories P1.1–P8.6 (label, description, examples, Lucide icon, Figma node), 5 moods |
| `design/assets/personal/manifest.json` | Extra UI icons (Lucide keys + Figma nodes) and the 4 artwork masters |
| `design/assets/personal/artwork/*.jpg` | Artwork masters (Figma image fills, ≥1024 px; illustrations, not personal photos) |

Area tints: peach `#EABCA6` (P1, P2, P5), mint `#9AE0C7` (P3), sky `#A8CCF2` (P4, P7),
lavender `#C9BFFF` (P6, P8). The tokens expose `color.area.P1…P8` as aliases so one area can be
re-tinted without code changes. Subcategory glyphs take their parent area's tint.

## Run

```powershell
cd tools/design-pipeline
npm ci            # first time (project-local: lucide-static 0.577.0, svg2vectordrawable, sharp)
npm run all       # tokens + taxonomy + assets
npm run check     # CI: exits 1 if any generated file is missing or stale
```

`npm run tokens` needs no dependencies. Both scripts are idempotent: they only write files whose
content changed and delete stale generated files in the folders they own. Generated output is
committed. Line endings are compared CRLF/LF-insensitively, so `--check` passes on Windows with
`core.autocrlf=true`. Raster outputs are tracked in `assets.lock.json` (source hash + settings),
so re-running on another OS does not re-encode unchanged artwork.

## Outputs

| Platform | Output | Use |
|---|---|---|
| iOS | `momentra/momentra/Design/Generated/PersonalTokens.generated.swift` | `PersonalTokens.Colors/Radius/Space/Size/Shadow/Typography`, `Text(..).personalText(PersonalTokens.Typography.section)` (Dynamic Type via `relativeTo`) |
| iOS | `…/Design/Generated/PersonalLifeTaxonomy.generated.swift` | `PersonalLifeTaxonomy`, `PersonalLifeArea`, `PersonalLifeSubcategory`, `PersonalMood` |
| iOS | `…/Design/Generated/PersonalIcons.generated.swift` | `PersonalIcon.<key>.image`, `PersonalArtwork.<key>.image` |
| iOS | `Assets.xcassets/PersonalTokens/*.colorset` | named colors `PersonalTokens/<Name>` (dark = light until a light theme exists) |
| iOS | `Assets.xcassets/PersonalIcons/<lucide-key>.imageset` | template SVG, preserves vector |
| iOS | `Assets.xcassets/PersonalArtwork/<key>.imageset` | @2x/@3x JPEG |
| Android | `apk/app/src/main/java/com/example/momentra/ui/theme/generated/PersonalTokens.kt` | `PersonalTokens.Colors/Radius/Space/Size/Shadow/Type`, `PersonalColorScheme`, `PersonalShapes`, `PersonalTheme { }` |
| Android | `…/generated/PersonalLifeTaxonomy.kt`, `…/generated/PersonalAssets.kt` | taxonomy catalog; `PersonalIcons.byKey`, `PersonalArtwork` |
| Android | `res/drawable/ic_pers_<key>.xml` | VectorDrawable, white stroke, tint at call site |
| Android | `res/drawable-nodpi/art_pers_<key>.webp` | artwork (WebP q80) |
| Backend | `backend/typescript/src/modules/personal/taxonomy.generated.ts` | typed codes, catalog, `isValidPersonalLifeContext()` |
| Backend | `frds/seeds/personal/personal_life_taxonomy.v1.seed.sql` | idempotent seed for `personal.life_area`, `personal.life_subcategory`, `personal.mood_state`. **Not a migration**, not in `MIGRATION_ORDER.txt`; the Phase 2 migration creates the tables and applies it |

The Xcode project uses file-system-synchronized groups (objectVersion 77), so new Swift files,
fonts and asset-catalog entries need no `project.pbxproj` edits.

## Icons

* Geometry comes from `lucide-static@0.577.0`, the Lucide release whose shapes match the Figma
  exports (checked: heart-pulse, laugh, dumbbell). The import normalises every glyph to a 24×24
  viewBox, stroke 1.7, round caps and joins, no fill. Color is never baked in: iOS renders as a
  template, and Android VectorDrawables are white and tinted at the call site.
* Icon set = `manifest.json` icons ∪ every taxonomy icon (areas, subcategories, moods) = 85 glyphs.
  4 subcategory glyphs come from Figma (P1.2, P2.6, P3.2, P6.2); the other 44 are Lucide picks
  (`iconSource: "lucide-proposal"`), pending design sign-off. Change a key in the JSON and re-run.
* Exact duplicates of existing repo assets are reported (manifest-only icons that duplicate are
  skipped). The current run found none.
* Not imported on purpose: orbit emblems (built in code as `OrbitEmblem`), tab-bar and top-bar
  chrome (shared across modes; already in `Nav*`/`Shell*`), and the Figma `@4x` whole-card exports.

## Fonts

Plus Jakarta Sans 2.071 (OFL 1.1, Tokotype): Regular, **Medium (added in v1.1)**, SemiBold, Bold
and ExtraBold, in `momentra/momentra/Fonts/` (registered in `momentra/Info.plist` `UIAppFonts`) and
`apk/app/src/main/res/font/` (`PlusJakartaSans` FontFamily in `ui/theme/Type.kt`). License text:
`THIRD_PARTY_NOTICES.md`.
