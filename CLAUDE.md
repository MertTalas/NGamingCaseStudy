# NGamingCaseStudy

Android app (Kotlin) that lists posts, lets the user edit a post's title and body, and supports swipe to delete.

## Requirements

- Opening screen is the list screen. Data: `GET https://jsonplaceholder.typicode.com/posts`.
- Each item shows a circular image, a title and a short description (two lines max), with a divider between items.
- Image URL: `https://picsum.photos/300/300?random=<position>&grayscale`, where `<position>` is the adapter position.
- Items can be removed with swipe to delete.
- Tapping an item opens a detail screen where the title and body can be edited.
- The UI follows the system theme (light/dark) and draws edge-to-edge, reacting to system bar insets.

## Tech stack

Kotlin, Hilt, Coroutines/Flow, Retrofit + OkHttp, MVVM, `ListAdapter` + `DiffUtil`, XML layouts with ViewBinding, Navigation Component, Coil.

## Architecture

Single Activity with two Fragments (list, detail). Unidirectional data flow: UI observes `StateFlow` from the ViewModel and sends events back.

```
com.mert.ngamingcasestudy
├── data
│   ├── remote       Retrofit service and DTOs
│   ├── mapper       DTO -> domain mapping
│   └── repository   PostRepositoryImpl (single source of truth)
├── domain
│   ├── model        Post
│   ├── repository   PostRepository interface
│   └── usecase      One use case per action, used by ViewModels
├── di               Hilt modules
└── ui
    ├── list         ListFragment, ListViewModel, PostAdapter
    └── detail       DetailFragment, DetailViewModel
```

- The repository loads posts from the API once and keeps them in an in-memory `StateFlow<List<Post>>`.
- jsonplaceholder does not persist writes, so edit and delete are applied locally. List and detail screens share the same repository, so changes show up in the list immediately.
- Clean architecture dependency rule: `ui -> domain <- data`. Domain has no Android, Retrofit or data imports; ViewModels depend only on use cases.
- DTOs are mapped to a `Post` domain model so the UI does not depend on the API shape.
- UI state is modeled as Loading / Success / Error.

## Design

- Source of truth: Figma file "NGAMING Listing App" (https://www.figma.com/design/sTouKV3dCMyiDo1Z3vZtOi). Pages: Foundations (colors, typography, shape, icons) and Screens (list, swipe to delete, detail, detail with keyboard, loading, error, empty; each in light and dark).
- Before building or changing a screen, read the matching frame from Figma (Figma MCP) and follow it for layout, spacing, typography and states. If the design and these notes disagree, the design wins.
- Colors are design tokens defined in `design/tokens.json` (semantic names, each with a light and a dark value). Android color resources are generated from it, see the `design-tokens` skill. Never hardcode hex values in layouts or code, and never edit the generated color files by hand.
- Typography is defined by hand in `res/values/typography.xml` (`TextAppearance.NGaming.*`, Montserrat + Inter bundled in `res/font`). Use these styles or the theme `textAppearance*` attributes instead of setting fonts or text sizes inline.

## Conventions

- Prefer small, focused classes and functions. No business logic in Fragments or adapters.
- Use `viewLifecycleOwner` with `repeatOnLifecycle` when collecting flows in Fragments.
- Clear ViewBinding references in `onDestroyView`.
- Colors come from theme attributes or `colors.xml` / `values-night/colors.xml`. No hardcoded colors in layouts.
- Commits are small and use imperative, descriptive messages.

## Build

Open in Android Studio, or run `./gradlew assembleDebug`. Tests: `./gradlew test`.
