package mathizu.project.stopthatscroll

import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityManager
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Vérification des optimisations de batterie
        checkBatteryOptimizations()

        val serviceIntent = Intent(this, BlockAccessibilityService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)

        // Initialisation des préférences partagées
        preferences = getSharedPreferences("StopThatScrollPrefs", Context.MODE_PRIVATE)

        // Initialisation des vues
        val mainButton: Button = findViewById(R.id.main_toggle_button)
        val youtubeSwitch: Switch = findViewById(R.id.youtube_switch)
        val instagramSwitch: Switch = findViewById(R.id.instagram_switch)
        val tiktokSwitch: Switch = findViewById(R.id.tiktok_switch)

        // Charger l'état initial des préférences
        val isBlockingEnabled = preferences.getBoolean("isBlockingEnabled", false)
        mainButton.text = if (isBlockingEnabled) "Actif" else "Bloquer"

        youtubeSwitch.isChecked = preferences.getBoolean("isYouTubeBlocked", false)
        instagramSwitch.isChecked = preferences.getBoolean("isInstagramBlocked", false)
        tiktokSwitch.isChecked = preferences.getBoolean("isTikTokBlocked", false)

        // Gérer les actions des boutons et switches
        mainButton.setOnClickListener {
            if (!isAccessibilityServiceEnabled()) {
                redirectToAccessibilitySettings()
                return@setOnClickListener
            }

            val newState = !preferences.getBoolean("isBlockingEnabled", false)
            mainButton.text = if (newState) "Actif" else "Bloquer"
            preferences.edit().putBoolean("isBlockingEnabled", newState).apply()
            Toast.makeText(this, if (newState) "Blocage activé" else "Blocage désactivé", Toast.LENGTH_SHORT).show()
        }

        youtubeSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit().putBoolean("isYouTubeBlocked", isChecked).apply()
            Toast.makeText(this, if (isChecked) "YouTube Shorts bloqués" else "YouTube Shorts débloqués", Toast.LENGTH_SHORT).show()
        }

        instagramSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit().putBoolean("isInstagramBlocked", isChecked).apply()
            Toast.makeText(this, if (isChecked) "Instagram Reels bloqués" else "Instagram Reels débloqués", Toast.LENGTH_SHORT).show()
        }

        tiktokSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit().putBoolean("isTikTokBlocked", isChecked).apply()
            Toast.makeText(this, if (isChecked) "TikTok bloqué" else "TikTok débloqué", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Vérifie si le service d'accessibilité est activé.
     */
    private fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        val serviceName = "${packageName}/${BlockAccessibilityService::class.java.canonicalName}"
        return enabledServices?.contains(serviceName) == true
    }

    /**
     * Redirige l'utilisateur vers les paramètres d'accessibilité pour activer le service.
     */
    private fun redirectToAccessibilitySettings() {
        Toast.makeText(this, "Activez le service d'accessibilité pour utiliser StopThatScroll.", Toast.LENGTH_LONG).show()
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    /**
     * Vérifie et demande à l'utilisateur de désactiver les optimisations de batterie.
     */
    private fun checkBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            val packageName = packageName
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                Toast.makeText(
                    this,
                    "Pour un fonctionnement optimal, désactivez les optimisations de batterie pour StopThatScroll.",
                    Toast.LENGTH_LONG
                ).show()
                startActivity(intent)
            }
        }
    }
}
