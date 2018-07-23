package com.fintech.lxf.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.NotificationManagerCompat
import com.fintech.lxf.R
import com.fintech.lxf.base.BaseActivity
import com.fintech.lxf.service.observer.NotificationListener

class ObserverActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_observer)

        startObserverService()
    }

    override fun onResume() {
        super.onResume()
        if (!isNotificationListenerEnabled(this))
            openNotificationListenSettings()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this,NotificationListener::class.java))
    }

    private fun startObserverService(){
        startService(Intent(this,NotificationListener::class.java))
    }

    private fun isNotificationListenerEnabled(context: Context): Boolean {
        val packageNames = NotificationManagerCompat.getEnabledListenerPackages(this)
        return packageNames.contains(context.packageName)
    }

    private fun openNotificationListenSettings() {
        try {
            val intent: Intent
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            } else {
                intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}
