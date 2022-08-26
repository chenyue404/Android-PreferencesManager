package fr.simon.marquis.preferencesmanager.ui

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorRes
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
