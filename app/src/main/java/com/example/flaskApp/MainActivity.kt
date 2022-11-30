package com.example.flaskApp

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform


class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var flaskThread: FlaskThread

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        supportActionBar?.hide()
        val mainActivity: Activity = this

        if (! Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        this.flaskThread = FlaskThread()
        flaskThread.start()

        // Flask takes about 2 seconds to start
        try {
            Thread.sleep(2 * 1000)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        this.webView = findViewById(R.id.web)
        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String,
                failingUrl: String
            ) {
                view.loadUrl("http://127.0.0.1:2022")
            }
        }

        // Remove this to show bottom android navbar
        mainActivity.window.decorView.setSystemUiVisibility(3846)

        // Credits: Some website i found
        // For those who dont know, this adds fullscreen support
        webView.webChromeClient = object : WebChromeClient() {
            private var mCustomView: View? = null
            private var mCustomViewCallback: CustomViewCallback? = null
            private var mOriginalOrientation = 0
            private var mOriginalSystemUiVisibility = 0
            override fun getDefaultVideoPoster(): Bitmap? {
                return BitmapFactory.decodeResource(
                    mainActivity.applicationContext.resources, 2130837573
                )
            }

            override fun onHideCustomView() { // Normal
                (mainActivity.window.decorView as FrameLayout).removeView(mCustomView)
                mCustomView = null
                mainActivity.window.decorView.setSystemUiVisibility(
                    mOriginalSystemUiVisibility
                )
                mainActivity.requestedOrientation = mOriginalOrientation
                mCustomViewCallback!!.onCustomViewHidden()
                mCustomViewCallback = null
            }

            override fun onShowCustomView( // Fullscreen
                paramView: View?,
                paramCustomViewCallback: CustomViewCallback?
            ) {
                if (mCustomView != null) {
                    onHideCustomView()
                    return
                }
                mCustomView = paramView
                mOriginalSystemUiVisibility = mainActivity.window.decorView.getSystemUiVisibility()
                mOriginalOrientation = mainActivity.requestedOrientation
                mCustomViewCallback = paramCustomViewCallback
                (mainActivity.window.decorView as FrameLayout).addView(
                    mCustomView,
                    FrameLayout.LayoutParams(-1, -1)
                )
                mainActivity.window.decorView.setSystemUiVisibility(3846)
            }
        }




        webView.loadUrl("http://127.0.0.1:2022")
        webView.settings.javaScriptEnabled = true
        webView.settings.setSupportZoom(false)
        //webView.addJavascriptInterface(this, "kotlin");
    }
}


class FlaskThread : Thread() {

    override fun run() {
        val py = Python.getInstance()
        try {
            py.getModule("main").callAttr("run")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}