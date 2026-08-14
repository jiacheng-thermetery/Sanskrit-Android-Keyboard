# App Icon Source

The app icon uses the Devanagari letter `अ` from Wikimedia Commons:

- Source: https://commons.wikimedia.org/wiki/File:Devanagari_%E0%A4%85.svg
- Original SVG: https://upload.wikimedia.org/wikipedia/commons/3/3f/Devanagari_%E0%A4%85.svg
- License/status: Public domain, released by the copyright holder.

The source glyph was rasterized into a 1024x1024 RGB PNG, recolored, centered,
and composited onto a solid background for the iOS app icon. The Android icons
are derived from that same composite:

- `res/mipmap-*/ic_launcher.png` — the composite, resized for each density.
- `res/drawable/ic_launcher_foreground.png` — the glyph keyed out of the
  composite by luminance and centered on a 432x432 transparent canvas, for use
  as the adaptive-icon foreground.
- `res/mipmap-anydpi-v26/ic_launcher.xml` — adaptive icon pairing that
  foreground with a solid `#1F3330` background, sampled from the original.
