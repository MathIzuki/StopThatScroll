package mathizu.project.stopthatscroll

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.SharedPreferences
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import androidx.core.app.NotificationCompat

class BlockAccessibilityService : AccessibilityService() {

    private lateinit var preferences: SharedPreferences


    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val rootNode = rootInActiveWindow ?: return

        // Lire l'état des préférences
        val preferences = getSharedPreferences("StopThatScrollPrefs", Context.MODE_PRIVATE)
        val isBlockingEnabled = preferences.getBoolean("isBlockingEnabled", false)
        val isYouTubeBlocked = preferences.getBoolean("isYouTubeBlocked", false)
        val isInstagramBlocked = preferences.getBoolean("isInstagramBlocked", false)
        val isTikTokBlocked = preferences.getBoolean("isTikTokBlocked", false)

        // Si le blocage global est désactivé, ne rien faire
        if (!isBlockingEnabled) return

        // Bloquer en fonction des préférences
        if (isYouTubeBlocked && containsYouTubeShorts(rootNode)) {
            performGlobalAction(GLOBAL_ACTION_BACK)
            Toast.makeText(this, "Accès aux Shorts YouTube bloqué !", Toast.LENGTH_SHORT).show()
        }

        if (isInstagramBlocked && containsReels(rootNode)) {
            performGlobalAction(GLOBAL_ACTION_BACK)
            Toast.makeText(this, "Accès aux Reels Instagram bloqué !", Toast.LENGTH_SHORT).show()
        }

        if (isTikTokBlocked && isTikTokApp(event)) {
            performGlobalAction(GLOBAL_ACTION_HOME)
            Toast.makeText(this, "Accès à TikTok bloqué !", Toast.LENGTH_SHORT).show()
        }
    }

    // Vérifie si le contenu est un Reel sur Instagram
    private fun containsReels(node: AccessibilityNodeInfo): Boolean {
        // Vérifie si le texte "Reels" apparaît dans la vue
        if (node.text?.toString()?.equals("Reels", ignoreCase = true) == true) {
            return true
        }

        // Parcourt les enfants pour trouver le texte
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let {
                if (containsReels(it)) return true
            }
        }

        return false
    }

    // Vérifie si le contenu est un Short sur YouTube
    private fun containsYouTubeShorts(node: AccessibilityNodeInfo): Boolean {
        // Log les vues pour voir ce qui se trouve dans le nœud
        logViews(node)

        // YouTube Shorts peuvent être identifiés par des caractéristiques spécifiques (ID, texte, ou structure de l'interface)
        val shortsText = node.text?.toString()?.contains("Shorts", ignoreCase = true) == true
        val shortsId = node.viewIdResourceName?.contains("shorts", ignoreCase = true) == true
        val videoClass = node.className?.toString()?.contains("android.widget.VideoView", ignoreCase = true) == true

        // Ignore les éléments qui ne sont pas des vidéos
        val ignoredClasses = listOf("android.widget.Button", "android.widget.ImageView", "android.widget.TextView")
        val isIgnoredClass = node.className?.toString() in ignoredClasses

        // Retourne true si un des critères de Shorts est trouvé et si ce n'est pas une classe ignorée
        return (shortsText || shortsId || videoClass) && !isIgnoredClass
    }



    // Vérifie si l'application active est TikTok
    private fun isTikTokApp(event: AccessibilityEvent): Boolean {
        // Vérifie si le package de l'application active correspond à TikTok
        val packageName = event.packageName?.toString()
        return packageName == "com.zhiliaoapp.musically" // Package ID de TikTok
    }


    // Log toutes les vues accessibles dans l'interface utilisateur
    private fun logViews(node: AccessibilityNodeInfo) {
        // Si le nœud est une feuille, on l'enregistre
        if (node.childCount == 0) {
            Log.d(
                "AppBlockerYtb",
                "View ID: ${node.viewIdResourceName}, Text: ${node.text}, Class: ${node.className}, Package: ${node.packageName}"
            )
        } else {
            // Si le nœud a des enfants, on les parcourt
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { logViews(it) }
            }
        }
    }

    // Interruption du service
    override fun onInterrupt() {
        Log.d("BlockAccessibilityService", "Service interrompu")
    }


    override fun onServiceConnected() {
        super.onServiceConnected()

        // Vérification pour éviter les exceptions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundServiceSafely()
        }

        // Configuration de base
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        }
        this.serviceInfo = info

        Toast.makeText(this, "Service de blocage activé", Toast.LENGTH_SHORT).show()
    }

    private fun startForegroundServiceSafely() {
        val notificationId = 1
        val channelId = "service_channel"

        // Crée le canal de notification pour Android 8.0 et supérieur
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Service Notifications",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Intent pour ouvrir l'application en cliquant sur la notification
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        // Construire la notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("StopThatScroll")
            .setContentText("Le service de blocage est actif")
            .setSmallIcon(R.drawable.ic_notification) // Remplace par un ID valide d'icône
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "service_channel", // ID du canal
                "Service Notifications", // Nom du canal
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        // Démarrer le service au premier plan
        startForeground(notificationId, notification)
    }


}
