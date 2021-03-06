/**
 * Copyright 2020 The Tari Project
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of
 * its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.tari.android.wallet.ui.fragment.store

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient

class EventsPropagatingWebViewClient : WebViewClient() {

    private val listeners = mutableListOf<WebViewEventListener>()

    fun addListener(listener: WebViewEventListener) {
        listeners.add(listener)
    }

    fun addListener(
        onPageStarted: (WebView, String, Bitmap?) -> Unit = { _, _, _ -> },
        onPageCommitVisible: (WebView, String) -> Unit = { _, _ -> },
        onPageFinished: (WebView, String) -> Unit = { _, _ -> }
    ): WebViewEventListener {
        return object : WebViewEventListener {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) =
                onPageStarted(view, url, favicon)

            override fun onPageCommitVisible(view: WebView, url: String) =
                onPageCommitVisible(view, url)

            override fun onPageFinished(view: WebView, url: String) = onPageFinished(view, url)
        }.also(this::addListener)
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        listeners.forEach { it.onPageStarted(view, url, favicon) }
    }

    override fun onPageCommitVisible(view: WebView, url: String) {
        super.onPageCommitVisible(view, url)
        listeners.forEach { it.onPageCommitVisible(view, url) }
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        listeners.forEach { it.onPageFinished(view, url) }
    }
}
