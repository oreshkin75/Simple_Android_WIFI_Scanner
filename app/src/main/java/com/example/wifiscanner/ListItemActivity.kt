package com.example.wifiscanner

import android.net.wifi.ScanResult
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_list_item.*

class ListItemActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_item)

        // выставляем текст нажатого элемента в TextView
        textView.text = intent.getStringExtra("text")
    }
}