package mathizu.project.stopthatscroll

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class BlockAccessibilityService : AccessibilityService() {



    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val rootNode = rootInActiveWindow ?: return

        // Log des vues pour voir tous les éléments d'accessibilité
        logViews(rootNode)

        // Bloque YouTube Shorts
        if (containsYouTubeShorts(rootNode)) {
            performGlobalAction(GLOBAL_ACTION_BACK)  // Revenir à la page précédente
            Toast.makeText(this, "Accès aux Shorts YouTube bloqué !", Toast.LENGTH_SHORT).show()
        }

        // Bloque Instagram Reels
        if (containsReels(rootNode)) {
            performGlobalAction(GLOBAL_ACTION_BACK)  // Revenir à la page précédente
            Toast.makeText(this, "Accès aux Reels Instagram bloqué !", Toast.LENGTH_SHORT).show()
        }

        // Bloque TikTok (entièrement, pas spécifique aux éléments)
        if (isTikTokApp(event)) {
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

    // Configuration du service d'accessibilité
    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS // Permet d'accéder aux IDs de vues
        }
        this.serviceInfo = info
        Toast.makeText(this, "Service de blocage activé", Toast.LENGTH_SHORT).show()
    }
}
