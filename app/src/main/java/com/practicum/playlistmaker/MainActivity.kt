package com.practicum.playlistmaker

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val searchButton = findViewById<Button>(R.id.magnifier)
        searchButton.setOnClickListener {
            Toast.makeText(this@MainActivity, "БУУУУУУ!", Toast.LENGTH_SHORT).show()
        }

        val mediaButton = findViewById<Button>(R.id.media_library)
        mediaButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                Toast.makeText(this@MainActivity, "Ха-Ха Испугался?", Toast.LENGTH_SHORT).show()
            }
        })

        val settingsButton = findViewById<Button>(R.id.settings)
        settingsButton.setOnClickListener {
            Toast.makeText(this@MainActivity, "Не бойся, я друг)))", Toast.LENGTH_SHORT).show()
        }
    }
}