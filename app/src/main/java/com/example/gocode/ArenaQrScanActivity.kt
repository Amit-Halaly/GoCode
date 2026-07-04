package com.example.gocode

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.journeyapps.barcodescanner.CaptureActivity

class ArenaQrScanActivity : CaptureActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addBackButton()
    }

    private fun addBackButton() {
        val density = resources.displayMetrics.density
        val button = TextView(this).apply {
            text = "Back"
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            textSize = 15f
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                cornerRadius = 8f * density
                setColor(Color.argb(210, 13, 25, 42))
                setStroke((1.5f * density).toInt(), Color.argb(230, 55, 242, 182))
            }
            setOnClickListener {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }

        val params = FrameLayout.LayoutParams(
            (96f * density).toInt(),
            (46f * density).toInt(),
            Gravity.TOP or Gravity.START
        ).apply {
            topMargin = (22f * density).toInt()
            marginStart = (18f * density).toInt()
        }
        addContentView(button, params as ViewGroup.LayoutParams)
    }
}
