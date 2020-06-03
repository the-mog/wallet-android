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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tari.android.wallet.R
import java.lang.ref.WeakReference

/**
 * Displays the list of emoji ids.
 *
 * @author The Tari Development Team
 */
internal class EmojiIdListAdapter(private val emojiIds: List<String>, listener: Listener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    EmojiIdViewHolder.Listener {

    private val listenerWeakReference = WeakReference(listener)

    var matchedIndex = -1

    /**
     * Item count.
     */
    override fun getItemCount() = emojiIds.size

    /**
     * Create the view holder instance.
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.emoji_id_list_item,
            parent,
            false
        )
        return EmojiIdViewHolder(view, this)
    }

    /**
     * Bind & display header or transaction.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as EmojiIdViewHolder).bind(
            position,
            emojiIds[position],
            matchedIndex == position,
            position == (emojiIds.size - 1)
        )
    }

    override fun emojiIdCopied(emojiId: String) {
        listenerWeakReference.get()?.emojiIdCopied(emojiId)
    }

    interface Listener {

        fun emojiIdCopied(emojiId: String)

    }

}