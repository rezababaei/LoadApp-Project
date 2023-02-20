package com.udacity

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.udacity.util.Constants
import com.udacity.util.sendNotification

const val PERMISSION_POST_NOTIFICATIONS = 0

class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    private lateinit var toolbar: Toolbar
    private lateinit var customButton: LoadingButton
    private lateinit var mainRadioGroup: RadioGroup

    private var fileName: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar = findViewById(R.id.toolbar)
        customButton = findViewById(R.id.custom_button)
        mainRadioGroup = findViewById(R.id.main_radio_group)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        customButton.setOnClickListener {
            var url = ""

            when (mainRadioGroup.checkedRadioButtonId) {
                R.id.glide_radio -> {
                    url = Constants.GlIDE_URL
                    fileName = R.string.glide_image_loading
                }
                R.id.current_repo_radio -> {
                    url = Constants.CURRENT_APP_URL
                    fileName = R.string.loadapp_current_repository
                }
                R.id.retrofit_radio -> {
                    url = Constants.RETROFIT_URL
                    fileName = R.string.retrofit_type_safe
                }
            }

            if (url.isNotEmpty()) {
                download(url)
                customButton.buttonState = ButtonState.Loading
            } else {
                customButton.buttonState = ButtonState.Clicked
                Toast.makeText(this@MainActivity, getString(R.string.please_select_file),
                    Toast.LENGTH_SHORT).show()
            }
        }

        requestPermissionToSendNotification(this)


    }


    private fun requestPermissionToSendNotification(context: Activity): Boolean {
        // Check if the Camera permission has been granted
        if ((ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED)
        ) {
            // Permission is already available, start camera preview
//            createChannelWithIdAndName()
            return true

        } else {
            // Permission is missing and must be requested.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermission(context,
                    Manifest.permission.POST_NOTIFICATIONS,
                    PERMISSION_POST_NOTIFICATIONS)
                return false
            }
        }
        return true
    }


    private val receiver = object : BroadcastReceiver() {

        @SuppressLint("Range")
        override fun onReceive(context: Context?, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadID == id) {
                val query = DownloadManager.Query()
                query.setFilterById(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0));
                val manager =
                    context!!.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val cursor: Cursor = manager.query(query)
                if (cursor.moveToFirst()) {
                    if (cursor.count > 0) {
                        val tempCursor = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        when (cursor.getInt(tempCursor)) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                customButton.buttonState = ButtonState.Completed
                                notificationManager.sendNotification(getString(fileName),
                                    applicationContext,
                                    Constants.SUCCESS)
                            }
                            else -> {
                                customButton.buttonState = ButtonState.Completed
                                notificationManager.sendNotification(getString(fileName),
                                    applicationContext,
                                    Constants.FAIL)
                            }
                        }


                    }
                }
            }
        }
    }


    private fun download(url: String) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) ||
            ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ) {
            // this will request for permission when user has not granted permission for the app
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1)
        } else {

            createChannel(getString(R.string.load_app_channel_id),
                getString(R.string.load_app_channel_name))

            val request =
                DownloadManager.Request(Uri.parse(url))
                    .setTitle(getString(R.string.app_name))
                    .setDescription(getString(R.string.app_description))
                    .setRequiresCharging(false)
                    .setAllowedOverMetered(true)

                    .setAllowedOverRoaming(true)

            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadID =
                downloadManager.enqueue(request)// enqueue puts the download request in the queue.
        }
    }


    private fun createChannel(channelId: String, channelName: String) {

        if (requestPermissionToSendNotification(this))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
                )
                    // TODO: Step 2.6 disable badges for this channel
                    .apply {
                        setShowBadge(false)
                    }
                notificationChannel.enableLights(true)
                notificationChannel.lightColor = Color.RED
                notificationChannel.enableVibration(true)
                notificationChannel.description = "Downloaded Files"

                notificationManager = ContextCompat.getSystemService(applicationContext,
                    NotificationManager::class.java) as NotificationManager
                notificationManager.createNotificationChannel(notificationChannel)
            }

    }

    /**
     * Requests the [android.Manifest.permission.POST_NOTIFICATIONS] permission.
     * If an additional rationale should be displayed, the user has to launch the request from
     * a SnackBar that includes additional information.
     */
    private fun requestPermission(context: Activity, permission: String, requestCode: Int) {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(context,
                permission)
        ) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            ActivityCompat.requestPermissions(context,
                arrayOf(permission),
                requestCode)
        } else {
//            "POST_NOTIFICATIONS_permission_not_available"
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(context,
                arrayOf(permission),
                requestCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_POST_NOTIFICATIONS -> {
                // Request for camera permission.
                if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission has been granted. Start camera preview Activity.
                    //"POST_NOTIFICATIONS_permission_granted"
//                    createChannelWithIdAndName()
                } else {
                    // Permission request was denied.
                    //"POST_NOTIFICATIONS_permission_denied"
                }
            }

        }
    }
}
