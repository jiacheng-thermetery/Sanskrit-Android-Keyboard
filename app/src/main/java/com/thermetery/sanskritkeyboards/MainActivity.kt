package com.thermetery.sanskritkeyboards

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * Host app — the Android counterpart of the iOS `ContentView`. It ships the
 * keyboards, explains how to turn them on, and gives you somewhere to try them.
 *
 * Built with plain platform views so the app carries no AndroidX dependency;
 * light/dark comes from `values/` + `values-night/`.
 */
class MainActivity : Activity() {

    private val density by lazy { resources.displayMetrics.density }
    private fun dp(v: Int): Int = (v * density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.app_name)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(32))
            setBackgroundColor(color(R.color.app_background))
        }

        root.addView(header())
        root.addView(spacer(20))
        root.addView(keyboardsCard())
        root.addView(spacer(20))
        root.addView(instructions())
        root.addView(spacer(20))
        root.addView(testField())
        root.addView(spacer(20))
        root.addView(iastReferenceCard())
        root.addView(spacer(20))
        root.addView(hkReferenceCard())
        root.addView(spacer(20))
        root.addView(velthuisReferenceCard())

        val scroll = ScrollView(this).apply {
            setBackgroundColor(color(R.color.app_background))
            addView(root, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        }
        setContentView(scroll)
    }

    // MARK: - Sections

    private fun header(): View = column().apply {
        addView(
            text("Saṃskṛta on Android, finally", size = 22f, bold = true)
        )
        addView(spacer(8))
        addView(
            text(
                "Six keyboards for typing Sanskrit. Pick the one that matches your habits.",
                size = 14f, secondary = true
            )
        )
    }

    private fun keyboardsCard(): View = card().apply {
        addView(text("Keyboards", size = 17f, bold = true))
        addView(spacer(14))
        keyboardRow("IAST", "QWERTY + long-press for diacritics. For occasional Sanskrit.")
        divider()
        keyboardRow("HK → IAST", "Type Harvard-Kyoto, see IAST appear live (A→ā, R→ṛ, S→ṣ, …).")
        divider()
        keyboardRow("HK → Devanāgarī", "Type Harvard-Kyoto, see Devanāgarī appear live (kRSNa → कृष्ण).")
        divider()
        keyboardRow("IAST → Devanāgarī", "Type IAST (long-press for ā ṛ ṣ ṇ …), see Devanāgarī appear live.")
        divider()
        keyboardRow("Velthuis → IAST", "Type Velthuis (.r .s ~n \"n aa …), see IAST appear live.")
        divider()
        keyboardRow("Velthuis → Devanāgarī", "Type Velthuis, see Devanāgarī appear live (k.r.s.na → कृष्ण).")
    }

    private fun LinearLayout.keyboardRow(name: String, subtitle: String) {
        addView(text(name, size = 15f, bold = true))
        addView(spacer(2))
        addView(text(subtitle, size = 13f, secondary = true))
    }

    private fun LinearLayout.divider() {
        addView(spacer(14))
        addView(View(this@MainActivity).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, dp(1).coerceAtLeast(1))
            setBackgroundColor(color(R.color.separator))
        })
        addView(spacer(14))
    }

    private fun instructions(): View = card().apply {
        addView(text("Enable a keyboard", size = 17f, bold = true))
        addView(spacer(12))
        stepRow("1", "Open Settings → System → Languages & input → On-screen keyboard → Manage keyboards.")
        addView(spacer(10))
        stepRow("2", "Turn on any of: IAST, HK → IAST, HK → Devanāgarī, IAST → Devanāgarī, Velthuis → IAST, Velthuis → Devanāgarī.")
        addView(spacer(10))
        stepRow("3", "In any text field, tap the keyboard-switch button (or 🌐 on our keyboards) and pick the one you want.")
        addView(spacer(14))

        addView(Button(this@MainActivity).apply {
            text = "Open keyboard settings"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        })
        addView(Button(this@MainActivity).apply {
            text = "Switch keyboard"
            setOnClickListener {
                getSystemService(InputMethodManager::class.java)?.showInputMethodPicker()
            }
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        })

        addView(spacer(6))
        addView(
            text(
                "These keyboards request no permissions — they do no networking and store nothing.",
                size = 13f, secondary = true
            )
        )
    }

    private fun LinearLayout.stepRow(number: String, body: String) {
        addView(LinearLayout(this@MainActivity).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(TextView(this@MainActivity).apply {
                text = number
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setTypeface(typeface, Typeface.BOLD)
                gravity = Gravity.CENTER
                setTextColor(color(R.color.brand_cream))
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(color(R.color.ic_launcher_background))
                }
                layoutParams = LinearLayout.LayoutParams(dp(22), dp(22))
            })
            addView(text(body, size = 14f).apply {
                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f).apply {
                    leftMargin = dp(10)
                }
            })
        })
    }

    private fun testField(): View = column().apply {
        addView(text("Try it", size = 17f, bold = true))
        addView(spacer(8))
        addView(EditText(this@MainActivity).apply {
            hint = "Tap here, switch keyboards, then try typing  saMskRtam."
            inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            gravity = Gravity.TOP or Gravity.START
            setTextColor(color(R.color.text_primary))
            setHintTextColor(color(R.color.text_secondary))
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = GradientDrawable().apply {
                cornerRadius = dp(10).toFloat()
                setColor(color(R.color.field_background))
                setStroke(dp(1).coerceAtLeast(1), color(R.color.separator))
            }
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, dp(140))
        })
    }

    private fun iastReferenceCard(): View = card().apply {
        addView(text("IAST keyboard — long-press cheatsheet", size = 17f, bold = true))
        addView(spacer(12))
        referenceRow("a", "ā")
        referenceRow("i", "ī")
        referenceRow("u", "ū")
        referenceRow("r", "ṛ ṝ")
        referenceRow("l", "ḷ ḹ")
        referenceRow("n", "ñ ṅ ṇ")
        referenceRow("t", "ṭ")
        referenceRow("d", "ḍ")
        referenceRow("s", "ś ṣ")
        referenceRow("m", "ṃ ṁ")
        referenceRow("h", "ḥ")
    }

    private fun hkReferenceCard(): View = card().apply {
        addView(text("Harvard-Kyoto cheatsheet", size = 17f, bold = true))
        addView(spacer(6))
        addView(
            text(
                "Used by both HK keyboards. Capitals are reserved for long vowels and retroflex/palatal consonants.",
                size = 13f, secondary = true
            )
        )
        addView(spacer(12))
        referenceRow("A I U", "ā ī ū")
        referenceRow("R RR", "ṛ ṝ")
        referenceRow("lR lRR", "ḷ ḹ")
        referenceRow("e ai", "e ai")
        referenceRow("o au", "o au")
        referenceRow("G J N", "ṅ ñ ṇ")
        referenceRow("T D Th Dh", "ṭ ḍ ṭh ḍh")
        referenceRow("z S", "ś ṣ")
        referenceRow("M H", "ṃ ḥ")
    }

    private fun velthuisReferenceCard(): View = card().apply {
        addView(text("Velthuis cheatsheet", size = 17f, bold = true))
        addView(spacer(6))
        addView(
            text(
                "Used by both Velthuis keyboards. Prefix `.` for retroflex/vocalic, `\"` for palatal sibilant & velar nasal, `~` for palatal nasal. Case is cosmetic.",
                size = 13f, secondary = true
            )
        )
        addView(spacer(12))
        referenceRow("aa ii uu", "ā ī ū")
        referenceRow(".r .rr", "ṛ ṝ")
        referenceRow(".l .ll", "ḷ ḹ")
        referenceRow("e ai", "e ai")
        referenceRow("o au", "o au")
        referenceRow("\"n ~n .n", "ṅ ñ ṇ")
        referenceRow(".t .th .d .dh", "ṭ ṭh ḍ ḍh")
        referenceRow("\"s .s", "ś ṣ")
        referenceRow(".m .h", "ṃ ḥ")
    }

    private fun LinearLayout.referenceRow(base: String, alts: String) {
        addView(LinearLayout(this@MainActivity).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(3), 0, dp(3))
            addView(TextView(this@MainActivity).apply {
                text = base
                typeface = Typeface.MONOSPACE
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                setTextColor(color(R.color.text_primary))
                layoutParams = LinearLayout.LayoutParams(dp(110), WRAP_CONTENT)
            })
            addView(text("→", size = 15f, secondary = true))
            addView(TextView(this@MainActivity).apply {
                text = alts
                typeface = Typeface.SERIF
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTextColor(color(R.color.text_primary))
                layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                    leftMargin = dp(10)
                }
            })
        })
    }

    // MARK: - Small view builders

    private fun column(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    }

    private fun card(): LinearLayout = column().apply {
        setPadding(dp(16), dp(16), dp(16), dp(16))
        background = GradientDrawable().apply {
            cornerRadius = dp(12).toFloat()
            setColor(color(R.color.card_background))
        }
    }

    private fun text(
        s: String,
        size: Float = 15f,
        bold: Boolean = false,
        secondary: Boolean = false,
    ): TextView = TextView(this).apply {
        text = s
        setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
        if (bold) setTypeface(typeface, Typeface.BOLD)
        setTextColor(color(if (secondary) R.color.text_secondary else R.color.text_primary))
    }

    private fun spacer(heightDp: Int): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, dp(heightDp))
    }

    @Suppress("DEPRECATION")
    private fun color(id: Int): Int = resources.getColor(id, theme)
}
