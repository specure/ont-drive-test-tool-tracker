package com.specure.updater.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.widget.Toast
import com.jakewharton.processphoenix.ProcessPhoenix
import timber.log.Timber

class GithubAppUpdateReloader : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            ProcessPhoenix.triggerRebirth(context)
            return
        }
        when (val status = intent?.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirmationIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                if (confirmationIntent != null) {
                    context?.startActivity(confirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }

            else -> {
                val message = intent?.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                Timber.d("status=$status, message=$message")
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

}