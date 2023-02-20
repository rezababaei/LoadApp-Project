/*
 * Copyright (C) 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.udacity.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.udacity.DetailActivity

import com.udacity.MainActivity
import com.udacity.R

// Notification ID.
private const val NOTIFICATION_ID = 0

/**
 * Builds and delivers the notification.
 *
 * @param context, activity context.
 */
fun NotificationManager.sendNotification(
    messageBody: String, applicationContext: Context, downloadState: String,
) {

    // Create the content intent for the notification, which launches
    // this activity
    val detailIntent = Intent(applicationContext, DetailActivity::class.java)
    detailIntent.apply {
        putExtra(Constants.FILE_NAME_EXTRA, messageBody)
        putExtra(Constants.DOWNLOAD_STATUS_EXTRA, downloadState)
    }

//    val resultPendingIntent =
//        PendingIntent.getActivity(applicationContext, NOTIFICATION_ID, detailIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
//        )

// Create the TaskStackBuilder  save the main activity state for a better user experience
    val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(applicationContext).run {
        // Add the intent, which inflates the back stack
        addNextIntentWithParentStack(detailIntent)
        // Get the PendingIntent containing the entire back stack
        getPendingIntent(0,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }



    val builder = NotificationCompat.Builder(applicationContext,
        applicationContext.getString(R.string.load_app_channel_id))
        .setSmallIcon(R.drawable.ic_assistant_black_24dp)
        .setContentTitle(applicationContext.getString(R.string.notification_title))
        .setContentText(messageBody)
//        .setContentIntent(contentPendingIntent)
        .setContentIntent(resultPendingIntent)
        .setAutoCancel(true)
        .addAction(
            R.drawable.ic_assistant_black_24dp,
            applicationContext.getString(R.string.notification_button),

            resultPendingIntent
        )
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    // Deliver the notification
//    notify(NOTIFICATION_ID, builder.build())
    with(NotificationManagerCompat.from(applicationContext)) {
        notify(NOTIFICATION_ID, builder.build())
    }
}

// TODO: Step 1.14 Cancel all notifications
fun NotificationManager.cancelNotifications() {
    cancelAll()
}