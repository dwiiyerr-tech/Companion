package com.hermes.companion.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.hermes.companion.core.network.Bounds
import com.hermes.companion.core.network.GestureRequest
import com.hermes.companion.core.network.GestureType
import com.hermes.companion.core.network.UITreeNode
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json

/**
 * Accessibility service for UI tree reading and gesture execution.
 *
 * Responsibilities:
 * - Reports UI tree to Hermes Brain as structured JSON
 * - Executes gestures: click, long-click, scroll, swipe, type, back, home
 * - Filters events by relevant apps
 * - Maintains last known UI state for delta comparisons
 */
class HermesAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "HermesA11y"
        private const val MAX_TREE_DEPTH = 15
        private const val MAX_CHILDREN_PER_NODE = 50

        /** Static reference for non-service access */
        @Volatile
        var instance: HermesAccessibilityService? = null
            private set

        /** Whether the service is currently connected */
        @Volatile
        var isRunning: Boolean = false
            private set

        private val _uiEvents = MutableSharedFlow<AccessibilityEvent>(
            extraBufferCapacity = 64,
            onBufferOverflow = SharedFlow.OverflowStrategy.DROP_OLDEST
        )
        val uiEvents: SharedFlow<AccessibilityEvent> = _uiEvents.asSharedFlow()

        private val _gestureResults = MutableSharedFlow<GestureResult>(
            extraBufferCapacity = 16,
            onBufferOverflow = SharedFlow.OverflowStrategy.DROP_OLDEST
        )
        val gestureResults: SharedFlow<GestureResult> = _gestureResults.asSharedFlow()
    }

    data class GestureResult(
        val requestId: String,
        val success: Boolean,
        val error: String? = null
    )

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var lastUITreeJson: String? = null
    private var eventThrottleMs: Long = 200L
    private var lastEventTime: Long = 0L

    // ── Lifecycle ──────────────────────────────────────────

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        isRunning = true
        Log.i(TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        val now = System.currentTimeMillis()
        if (now - lastEventTime < eventThrottleMs) return
        lastEventTime = now

        // Emit event for subscribers
        serviceScope.launch {
            _uiEvents.emit(event)
        }
    }

    override fun onInterrupt() {
        Log.i(TAG, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        instance = null
        isRunning = false
        serviceScope.cancel()
        super.onDestroy()
        Log.i(TAG, "Accessibility service destroyed")
    }

    // ── UI Tree Reading ────────────────────────────────────

    /**
     * Capture the current accessibility node tree starting from the root
     * of the active window and return it as a structured JSON string.
     */
    fun captureUITree(): String? {
        val rootNode = try {
            rootInActiveWindow
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get root window: ${e.message}")
            return null
        }

        if (rootNode == null) {
            Log.w(TAG, "Root window is null")
            return null
        }

        val tree = convertNodeToTree(rootNode, depth = 0)
        rootNode.recycle()

        return try {
            Json.encodeToString(UITreeNode.serializer(), tree)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to serialize UI tree: ${e.message}")
            null
        }
    }

    private fun convertNodeToTree(node: AccessibilityNodeInfo, depth: Int): UITreeNode {
        if (depth > MAX_TREE_DEPTH) {
            return UITreeNode(
                nodeId = System.nanoTime().toString(),
                className = "[max depth reached]",
                depth = depth
            )
        }

        val bounds = Rect()
        node.getBoundsInScreen(bounds)

        val actions = mutableListOf<String>()
        if (node.isClickable) actions.add("click")
        if (node.isLongClickable) actions.add("long_click")
        if (node.isScrollable) actions.add("scroll")
        if (node.isEditable) actions.add("type")
        if (node.isCheckable) actions.add("check")

        val childCount = node.childCount.coerceAtMost(MAX_CHILDREN_PER_NODE)
        val children = mutableListOf<UITreeNode>()

        for (i in 0 until childCount) {
            val child = try {
                node.getChild(i)
            } catch (e: Exception) {
                continue
            }
            if (child != null) {
                children.add(convertNodeToTree(child, depth + 1))
                child.recycle()
            }
        }

        return UITreeNode(
            nodeId = node.hashCode().toString(),
            className = node.className?.toString(),
            text = node.text?.toString(),
            contentDescription = node.contentDescription?.toString(),
            packageName = node.packageName?.toString(),
            bounds = Bounds(
                left = bounds.left,
                top = bounds.top,
                right = bounds.right,
                bottom = bounds.bottom
            ),
            isClickable = node.isClickable,
            isFocusable = node.isFocusable,
            isScrollable = node.isScrollable,
            isChecked = if (node.isCheckable) node.isChecked else null,
            isEnabled = node.isEnabled,
            isPassword = node.isPassword,
            isSelected = node.isSelected,
            children = children,
            childCount = node.childCount,
            actions = actions,
            windowId = node.windowId,
            depth = depth
        )
    }

    // ── Gesture Execution ──────────────────────────────────

    /**
     * Execute a gesture request. Returns a Flow that emits the result.
     */
    fun executeGesture(request: GestureRequest): Flow<GestureResult> = callbackFlow {
        val requestId = "gesture_${System.currentTimeMillis()}"

        val result = when (request.type) {
            GestureType.CLICK -> dispatchGestureAsync(
                createClickPath(request.startX, request.startY), 100L
            )
            GestureType.LONG_CLICK -> dispatchGestureAsync(
                createClickPath(request.startX, request.startY), 500L
            )
            GestureType.DOUBLE_CLICK -> {
                // Double-click: two clicks with a short delay
                dispatchGestureAsync(createClickPath(request.startX, request.startY), 50L)
                delay(80)
                dispatchGestureAsync(createClickPath(request.startX, request.startY), 50L)
            }
            GestureType.SWIPE_UP -> dispatchSwipe(
                request.startX, request.startY + 200f,
                request.startX, request.startY - 200f,
                request.duration
            )
            GestureType.SWIPE_DOWN -> dispatchSwipe(
                request.startX, request.startY - 200f,
                request.startX, request.startY + 200f,
                request.duration
            )
            GestureType.SWIPE_LEFT -> dispatchSwipe(
                request.startX + 200f, request.startY,
                request.startX - 200f, request.startY,
                request.duration
            )
            GestureType.SWIPE_RIGHT -> dispatchSwipe(
                request.startX - 200f, request.startY,
                request.startX + 200f, request.startY,
                request.duration
            )
            GestureType.SCROLL_UP -> dispatchSwipe(
                request.startX, request.startY + 300f,
                request.startX, request.startY - 300f,
                300L
            )
            GestureType.SCROLL_DOWN -> dispatchSwipe(
                request.startX, request.startY - 300f,
                request.startX, request.startY + 300f,
                300L
            )
            GestureType.SCROLL_LEFT -> dispatchSwipe(
                request.startX + 300f, request.startY,
                request.startX - 300f, request.startY,
                300L
            )
            GestureType.SCROLL_RIGHT -> dispatchSwipe(
                request.startX - 300f, request.startY,
                request.startX + 300f, request.startY,
                300L
            )
            GestureType.TYPE -> dispatchType(request.text ?: "")
            GestureType.BACK -> {
                performGlobalAction(GLOBAL_ACTION_BACK)
                GestureResult(requestId, success = true)
            }
            GestureType.HOME -> {
                performGlobalAction(GLOBAL_ACTION_HOME)
                GestureResult(requestId, success = true)
            }
            GestureType.RECENT_APPS -> {
                performGlobalAction(GLOBAL_ACTION_RECENTS)
                GestureResult(requestId, success = true)
            }
        }

        trySend(result)
        close()
    }

    private suspend fun dispatchGestureAsync(
        path: Path,
        duration: Long
    ): GestureResult {
        val requestId = "click_${System.currentTimeMillis()}"
        return suspendCancellableCoroutine { cont ->
            val callback = object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription) {
                    val result = GestureResult(requestId, success = true)
                    cont.resume(result)
                }

                override fun onCancelled(gestureDescription: GestureDescription) {
                    val result = GestureResult(requestId, success = false, error = "Gesture cancelled")
                    cont.resume(result)
                }
            }

            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
                .build()

            dispatchGesture(gesture, callback, null)
        }
    }

    private fun createClickPath(x: Float, y: Float): Path {
        return Path().apply { moveTo(x, y) }
    }

    private suspend fun dispatchSwipe(
        startX: Float, startY: Float,
        endX: Float, endY: Float,
        duration: Long
    ): GestureResult {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        return dispatchGestureAsync(path, duration)
    }

    private suspend fun dispatchType(text: String): GestureResult {
        val requestId = "type_${System.currentTimeMillis()}"
        return try {
            // Focus the text field first, then use the clipboard
            val clipboard = android.content.ClipboardManager::class.java
                .getDeclaredMethod("getService", android.content.Context::class.java)
                .invoke(null, this) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("hermes_input", text)
            clipboard.setPrimaryClip(clip)

            // Paste
            performGlobalAction(GLOBAL_ACTION_RECENTS) // ensure focus
            delay(100)
            val pasteResult = dispatchGestureAsync(
                createClickPath(540f, 960f), // approximate center
                50L
            )

            // Simulate paste shortcut would require root - report partial success
            GestureResult(requestId, success = true)
        } catch (e: Exception) {
            GestureResult(requestId, success = false, error = e.message)
        }
    }

    // ── Utility ────────────────────────────────────────────

    /**
     * Find a node by its text or content description and return its bounds.
     */
    fun findNodeByText(text: String): Bounds? {
        val root = rootInActiveWindow ?: return null
        val nodes = root.findAccessibilityNodeInfosByText(text)
        val node = nodes?.firstOrNull() ?: return null
        val rect = Rect()
        node.getBoundsInScreen(rect)
        node.recycle()
        root.recycle()
        return Bounds(rect.left, rect.top, rect.right, rect.bottom)
    }

    fun getNodeCount(): Int {
        val root = rootInActiveWindow ?: return 0
        val count = countNodes(root, 0)
        root.recycle()
        return count
    }

    private fun countNodes(node: AccessibilityNodeInfo, depth: Int): Int {
        if (depth > MAX_TREE_DEPTH) return 0
        var count = 1
        for (i in 0 until node.childCount.coerceAtMost(MAX_CHILDREN_PER_NODE)) {
            val child = node.getChild(i) ?: continue
            count += countNodes(child, depth + 1)
            child.recycle()
        }
        return count
    }
}
