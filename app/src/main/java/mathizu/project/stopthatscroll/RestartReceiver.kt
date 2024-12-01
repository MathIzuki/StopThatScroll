package mathizu.project.stopthatscroll

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == "android.intent.action.BOOT_COMPLETED") {
            val serviceIntent = Intent(context, BlockAccessibilityService::class.java)
            context.startService(serviceIntent)
        }
    }
}
