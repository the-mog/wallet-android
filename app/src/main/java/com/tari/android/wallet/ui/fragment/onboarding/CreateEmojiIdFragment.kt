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
package com.tari.android.wallet.ui.fragment.onboarding

import android.animation.*
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.BindDimen
import butterknife.BindString
import butterknife.BindView
import butterknife.OnClick
import com.airbnb.lottie.LottieAnimationView
import com.daasuu.ei.Ease
import com.daasuu.ei.EasingInterpolator
import com.tari.android.wallet.R
import com.tari.android.wallet.di.WalletModule
import com.tari.android.wallet.ui.activity.AuthActivity
import com.tari.android.wallet.ui.fragment.BaseFragment
import com.tari.android.wallet.ui.util.UiUtil
import com.tari.android.wallet.util.Constants
import com.tari.android.wallet.util.Constants.UI.CreateEmojiId
import com.tari.android.wallet.util.EmojiUtil
import javax.inject.Inject
import javax.inject.Named

/**
 * onBoarding flow : wallet creation sequence
 *
 * @author The Tari Development Team
 */
class CreateEmojiIdFragment : BaseFragment() {

    @BindView(R.id.create_emoji_id_txt_hello)
    lateinit var helloText: TextView
    @BindView(R.id.create_emoji_id_txt_just_sec_desc)
    lateinit var justSecDescText: TextView
    @BindView(R.id.create_emoji_id_txt_just_sec_title)
    lateinit var justSecTitle: TextView
    @BindView(R.id.create_emoji_id_hello_text_back_view)
    lateinit var helloTextBackView: View
    @BindView(R.id.create_emoji_id_checkmark_anim)
    lateinit var checkMarkAnim: LottieAnimationView
    @BindView(R.id.create_emoji_id_txt_wallet_address_desc)
    lateinit var walletAddressDescText: TextView
    @BindView(R.id.create_emoji_id_btn_create_emoji_id)
    lateinit var createEmojiIdButton: Button
    @BindView(R.id.create_emoji_id_just_sec_back_view)
    lateinit var justSecBackView: View
    @BindView(R.id.create_emoji_id_nerd_face_emoji)
    lateinit var nerdFaceEmoji: LottieAnimationView
    @BindView(R.id.create_emoji_id_txt_create_your_emoji_id)
    lateinit var createYourEmojiIdText: TextView
    @BindView(R.id.create_emoji_id_text_back_view)
    lateinit var createEmojiIdTextBackView: View
    @BindView(R.id.create_emoji_id_txt_awesome)
    lateinit var awesomeText: TextView
    @BindView(R.id.create_emoji_id_white_bg_view)
    lateinit var whiteBgView: View
    @BindView(R.id.create_emoji_id_awesome_text_back_view)
    lateinit var awesomeTextBackView: View
    @BindView(R.id.create_emoji_id_emoji_wheel_anim)
    lateinit var emojiWheelAnimView: LottieAnimationView
    @BindView(R.id.create_emoji_id_txt_emoji_id)
    lateinit var emojiIdTextView: TextView
    @BindView(R.id.create_emoji_id_your_emoji_id_title_container)
    lateinit var yourEmojiTitleText: LinearLayout
    @BindView(R.id.create_emoji_id_your_emoji_title_back_view)
    lateinit var yourEmojiTitleBackView: View
    @BindView(R.id.create_emoji_id_btn_continue)
    lateinit var continueButton: Button

    @BindDimen(R.dimen.create_emoji_id_button_bottom_margin)
    @JvmField
    var createEmojiButtonBottomMargin = 0


    @BindString(R.string.create_emoji_id_set_of_emoji_your_wallet_address_desc)
    @JvmField
    var yourWalletAddressDescString = ""

    /**
     * Emoji id chunk separator char.
     */
    @BindString(R.string.emoji_id_chunk_separator_char)
    lateinit var emojiIdChunkSeparator: String

    @Inject
    @Named(WalletModule.FieldName.emojiId)
    lateinit var emojiId: String

    private val uiHandler = Handler()

