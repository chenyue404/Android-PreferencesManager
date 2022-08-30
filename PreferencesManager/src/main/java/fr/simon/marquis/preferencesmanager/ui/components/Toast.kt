package fr.simon.marquis.preferencesmanager.ui.components

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

fun Context.showToast(@StringRes res: Int) {
    val string = getString(res)
    showToast(string)
}

fun Context.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}
