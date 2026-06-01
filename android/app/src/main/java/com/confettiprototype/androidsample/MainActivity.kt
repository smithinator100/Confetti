package com.confettiprototype.androidsample

import android.os.Bundle
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.floatingactionbutton.FloatingActionButton
import confetti.ConfettiColorFamily
import confetti.ConfettiConfiguration
import confetti.ConfettiShape
import confetti.ConfettiView
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val heroSizePx = dp(108)
        val burstCanvasSizePx = dp(320)
        val marginTopPx = dp(32)
        val marginBottomPx = dp(24)

        var particleCount = 60.0
        var startVelocity = 160.0
        var spread = 96.0
        var decay = 0.9
        var gravityStrength = 3.0
        var drift = 0.0
        var duration = 5.0
        var fadeOutVariance = 0.6
        var xSpin = 0.0
        var ySpin = 0.0
        var zSpin = 0.2
        var size = 10.0
        var sizeVariation = 1.6
        var pictogramScaleSize = 1.1
        var pictogramScaleDuration = 0.6
        var frontTransitionDelayMs = 200.0

        val enabledShapes = mutableSetOf(ConfettiShape.STAR, ConfettiShape.BLOB, ConfettiShape.RECT)
        val enabledColorFamilies = mutableSetOf(
            ConfettiColorFamily.MANDARIN,
            ConfettiColorFamily.BLOSSOM,
            ConfettiColorFamily.POLLEN,
        )

        var confettiView: ConfettiView? = null
        fun applyConfiguration() {
            val targetView = confettiView ?: return
            targetView.configuration = ConfettiConfiguration(
                particleCount = particleCount.roundToInt(),
                startVelocity = startVelocity,
                spread = spread,
                decay = decay,
                gravity = gravityStrength,
                drift = drift,
                duration = duration,
                fadeOutVariance = fadeOutVariance,
                xSpin = xSpin,
                ySpin = ySpin,
                zSpin = zSpin,
                size = size,
                sizeVariation = sizeVariation,
                pictogramScaleSize = pictogramScaleSize,
                pictogramScaleDuration = pictogramScaleDuration,
                frontTransitionDelayMs = frontTransitionDelayMs.roundToInt().toLong(),
                enabledShapes = enabledShapes.toSet(),
                enabledColorFamilies = enabledColorFamilies.toSet(),
            )
        }

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            clipChildren = false
            clipToPadding = false
            setPadding(dp(24), marginTopPx, dp(24), dp(24))
        }

        val stageLayout = FrameLayout(this).apply {
            clipChildren = false
            clipToPadding = false
            layoutParams = LinearLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = marginBottomPx
            }
        }

        confettiView = ConfettiView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                burstCanvasSizePx,
                burstCanvasSizePx,
                Gravity.CENTER,
            )
            contentDescription = "ConfettiView"
            pictogramSizePx = heroSizePx
            pictogramDrawable = AppCompatResources.getDrawable(
                context,
                R.drawable.subscription_check_hero,
            )
        }

        stageLayout.addView(confettiView)

        val confettiButton = Button(this).apply {
            text = "Confetti"
            setOnClickListener { confettiView?.fire() }
        }
        val settingsButton = Button(this).apply {
            text = "Settings"
        }

        val actionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
        actionRow.addView(confettiButton)
        actionRow.addView(
            settingsButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { leftMargin = dp(12) },
        )

        rootLayout.addView(stageLayout)
        rootLayout.addView(
            actionRow,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ),
        )

        val tweakScroll = ScrollView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            )
        }

        val tweakPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }
        tweakScroll.addView(tweakPanel)

        addSectionTitle(tweakPanel, "Tweak Settings")
        addSliderRow(tweakPanel, "particleCount", 10.0, 120.0, 1.0, particleCount, { "${it.roundToInt()}" }) {
            particleCount = it
            applyConfiguration()
        }
        addSliderRow(tweakPanel, "startVelocity", 5.0, 220.0, 1.0, startVelocity, { "${it.roundToInt()}" }) {
            startVelocity = it
            applyConfiguration()
        }
        addSliderRow(tweakPanel, "spread", 10.0, 180.0, 1.0, spread, { "${it.roundToInt()}" }) {
            spread = it
            applyConfiguration()
        }
        addSliderRow(tweakPanel, "decay", 0.80, 1.00, 0.01, decay, { "%.2f".format(it) }) {
            decay = it
            applyConfiguration()
        }
        addSliderRow(tweakPanel, "gravity", 0.0, 3.0, 0.1, gravityStrength, { "%.1f".format(it) }) {
            gravityStrength = it
            applyConfiguration()
        }
        addSliderRow(tweakPanel, "drift", -2.0, 2.0, 0.1, drift, { "%.1f".format(it) }) {
            drift = it
            applyConfiguration()
        }
        addSliderRow(tweakPanel, "duration", 1.0, 5.0, 0.1, duration, { "%.1f".format(it) }) {
            duration = it
            applyConfiguration()
        }
        addSliderRow(tweakPanel, "fadeOutVariance", 0.0, 0.6, 0.1, fadeOutVariance, { "%.1f".format(it) }) {
            fadeOutVariance = it
            applyConfiguration()
        }
        addSliderRow(tweakPanel, "xSpin", 0.0, 1.0, 0.1, xSpin, { "%.1f".format(it) }) {
            xSpin = it
            applyConfiguration()
        }
        addSliderRow(tweakPanel, "ySpin", 0.0, 1.0, 0.1, ySpin, { "%.1f".format(it) }) {
            ySpin = it
            applyConfiguration()
        }
        addSliderRow(tweakPanel, "zSpin", 0.0, 1.0, 0.1, zSpin, { "%.1f".format(it) }) {
            zSpin = it
            applyConfiguration()
        }
        addSliderRow(tweakPanel, "size", 0.2, 14.0, 0.1, size, { "%.1f".format(it) }) {
            size = it
            applyConfiguration()
            confettiView?.fire()
        }
        addSliderRow(tweakPanel, "sizeVariation", 0.0, 2.0, 0.1, sizeVariation, { "%.1f".format(it) }) {
            sizeVariation = it
            applyConfiguration()
            confettiView?.fire()
        }
        addSliderRow(tweakPanel, "pictogramScaleSize", 1.0, 1.8, 0.1, pictogramScaleSize, { "%.1f".format(it) }) {
            pictogramScaleSize = it
            applyConfiguration()
        }
        addSliderRow(tweakPanel, "pictogramScaleDuration", 0.1, 1.5, 0.1, pictogramScaleDuration, { "%.1f".format(it) }) {
            pictogramScaleDuration = it
            applyConfiguration()
        }
        addSliderRow(tweakPanel, "frontTransitionMs", 0.0, 800.0, 10.0, frontTransitionDelayMs, { "${it.roundToInt()}" }) {
            frontTransitionDelayMs = it
            applyConfiguration()
        }

        addSectionTitle(tweakPanel, "Shapes")
        addToggleRow(
            tweakPanel,
            options = listOf(
                "Star" to ConfettiShape.STAR,
                "Blob" to ConfettiShape.BLOB,
                "Rectangle" to ConfettiShape.RECT,
                "Strip" to ConfettiShape.STRIP,
            ),
            selected = enabledShapes,
        ) { shape, isChecked ->
            if (isChecked) enabledShapes.add(shape) else enabledShapes.remove(shape)
            if (enabledShapes.isEmpty()) enabledShapes.add(shape)
            applyConfiguration()
        }

        addSectionTitle(tweakPanel, "Color Families")
        addToggleRow(
            tweakPanel,
            options = listOf(
                "Mandarin" to ConfettiColorFamily.MANDARIN,
                "Pondwater" to ConfettiColorFamily.PONDWATER,
                "Lilypad" to ConfettiColorFamily.LILYPAD,
                "Blossom" to ConfettiColorFamily.BLOSSOM,
                "Pollen" to ConfettiColorFamily.POLLEN,
            ),
            selected = enabledColorFamilies,
        ) { family, isChecked ->
            if (isChecked) enabledColorFamilies.add(family) else enabledColorFamilies.remove(family)
            if (enabledColorFamilies.isEmpty()) enabledColorFamilies.add(family)
            applyConfiguration()
        }

        val settingsOverlay = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            )
            visibility = View.GONE
            setBackgroundColor(Color.argb(180, 0, 0, 0))
            isClickable = true
        }

        val settingsCard = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER,
            ).apply {
                leftMargin = dp(20)
                topMargin = dp(56)
                rightMargin = dp(20)
                bottomMargin = dp(96)
            }
            setBackgroundColor(Color.WHITE)
            isClickable = true
        }
        settingsCard.addView(tweakScroll)
        settingsOverlay.addView(settingsCard)

        val saveFab = FloatingActionButton(this).apply {
            contentDescription = "Save settings"
            setImageResource(android.R.drawable.ic_menu_save)
            setOnClickListener {
                settingsOverlay.visibility = View.GONE
            }
        }
        settingsOverlay.addView(
            saveFab,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.END,
            ).apply {
                rightMargin = dp(28)
                bottomMargin = dp(28)
            },
        )

        settingsOverlay.setOnClickListener { settingsOverlay.visibility = View.GONE }
        settingsCard.setOnClickListener { }
        settingsButton.setOnClickListener { settingsOverlay.visibility = View.VISIBLE }

        val screenRoot = FrameLayout(this).apply {
            addView(
                rootLayout,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                ),
            )
            addView(settingsOverlay)
        }

        setContentView(screenRoot)
        applyConfiguration()
    }

    private fun dp(value: Int): Int {
        val pixels = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics,
        )
        return pixels.roundToInt()
    }

    private fun addSectionTitle(parent: LinearLayout, title: String) {
        val label = TextView(this).apply {
            text = title
            textSize = 16f
            setPadding(0, dp(12), 0, dp(6))
        }
        parent.addView(label)
    }

    private fun addSliderRow(
        parent: LinearLayout,
        name: String,
        min: Double,
        max: Double,
        step: Double,
        initial: Double,
        formatter: (Double) -> String,
        onValueChanged: (Double) -> Unit,
    ) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { bottomMargin = dp(8) }
        }

        val valueLabel = TextView(this)
        val steps = ((max - min) / step).roundToInt().coerceAtLeast(1)
        val seekBar = SeekBar(this).apply {
            this.max = steps
            progress = (((initial - min) / step).roundToInt()).coerceIn(0, steps)
        }

        fun valueFromProgress(progress: Int): Double {
            val raw = min + progress * step
            val rounded = (raw / step).roundToInt() * step
            return rounded.coerceIn(min, max)
        }

        fun updateLabel(progress: Int) {
            val value = valueFromProgress(progress)
            valueLabel.text = "$name: ${formatter(value)}"
        }

        updateLabel(seekBar.progress)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val value = valueFromProgress(progress)
                updateLabel(progress)
                onValueChanged(value)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        row.addView(valueLabel)
        row.addView(seekBar)
        parent.addView(row)
    }

    private fun <T> addToggleRow(
        parent: LinearLayout,
        options: List<Pair<String, T>>,
        selected: Set<T>,
        onToggle: (T, Boolean) -> Unit,
    ) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { bottomMargin = dp(8) }
        }

        options.forEach { (label, value) ->
            val checkbox = CheckBox(this).apply {
                text = label
                isChecked = selected.contains(value)
                setOnCheckedChangeListener { _, isChecked ->
                    onToggle(value, isChecked)
                }
            }
            row.addView(checkbox)
        }

        parent.addView(row)
    }
}
