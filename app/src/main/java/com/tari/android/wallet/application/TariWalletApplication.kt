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
package com.tari.android.wallet.application

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.tari.android.wallet.di.*
import com.tari.android.wallet.notification.NotificationHelper
import com.tari.android.wallet.service.TariTorService
import com.tari.android.wallet.service.TariTorServiceListener
import com.tari.android.wallet.service.TorService
import com.tari.android.wallet.util.SharedPrefsWrapper
import com.tari.android.wallet.util.WalletUtil
import com.tari.android.wallet.util.getProcessNameCompat
import net.danlew.android.joda.JodaTimeAndroid
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.DownloadTracker
import org.matomo.sdk.extra.TrackHelper
import javax.inject.Inject
import javax.inject.Named

/**
 * Main application class.
 *
 * @author The Tari Development Team
 */
internal class TariWalletApplication : Application(), LifecycleObserver {

    @JvmField
    @field:[Inject Named(ConfigModule.FieldName.deleteExistingWallet)]
    var deleteExistingWallet: Boolean = false
    @Inject
    @Named(WalletModule.FieldName.walletFilesDirPath)
    lateinit var walletFilesDirPath: String
    @Inject
    lateinit var notificationHelper: NotificationHelper
    @Inject
    lateinit var tracker: Tracker

    @Inject
    lateinit var torConfig: TorConfig

    lateinit var appComponent: ApplicationComponent
    private lateinit var sharedPrefsWrapper: SharedPrefsWrapper
    private val sharedPrefsFileName = "tari_wallet_shared_prefs"
    private val activityLifecycleCallbacks = ActivityLifecycleCallbacks()
    var isInForeground = false
        private set

    init {
        System.loadLibrary("native-lib")
    }

    val currentActivity: Activity?
        get() {
            return activityLifecycleCallbacks.currentActivity
        }

    override fun onCreate() {
        super.onCreate()
        if (getProcessNameCompat(this)?.contains("torservice") == true) {
            // Process is from TOR service, don't do anything
            return
        }

        registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
        Logger.addLogAdapter(AndroidLogAdapter())
        JodaTimeAndroid.init(this)
        sharedPrefsWrapper = SharedPrefsWrapper(
            this,
            getSharedPreferences(
                sharedPrefsFileName,
                Context.MODE_PRIVATE
            )
        )
        appComponent = initDagger(this)
        appComponent.inject(this)
        if (deleteExistingWallet) {
            WalletUtil.clearWalletFiles(walletFilesDirPath)
            sharedPrefsWrapper.clean()
        }

        notificationHelper.createNotificationChannels()

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        initTorProxy()
		
        TrackHelper.track().download().identifier(
            DownloadTracker.Extra.ApkChecksum(this)
        ).with(tracker)
    }

    private fun initDagger(app: TariWalletApplication): ApplicationComponent =
        DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(app, sharedPrefsWrapper))
            .build()

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        Logger.d("App in background.")
        isInForeground = false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        Logger.d("App in foreground.")
        isInForeground = true
    }


    private fun initTorProxy() {
        val bindIntent = Intent(this, TorService::class.java)
        bindService(bindIntent, torProxyConnection, Context.BIND_AUTO_CREATE)
    }

    private val torProxyConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Logger.d("TOR service disconnected")
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Logger.d("TOR service connected")
            val torService = TariTorService.Stub.asInterface(service)
            torService.registerListener(torProxyListener)

            torService.start(
                torConfig.proxyPort,
                torConfig.controlHost,
                torConfig.controlPort,
                torConfig.sock5Username,
                torConfig.sock5Password
            )
        }
    }

    private val torProxyListener = object : TariTorServiceListener.Stub() {
        override fun onTorServiceError(error: String?) {
            Logger.e("Tor service error $error")
        }
    }
}