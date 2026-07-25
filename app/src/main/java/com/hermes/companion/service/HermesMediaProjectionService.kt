package com.hermes.companion.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.ByteArrayOutputStream

/**
 * Foreground service for screen capture via MediaProjection.
 *
 * Responsibilities:
 * - Acquire MediaProjection from user consent
 * - Capture screenshots on-demand as Base64-encoded images
 * - Support continuous capture mode at configurable intervals
 * - Integration with Hermes Brain vision pipeline
 * - Manage VirtualDisplay lifecycle
 */
class HermesMediaProjectionService : Service() {

    companion object {
        private const val TAG = "MediaProjection"
        private const val CHANNEL_ID = "hermes_screen_channel"
        private const val NOTIFICATION_ID = 1003
        private const val DEFAULT_QUALITY = 80
        private const val DEFAULT_MAX_DIMENSION = 1024

        const val ACTION_START = "com.hermes.companion.action.START_CAPTURE"
        const val ACTION_STOP = "com.hermes.companion.action.STOP_CAPTURE"
        const val ACTION_CAPTURE_ONCE = "com.hermes.companion.action.CAPTURE_ONCE"
        const val ACTION_START_CONTINUOUS = "com.hermes.companion.action.START_CONTINUOUS"
        const val EXTRA_RESULT_CODE = "resultCode"
        const val EXTRA_RESULT_DATA = "resultData"
        const val EXTRA_INTERVAL_MS = "intervalMs"

        /** Static reference */
        @Volatile
        var instance: HermesMediaProjectionService? = null
            private set

        @Volatile
        var isRunning: Boolean = false
            private set
    }

    // ── State ──────────────────────────────────────────────

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _screenshots = MutableSharedFlow<String>(
        extraBufferCapacity = 16,
        onBufferOverflow = SharedFlow.OverflowStrategy.DROP_OLDEST
    )
    /** Emits Base64-encoded screenshot images on each capture. */
    val screenshots: SharedFlow<String> = _screenshots.asSharedFlow()

    private val _captureState = MutableStateFlow(false)
    val captureState: StateFlow<Boolean> = _captureState.asStateFlow()

    private var continuousJob: Job? = null
    private var screenWidth = 1080
    private var screenHeight = 1920
    private var screenDpi = 320

    // ── Lifecycle ──────────────────────────────────────────

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
        getScreenMetrics()
        Log.i(TAG, "MediaProjectionService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, -1)
                val resultData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_RESULT_DATA)
                }
                if (resultCode != -1 && resultData != null) {
                    startCapture(resultCode, resultData)
                }
            }
            ACTION_STOP -> {
                stopCapture()
                stopSelf()
            }
            ACTION_CAPTURE_ONCE -> {
                captureScreenshot()
            }
            ACTION_START_CONTINUOUS -> {
                val interval = intent.getLongExtra(EXTRA_INTERVAL_MS, 1000L)
                startContinuousCapture(interval)
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopCapture()
        instance = null
        serviceScope.cancel()
        super.onDestroy()
    }

    // ── Capture Setup ──────────────────────────────────────

    private fun getScreenMetrics() {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        screenDpi = metrics.densityDpi
    }

    private fun startCapture(resultCode: Int, resultData: Intent) {
        startForeground(NOTIFICATION_ID, buildNotification("Screen capture active"))
        isRunning = true

        val projManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projManager.getMediaProjection(resultCode, resultData)

        mediaProjection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                Log.i(TAG, "MediaProjection stopped externally")
                stopCapture()
            }

            override fun onCapturedFrameResize(width: Int, height: Int) {
                Log.d(TAG, "Frame size changed: ${width}x${height}")
            }
        }, null)

        setupVirtualDisplay()
        _captureState.value = true
        updateNotification("Screen capture active")
        Log.i(TAG, "Screen capture started: ${screenWidth}x${screenHeight}")
    }

    private fun setupVirtualDisplay() {
        val w = screenWidth.coerceAtMost(DEFAULT_MAX_DIMENSION)
        val h = screenHeight.coerceAtMost(DEFAULT_MAX_DIMENSION)

        imageReader = ImageReader.newInstance(
            w, h,
            PixelFormat.RGBA_8888,
            2 // max images in buffer
        )

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "HermesCapture",
            w, h, screenDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            serviceScope.coroutineContext[CoroutineDispatcher]?.let { null }
        )

        Log.d(TAG, "VirtualDisplay created: ${w}x${h} @ ${screenDpi}dpi")
    }

    // ── Screenshot Capture ─────────────────────────────────

    /**
     * Capture a single screenshot and emit the Base64 result.
     */
    fun captureScreenshot() {
        serviceScope.launch {
            try {
                val base64 = captureImageAsBase64()
                if (base64 != null) {
                    _screenshots.emit(base64)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Screenshot capture failed: ${e.message}")
            }
        }
    }

    /**
     * Returns a suspend function that captures and returns Base64.
     */
    suspend fun getScreenshotAsBase64(quality: Int = DEFAULT_QUALITY): String? {
        return withContext(Dispatchers.IO) {
            captureImageAsBase64(quality)
        }
    }

    private fun captureImageAsBase64(quality: Int = DEFAULT_QUALITY): String? {
        val reader = imageReader ?: return null

        val image = try {
            reader.acquireLatestImage()
        } catch (e: Exception) {
            Log.e(TAG, "acquireLatestImage failed: ${e.message}")
            return null
        } ?: return null

        return try {
            val plane = image.planes[0]
            val buffer = plane.buffer
            val pixelStride = plane.pixelStride
            val rowStride = plane.rowStride
            val rowPadding = rowStride - pixelStride * image.width

            val bitmap = Bitmap.createBitmap(
                image.width + rowPadding / pixelStride,
                image.height,
                Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(buffer)

            // Crop to actual screen size if needed
            val cropped = if (bitmap.width > image.width || bitmap.height > image.height) {
                Bitmap.createBitmap(bitmap, 0, 0, image.width, image.height).also {
                    if (it != bitmap) bitmap.recycle()
                }
            } else {
                bitmap
            }

            val outputStream = ByteArrayOutputStream()
            cropped.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            cropped.recycle()

            Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Image encoding failed: ${e.message}")
            null
        } finally {
            image.close()
        }
    }

    // ── Continuous Capture ─────────────────────────────────

    fun startContinuousCapture(intervalMs: Long = 1000L) {
        stopContinuousCapture()
        continuousJob = serviceScope.launch {
            while (isActive && _captureState.value) {
                captureScreenshot()
                delay(intervalMs)
            }
        }
        Log.i(TAG, "Continuous capture started: ${intervalMs}ms interval")
    }

    fun stopContinuousCapture() {
        continuousJob?.cancel()
        continuousJob = null
    }

    private fun stopCapture() {
        stopContinuousCapture()
        _captureState.value = false
        virtualDisplay?.release()
        virtualDisplay = null
        imageReader?.close()
        imageReader = null
        mediaProjection?.stop()
        mediaProjection = null
        isRunning = false
        updateNotification("Screen capture stopped")
    }

    // ── Notification ───────────────────────────────────────

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Screen Capture",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active screen capture session"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Hermes Screen Capture")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(text: String) {
        try {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(NOTIFICATION_ID, buildNotification(text))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update notification: ${e.message}")
        }
    }
}
