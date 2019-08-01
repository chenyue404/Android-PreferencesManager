package fr.simon.marquis.preferencesmanager.ui

/*
 * Copyright (C) 2014 Chris Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView

import fr.simon.marquis.preferencesmanager.R

/**
 * Layout which an [android.widget.EditText] to show a floating label when the hint is hidden
 * due to the user inputting text.
 *
 * @see [Matt D. Smith on Dribble](https://dribbble.com/shots/1254439--GIF-Mobile-Form-Interaction)
 *
 * @see [Brad Frost's blog post](http://bradfrostweb.com/blog/post/float-label-pattern/)
 */
class FloatLabelLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : FrameLayout(context, attrs, defStyle) {

    private var mEditText: EditText? = null
    private val mLabel: TextView

    init {

        val a = context
                .obtainStyledAttributes(attrs, R.styleable.FloatLabelLayout)

        val sidePadding = a.getDimensionPixelSize(
                R.styleable.FloatLabelLayout_floatLabelSidePadding,
                dipsToPix())

        mLabel = TextView(context)
        mLabel.setPadding(sidePadding, 0, sidePadding, 0)
        mLabel.visibility = View.INVISIBLE

        mLabel.setTextAppearance(
                a.getResourceId(R.styleable.FloatLabelLayout_floatLabelTextAppearance,
                        android.R.style.TextAppearance_Small)
        )

        addView(mLabel, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

        a.recycle()
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        var layoutParams = params
        if (child is EditText) {
            // If we already have an EditText, throw an exception
            if (mEditText != null) {
                throw IllegalArgumentException("We already have an EditText, can only have one")
            }

            // Update the layout params so that the EditText is at the bottom, with enough top
            // margin to show the label
            val lp = LayoutParams(layoutParams)
            lp.gravity = Gravity.BOTTOM
            lp.topMargin = mLabel.textSize.toInt()
            layoutParams = lp

            setEditText(child)
        }

        // Carry on adding the View...
        super.addView(child, index, layoutParams)
    }

    private fun setEditText(editText: EditText) {
        mEditText = editText

        // Add a TextWatcher so that we know when the text input has changed
        mEditText!!.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                if (TextUtils.isEmpty(s)) {
                    // The text is empty, so hide the label if it is visible
                    if (mLabel.visibility == View.VISIBLE) {
                        hideLabel()
                    }
                } else {
                    // The text is not empty, so show the label if it is not visible
                    if (mLabel.visibility != View.VISIBLE) {
                        showLabel()
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

        })

        // Add focus listener to the EditText so that we can notify the label that it is activated.
        // Allows the use of a ColorStateList for the text color on the label
        mEditText!!.setOnFocusChangeListener { _, focused -> mLabel.isActivated = focused }

        mLabel.text = mEditText!!.hint
    }

    /**
     * Show the label using an animation
     */
    private fun showLabel() {
        mLabel.visibility = View.VISIBLE
        mLabel.alpha = 0f
        mLabel.translationY = mLabel.height.toFloat()
        mLabel.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(ANIMATION_DURATION)
                .setListener(null).start()
    }

    /**
     * Hide the label using an animation
     */
    private fun hideLabel() {
        mLabel.alpha = 1f
        mLabel.translationY = 0f
        mLabel.animate()
                .alpha(0f)
                .translationY(mLabel.height.toFloat())
                .setDuration(ANIMATION_DURATION)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        mLabel.visibility = View.GONE
                    }
                }).start()
    }

    /**
     * Helper method to convert dips to pixels.
     */
    private fun dipsToPix(): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_PADDING_LEFT_RIGHT_DP,
                resources.displayMetrics).toInt()
    }

    companion object {

        private const val ANIMATION_DURATION: Long = 150

        private const val DEFAULT_PADDING_LEFT_RIGHT_DP = 12f
    }
}