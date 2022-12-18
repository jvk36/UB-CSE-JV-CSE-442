package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import kotlinx.android.synthetic.main.activity_search_social_media.*
import java.net.URLEncoder

class SearchSocialMedia : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_social_media)
        btnGoogle.setOnClickListener {
            loadWebActivity("https://google.com/search?q=$encodedSearchTerm")
        }

        btnTwitter.setOnClickListener{
            loadWebActivity("https://twitter.com/search?q=$encodedSearchTerm")
        }

        btnReddit.setOnClickListener{
            loadWebActivity("https://www.reddit.com/search/?q=$encodedSearchTerm")
        }

        btnFacebook.setOnClickListener {
            loadWebActivity("https://www.facebook.com/search/top?q=$encodedSearchTerm")
        }

        etSearch.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                toggleButtonsState(etSearch.text.isNotEmpty())
            }
        })

        toggleButtonsState(false)
    }

    private val encodedSearchTerm : String
        get() = URLEncoder.encode(etSearch.text.toString(),"UTF-8")

    private fun loadWebActivity(url: String) {
        val intent = Intent(this, WebSearch::class.java)
        intent.putExtra("url", url)
        startActivity(intent)
    }

    private fun toggleButtonsState(enabled: Boolean){
        btnGoogle.isEnabled = enabled
        btnTwitter.isEnabled = enabled
        btnReddit.isEnabled = enabled
        btnFacebook.isEnabled = enabled
    }
}
