package com.hermes.companion.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.view.MotionEvent
import android.view.Gravity
import android.view.WindowManager

/**
 * Floating window service using WindowManager.
 *
 * Displays a draggable floating bubble over other apps that can expand to show
 * quick actions, connection status, and mission progress. Handles three display
 * modes:
 *
 *   BUBBLE → Small circular icon, draggable
 *   COMPACT → Small horizontal bar with a few quick actions
 *   EXPANDED → Full-width panel with detailed status & actions
 *
 * Requires SYSTEM_ALERT_WINDOW permission.
 */
class OverlayService : Service() {

    companion object {
        private const val TAG = "OverlayService"
        private const val BUBBLE_SIZE_DP = 48
        private const val COMPACT_HEIGHT_DP = 56
        private const val EXPANDED_HEIGHT_DP = 320

        const val ACTION_SHOW = "com.hermes.companion.action.SHOW_OVERLAY"
        const val ACTION_HIDE = "com.hermes.companion.action.HIDE_OVERLAY"
        const val ACTION_SET_STATUS = "com.hermes.companion.action.SET_OVERLAY_STATUS"
        const val EXTRA_STATUS = "status"
        const val EXTRA_MISSION_PROGRESS = "mission_progress"

        fun startService(context: Context) {
            val intent = Intent(context, OverlayService::class.java).apply {
                action = ACTION_SHOW
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            context.startService(Intent(context, OverlayService::class.java).apply {
                action = ACTION_HIDE
            })
        }
    }

    // ── Overlay Mode ───────────────────────────────────────

    enum class OverlayMode {
        BUBBLE, COMPACT, EXPANDED
    }

    // ── State ──────────────────────────────────────────────

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var currentMode: OverlayMode = OverlayMode.BUBBLE
    private var brainConnected: Boolean = false
    private var missionProgress: Float = 0f
    private var isListening: Boolean = false

    private lateinit var layoutParams: WindowManager.LayoutParams
    private val serviceScope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.Dispatchers.Main + kotlinx.coroutines.SupervisorJob()
    )

