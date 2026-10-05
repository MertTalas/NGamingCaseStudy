---
name: design-tokens
description: Regenerate the Android light and dark color resources from design/tokens.json. Use when a color changes, a token is added or removed, or tokens.json is updated from Figma.
---

# Design tokens

`design/tokens.json` is the single source of truth for colors. The files below are generated from it and must not be edited by hand:

- `app/src/main/res/values/colors.xml` (light)
- `app/src/main/res/values-night/colors.xml` (dark)

Only colors are generated. Typography lives in `app/src/main/res/values/typography.xml` and is edited by hand (see below).

## Token format

Semantic camelCase names, each with a light and a dark value (`#RRGGBB` or `#AARRGGBB`):

```json
{
  "color": {
    "primary":    { "light": "#FFD100", "dark": "#FFD100" },
    "background": { "light": "#FFFFFF", "dark": "#121212" }
  }
}
```

The values come from the Figma "Foundations" page (Color, light and dark theme), which uses identical token names in both themes.

The generator writes each token as a snake_case color resource: `onSurfaceVariant` becomes `@color/on_surface_variant`.

## Workflow

1. Change only `design/tokens.json` (or update it from Figma, see below).
2. Run the generator: `python3 tools/generate_colors.py`.
3. Check that the diff touches only `design/tokens.json` and the two generated files (plus `themes.xml` if a Material-role token was added).
4. Build and look at both themes.

## Rules for the generator

- Fail with a clear message if a token is missing its light or dark value, or has an invalid hex.
- Output a stable order and a "generated, do not edit" header so diffs stay small.

## Wiring tokens

Tokens with a Material 3 role are wired once in `values/themes.xml`. There is no `values-night/themes.xml`, because the night colors come from `values-night/colors.xml`.

| Token | Theme attribute |
|---|---|
| `primary` / `onPrimary` | `colorPrimary` / `colorOnPrimary` |
| `background` / `onBackground` | `android:colorBackground`, `colorSurface` / `colorOnBackground`, `colorOnSurface` |
| `surfaceVariant` / `onSurfaceVariant` | `colorSurfaceVariant` / `colorOnSurfaceVariant` |
| `outline` | `colorOutline`, `colorOutlineVariant` |
| `error` | `colorError` |
| `inverseSurface` / `onInverseSurface` | `colorSurfaceInverse` / `colorOnSurfaceInverse` |
| `inversePrimary` | `colorPrimaryInverse` |

The other tokens have no Material role and are used directly as `@color/<name>`: `topBar`, `onTopBar`, `accent`, `delete`, `onDelete`, `placeholder`, `keyboard`, `keyboardKey`.

## Typography (not generated)

`values/typography.xml` defines one `TextAppearance.NGaming.*` style per Figma text style. The fonts are static TTFs in `res/font` (Montserrat SemiBold/Bold, Inter Regular/Medium):

| Figma style | Style | Spec | Theme attribute |
|---|---|---|---|
| TopBar/Title | `TopBarTitle` | Montserrat Bold 20/28 | set on the toolbar |
| Title/Large | `TitleLarge` | Montserrat SemiBold 20/28 | `textAppearanceTitleLarge` |
| Title/Item | `TitleItem` | Montserrat SemiBold 16/22 | `textAppearanceTitleMedium` |
| Body/Large | `BodyLarge` | Inter Regular 16/24 | `textAppearanceBodyLarge` |
| Body/Item | `BodyItem` | Inter Regular 14/20 | `textAppearanceBodyMedium` |
| Label | `Label` | Inter Medium 12/16 | `textAppearanceLabelMedium` |
| Button | `Button` | Montserrat SemiBold 14/20, caps | `textAppearanceLabelLarge` |

If a text style changes in Figma, edit `typography.xml` by hand. If it needs a new weight, add the matching static TTF to `res/font`.

## Updating tokens from Figma

Read the color variables of the Figma file through the Figma MCP (variable definitions), or from a screenshot of the Foundations page. Map each light and dark pair into `design/tokens.json`, then follow the workflow above. If a Figma value and the json disagree, Figma wins.
