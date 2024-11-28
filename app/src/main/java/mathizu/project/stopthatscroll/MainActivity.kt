package mathizu.project.stopthatscroll

import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var isBlockingEnabled = false
    private var isYouTubeBlocked = false
    private var isInstagramBlocked = false
    private var isTikTokBlocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mainButton: Button = findViewById(R.id.main_toggle_button)
        val youtubeSwitch: Switch = findViewById(R.id.youtube_switch)
        val instagramSwitch: Switch = findViewById(R.id.instagram_switch)
        val tiktokSwitch: Switch = findViewById(R.id.tiktok_switch)

        // Gros bouton pour activer/désactiver le blocage général
        mainButton.setOnClickListener {
            isBlockingEnabled = !isBlockingEnabled
            mainButton.text = if (isBlockingEnabled) "Actif" else "Bloquer"
            Toast.makeText(this, if (isBlockingEnabled) "Blocage activé" else "Blocage désactivé", Toast.LENGTH_SHORT).show()
        }

        // Switch pour YouTube
        youtubeSwitch.setOnCheckedChangeListener { _, isChecked ->
            isYouTubeBlocked = isChecked
            Toast.makeText(this, if (isChecked) "YouTube Shorts bloqués" else "YouTube Shorts débloqués", Toast.LENGTH_SHORT).show()
        }

        // Switch pour Instagram
        instagramSwitch.setOnCheckedChangeListener { _, isChecked ->
            isInstagramBlocked = isChecked
            Toast.makeText(this, if (isChecked) "Instagram Reels bloqués" else "Instagram Reels débloqués", Toast.LENGTH_SHORT).show()
        }

        // Switch pour TikTok
        tiktokSwitch.setOnCheckedChangeListener { _, isChecked ->
            isTikTokBlocked = isChecked
            Toast.makeText(this, if (isChecked) "TikTok bloqué" else "TikTok débloqué", Toast.LENGTH_SHORT).show()
        }
    }
}