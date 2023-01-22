package fr.simon.marquis.preferencesmanager

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.topjohnwu.superuser.Shell
import fr.simon.marquis.preferencesmanager.ui.applist.AppListActivity
import kotlinx.coroutines.launch

/**
 * A Splash activity to launch libsu shell request
 */
@SuppressLint("CustomSplashScreen")
class LaunchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen().setKeepOnScreenCondition { true }
        lifecycleScope.launch {
            Shell.getShell()
            startActivity(Intent(this@LaunchActivity, AppListActivity::class.java))
            finish()
        }
    }
}
