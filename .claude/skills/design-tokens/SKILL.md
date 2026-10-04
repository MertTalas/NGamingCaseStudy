---
name: design-tokens
description: Regenerate the Android light and dark color resources from design/tokens.json. Use when a color changes, a token is added or removed, or tokens.json is updated from Figma.
---

# Design tokens

`design/tokens.json` is the single source of truth for colors. The files below are generated from it and must not be edited by hand:

- `app/src/main/res/values/colors.xml` (light)
- `app/src/main/res/values-night/colors.xml` (dark)

## Token format

Semantic names, each with a light and a dark value (`#RRGGBB` or `#AARRGGBB`):

```json
{
  "color": {
    "primary":      { "light": "#FFD100", "dark": "#FFD100" },
    "onPrimary":    { "light": "#111111", "dark": "#111111" },
    "background":   { "light": "#FFFFFF", "dark": "#121212" },
    "error":        { "light": "#D93025", "dark": "#D93025" }
  }
}
```

Values above are examples. The real values come from the Figma variable collections "Color · Light" and "Color · Dark", which use identical token names.

## Workflow

1. Change only `design/tokens.json` (or re-export it from Figma, see below).
2. Run the generator: `python3 tools/generate_colors.py`.
3. Check that the diff touches only `design/tokens.json` and the two generated files.
4. Build and look at both themes.

## Rules for the generator

- Fail with a clear message if a token is missing its light or dark value, or has an invalid hex.
- Output a stable order and a "generated, do not edit" header so diffs stay small.
- Token names map to Material theme attributes in `themes.xml` (for example `primary` to `colorPrimary`, `onError` to `colorOnError`). Adding a token only requires wiring it once in `themes.xml`.

## Updating tokens from Figma

Read the variables of the Figma file through the Figma MCP (variable definitions), map each light and dark pair into `design/tokens.json`, then follow the workflow above. If a Figma value and the json disagree, Figma wins.
