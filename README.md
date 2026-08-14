# Sanskrit Keyboards for Android

Six custom Android keyboards for typing Sanskrit. Pick whichever matches your habits — they coexist, you enable each one separately in Settings, and switch between them with the 🌐 globe key.

This is a port of [Sanskrit-iOS-Keyboard](https://github.com/jiacheng-thermetery/Sanskrit-iOS-Keyboard). The transliteration tables, key layouts, popover behaviour and keyboard geometry are ported one-for-one; see [Differences from the iOS version](#differences-from-the-ios-version) for the handful of places where Android's input APIs made a different choice correct.

| Keyboard                  | Input                  | Output                  | When to use                                              |
|---------------------------|------------------------|-------------------------|----------------------------------------------------------|
| **IAST**                  | QWERTY + long-press    | IAST (`ā ī ṛ ñ ṣ ṃ ḥ`)  | Occasional Sanskrit; same gesture as accented letters.   |
| **HK → IAST**             | Harvard-Kyoto          | IAST                    | Fluent HK typists who want diacritics fast.              |
| **HK → Devanāgarī**       | Harvard-Kyoto          | Devanāgarī (`कृष्ण`)    | Devanāgarī output without an Indic keyboard.             |
| **IAST → Devanāgarī**     | QWERTY + long-press    | Devanāgarī              | Read in IAST while typing; render Devanāgarī.            |
| **Velthuis → IAST**       | Velthuis (`.r ~n "s`)  | IAST                    | Velthuis muscle memory from devnag / LaTeX.              |
| **Velthuis → Devanāgarī** | Velthuis               | Devanāgarī              | Same, with Devanāgarī output.                            |

The five transliterating keyboards (everything except plain IAST) do live, Wylie-style transliteration: each keystroke is buffered, the *whole* pending sequence is re-transliterated, and the on-screen text is replaced with the new rendering. Type `R` in HK and you see `ṛ`; type another `R` and `ṛ` is replaced with `ṝ`. Space (or any non-buffer character) commits and resets the buffer. The Velthuis keyboards treat `.` `"` `~` as letter-like — `.` followed by `r` becomes `ṛ`/`ऋ`, not a period.

## Install

Grab `sanskrit-keyboards-1.0.apk` from the [latest release](https://github.com/jiacheng-thermetery/Sanskrit-Android-Keyboard/releases/latest) and install it. Android will ask you to allow installing from your browser or file manager the first time — this app is not on Google Play.

Requires Android 7.0 (API 24) or newer.

## Enable the keyboards

After install:

1. Settings → System → Languages & input → On-screen keyboard → **Manage keyboards**
2. Turn on any of **IAST**, **HK → IAST**, **HK → Devanāgarī**, **IAST → Devanāgarī**, **Velthuis → IAST**, **Velthuis → Devanāgarī** (enable as many as you want).
3. In any text field, tap the keyboard-switch button in the navigation bar — or the 🌐 globe key on one of these keyboards — and pick the one you want.

The **Sanskrit Keyboards** app itself has buttons for both steps, plus cheatsheets and a scratch field to try things in.

Android will warn you that an input method "may be able to collect all the text you type". That warning is shown for every third-party keyboard. These keyboards request **no permissions at all** — you can confirm that in the app's App info page, and in `AndroidManifest.xml`.

## IAST keyboard — long-press cheatsheet

The same long-press layout is used by **IAST → Devanāgarī** — only the output script differs.

| Base | Long-press variants  |
|------|----------------------|
| `a`  | `ā`                  |
| `i`  | `ī`                  |
| `u`  | `ū`                  |
| `r`  | `ṛ`, `ṝ`             |
| `l`  | `ḷ`, `ḹ`             |
| `n`  | `ñ`, `ṅ`, `ṇ`        |
| `t`  | `ṭ`                  |
| `d`  | `ḍ`                  |
| `s`  | `ś`, `ṣ`             |
| `m`  | `ṃ`, `ṁ`             |
| `h`  | `ḥ`                  |
| `\|` | `।`, `॥` (in 123 mode) |

Shift gives uppercase variants (`Ā Ī Ū Ṛ ...`).

## Harvard-Kyoto cheatsheet

Used by both HK keyboards. Capitals are reserved for long vowels and retroflex/palatal consonants.

| HK input        | IAST     | Devanāgarī (independent) |
|-----------------|----------|--------------------------|
| `a A`           | `a ā`    | `अ आ`                    |
| `i I`           | `i ī`    | `इ ई`                    |
| `u U`           | `u ū`    | `उ ऊ`                    |
| `R RR`          | `ṛ ṝ`    | `ऋ ॠ`                    |
| `lR lRR`        | `ḷ ḹ`    | `ऌ ॡ`                    |
| `e ai o au`     | `e ai o au` | `ए ऐ ओ औ`             |
| `k kh g gh G`   | `k kh g gh ṅ` | `क ख ग घ ङ`         |
| `c ch j jh J`   | `c ch j jh ñ` | `च छ ज झ ञ`         |
| `T Th D Dh N`   | `ṭ ṭh ḍ ḍh ṇ` | `ट ठ ड ढ ण`         |
| `t th d dh n`   | `t th d dh n` | `त थ द ध न`         |
| `p ph b bh m`   | `p ph b bh m` | `प फ ब भ म`         |
| `y r l v`       | `y r l v`     | `य र ल व`           |
| `z S s h`       | `ś ṣ s h`     | `श ष स ह`           |
| `M H`           | `ṃ ḥ`         | `ं ः`                |

Examples (HK → Devanāgarī):

- `namaste` → `नमस्ते`
- `saMskRtam` → `संस्कृतम्`
- `zrI` → `श्री`
- `kRSNa` → `कृष्ण`
- `dharma` → `धर्म`

## Velthuis cheatsheet

Used by both Velthuis keyboards. The prefix marks `.` (retroflex / vocalic / modifier), `"` (palatal sibilant & velar nasal), and `~` (palatal nasal) are letter-like — they go into the buffer instead of committing it. Case is cosmetic; `K` and `k` both give `क`/`k`. The on-screen popover exposes the common bigrams (`.r .rr`, `.t`, `.d`, `.n ~n "n`, `.s "s`, `.l .ll`, `.m`, `.h`, `aa ii uu`) as long-press shortcuts so you don't have to switch to the symbols layer for `.` `"` `~`.

| Velthuis input        | IAST          | Devanāgarī (independent) |
|-----------------------|---------------|--------------------------|
| `a aa`                | `a ā`         | `अ आ`                    |
| `i ii`                | `i ī`         | `इ ई`                    |
| `u uu`                | `u ū`         | `उ ऊ`                    |
| `.r .rr`              | `ṛ ṝ`         | `ऋ ॠ`                    |
| `.l .ll`              | `ḷ ḹ`         | `ऌ ॡ`                    |
| `e ai o au`           | `e ai o au`   | `ए ऐ ओ औ`                |
| `k kh g gh "n`        | `k kh g gh ṅ` | `क ख ग घ ङ`              |
| `c ch j jh ~n`        | `c ch j jh ñ` | `च छ ज झ ञ`              |
| `.t .th .d .dh .n`    | `ṭ ṭh ḍ ḍh ṇ` | `ट ठ ड ढ ण`              |
| `t th d dh n`         | `t th d dh n` | `त थ द ध न`              |
| `p ph b bh m`         | `p ph b bh m` | `प फ ब भ म`              |
| `y r l v`             | `y r l v`     | `य र ल व`                |
| `"s .s s h`           | `ś ṣ s h`     | `श ष स ह`                |
| `.m .h`               | `ṃ ḥ`         | `ं ः`                     |

Examples (Velthuis → Devanāgarī):

- `namaste` → `नमस्ते`
- `sa.msk.rtam` → `संस्कृतम्`
- `"srii` → `श्री`
- `k.r.s.na` → `कृष्ण`
- `j~naana` → `ज्ञान`
- `dharma` → `धर्म`

## Build

You need a JDK 17+ and the Android SDK (platform 34, build-tools 34). No Android Studio required.

```sh
git clone https://github.com/jiacheng-thermetery/Sanskrit-Android-Keyboard
cd Sanskrit-Android-Keyboard
echo "sdk.dir=/path/to/your/android-sdk" > local.properties

./gradlew test              # 29 unit tests
./gradlew assembleDebug     # app/build/outputs/apk/debug/app-debug.apk
```

### Signing a release build

Release signing is configured **out of tree** — no keystore, password or `keystore.properties` is ever committed. Point the build at your keystore either with a `keystore.properties` file at the repo root (gitignored):

```properties
storeFile=/absolute/path/to/sanskrit-keyboards-release.jks
storePassword=…
keyAlias=sanskrit
keyPassword=…
```

or with environment variables:

```sh
export SANSKRIT_KEYSTORE_FILE=/absolute/path/to/sanskrit-keyboards-release.jks
export SANSKRIT_KEYSTORE_PASSWORD=…
export SANSKRIT_KEY_ALIAS=sanskrit
export SANSKRIT_KEY_PASSWORD=…
./gradlew assembleRelease
```

With neither present, `assembleRelease` still succeeds — it just produces an unsigned APK.

To create a fresh key:

```sh
keytool -genkeypair -v \
  -keystore sanskrit-keyboards-release.jks -storetype PKCS12 \
  -alias sanskrit -keyalg RSA -keysize 4096 -validity 10000 \
  -dname "CN=Sanskrit Keyboards, O=Thermetery, C=US"
```

Keep that file safe and out of the repository. Android identifies an app by its signing key: **lose it and you cannot ship an update that upgrades an existing install** — users would have to uninstall and reinstall, losing app data.

### Cutting a release

`.github/workflows/release.yml` builds and tests on every run, and publishes a signed APK to a GitHub Release when both a tag and a signing key are present:

| Trigger | Result |
|---|---|
| Push a `v*` tag | test → build → sign → publish a Release |
| Run manually with a tag | same |
| Run manually with no tag | test → build only; the APK is kept as a workflow artifact |

Without the signing secrets it still runs green and produces an unsigned APK artifact, so you can validate the pipeline before configuring anything.

Signing needs four repository secrets (Settings → Secrets and variables → Actions):

| Secret | Value |
|---|---|
| `KEYSTORE_BASE64` | `base64 -w0 sanskrit-keyboards-release.jks` |
| `KEYSTORE_PASSWORD` | the store password |
| `KEY_ALIAS` | `sanskrit` |
| `KEY_PASSWORD` | the key password |

With those set, either push a tag or run the workflow from the Actions tab:

```sh
git tag v1.1 && git push origin v1.1
```

The workflow decodes the keystore to a temp file outside the workspace and deletes it in an `always()` step, so it never lands in the repo or in a build artifact.

## Project layout

```
.
├── app/build.gradle.kts                       # module config + out-of-tree signing
└── app/src/main/
    ├── AndroidManifest.xml                    # six <service> IME declarations
    ├── java/com/thermetery/sanskritkeyboards/
    │   ├── MainActivity.kt                    # host app: setup, cheatsheets, scratch field
    │   ├── core/KeyDefinition.kt              # key model, modes, shift state
    │   ├── layouts/
    │   │   ├── LayoutCommon.kt                # shared key constructors + bottom row
    │   │   ├── IastLayout.kt                  # popover layout (IAST + IAST→Devanāgarī)
    │   │   ├── HkLayout.kt                    # plain QWERTY (both HK keyboards)
    │   │   └── VelthuisLayout.kt              # QWERTY with Velthuis bigram popovers
    │   ├── translit/
    │   │   ├── Transliterator.kt              # scheme interface, live buffer session
    │   │   ├── DevanagariScheme.kt            # shared syllable composer
    │   │   ├── HkToIast.kt                    # greedy longest-match
    │   │   ├── HkToDevanagari.kt              # syllable composer tables
    │   │   ├── IastToDevanagari.kt            # syllable composer tables
    │   │   ├── VelthuisToIast.kt              # greedy longest-match
    │   │   └── VelthuisToDevanagari.kt        # syllable composer tables
    │   ├── ui/
    │   │   ├── Theme.kt                       # light/dark key colours
    │   │   ├── KeyButton.kt                   # one key: drawing, touch, long-press
    │   │   ├── PopoverView.kt                 # long-press alternates strip
    │   │   └── KeyboardView.kt                # layout + popover placement
    │   └── ime/
    │       ├── SanskritInputMethodService.kt  # shared IME behaviour
    │       └── Keyboards.kt                   # the six services
    └── res/xml/method_*.xml                   # one input-method descriptor per keyboard
```

## Customizing

- **IAST popover alternates** (also affects IAST → Devanāgarī): edit `layouts/IastLayout.kt` — change the second argument to any `ch(...)` call.
- **HK rules**: edit `translit/HkToIast.kt` (flat map) or `translit/HkToDevanagari.kt` (consonant/vowel/modifier tables).
- **HK QWERTY layout**: edit `layouts/HkLayout.kt` — affects both HK keyboards.
- **IAST → Devanāgarī rules**: edit `translit/IastToDevanagari.kt`.
- **Velthuis popover bigrams**: edit `layouts/VelthuisLayout.kt` — the long-press alts on `r`, `t`, `n`, `s`, etc. that emit `.r`, `.t`, `~n`, `"s`, … in one gesture.
- **Velthuis rules**: edit `translit/VelthuisToIast.kt` or `translit/VelthuisToDevanagari.kt`.
- **Key colours**: edit `ui/Theme.kt`.

Every scheme's `transliterate()` is a pure function with no Android dependencies, so rule changes can be covered by a plain JVM test in `app/src/test/`.

## Differences from the iOS version

The tables, layouts and gestures are identical. Three things differ, in each case because Android provides a better primitive than the workaround iOS forced:

- **Live update uses the composing region.** iOS has no composing-text API, so the iOS code tracks how many Unicode scalars it last inserted and issues that many `deleteBackward()` calls before re-inserting. Android has `setComposingText`, which replaces the pending text atomically — so there is no delete-counting to get wrong, and the editor keeps correct cursor and selection state throughout. This is why `Transliterator` here exposes a pure `transliterate()` rather than the iOS `Edit(deleteCount:insert:)` struct.
- **The return key honours the editor's action.** On iOS it always inserts `\n`. On Android a single-line field advertises an action (Search, Send, Done…) and a literal newline does nothing there, leaving the user stuck — so the key performs the action when the editor asks for one, and inserts a newline otherwise.
- **The Devanāgarī syllable composer is shared.** iOS repeats it once per keyboard (HK, IAST and Velthuis each carry a copy); here it lives in `DevanagariScheme` and the three schemes supply only their token tables. A cross-scheme test asserts all three still produce identical Devanāgarī for the same word.

One behaviour is worth restating because it surprises people: backspace in a transliterating keyboard removes one *input* character at a time and re-renders, so deleting through `kRSNa` walks back `कृष्ण` → `कृष्` → `कृ` → `क्`. Hit space first to commit the current rendering and "lock it in". Moving the cursor also commits the buffer, so a keystroke after a cursor move starts fresh.

## Privacy

No permissions, no network, no storage, no analytics, no dependencies in the shipped APK. See [PRIVACY.md](PRIVACY.md).

## Licence

MIT — see [LICENSE](LICENSE), same as the [iOS app](https://github.com/jiacheng-thermetery/Sanskrit-iOS-Keyboard) this was ported from. App icon attribution is in [APP_ICON_SOURCE.md](APP_ICON_SOURCE.md).
