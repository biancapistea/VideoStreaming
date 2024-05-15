package com.example.videostreaming

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.videostreaming.media.VideoLogic


class MainActivity : AppCompatActivity() {
    private var startingFragment: StartingFragment? = null
    private var visibleDrawer = false
    private var isRemoteUserVideoDisplayed = false
    private var appInstance: VideoLogic? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        appInstance = VideoLogic.getInstance(baseContext)
        setupFragments()
        setUpButtons()
    }

    override fun onStart() {
        super.onStart()
        checkPermissions()
    }


    private fun setUpButtons() {
        val button = findViewById<Button>(R.id.toggleDrawButton)
        val drawer = findViewById<CustomDrawingView>(R.id.drawingView)
        toggleRemoteUserVideo(findViewById(R.id.videoView))
        button.setOnClickListener {
            if (visibleDrawer) {
                drawer.visibility = View.GONE
                drawer.clear()
            } else {
                drawer.visibility = View.VISIBLE
            }
            visibleDrawer = !visibleDrawer
        }
    }

    private fun toggleRemoteUserVideo(videoView: VideoView) {
        val toggleButton = findViewById<Button>(R.id.toggleButton)

        toggleButton.setOnClickListener {
            if (isRemoteUserVideoDisplayed) {
                videoView.stopPlayback()
                videoView.visibility = View.GONE
            } else {
                videoView.visibility = View.VISIBLE
                videoView.setVideoPath("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
                videoView.start()
            }
            isRemoteUserVideoDisplayed = !isRemoteUserVideoDisplayed
        }
    }


    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val requiredPermissions: MutableList<String> = ArrayList(2)
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requiredPermissions.add(Manifest.permission.RECORD_AUDIO)
            }
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requiredPermissions.add(Manifest.permission.CAMERA)
            }
            if (requiredPermissions.size != 0) {
                requestPermissions(requiredPermissions.toTypedArray<String>(), 1)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onDestroy() {
        super.onDestroy()
        finish()
    }

    private fun addFragment(fragment: Fragment?, id: Int) {
        val manager = supportFragmentManager
        manager.beginTransaction()
            .replace(id, fragment!!)
            .setReorderingAllowed(true)
            .commit()
    }

    private fun setupFragments() {
        startingFragment = StartingFragment.newInstance()
        addFragment(startingFragment, R.id.videoButtons)
    }
}