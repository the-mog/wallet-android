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
package com.tari.android.wallet.ui.fragment.send

import android.animation.*
import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import butterknife.*
import com.daasuu.ei.Ease
import com.daasuu.ei.EasingInterpolator
import com.tari.android.wallet.R
import com.tari.android.wallet.model.*
import com.tari.android.wallet.service.TariWalletService
import com.tari.android.wallet.ui.component.EmojiIdSummaryViewController
import com.tari.android.wallet.ui.fragment.BaseFragment
import com.tari.android.wallet.ui.util.UiUtil
import com.tari.android.wallet.util.Constants
import com.tari.android.wallet.util.EmojiUtil
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.lang.Long.max
import java.lang.ref.WeakReference
import javax.inject.Inject

/**
 * Add a note to the transaction & send it through this fragment.
 *
 * @author The Tari Development Team
 */
class AddNoteAndSendFragment(private val walletService: TariWalletService) : BaseFragment(),
    TextWatcher, View.OnTouchListener {

    @BindView(R.id.add_note_and_send_vw_root)
    lateinit var rootView: View
    @BindView(R.id.add_note_and_send_txt_title)
    lateinit var titleTextView: TextView
    @BindView(R.id.add_note_and_send_btn_back)
    lateinit var backButton: ImageButton
    @BindView(R.id.add_note_and_send_vw_emoji_id_summary_container)
    lateinit var emojiIdContainerView: View
    @BindView(R.id.add_note_and_send_vw_emoji_id_summary)
    lateinit var emojiIdSummaryView: View
    @BindView(R.id.add_note_and_send_vw_full_emoji_container)
    lateinit var fullEmojiIdContainerView: View
    @BindView(R.id.add_note_and_send_txt_full_emoji_id)
    lateinit var fullEmojiIdTextView: TextView

    @BindView(R.id.add_note_and_send_edit_note)
    lateinit var noteEditText: EditText

    @BindView(R.id.add_note_and_send_vw_slide_button_container)
    lateinit var slideButtonContainerView: View
    @BindView(R.id.add_note_and_send_vw_slide_enabled_bg)
    lateinit var slideBgEnabledView: View
    @BindView(R.id.add_note_and_send_txt_slide_to_send_disabled)
    lateinit var slideToSendDisabledTextView: TextView
    @BindView(R.id.add_note_and_send_txt_slide_to_send_enabled)
    lateinit var slideToSendEnabledTextView: TextView
    @BindView(R.id.add_note_and_send_img_slide_to_send_arrow_enabled)
    lateinit var slideArrowIconEnabledImageView: ImageView
    @BindView(R.id.add_note_and_send_vw_slide)
    lateinit var slideView: View
    @BindView(R.id.add_note_and_send_prog_bar)
    lateinit var progressBar: ProgressBar

    /**
     * Emoji id chunk separator char.
     */
    @BindString(R.string.emoji_id_chunk_separator_char)
    lateinit var emojiIdChunkSeparator: String

    /**
     * Dimmers.
     */
    @BindViews(
        R.id.add_note_and_send_vw_top_dimmer,
        R.id.add_note_and_send_vw_bottom_dimmer
    )
    lateinit var dimmerViews: List<@JvmSuppressWildcards View>

    @BindDimen(R.dimen.add_note_slide_button_left_margin)
    @JvmField
    var slideViewMarginStart = 0
    @BindDimen(R.dimen.add_note_slide_button_width)
    @JvmField
    var slideViewWidth = 0

    @BindColor(R.color.white)
    @JvmField
    var whiteColor = 0

    @Inject
    lateinit var tracker: Tracker

    private val wr = WeakReference(this)
    private lateinit var listenerWR: WeakReference<Listener>

    // slide button animation related variables
    private var slideButtonXDelta = 0
    private var slideButtonLastMarginStart = 0
    private var slideButtonContainerWidth = 0

    /**
     * Formats the summarized emoji id.
     */
    private lateinit var emojiIdSummaryController: EmojiIdSummaryViewController

    /**
     * Tx properties.
     */
    private lateinit var recipientUser: User
    private lateinit var amount: MicroTari
    private lateinit var fee: MicroTari

    override val contentViewId: Int = R.layout.fragment_add_note_and_send

    companion object {

        fun newInstance(walletService: TariWalletService): AddNoteAndSendFragment {
            return AddNoteAndSendFragment(walletService)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // get tx properties
        recipientUser = arguments!!.getParcelable("recipientUser")!!
        amount = arguments!!.getParcelable("amount")!!
        fee = arguments!!.getParcelable("fee")!!
        emojiIdSummaryController = EmojiIdSummaryViewController(emojiIdSummaryView)
        fullEmojiIdContainerView.visibility = View.GONE
        displayAliasOrEmojiId()
        UiUtil.setProgressBarColor(progressBar, whiteColor)
        dimmerViews.forEach {
            it.visibility = View.GONE
        }
        noteEditText.addTextChangedListener(this)
        slideView.setOnTouchListener(this)

        // disable "send" slider
        disableCallToAction()
        focusEditTextAndShowKeyboard()

        noteEditText.imeOptions = EditorInfo.IME_ACTION_DONE
        noteEditText.setRawInputType(InputType.TYPE_CLASS_TEXT)

        TrackHelper.track()
            .screen("/home/send_tari/add_note")
            .title("Send Tari - Add Note")
            .with(tracker)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listenerWR = WeakReference(context as Listener)
    }

    override fun onDestroy() {
        noteEditText.removeTextChangedListener(this)
        super.onDestroy()
    }

    private fun displayAliasOrEmojiId() {
        if (recipientUser is Contact) {
            emojiIdContainerView.visibility = View.GONE
            titleTextView.visibility = View.VISIBLE
            titleTextView.text = (recipientUser as Contact).alias
        } else {
            // TODO to be changed once the emoji id graphic design spec is clear
            val shortenedEmojiId = EmojiUtil.getShortenedEmojiId(recipientUser.publicKey.emojiId)
                ?: throw RuntimeException("Invalid emoji id: " + recipientUser.publicKey.emojiId)
            displayEmojiId(shortenedEmojiId)
        }
    }

    private fun displayEmojiId(emojiId: String) {
        emojiIdContainerView.visibility = View.VISIBLE
        emojiIdSummaryController.display(emojiId)
        titleTextView.visibility = View.GONE
        // make chunks
        val separatorIndices = EmojiUtil.getNewChunkSeparatorIndices(emojiId)
        val builder = StringBuilder(emojiId)
        for ((i, index) in separatorIndices.iterator().withIndex()) {
            builder.insert((index + i), emojiIdChunkSeparator)
        }
        fullEmojiIdTextView.text = builder.toString()
    }

    @OnClick(R.id.add_note_and_send_btn_back)
    fun onBackButtonClicked(view: View) {
        UiUtil.temporarilyDisableClick(view)
        // going back before hiding keyboard causes a blank white area on the screen
        // wait a while, then forward the back action to the host activity
        val mActivity = activity ?: return
        UiUtil.hideKeyboard(mActivity)
        rootView.postDelayed({
            mActivity.onBackPressed()
        }, Constants.UI.shortDurationMs)
    }

    /**
     * Display full emoji id and dim out all other views.
     */
    @OnClick(R.id.add_note_and_send_vw_emoji_id_summary_container)
    fun emojiIdClicked() {
        emojiIdContainerView.visibility = View.GONE
        fullEmojiIdContainerView.visibility = View.VISIBLE
        backButton.visibility = View.INVISIBLE
        dimmerViews.forEach {
            it.visibility = View.VISIBLE
        }
    }

    /**
     * Dimmer clicked - hide dimmers.
     */
    @OnClick(
        R.id.add_note_and_send_vw_top_dimmer,
        R.id.add_note_and_send_vw_bottom_dimmer
    )
    fun onEmojiIdDimmerClicked() {
        emojiIdContainerView.visibility = View.VISIBLE
        fullEmojiIdContainerView.visibility = View.GONE
        backButton.visibility = View.VISIBLE
        dimmerViews.forEach {
            it.visibility = View.GONE
        }
    }

    private fun focusEditTextAndShowKeyboard() {
        val mActivity = activity ?: return
        noteEditText.requestFocus()
        val imm = mActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(
            InputMethodManager.SHOW_FORCED,
            InputMethodManager.HIDE_IMPLICIT_ONLY
        )
    }

    private fun enableCallToAction() {
        if (slideBgEnabledView.visibility == View.VISIBLE) {
            return
        }
        slideView.setOnTouchListener(this)

        slideToSendDisabledTextView.visibility = View.INVISIBLE

        slideToSendEnabledTextView.alpha = 0f
        slideToSendEnabledTextView.visibility = View.VISIBLE
        slideArrowIconEnabledImageView.alpha = 0f
        slideArrowIconEnabledImageView.visibility = View.VISIBLE
        slideBgEnabledView.alpha = 0f
        slideBgEnabledView.visibility = View.VISIBLE

        val textViewAnim = ObjectAnimator.ofFloat(slideToSendEnabledTextView, "alpha", 0f, 1f)
        val arrowAnim = ObjectAnimator.ofFloat(slideArrowIconEnabledImageView, "alpha", 0f, 1f)
        val bgViewAnim = ObjectAnimator.ofFloat(slideBgEnabledView, "alpha", 0f, 1f)

        // the animation set
        val animSet = AnimatorSet()
        animSet.playTogether(textViewAnim, arrowAnim, bgViewAnim)
        animSet.duration = Constants.UI.shortDurationMs
        animSet.start()
    }

    private fun disableCallToAction() {
        slideToSendDisabledTextView.visibility = View.VISIBLE
        slideBgEnabledView.visibility = View.GONE
        slideToSendEnabledTextView.visibility = View.GONE
        slideArrowIconEnabledImageView.visibility = View.GONE
        slideView.setOnTouchListener(null)
    }

    // region text change listener

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

    }

    override fun afterTextChanged(s: Editable) {
        if (s.toString().isNotEmpty()) {
            enableCallToAction()
        } else {
            disableCallToAction()
        }
    }

    // endregion

    /**
     * Controls the slide button animation & behaviour on drag.
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(
        view: View,
        event: MotionEvent
    ): Boolean {
        val x = event.rawX.toInt()
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                slideButtonContainerWidth = slideButtonContainerView.width
                val layoutParams = view.layoutParams as RelativeLayout.LayoutParams
                slideButtonXDelta = x - layoutParams.marginStart
            }
            MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_POINTER_UP -> {
            }
            MotionEvent.ACTION_MOVE -> {
                val layoutParams = view.layoutParams as RelativeLayout.LayoutParams
                val newLeftMargin = x - slideButtonXDelta
                slideButtonLastMarginStart = if (newLeftMargin < slideViewMarginStart) {
                    slideViewMarginStart
                } else {
                    if (newLeftMargin + slideViewWidth + slideViewMarginStart >= slideButtonContainerWidth) {
                        slideButtonContainerWidth - slideViewWidth - slideViewMarginStart
                    } else {
                        x - slideButtonXDelta
                    }
                }
                layoutParams.marginStart = slideButtonLastMarginStart
                val alpha =
                    1f - slideButtonLastMarginStart.toFloat() / (slideButtonContainerWidth - slideViewMarginStart - slideViewWidth)
                slideToSendEnabledTextView.alpha = alpha
                slideToSendDisabledTextView.alpha = alpha

                view.layoutParams = layoutParams
            }
            MotionEvent.ACTION_UP -> if (slideButtonLastMarginStart < slideButtonContainerWidth / 2) {
                val anim = ValueAnimator.ofInt(
                    slideButtonLastMarginStart,
                    slideViewMarginStart
                )
                anim.addUpdateListener { valueAnimator: ValueAnimator ->
                    val fragment = wr.get() ?: return@addUpdateListener
                    val margin = valueAnimator.animatedValue as Int
                    UiUtil.setStartMargin(fragment.slideView, margin)
                    fragment.slideToSendEnabledTextView.alpha =
                        1f - margin.toFloat() / (fragment.slideButtonContainerWidth - fragment.slideViewMarginStart - fragment.slideViewWidth)
                }
                anim.duration = Constants.UI.shortDurationMs
                anim.interpolator = EasingInterpolator(Ease.QUART_IN_OUT)
                anim.startDelay = 0
                anim.start()
            } else {
                val fragment = wr.get() ?: return false
                // disable input
                noteEditText.inputType = InputType.TYPE_NULL
                fragment.listenerWR.get()?.sendTxStarted(this@AddNoteAndSendFragment)
                // complete slide animation
                val anim = ValueAnimator.ofInt(
                    slideButtonLastMarginStart,
                    slideButtonContainerWidth - slideViewMarginStart - slideViewWidth
                )
                anim.addUpdateListener { valueAnimator: ValueAnimator ->

                    val margin = valueAnimator.animatedValue as Int
                    UiUtil.setStartMargin(fragment.slideView, margin)
                    slideToSendEnabledTextView.alpha =
                        1f - margin.toFloat() / (fragment.slideButtonContainerWidth - fragment.slideViewMarginStart - fragment.slideViewWidth)
                }
                anim.duration = Constants.UI.shortDurationMs
                anim.interpolator = EasingInterpolator(Ease.QUART_IN_OUT)
                anim.startDelay = 0
                anim.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) { // done
                        wr.get()?.slideAnimationCompleted()
                    }
                })
                anim.start()
            }
        }
        return false
    }

    private fun slideAnimationCompleted() {
        // hide slide view
        val anim = ValueAnimator.ofFloat(
            1f,
            0f
        )
        anim.addUpdateListener { valueAnimator: ValueAnimator ->
            val fragment = wr.get() ?: return@addUpdateListener
            fragment.slideView.alpha = valueAnimator.animatedValue as Float
        }
        anim.duration = Constants.UI.shortDurationMs
        anim.interpolator = EasingInterpolator(Ease.QUART_IN_OUT)
        anim.startDelay = 0
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) { // done
                val fragment = wr.get() ?: return
                fragment.progressBar.visibility = View.VISIBLE
                fragment.rootView.postDelayed({
                    wr.get()?.hideKeyboard()
                }, Constants.UI.AddNoteAndSend.preKeyboardHideWaitMs)
            }
        })
        anim.start()
    }

    private fun hideKeyboard() {
        val mActivity = activity ?: return
        UiUtil.hideKeyboard(mActivity)
        noteEditText.clearFocus()
        AsyncTask.execute {
            wr.get()?.sendTari()
        }
    }

    private fun sendTari() {
        val note = noteEditText.text.toString()
        val timeStartMs = System.currentTimeMillis()
        val error = WalletError()
        val success = walletService.sendTari(recipientUser, amount, fee, note, error)
        val timeElapsed = System.currentTimeMillis() - timeStartMs
        val waitMs = max(
            0,
            Constants.UI.AddNoteAndSend.postSendDelayMs - timeElapsed
        )
        rootView.postDelayed({
            if (success && error.code == WalletErrorCode.NO_ERROR) {
                wr.get()?.sendTariSuccessful()
            } else {
                wr.get()?.sendTariError()
            }
        }, waitMs)
    }

    private fun sendTariSuccessful() {
        val note = noteEditText.text.toString()
        progressBar.visibility = View.INVISIBLE
        listenerWR.get()?.sendTxSuccessful(
            this,
            recipientUser,
            amount,
            fee,
            note
        )
    }

    private fun sendTariError() {
        // enable input
        noteEditText.inputType =
            (InputType.TYPE_TEXT_VARIATION_FILTER
                    or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                    or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)

        slideToSendEnabledTextView.alpha = 1f
        slideToSendEnabledTextView.visibility = View.VISIBLE
        // move slider back to its original position
        UiUtil.setStartMargin(slideView, slideViewMarginStart)
        progressBar.visibility = View.INVISIBLE
        slideView.alpha = 1f
        // notify listener
        listenerWR.get()?.sendTxFailed(this)
    }

    /**
     * Listener interface - to be implemented by the host activity.
     */
    interface Listener {

        /**
         * Notify the listener that send is in progress.
         */
        fun sendTxStarted(sourceFragment: AddNoteAndSendFragment)

        /**
         * Notify the listener that the send process has failed.
         */
        fun sendTxFailed(sourceFragment: AddNoteAndSendFragment)

        /**
         * Notify the listener that the transaction has been sent successfully.
         */
        fun sendTxSuccessful(
            sourceFragment: AddNoteAndSendFragment,
            recipientUser: User,
            amount: MicroTari,
            fee: MicroTari,
            note: String
        )

    }

}