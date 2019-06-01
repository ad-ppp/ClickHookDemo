package com.example.jacky.clickhookdemo

import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class KotlinActivity : TestActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val button = Button(this)
        setContentView(button)

        button.text = "点击我"
        button.setOnClickListener {
            Toast.makeText(this, "lala", Toast.LENGTH_LONG).show()
        }
    }
}