package com.example.gocode

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val versionTv = findViewById<TextView>(R.id.tvVersion)
        val versionName = packageManager
            .getPackageInfo(packageName, 0)
            .versionName

        versionTv.text = "Version $versionName"
    }
}
