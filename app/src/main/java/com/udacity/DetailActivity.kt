package com.udacity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.udacity.util.Constants


class DetailActivity : AppCompatActivity() {

    lateinit var toolbar:Toolbar
    lateinit var okButton:Button
    lateinit var fileNameTextView:TextView
    lateinit var statusTextView:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        toolbar=findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        okButton=findViewById(R.id.ok_button)
        fileNameTextView=findViewById(R.id.file_name_text)
        statusTextView=findViewById(R.id.status_text)

        val fileName = intent.getStringExtra(Constants.FILE_NAME_EXTRA).toString()
        val downloadStatus = intent.getStringExtra(Constants.DOWNLOAD_STATUS_EXTRA).toString()
        fileNameTextView.text = fileName
        statusTextView.text = downloadStatus
        if(downloadStatus == Constants.FAIL){
            statusTextView.setTextColor(Color.RED)
        }else{
            statusTextView.setTextColor(getColor(R.color.colorPrimaryDark))
        }

        okButton.setOnClickListener {
            finish()
        }


    }

}