    // ── Lifecycle ──────────────────────────────────────────

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        Log.i(TAG, "OverlayService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW -> showOverlay()
            ACTION_HIDE -> hideOverlay()
            ACTION_SET_STATUS -> {
                brainConnected = intent.getBooleanExtra(EXTRA_STATUS, false)
                missionProgress = intent.getFloatExtra(EXTRA_MISSION_PROGRESS, 0f)
                updateAppearance()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        hideOverlay()
        serviceScope.cancel()
        super.onDestroy()
    }

    // ── Overlay Management ─────────────────────────────────

    @SuppressLint("ClickableViewAccessibility")
    private fun showOverlay() {
        if (overlayView != null) return

        val dp = resources.displayMetrics.density
        val bubbleSizePx = (BUBBLE_SIZE_DP * dp).toInt()

        layoutParams = WindowManager.LayoutParams(
            bubbleSizePx,
            bubbleSizePx,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 200
        }

        overlayView = createBubbleView()
        windowManager?.addView(overlayView, layoutParams)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createBubbleView(): View {
        val dp = resources.displayMetrics.density
        val bubbleSizePx = (BUBBLE_SIZE_DP * dp).toInt()

        // Bubble container
        val container = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Circular bubble
        val bubble = View(this).apply {
            layoutParams = FrameLayout.LayoutParams(bubbleSizePx, bubbleSizePx).apply {
                gravity = Gravity.CENTER
            }
            // We'll apply a circular background programmatically
            val bg = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(0xFF4CAF50.toInt()) // green when connected
                setStroke(2, 0xFF2E7D32.toInt())
            }
            background = bg
            contentDescription = "Hermes bubble"
        }
        container.addView(bubble)

        // Status dot (small indicator)
        val statusDot = View(this).apply {
            val dotSize = (12 * dp).toInt()
            layoutParams = FrameLayout.LayoutParams(dotSize, dotSize).apply {
                gravity = Gravity.TOP or Gravity.END
                marginEnd = (2 * dp).toInt()
                topMargin = (2 * dp).toInt()
            }
            val dotBg = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(if (brainConnected) 0xFF4CAF50.toInt() else 0xFFF44336.toInt())
            }
            background = dotBg
            tag = "status_dot"
        }
        container.addView(statusDot)

        // Touch handling: drag + tap to expand
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false
        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop

        container.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (Math.abs(dx) > touchSlop || Math.abs(dy) > touchSlop) {
                        isDragging = true
                    }
                    if (isDragging) {
                        layoutParams.x = initialX + dx
                        layoutParams.y = initialY + dy
                        windowManager?.updateViewLayout(overlayView, layoutParams)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        onBubbleTapped()
                    }
                    true
                }
                else -> false
            }
        }

        return container
    }

    // ── Mode Transitions ───────────────────────────────────

    private fun onBubbleTapped() {
        currentMode = when (currentMode) {
            OverlayMode.BUBBLE -> OverlayMode.COMPACT
            OverlayMode.COMPACT -> OverlayMode.EXPANDED
            OverlayMode.EXPANDED -> OverlayMode.BUBBLE
        }
        rebuildOverlay()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun rebuildOverlay() {
        if (overlayView == null) return

        val dp = resources.displayMetrics.density
        val bubbleSizePx = (BUBBLE_SIZE_DP * dp).toInt()

        when (currentMode) {
            OverlayMode.BUBBLE -> {
                // Recreate bubble view
                val oldView = overlayView
                overlayView = createBubbleView()
                windowManager?.removeView(oldView)
                windowManager?.addView(overlayView, layoutParams)
            }
            OverlayMode.COMPACT -> {
                // Compact bar
                val oldView = overlayView
                overlayView = createCompactView()
                windowManager?.removeView(oldView)

                val barWidth = (240 * dp).toInt()
                val barHeight = (COMPACT_HEIGHT_DP * dp).toInt()
                layoutParams.width = barWidth
                layoutParams.height = barHeight
                windowManager?.addView(overlayView, layoutParams)
            }
            OverlayMode.EXPANDED -> {
                // Full expanded panel
                val oldView = overlayView
                overlayView = createExpandedView()
                windowManager?.removeView(oldView)

                val panelWidth = (320 * dp).toInt()
                val panelHeight = (EXPANDED_HEIGHT_DP * dp).toInt()
                layoutParams.width = panelWidth
                layoutParams.height = panelHeight
                windowManager?.addView(overlayView, layoutParams)
            }
        }
    }

    private fun createCompactView(): View {
        val dp = resources.displayMetrics.density
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16, 0, 16, 0)
            val bg = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 12 * dp
                setColor(0xFF303030.toInt())
                setStroke(1, 0xFF505050.toInt())
            }
            background = bg
        }

        // Connection indicator
        val indicator = View(this).apply {
            val size = (10 * dp).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginEnd = (8 * dp).toInt()
            }
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(if (brainConnected) 0xFF4CAF50.toInt() else 0xFFF44336.toInt())
            }
            tag = "status_indicator"
        }
        container.addView(indicator)

        // Label
        val label = TextView(this).apply {
            text = if (brainConnected) "Brain Connected" else "Brain Offline"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 12f
            tag = "status_label"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        container.addView(label)

        // Mission progress (if any)
        if (missionProgress > 0f) {
            val progressText = TextView(this).apply {
                text = "${(missionProgress * 100).toInt()}%"
                setTextColor(0xFF80CBC4.toInt())
                textSize = 11f
                tag = "progress_label"
            }
            container.addView(progressText)
        }

        return container
    }

    private fun createExpandedView(): View {
        val dp = resources.displayMetrics.density
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            val bg = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 16 * dp
                setColor(0xFF1A1A2E.toInt())
                setStroke(1, 0xFF303060.toInt())
            }
            background = bg
        }

        // Header
        val header = TextView(this).apply {
            text = "⚡ Hermes Companion"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 14f
            setPadding(0, 0, 0, (8 * dp).toInt())
        }
        container.addView(header)

        // Status row
        val statusRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, (4 * dp).toInt(), 0, (8 * dp).toInt())
        }

        val statusDot = View(this).apply {
            val size = (10 * dp).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginEnd = (8 * dp).toInt()
            }
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(if (brainConnected) 0xFF4CAF50.toInt() else 0xFFF44336.toInt())
            }
            tag = "status_dot"
        }
        statusRow.addView(statusDot)

        val statusLabel = TextView(this).apply {
            text = if (brainConnected) "Brain Connected" else "Brain Offline"
            setTextColor(0xFFB0BEC5.toInt())
            textSize = 12f
            tag = "status_label"
        }
        statusRow.addView(statusLabel)
        container.addView(statusRow)

        // Mission progress bar label
        if (missionProgress > 0f) {
            val missionLabel = TextView(this).apply {
                text = "Mission Progress: ${(missionProgress * 100).toInt()}%"
                setTextColor(0xFF80CBC4.toInt())
                textSize = 11f
                tag = "mission_progress_label"
            }
            container.addView(missionLabel)

            // Simple progress indicator
            val progressBar = View(this).apply {
                val height = (4 * dp).toInt()
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, height
                ).apply {
                    topMargin = (4 * dp).toInt()
                }
                val bgDrawable = android.graphics.drawable.GradientDrawable().apply {
                    cornerRadius = height / 2f
                    setColor(0xFF263238.toInt())
                }
                background = bgDrawable
                tag = "progress_bar"
            }
            container.addView(progressBar)
        }

        // Quick actions header
        val actionsHeader = TextView(this).apply {
            text = "Quick Actions"
            setTextColor(0xFF78909C.toInt())
            textSize = 10f
            setPadding(0, (12 * dp).toInt(), 0, (4 * dp).toInt())
        }
        container.addView(actionsHeader)

        // Action buttons row
        val actionsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val actionBtns = listOf(
            "🎤 Voice" to "voice",
            "📋 Mission" to "mission",
            "🔊 Logs" to "logs",
            "✖ Close" to "close"
        )

        actionBtns.forEach { (label, action) ->
            val btn = TextView(this).apply {
                text = label
                setTextColor(0xFFB3E5FC.toInt())
                textSize = 11f
                setPadding(12, 8, 12, 8)
                val btnBg = android.graphics.drawable.GradientDrawable().apply {
                    cornerRadius = 8 * dp
                    setColor(0xFF1B2838.toInt())
                    setStroke(1, 0xFF37474F.toInt())
                }
                background = btnBg
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = (6 * dp).toInt()
                }
                tag = action
            }
            btn.setOnClickListener { onQuickAction(action) }
            actionsRow.addView(btn)
        }
        container.addView(actionsRow)

        // Collapse button
        val collapseBtn = TextView(this).apply {
            text = "▲ Minimize"
            setTextColor(0xFF78909C.toInt())
            textSize = 10f
            gravity = Gravity.CENTER
            setPadding(0, (12 * dp).toInt(), 0, 0)
        }
        collapseBtn.setOnClickListener {
            currentMode = OverlayMode.BUBBLE
            rebuildOverlay()
        }
        container.addView(collapseBtn)

        return container
    }

    // ── Quick Actions ──────────────────────────────────────

    private fun onQuickAction(action: String) {
        when (action) {
            "voice" -> {
                isListening = !isListening
                // Broadcast to VoiceService
                sendBroadcast(Intent("com.hermes.companion.action.TOGGLE_VOICE"))
            }
            "mission" -> {
                sendBroadcast(Intent("com.hermes.companion.action.SHOW_MISSION"))
            }
            "logs" -> {
                sendBroadcast(Intent("com.hermes.companion.action.SHOW_LOGS"))
            }
            "close" -> {
                currentMode = OverlayMode.BUBBLE
                rebuildOverlay()
            }
        }
    }

    // ── Status Updates ─────────────────────────────────────

    private fun updateAppearance() {
        val statusDot = overlayView?.findViewWithTag<View>("status_dot")
            ?: overlayView?.findViewWithTag<View>("status_indicator")
        statusDot?.background = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.OVAL
            setColor(if (brainConnected) 0xFF4CAF50.toInt() else 0xFFF44336.toInt())
        }

        val statusLabel = overlayView?.findViewWithTag<TextView>("status_label")
        statusLabel?.text = if (brainConnected) "Brain Connected" else "Brain Offline"
    }

    // ── Cleanup ────────────────────────────────────────────

    private fun hideOverlay() {
        overlayView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove overlay: ${e.message}")
            }
        }
        overlayView = null
    }
}
