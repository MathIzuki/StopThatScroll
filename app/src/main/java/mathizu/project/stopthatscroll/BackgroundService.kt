package mathizu.project.stopthatscroll

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class BackgroundService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("BackgroundService", "Service en arrière-plan démarré")

        // Ici, vous pouvez effectuer des tâches comme surveiller des applications ou enregistrer des données
        performBackgroundTask()

        return START_STICKY // Le service redémarre automatiquement si le système l’arrête
    }

    private fun performBackgroundTask() {
        // Exécution d'une tâche (vous pouvez personnaliser)
        Log.d("BackgroundService", "Tâche en cours dans le service")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // Ce service ne supporte pas la liaison
    }

    override fun onDestroy() {
        Log.d("BackgroundService", "Service en arrière-plan arrêté")
        super.onDestroy()
    }
}
