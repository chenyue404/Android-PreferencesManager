package fr.simon.marquis.preferencesmanager.ui

import android.app.Activity
import com.afollestad.materialdialogs.MaterialDialog
import fr.simon.marquis.preferencesmanager.R

fun Activity.rootDialog() {

    MaterialDialog(this).show {
        title(R.string.no_root_title)
        message(R.string.no_root_message)
        icon(R.drawable.ic_action_emo_evil)
        positiveButton(R.string.no_root_button) {
            finish()
        }
        cancelOnTouchOutside(false)
    }

}

fun Activity.aboutDialog() {

    val appVersion = this.packageManager.getPackageInfo(packageName, 0).versionName
    val appTitle = getString(R.string.app_name) + "\n" + appVersion

    MaterialDialog(this).show {
        title(text = appTitle)
        icon(R.drawable.ic_launcher)
        cancelOnTouchOutside(false)
        message(R.string.about_body) { html() }
        positiveButton(R.string.close)
    }
}