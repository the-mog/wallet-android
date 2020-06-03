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
package com.tari.android.wallet.ui.fragment.debug

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.orhanobut.logger.Logger
import com.tari.android.wallet.R
import com.tari.android.wallet.databinding.FragmentEmojiIdTestBinding
import com.tari.android.wallet.ffi.FFIPrivateKey
import com.tari.android.wallet.ffi.FFIPublicKey
import com.tari.android.wallet.ui.extension.appComponent
import com.tari.android.wallet.ui.extension.string
import com.tari.android.wallet.ui.fragment.debug.adapter.EmojiIdListAdapter
import com.tari.android.wallet.util.Constants.Wallet.emojiIdLength
import com.tari.android.wallet.util.EmojiUtil
import com.tari.android.wallet.util.extractEmojis
import javax.inject.Inject

/**
 * Debug screen fragment for testing emojid ids.
 *
 * @author The Tari Development Team
 */
internal class EmojiIdTestFragment : Fragment(), EmojiIdListAdapter.Listener {

    @Inject
    lateinit var clipboardManager: ClipboardManager

    private val emojiIds = mutableListOf<String>()

    /**
     * List, adapter & layout manager.
     */
    private lateinit var recyclerViewAdapter: EmojiIdListAdapter
    private lateinit var recyclerViewLayoutManager: RecyclerView.LayoutManager

    private var _ui: FragmentEmojiIdTestBinding? = null
    private val ui get() = _ui

    private val handler = Handler(Looper.getMainLooper())

    private var startedFirstTime = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = FragmentEmojiIdTestBinding.inflate(inflater, container, false).also {
        _ui = it
    }.root

    override fun onDestroyView() {
        ui?.recyclerView?.layoutManager = null
        ui?.recyclerView?.adapter = null
        _ui = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EmojiIdTestFragmentVisitor.visit(this)
        generateEmojiIds()
        setupUI()
    }

    override fun onStart() {
        super.onStart()
        if (startedFirstTime) {
            startedFirstTime = false
            return
        }
        recyclerViewAdapter.matchedIndex = -1
        recyclerViewAdapter.notifyDataSetChanged()
        ui?.infoTextView?.text = ""

        handler.postDelayed({
            recyclerViewAdapter.matchedIndex = checkClipboard()
            recyclerViewAdapter.notifyDataSetChanged()
            if (recyclerViewAdapter.matchedIndex >= 0) {
                ui?.recyclerView?.smoothScrollToPosition(recyclerViewAdapter.matchedIndex)
            }
        }, 250)
    }

    /**
     * Checks clipboard data for a valid deep link or an emoji id.
     */
    private fun checkClipboard(): Int {
        val clipboardString = clipboardManager.primaryClip?.getItemAt(0)?.text?.toString()
        if (clipboardString == null) {
            ui?.infoTextView?.text = string(R.string.debug_emoji_id_test_no_emoji_id_in_clipboard)
            return -1
        }
        val emojis = clipboardString.trim().extractEmojis()
        // search in windows of length = emoji id length
        var currentIndex = emojis.size - emojiIdLength
        var keyEmojiId: String? = null
        while (currentIndex >= 0) {
            val emojiWindow =
                emojis
                    .subList(currentIndex, currentIndex + emojiIdLength)
                    .joinToString(separator = "")
            // there is a chunked emoji id in the clipboard
            try {
                val publicKeyFFI = FFIPublicKey(emojiWindow)
                keyEmojiId = publicKeyFFI.getEmojiNodeId()
                publicKeyFFI.destroy()
                break
            } catch (ignored: Throwable) {

            }
            --currentIndex
        }
        return if (keyEmojiId != null) {
            // search in the list
            val index = emojiIds.indexOf(keyEmojiId)
            if (index >= 0) {
                ui?.infoTextView?.text = string(R.string.debug_emoji_id_test_emoji_id_matches)
            } else {
                ui?.infoTextView?.text = string(R.string.debug_emoji_id_test_emoji_id_not_in_list)
            }
            index
        } else {
            ui?.infoTextView?.text = string(R.string.debug_emoji_id_test_no_emoji_id_in_clipboard)
            -1
        }
    }

    private fun setupUI() {
        // initialize recycler view
        recyclerViewLayoutManager = LinearLayoutManager(activity)
        recyclerViewAdapter = EmojiIdListAdapter(emojiIds, this)
        ui?.let {
            it.recyclerView.layoutManager = recyclerViewLayoutManager
            it.recyclerView.adapter = recyclerViewAdapter
        }
    }

    private fun generateEmojiIds() {
        val emojiSet = EmojiUtil.emojiSet.toMutableSet()
        while (emojiSet.isNotEmpty()) {
            val privateKeyFFI = FFIPrivateKey()
            val publicKeyFFI = FFIPublicKey(privateKeyFFI)
            val emojiId = publicKeyFFI.getEmojiNodeId()
            publicKeyFFI.destroy()
            privateKeyFFI.destroy()

            emojiIds += emojiId
            for (emoji in emojiId.extractEmojis()) {
                emojiSet.remove(emoji)
            }
        }
    }

    override fun emojiIdCopied(emojiId: String) {
        val mActivity = activity ?: return
        val clipBoard = ContextCompat.getSystemService(mActivity, ClipboardManager::class.java)
        val clipboardData = ClipData.newPlainText(
            "Tari Wallet Identity",
            emojiId
        )
        clipBoard?.setPrimaryClip(clipboardData)
        // UI updates
        ui?.infoTextView?.text = string(R.string.debug_emoji_id_test_emoji_id_copied)
        if (recyclerViewAdapter.matchedIndex >= 0) {
            val previousMatched = recyclerViewAdapter.matchedIndex
            recyclerViewAdapter.matchedIndex = -1
            recyclerViewAdapter.notifyItemChanged(previousMatched)
        }
    }

    private object EmojiIdTestFragmentVisitor {
        internal fun visit(fragment: EmojiIdTestFragment) {
            fragment.requireActivity().appComponent.inject(fragment)
        }
    }

}