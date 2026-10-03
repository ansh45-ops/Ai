package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlin.math.abs

class JarvisFloatingService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var params: WindowManager.LayoutParams? = null

    private var isExpanded = false

    companion object {
        const val CHANNEL_ID = "jarvis_floating_hud_channel"
        const val NOTIFICATION_ID = 4201
        const val ACTION_STOP = "com.example.action.STOP_FLOATING"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = buildForegroundNotification()
        startForeground(NOTIFICATION_ID, notification)

        if (Settings.canDrawOverlays(this)) {
            initFloatingOverlay()
        } else {
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initFloatingOverlay() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 80
            y = 300
        }

        // Programmatically build the floating Sci-Fi HUD View
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(12, 12, 12, 12)
        }

        // Circular Arc-Reactor Button
        val arcReactorButton = ImageView(this).apply {
            setImageResource(R.drawable.ic_jarvis_logo)
            val sizePx = (58 * resources.displayMetrics.density).toInt()
            layoutParams = LinearLayout.LayoutParams(sizePx, sizePx)
            val glowBg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#E60A162B"))
                setStroke((2 * resources.displayMetrics.density).toInt(), Color.parseColor("#00E5FF"))
            }
            background = glowBg
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setPadding(8, 8, 8, 8)
            elevation = 16f
        }

        // Expanded Mini-HUD Panel
        val panelLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val panelBg = GradientDrawable().apply {
                setColor(Color.parseColor("#F2081226"))
                cornerRadius = 16 * resources.displayMetrics.density
                setStroke((1.5f * resources.displayMetrics.density).toInt(), Color.parseColor("#00E5FF"))
            }
            background = panelBg
            setPadding(24, 20, 24, 20)
            visibility = View.GONE
            val widthPx = (240 * resources.displayMetrics.density).toInt()
            layoutParams = LinearLayout.LayoutParams(widthPx, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (10 * resources.displayMetrics.density).toInt()
            }
        }

        val hudTitle = TextView(this).apply {
            text = "J.A.R.V.I.S. MINI-HUD"
            setTextColor(Color.parseColor("#00E5FF"))
            textSize = 13f
            typeface = android.graphics.Typeface.MONOSPACE
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 12)
        }

        val btnVoice = Button(this).apply {
            text = "⚡ VOICE DIRECTIVE"
            setTextColor(Color.parseColor("#E0F7FA"))
            textSize = 11f
            val btnBg = GradientDrawable().apply {
                setColor(Color.parseColor("#1A00E5FF"))
                cornerRadius = 8 * resources.displayMetrics.density
                setStroke(1, Color.parseColor("#00E5FF"))
            }
            background = btnBg
            setOnClickListener {
                openAppWithMode("VOICE")
            }
        }

        val btnScreen = Button(this).apply {
            text = "🎯 SCREEN ANALYSIS"
            setTextColor(Color.parseColor("#E0F7FA"))
            textSize = 11f
            val btnBg = GradientDrawable().apply {
                setColor(Color.parseColor("#1A00E5FF"))
                cornerRadius = 8 * resources.displayMetrics.density
                setStroke(1, Color.parseColor("#00E5FF"))
            }
            background = btnBg
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 12
            }
            setOnClickListener {
                openAppWithMode("SCREEN_ANALYSIS")
            }
        }

        val btnOpenFull = Button(this).apply {
            text = "💠 EXPAND INTERFACE"
            setTextColor(Color.parseColor("#80DEEA"))
            textSize = 11f
            val btnBg = GradientDrawable().apply {
                setColor(Color.parseColor("#102242"))
                cornerRadius = 8 * resources.displayMetrics.density
                setStroke(1, Color.parseColor("#4000E5FF"))
            }
            background = btnBg
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 12
            }
            setOnClickListener {
                openAppWithMode("CHAT")
            }
        }

        val btnClose = Button(this).apply {
            text = "✕ DISMISS HUD"
            setTextColor(Color.parseColor("#FF5252"))
            textSize = 10f
            background = null
            setOnClickListener {
                stopSelf()
            }
        }

        panelLayout.addView(hudTitle)
        panelLayout.addView(btnVoice)
        panelLayout.addView(btnScreen)
        panelLayout.addView(btnOpenFull)
        panelLayout.addView(btnClose)

        rootLayout.addView(arcReactorButton)
        rootLayout.addView(panelLayout)

        // Drag & Touch Handling
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isClick = false

        arcReactorButton.setOnTouchListener { _, event ->
            val p = params ?: return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = p.x
                    initialY = p.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isClick = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (abs(dx) > 10 || abs(dy) > 10) {
                        isClick = false
                    }
                    p.x = initialX + dx
                    p.y = initialY + dy
                    windowManager?.updateViewLayout(rootLayout, p)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        isExpanded = !isExpanded
                        panelLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
                    }
                    true
                }
                else -> false
            }
        }

        floatingView = rootLayout
        try {
            windowManager?.addView(rootLayout, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openAppWithMode(mode: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("EXTRA_MODE", mode)
        }
        startActivity(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "JARVIS Floating Assistant",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active floating HUD overlay for JARVIS AI"
                enableLights(false)
                enableVibration(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val pendingOpen = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, JarvisFloatingService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStop = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("J.A.R.V.I.S. Floating HUD Active")
            .setContentText("Tap to open assistant • Floating arc reactor on screen")
            .setSmallIcon(R.drawable.ic_jarvis_logo)
            .setContentIntent(pendingOpen)
            .addAction(R.drawable.ic_jarvis_logo, "Dismiss HUD", pendingStop)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        floatingView = null
    }
}
