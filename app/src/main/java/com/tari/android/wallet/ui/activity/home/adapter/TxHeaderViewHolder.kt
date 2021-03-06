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
package com.tari.android.wallet.ui.activity.home.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.tari.android.wallet.R.string.*
import com.tari.android.wallet.databinding.HomeTxListHeaderBinding
import com.tari.android.wallet.ui.extension.string
import org.joda.time.LocalDate

/**
 * Transaction list section header view holder.
 *
 * @author The Tari Development Team
 */
class TxHeaderViewHolder(view: View, private val type: Type) :
    RecyclerView.ViewHolder(view) {

    enum class Type {
        PENDING_TXS,
        DATE
    }

    private val dateFormat = "MMMM dd, yyyy"

    private var date: LocalDate? = null

    private val ui = HomeTxListHeaderBinding.bind(view)

    fun bind(date: LocalDate?, position: Int) {
        ui.txListHeaderSeparatorView.visibility = if (position == 0) View.GONE else View.VISIBLE
        if (type == Type.PENDING_TXS) {
            ui.txListHeaderTitleTextView.text = string(home_tx_list_header_pending_txs)
        } else if (type == Type.DATE) {
            this.date = date!!
            val todayDate = LocalDate.now()
            val yesterdayDate = todayDate.minusDays(1)
            ui.txListHeaderTitleTextView.text = when {
                date.isEqual(todayDate) -> string(home_tx_list_header_today)
                date.isEqual(yesterdayDate) -> string(home_tx_list_header_yesterday)
                else -> date.toString(dateFormat)
            }
        }

    }
}
