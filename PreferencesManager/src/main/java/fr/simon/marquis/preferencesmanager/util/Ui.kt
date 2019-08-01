/*
 * Copyright (C) 2013 Simon Marquis (http://www.simon-marquis.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package fr.simon.marquis.preferencesmanager.util

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
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
        val span2 = StyleSpan(android.graphics.Typeface.BOLD)
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

