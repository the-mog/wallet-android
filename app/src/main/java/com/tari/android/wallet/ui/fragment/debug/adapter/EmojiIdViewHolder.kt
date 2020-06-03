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
package com.tari.android.wallet.ui.fragment.debug.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.tari.android.wallet.databinding.EmojiIdListItemBinding
import com.tari.android.wallet.util.EmojiUtil
import com.tari.android.wallet.R.string.emoji_id_chunk_separator
import com.tari.android.wallet.R.color.black
import com.tari.android.wallet.R.color.light_gray
import com.tari.android.wallet.ui.component.EmojiIdCopiedViewController
import com.tari.android.wallet.ui.extension.*
import com.tari.android.wallet.ui.extension.color
import com.tari.android.wallet.ui.extension.gone
import com.tari.android.wallet.ui.extension.string
import com.tari.android.wallet.ui.extension.visible
import com.tari.android.wallet.ui.util.UiUtil
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import java.lang.ref.WeakReference

/**
 * Displays an emoji id for the test screen.
 *
 * @author The Tari Development Team
 */
internal class EmojiIdViewHolder(view: View, listener: Listener) : RecyclerView.ViewHolder(view) {

    private lateinit var emojiId: String
    private val ui: EmojiIdListItemBinding = EmojiIdListItemBinding.bind(view)
    private val listenerWeakReference = WeakReference(listener)

    /**
     * Animates the emoji id "copied" text.
     */
    private val emojiIdCopiedViewController: EmojiIdCopiedViewController

    init {
        emojiIdCopiedViewController = EmojiIdCopiedViewController(ui.emojiIdCopiedView)
        OverScrollDecoratorHelper.setUpOverScroll(ui.fullEmojiIdScrollView)
    }

    fun bind(
        index: Int,
        emojiId: String,
        isMatched: Boolean,
        isLast: Boolean
    ) {
        this.emojiId = emojiId
        ui.indexTextView.text = (index + 1).toString()
        ui.fullEmojiIdTextView.text = EmojiUtil.getFullEmojiIdSpannable(
            emojiId,
            string(emoji_id_chunk_separator),
            color(black),
            color(light_gray)
        )
        ui.fullEmojiIdScrollView.scrollTo(0, 0)
        ui.emojiIdCopiedView.root.invisible()
        ui.fullEmojiIdTextView.setOnClickListener { onCopyEmojiIdButtonClicked(it) }
        if (isMatched) {
            ui.matchIndicatorView.visible()
        } else {
            ui.matchIndicatorView.invisible()
        }
        if (isLast) {
            ui.bottomSpacerView.visible()
        } else {
            ui.bottomSpacerView.gone()
        }
    }

    private fun onCopyEmojiIdButtonClicked(view: View) {
        UiUtil.temporarilyDisableClick(view)
        ui.emojiIdCopiedView.root.visible()
        emojiIdCopiedViewController.showEmojiIdCopiedAnim(fadeOutOnEnd = true)
        listenerWeakReference.get()?.emojiIdCopied(emojiId)
    }

    interface Listener {

        fun emojiIdCopied(emojiId: String)

    }

}