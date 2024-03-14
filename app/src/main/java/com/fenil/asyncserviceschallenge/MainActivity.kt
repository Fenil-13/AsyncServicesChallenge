package com.fenil.asyncserviceschallenge

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.fenil.asyncserviceschallenge.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    private var isVideoRunning = false
    private var playbackPosition = 0
    private val videoUri: Uri
        get() = Uri.parse("android.resource://" + packageName + "/" + R.raw.video)

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.getParcelableExtra<SampleData>(Constant.DATA_KEY)?.let {
                binding.channel1data.text = it.channel1Data
                binding.channel2data.text = it.channel2Data
                binding.channel3data.text = it.channel3Data
                binding.channel4data.text = it.channel4Data
            }

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.videoView.setOnCompletionListener {
            binding.videoView.start()
        }
        startReadingDataService()
    }

    private fun startReadingDataService() {
        val serviceIntent = Intent(this, SampleDecodeService::class.java)
        startService(serviceIntent)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        playbackPosition = binding.videoView.currentPosition
        binding.videoView.pause()
    }
    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(Constant.ACTION_SAMPLE_UI_UPDATE)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)

        binding.videoView.setVideoURI(videoUri)
        if (isVideoRunning){
            binding.videoView.seekTo(playbackPosition)
        }else{
            isVideoRunning = true
        }
        binding.videoView.start()
    }
}