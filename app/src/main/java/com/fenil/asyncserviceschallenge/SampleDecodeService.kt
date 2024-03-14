package com.fenil.asyncserviceschallenge

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import java.io.IOException
import java.util.concurrent.Executors
import java.util.regex.Matcher
import java.util.regex.Pattern

@OptIn(DelicateCoroutinesApi::class)
class SampleDecodeService : Service()  {

    private val displayRate = 100

    private val executor = Executors.newFixedThreadPool(1).asCoroutineDispatcher()
    private val scope = CoroutineScope(executor)
    private val channel1Executor = newSingleThreadContext("Channel1")
    private val channel2Executor = newSingleThreadContext("Channel2")
    private val channel3Executor = newSingleThreadContext("Channel3")
    private val channel4Executor = newSingleThreadContext("Channel4")


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startDecoding()
        return START_NOT_STICKY
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    private fun startDecoding() {
        readSamplesInBackground()
    }

    /**
     * Reads samples from a file in the background and broadcasts the processed data.
     * Extract every sample via Pattern and decode the data [separated by 6 byte hex code]
     * decodeAndDisplayData() function decode and wait for a result
     * if current sample is 100th item then display to UI via LocalBroadcastManager
     */
    private fun readSamplesInBackground() {
        scope.launch {
            try {
                applicationContext.assets.open(Constant.FILE_NAME).bufferedReader().use { br ->
                    var line: String?
                    var count = 0
                    while ((br.readLine().also { line = it }) != null) {
                        val pattern: Pattern = Pattern.compile("!(.*?)!")
                        val matcher: Matcher = pattern.matcher(line!!)
                        while (matcher.find()) {
                            var currentSample = matcher.group(1)
                            if (currentSample != null && currentSample.length == 26) {
                                currentSample = currentSample.substring(2)
                                val channel1Data = currentSample.substring(0, 6)
                                val channel2Data = currentSample.substring(6, 12)
                                val channel3Data = currentSample.substring(12, 18)
                                val channel4Data = currentSample.substring(18, 24)
                                val task  = async { decodeAndDisplayData(SampleData(channel1Data, channel2Data, channel3Data, channel4Data)) }
                                val processedData = task.await()
                                if (count % displayRate == 0) {
                                    val intent = Intent(Constant.ACTION_SAMPLE_UI_UPDATE)
                                    intent.putExtra(Constant.DATA_KEY, processedData)
                                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                                }
                                count++
                            }
                        }
                    }
                    stopSelf()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                showToast("Something Went Wrong!! Stopping Service")
                stopSelf()
            }
        }
    }

    /**
     * lunch for different coroutine which run on 4 different Thread and wait for result
     */
    private suspend fun decodeAndDisplayData(sampleData: SampleData) : SampleData {
        val result = SampleData()
        val job1 = CoroutineScope(channel1Executor).launch {
            result.channel1Data = decodeChannel(sampleData.channel1Data)
        }
        val job2 = CoroutineScope(channel2Executor).launch {
            result.channel2Data = decodeChannel(sampleData.channel2Data)
        }
        val job3 = CoroutineScope(channel3Executor).launch {
            result.channel3Data = decodeChannel(sampleData.channel3Data)
        }
        val job4 = CoroutineScope(channel4Executor).launch {
            result.channel4Data = decodeChannel(sampleData.channel4Data)
        }
        joinAll(job1, job2, job3, job4)
        return result
    }

    /**
     * Decodes a hexadecimal voltage value into a decimal string.
     *
     * @param hexVoltageValue the hexadecimal voltage value to decode
     * @return the decimal string of the voltage value
     */
    private fun decodeChannel(hexVoltageValue: String): String {
        return hexVoltageValue.toInt(16).toString()
    }


    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.close()
        channel1Executor.close()
        channel2Executor.close()
        channel3Executor.close()
        channel4Executor.close()
    }
}