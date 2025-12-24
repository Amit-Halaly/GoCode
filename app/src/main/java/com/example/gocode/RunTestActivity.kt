package com.example.gocode

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gocode.network.ApiClient
import com.example.gocode.network.RunRequest
import kotlinx.coroutines.launch

class RunTestActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_test)

        val btnRun = findViewById<Button>(R.id.btnRun)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        btnRun.setOnClickListener {
            tvResult.text = "Running..."

            lifecycleScope.launch {
                try {
                    val code = """
                        public class Main {
                            public static void main(String[] args) {
                                System.out.println("Hello from GoCode!");
                            }
                        }
                    """.trimIndent()

                    val res = ApiClient.execApi.run(
                        RunRequest(language = "java", code = code, input = "")
                    )

                    tvResult.text = buildString {
                        appendLine("exitCode: ${res.exitCode}")
                        appendLine("output:")
                        appendLine(res.output)
                        appendLine("error:")
                        appendLine(res.error)
                    }
                } catch (e: Exception) {
                    tvResult.text = "Request failed: ${e.message}"
                }
            }
        }
    }
}