    override val contentViewId = R.layout.fragment_create_emoji_id

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val shortEmojiId = EmojiUtil.getShortenedEmojiId(emojiId)!!
        val chunkedEmojiId = EmojiUtil.getChunkedEmojiId(
            shortEmojiId,
            emojiIdChunkSeparator
        )
        emojiIdTextView.text = chunkedEmojiId
        setupUi()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        uiHandler.removeCallbacksAndMessages(null)
    }

    private fun setupUi() {
        whiteBgView.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    whiteBgView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    whiteBgView.translationY = -whiteBgView.height.toFloat()
                    playStartupWhiteBgAnimation()
                }
            })

        checkMarkAnim.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                startCreateEmojiAnimation()
            }
        })

        nerdFaceEmoji.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                nerdFaceEmoji.playAnimation()
                nerdFaceEmoji.progress = 0.10f
            }
        })
    }

    @OnClick(R.id.create_emoji_id_btn_continue)
    fun onContinueButtonClick() {
        UiUtil.temporarilyDisableClick(continueButton)
        val animatorSet = animateButtonClick(continueButton)
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                showAuthActivity()
            }
        })
    }

    private fun showAuthActivity() {
        activity?.let {
            val intent = Intent(it, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            it.finish()
        }
    }

    @OnClick(R.id.create_emoji_id_btn_create_emoji_id)
    fun onCreateEmojiIdButtonClick() {
        UiUtil.temporarilyDisableClick(createEmojiIdButton)
        val animatorSet = animateButtonClick(createEmojiIdButton)
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                showEmojiWheelAnimation()
            }
        })

    }

    private fun animateButtonClick(button: Button): AnimatorSet {
        val scaleDownBtnAnim = ValueAnimator.ofFloat(
            Constants.UI.Button.clickScaleAnimFullScale,
            Constants.UI.Button.clickScaleAnimSmallScale
        )
        scaleDownBtnAnim.addUpdateListener { valueAnimator: ValueAnimator ->
            val scale = valueAnimator.animatedValue as Float
            button.scaleX = scale
            button.scaleY = scale
        }
        scaleDownBtnAnim.duration = Constants.UI.Button.clickScaleAnimDurationMs
        scaleDownBtnAnim.startDelay = Constants.UI.Button.clickScaleAnimStartOffset
        scaleDownBtnAnim.interpolator = DecelerateInterpolator()

        val scaleUpBtnAnim = ValueAnimator.ofFloat(
            Constants.UI.Button.clickScaleAnimSmallScale,
            Constants.UI.Button.clickScaleAnimFullScale
        )
        scaleUpBtnAnim.addUpdateListener { valueAnimator: ValueAnimator ->
            val scale = valueAnimator.animatedValue as Float
            button.scaleX = scale
            button.scaleY = scale
        }
        scaleUpBtnAnim.duration = Constants.UI.Button.clickScaleAnimReturnDurationMs
        scaleUpBtnAnim.startDelay = Constants.UI.Button.clickScaleAnimReturnStartOffset
        scaleUpBtnAnim.interpolator = AccelerateInterpolator()

        val animSet = AnimatorSet()
        animSet.playSequentially(scaleDownBtnAnim, scaleUpBtnAnim)
        animSet.start()
        return animSet
    }

    private fun showEmojiWheelAnimation() {
        emojiWheelAnimView.playAnimation()

        val fadeOutAnim = ValueAnimator.ofFloat(1f, 0f)
        fadeOutAnim.addUpdateListener { valueAnimator: ValueAnimator ->
            val alpha = valueAnimator.animatedValue as Float
            nerdFaceEmoji.alpha = alpha
            awesomeText.alpha = alpha
            createYourEmojiIdText.alpha = alpha
            walletAddressDescText.alpha = alpha
            createEmojiIdButton.alpha = alpha
        }
        fadeOutAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                createEmojiIdButton.visibility = View.GONE
                awesomeTextBackView.visibility = View.GONE
                justSecBackView.visibility = View.GONE
                createEmojiIdTextBackView.visibility = View.GONE
            }
        })

        fadeOutAnim.startDelay = CreateEmojiId.walletCreationFadeOutAnimDelayMs
        fadeOutAnim.duration = CreateEmojiId.walletCreationFadeOutAnimDurationMs
        fadeOutAnim.start()

        uiHandler.postDelayed(
            { startYourEmojiIdViewAnimation() },
            emojiWheelAnimView.duration - CreateEmojiId.awesomeTextAnimDurationMs
        )
    }

    private fun startYourEmojiIdViewAnimation() {
        walletAddressDescText.text = yourWalletAddressDescString
        val buttonOffset = -(continueButton.height + createEmojiButtonBottomMargin).toFloat()
        val buttonTransAnim: ObjectAnimator =
            ObjectAnimator.ofFloat(continueButton, View.TRANSLATION_Y, 0f, buttonOffset)

        val buttonFadeInAnim = ValueAnimator.ofFloat(0f, 1f)
        buttonFadeInAnim.addUpdateListener { valueAnimator: ValueAnimator ->
            val alpha = valueAnimator.animatedValue as Float
            continueButton.alpha = alpha
        }

        val buttonAnimSet = AnimatorSet()
        buttonAnimSet.playTogether(buttonTransAnim, buttonFadeInAnim)
        buttonAnimSet.duration = CreateEmojiId.continueButtonAnimDurationMs

        val emojiContainerImageScaleAnim = ValueAnimator.ofFloat(2f, 1.5f)
        emojiContainerImageScaleAnim.addUpdateListener { animation ->
            val scale = animation.animatedValue.toString().toFloat()
            emojiIdTextView.scaleX = scale
            emojiIdTextView.scaleY = scale
        }
        emojiContainerImageScaleAnim.startDelay = CreateEmojiId.emojiIdImageViewAnimDelayMs

        val titleOffset = -(yourEmojiTitleText.height).toFloat()
        val yourEmojiTitleAnim =
            ObjectAnimator.ofFloat(yourEmojiTitleText, View.TRANSLATION_Y, 0f, titleOffset)
        yourEmojiTitleAnim.startDelay = CreateEmojiId.yourEmojiIdTextAnimDelayMs
        yourEmojiTitleAnim.duration = CreateEmojiId.yourEmojiIdTextAnimDurationMs
        yourEmojiTitleAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
                yourEmojiTitleBackView.visibility = View.VISIBLE
                yourEmojiTitleText.visibility = View.VISIBLE

            }
        })

        val fadeInAnim = ValueAnimator.ofFloat(0f, 1f)
        fadeInAnim.addUpdateListener { valueAnimator: ValueAnimator ->
            val alpha = valueAnimator.animatedValue as Float
            emojiIdTextView.alpha = alpha
            walletAddressDescText.alpha = alpha
        }
        fadeInAnim.startDelay = CreateEmojiId.emojiIdImageViewAnimDelayMs
        fadeInAnim.duration = CreateEmojiId.continueButtonAnimDurationMs

        val animSet = AnimatorSet()
        animSet.playTogether(
            buttonAnimSet,
            emojiContainerImageScaleAnim,
            fadeInAnim,
            yourEmojiTitleAnim
        )
        animSet.duration = CreateEmojiId.emojiIdCreationViewAnimDurationMs
        animSet.interpolator = EasingInterpolator(Ease.QUINT_IN)
        animSet.start()
    }

    private fun startCreateEmojiAnimation() {
        nerdFaceEmoji.translationY = -(nerdFaceEmoji.height).toFloat()
        nerdFaceEmoji.alpha = 1f
        nerdFaceEmoji.playAnimation()

        val fadeInAnim = ValueAnimator.ofFloat(0f, 1f)
        fadeInAnim.addUpdateListener { valueAnimator: ValueAnimator ->
            val alpha = valueAnimator.animatedValue as Float
            walletAddressDescText.alpha = alpha
            createEmojiIdButton.alpha = alpha
        }

        val createNowAnim: ObjectAnimator =
            ObjectAnimator.ofFloat(
                createYourEmojiIdText,
                View.TRANSLATION_Y,
                0f,
                -createYourEmojiIdText.height.toFloat()
            )
        createNowAnim.duration = CreateEmojiId.awesomeTextAnimDurationMs

        val awesomeAnim: ObjectAnimator =
            ObjectAnimator.ofFloat(
                awesomeText,
                View.TRANSLATION_Y,
                0f,
                -awesomeText.height.toFloat()
            )
        awesomeAnim.duration = CreateEmojiId.awesomeTextAnimDurationMs

        val offset = -(createEmojiIdButton.height + createEmojiButtonBottomMargin).toFloat()
        val buttonAnim: ObjectAnimator =
            ObjectAnimator.ofFloat(createEmojiIdButton, View.TRANSLATION_Y, 0f, offset)
        buttonAnim.duration = CreateEmojiId.awesomeTextAnimDurationMs
        buttonAnim.startDelay = CreateEmojiId.createEmojiButtonAnimDelayMs

        val animSet = AnimatorSet()
        animSet.playTogether(fadeInAnim, createNowAnim, awesomeAnim, buttonAnim)

        animSet.startDelay = CreateEmojiId.viewOverlapDelayMs
        animSet.duration = CreateEmojiId.createEmojiViewAnimDurationMs
        animSet.interpolator = EasingInterpolator(Ease.QUINT_OUT)
        animSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
                createEmojiIdTextBackView.visibility = View.VISIBLE
                awesomeTextBackView.visibility = View.VISIBLE
                awesomeText.visibility = View.VISIBLE
                createYourEmojiIdText.visibility = View.VISIBLE
                helloTextBackView.visibility = View.GONE
                justSecBackView.visibility = View.GONE
            }
        })
        animSet.start()
    }

    private fun startSecondViewTextAnimation() {
        val helloTextFadeOutAnim = ValueAnimator.ofFloat(1f, 0f)
        helloTextFadeOutAnim.duration = CreateEmojiId.shortAlphaAnimDuration

        helloTextFadeOutAnim.addUpdateListener { valueAnimator: ValueAnimator ->
            val alpha = valueAnimator.animatedValue as Float
            helloText.alpha = alpha
        }

        helloTextFadeOutAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                helloText.visibility = View.GONE
            }
        })
        helloTextFadeOutAnim.start()
        uiHandler.postDelayed(
            { showSecondViewByAnim() },
            CreateEmojiId.viewOverlapDelayMs
        )
    }

    private fun showSecondViewByAnim() {
        val offset = -justSecTitle.height.toFloat()
        val titleAnim: ObjectAnimator =
            ObjectAnimator.ofFloat(justSecTitle, View.TRANSLATION_Y, 0f, offset)
        titleAnim.interpolator = EasingInterpolator(Ease.QUINT_OUT)
        titleAnim.startDelay = CreateEmojiId.titleShortAnimDelayMs
        val descAnim: ObjectAnimator =
            ObjectAnimator.ofFloat(justSecDescText, View.TRANSLATION_Y, 0f, offset)
        descAnim.interpolator = EasingInterpolator(Ease.QUINT_OUT)

        val animSet = AnimatorSet()
        animSet.playTogether(titleAnim, descAnim)
        animSet.duration = CreateEmojiId.helloTextAnimDurationMs
        animSet.interpolator = EasingInterpolator(Ease.QUART_OUT)
        animSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
                justSecDescText.visibility = View.VISIBLE
                justSecTitle.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                uiHandler.postDelayed(
                    { startCheckMarkAnimation() },
                    CreateEmojiId.viewChangeAnimDelayMs
                )
            }
        })
        animSet.start()
    }

    private fun startCheckMarkAnimation() {
        val fadeOut = ValueAnimator.ofFloat(1f, 0f)
        fadeOut.duration = CreateEmojiId.shortAlphaAnimDuration
        fadeOut.addUpdateListener { valueAnimator: ValueAnimator ->
            val alpha = valueAnimator.animatedValue as Float
            justSecDescText.alpha = alpha
            justSecTitle.alpha = alpha
        }

        fadeOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                super.onAnimationEnd(animation, isReverse)
                checkMarkAnim.visibility = View.VISIBLE

            }
        })
        fadeOut.start()
        checkMarkAnim.playAnimation()
    }

    private fun playStartupWhiteBgAnimation() {
        val whiteBgViewAnim: ObjectAnimator =
            ObjectAnimator.ofFloat(
                whiteBgView,
                View.TRANSLATION_Y,
                -whiteBgView.height.toFloat(),
                0f
            )

        whiteBgViewAnim.startDelay = CreateEmojiId.whiteBgAnimDelayMs
        whiteBgViewAnim.duration = CreateEmojiId.whiteBgAnimDurationMs
        whiteBgViewAnim.interpolator = EasingInterpolator(Ease.SINE_OUT)
        whiteBgViewAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                startInitHelloTextAnimation()
            }

            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
                whiteBgView.visibility = View.VISIBLE
            }
        })
        whiteBgViewAnim.start()
    }

    private fun startInitHelloTextAnimation() {
        val offset = -helloText.height.toFloat()
        val helloTextAnim: ObjectAnimator =
            ObjectAnimator.ofFloat(helloText, View.TRANSLATION_Y, 0f, offset)

        helloTextAnim.duration = CreateEmojiId.helloTextAnimDurationMs
        helloTextAnim.interpolator = EasingInterpolator(Ease.QUINT_OUT)

        helloTextAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
                helloTextBackView.visibility = View.VISIBLE
                helloText.visibility = View.VISIBLE
                justSecBackView.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                uiHandler.postDelayed({
                    startSecondViewTextAnimation()
                }, CreateEmojiId.viewChangeAnimDelayMs)
            }
        })
        helloTextAnim.start()
    }
}