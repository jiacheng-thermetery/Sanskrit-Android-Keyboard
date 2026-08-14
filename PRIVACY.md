# Privacy Policy

**Effective: August 14, 2026**

This policy covers **Sanskrit Keyboards**, a free Android app of custom keyboards for typing Sanskrit in IAST, Devanāgarī, Harvard-Kyoto, and Velthuis transliteration schemes.

## Summary

**The app collects nothing.** It makes no network calls, contains no analytics or third-party SDKs, and stores no user data anywhere — locally, remotely, or otherwise. It declares **no Android permissions at all**, including no `INTERNET` permission, so the operating system itself would refuse any attempt to send data off the device even if a future bug tried to.

## What this means for the text you type

When you type with one of the keyboards, Android routes each keystroke to the input method service. The service does a transliteration lookup against a fixed in-memory table — for example, `R` → `ṛ` for Harvard-Kyoto, or `.r` → `ṛ` for Velthuis — and returns the result to the text field you were typing into.

That lookup happens entirely on your device. The result is never logged, saved to a file, sent to a server, or shared with any other app. The keyboards keep a short in-memory buffer of the word you are currently typing, purely so they can re-render it as you type; it is discarded as soon as you press space, move the cursor, or leave the text field, and it is never written to storage.

## About Android's keyboard warning

When you enable any third-party keyboard, Android shows a warning that the input method "may be able to collect all the text you type, including personal data like passwords and credit card numbers". Android shows this for *every* third-party keyboard, because the input-method API technically allows it.

These keyboards do not do that. Two things let you verify it rather than take our word for it:

- The app requests **no permissions**, which you can confirm under Settings → Apps → Sanskrit Keyboards → Permissions, or by reading `AndroidManifest.xml`. Without the `INTERNET` permission, Android will not let the app open a network connection.
- The full source is public, and the shipped APK has **no third-party dependencies** — nothing is pulled in that could exfiltrate anything.

## Data we collect, store, share, sell, or transfer

None. Zero personal data. Zero usage data. Zero identifiers. Zero diagnostics. We have no data about you to retain, no data to delete on request, and no third parties to share data with.

## Third parties

There are none. The shipped app contains no third-party libraries, frameworks, SDKs, or services beyond what ships with Android itself. (The test suite uses JUnit and Robolectric; neither is part of the installed app.)

## Children

The app is suitable for all ages and collects no data from anyone, regardless of age.

## Open source

Full source code is published at <https://github.com/jiacheng-thermetery/Sanskrit-Android-Keyboard>. Every claim in this policy can be verified by reading the code.

## Changes

If this policy ever changes, the updated version will replace this file in the repository and the **Effective** date above will be updated. The git history preserves every prior version.

## Contact

- File an issue: <https://github.com/jiacheng-thermetery/Sanskrit-Android-Keyboard/issues>
- Email: jiacheng@thermetery.com
