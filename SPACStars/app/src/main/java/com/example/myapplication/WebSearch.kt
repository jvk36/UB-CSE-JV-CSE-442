package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_web_search.*

class WebSearch : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_search)
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        val url = savedInstanceState?.getString("url") ?: intent.getStringExtra("url")

        webView.loadUrl(url!!)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("url", webView.url)
    }
}
