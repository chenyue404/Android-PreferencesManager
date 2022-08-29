package fr.simon.marquis.preferencesmanager.ui.components

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

fun Context.showToast(@StringRes res: Int? = null, text: String? = null) {
    val message = res?.let {
        this.getString(it)
    }?.let {
        text
    }

    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
