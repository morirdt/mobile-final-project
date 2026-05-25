package com.example.mobilefinalproject.ui.common

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar

/**
 * Simple reusable full-screen loading overlay that can be attached to any fragment root.
 */
class LoadingOverlayController(
    context: Context,
    private val root: ViewGroup
) {
    private val overlay: FrameLayout = FrameLayout(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        setBackgroundColor(Color.parseColor("#66000000"))
        isClickable = true
        isFocusable = true
        visibility = View.GONE

        addView(
            ProgressBar(context).apply {
                isIndeterminate = true
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        )
    }

    init {
        root.addView(overlay)
    }

    fun show() {
        overlay.visibility = View.VISIBLE
        overlay.bringToFront()
    }

    fun hide() {
        overlay.visibility = View.GONE
    }

    fun detach() {
        root.removeView(overlay)
    }
}


