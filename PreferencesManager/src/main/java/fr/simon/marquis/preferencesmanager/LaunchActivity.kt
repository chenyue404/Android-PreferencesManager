package fr.simon.marquis.preferencesmanager

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.topjohnwu.superuser.Shell
import fr.simon.marquis.preferencesmanager.ui.applist.AppListActivity

/**
 * A Splash activity to launch libsu shell request
 */
class LaunchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Shell.getShell {
            val intent = Intent(this, AppListActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
