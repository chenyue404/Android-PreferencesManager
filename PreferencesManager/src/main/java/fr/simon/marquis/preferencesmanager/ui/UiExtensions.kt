package fr.simon.marquis.preferencesmanager.ui

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorRes
import com.afollestad.materialdialogs.MaterialDialog
import fr.simon.marquis.preferencesmanager.R
import java.util.regex.Pattern

fun Context.hideSoftKeyboard(view: View) {
    val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun createSpannable(pattern: Pattern?, color: Int, s: String): SpannableStringBuilder {
    val spannable = SpannableStringBuilder(s)
    if (pattern == null)
        return spannable
    val matcher = pattern.matcher(s)
    while (matcher.find()) {
        val span = ForegroundColorSpan(color)
        val span2 = StyleSpan(Typeface.BOLD)
        spannable.setSpan(span2, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(span, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    return spannable
}

fun Context.animateView(view: View, show: Boolean, animate: Boolean) {
    view.visibility = if (show) View.VISIBLE else View.GONE
    if (animate) {
        val animation = AnimationUtils.loadAnimation(this, if (show) android.R.anim.fade_in else android.R.anim.fade_out)
        if (animation != null) {
            view.startAnimation(animation)
        }
    }
}

fun Activity.displayNoRoot() {
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

fun View.hide() {
    this.visibility = View.GONE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun Context.getResColor(@ColorRes color: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        this.resources.getColor(color, null)
    } else {
        @Suppress("DEPRECATION")
        this.resources.getColor(color)
    }
}