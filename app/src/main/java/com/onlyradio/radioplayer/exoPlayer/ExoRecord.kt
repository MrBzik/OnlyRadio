package com.onlyradio.radioplayer.exoPlayer

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import android.widget.Toast
import com.onlyradio.radioplayer.data.local.entities.Recording
import com.onlyradio.radioplayer.utils.Constants
import com.onlyradio.radioplayer.utils.Constants.TITLE_UNKNOWN
import com.onlyradio.radioplayer.utils.Utils
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.audio.AudioProcessor
import com.onlyradio.radioplayer.R
import dev.brookmg.exorecord.lib.ExoRecord
import dev.brookmg.exorecord.lib.IExoRecord
import dev.brookmg.exorecordogg.ExoRecordOgg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Timer
import java.util.TimerTask


private const val RECORDING_HANDLER = "keeps info about last started recording"
private const val IS_RECORDING_HANDLED = "is recording handled"
private const val RECORDING_FILE_NAME = "file and path of the recording"
private const val RECORDING_NAME = "name of recording"
private const val RECORDING_TIMESTAMP = "rec. time stamp"
private const val RECORDING_ICON_URL = "rec. url path"
private const val RECORDING_SAMPLE_RATE = "rec. sample rate"
private const val RECORDING_CHANNELS_COUNT = "rec. channels count"


class ExoRecordImpl (private val service: RadioService) {

    private val recordingCheck : SharedPreferences by lazy {
        service.getSharedPreferences(RECORDING_HANDLER, Context.MODE_PRIVATE)
    }

    private val exoRecord by lazy {
        service.exoRecord
    }

    private var recSampleRate = 0
    private var recChannelsCount = 2


    fun onCommandStartRecording(){
        if(isExoRecordListenerToSet){
            setExoRecordListener()
        }
        startRecording()
    }


    private val exoRecordListener = object : ExoRecord.ExoRecordListener{

        lateinit var timer : Timer
        var duration = 0L


        override fun onStartRecording(recordFileName: String) {

//            val notification = RecordingNotification(this@RadioService) {
//                currentRadioStation?.name ?: ""
//            }

//            notification.showNotification()

            service.radioSource.exoRecordState.postValue(true)
            service.radioSource.exoRecordFinishConverting.postValue(false)

            timer = Timer()
            timer.scheduleAtFixedRate(object : TimerTask() {
                val startTime = System.currentTimeMillis()
                override fun run() {
                    duration = (System.currentTimeMillis() - startTime)

                    if(duration > RadioService.autoStopRec) {
                        if(RadioService.autoStopRec != 180 * 60000)
                            stopRecording()
                    } else {

                        val time = Utils.timerFormat(duration)

                        service.radioNotificationManager.recordingDuration = time
                        service.radioNotificationManager.updateNotification()

                        service.radioSource.exoRecordTimer.postValue(time)
                    }
                }
            }, 0L, 1000L)

            recordingCheck.edit().apply{


                val recordingName = if(RadioService.isToUseTitleForRecNaming
                    && RadioService.currentlyPlayingSong != TITLE_UNKNOWN
                ) {
                    RadioService.currentlyPlayingSong
                } else service.currentRadioStation?.name ?: ""

                putBoolean(IS_RECORDING_HANDLED, false)
                putString(RECORDING_FILE_NAME, recordFileName)
                putString(RECORDING_NAME, recordingName)
                putString(RECORDING_ICON_URL, service.currentRadioStation?.favicon ?: "")
                putInt(RECORDING_SAMPLE_RATE, recSampleRate)
                putInt(RECORDING_CHANNELS_COUNT, recChannelsCount)
                putLong(RECORDING_TIMESTAMP, System.currentTimeMillis())
            }.apply()


            service.radioNotificationManager.updateForStartRecording()


        }

        override fun onStopRecording(record: IExoRecord.Record) {

//            NotificationManagerCompat.from(this@RadioService).cancel(RECORDING_NOTIFICATION_ID)


            service.radioNotificationManager.updateForStopRecording()

            timer.cancel()
            isConverterWorking = true
            service.radioSource.exoRecordState.postValue(false)

            convertRecording(record.filePath, recSampleRate, recChannelsCount, System.currentTimeMillis(), duration)

        }
    }


    private fun convertRecording(
        filePath: String, sampleRate : Int, channelsCount : Int,
        timeStamp : Long, duration : Long
    )
            = service.serviceScope.launch(Dispatchers.IO) {

        var isSuccess = true

        try {
            val recQualityPref = service.getSharedPreferences(Constants.RECORDING_QUALITY_PREF, Context.MODE_PRIVATE)
            val setting = recQualityPref.getFloat(
                Constants.RECORDING_QUALITY_PREF,
                Constants.REC_QUALITY_DEF
            )
            
            ExoRecordOgg.convertFile(
                service.application,
                filePath,
                sampleRate,
                channelsCount,
                setting
            ){ progress ->

                if(progress == 100.0f && isSuccess){

                    insertNewRecording(
                        filePath,
                        timeStamp,
                        duration
                    )
                    service.deleteFile(filePath)

                    onConversionEnd()
                }
            }
        } catch (e: java.lang.Exception){

            isSuccess = false

            try {
                val fileList = service.fileList().filter {
                    it.endsWith(".wav")
                }

                fileList.forEach { name ->

                    service.deleteFile(name)
                }

                onConversionEnd()

                withContext(Dispatchers.Main){
                    Toast.makeText(service, service.getText(R.string.exorecord_error), Toast.LENGTH_LONG).show()
                }

            } catch (ex : Exception){

            }
        }

        this.cancel()

    }

    private fun onConversionEnd(){
        isConverterWorking = false
        service.radioSource.exoRecordFinishConverting.postValue(true)
        recordingCheck.edit().putBoolean(IS_RECORDING_HANDLED, true).apply()
    }

    private suspend fun insertNewRecording(
        recId : String, timeStamp : Long, duration : Long
    ) {

//        Log.d("CHECKTAGS", "inserting new recording")

        val id = recId.replace(".wav", ".ogg")
        val iconUri = recordingCheck.getString(RECORDING_ICON_URL, "") ?: ""
        val name = "Rec. ${ recordingCheck.getString(RECORDING_NAME, "") ?: ""}"

        var dur = duration

        if(duration == 0L){
            dur = getRecordingDuration(id)
        }


        service.radioSource.insertRecording(
            Recording(
                id, iconUri, timeStamp, name, dur
            )
        )
    }


    private var isExoRecordListenerToSet = true

    private var isConverterWorking = false

    private fun setExoRecordListener(){
        exoRecord.addExoRecordListener("MainListener", exoRecordListener)
        isExoRecordListenerToSet = false
    }


     fun startRecording () = service.serviceScope.launch {
        if(!isConverterWorking){

            val format = service.exoPlayer.audioFormat
            val sampleRate = format?.sampleRate ?: 0
            val channels = format?.channelCount ?: 0


//            Log.d("CHECKTAGS", "$sampleRate, $channels")


            recSampleRate = if(sampleRate == 22050 && format?.sampleMimeType == "audio/mp4a-latm" ||
                sampleRate == 24000 && format?.sampleMimeType == "audio/mp4a-latm"
            ) {
                recChannelsCount = 2
                sampleRate*2
            } else {
                recChannelsCount = channels
                sampleRate
            }

//            Log.d("CHECKTAGS", "rec in : $recChannelsCount, $recSampleRate")

            exoRecord.exoRecordProcessor.configure(AudioProcessor.AudioFormat(sampleRate, channels,  C.ENCODING_PCM_16BIT))

            exoRecord.startRecording()
        }
    }

    fun stopRecording () = service.serviceScope.launch(Dispatchers.IO) {
        if(!isConverterWorking){
            exoRecord.stopRecording()
        }
    }


    fun checkRecordingAndRecoverIfNeeded(){

        val check = recordingCheck.getBoolean(IS_RECORDING_HANDLED, true)

        if(!check){

            service.serviceScope.launch(Dispatchers.IO){
                val recId = recordingCheck.getString(RECORDING_FILE_NAME, "")
                recId?.let { id ->
                    val duration = getRecordingDuration(id)

                    convertRecording(
                        id,
                        recordingCheck.getInt(RECORDING_SAMPLE_RATE, 44100),
                        recordingCheck.getInt(RECORDING_CHANNELS_COUNT, 2),
                        recordingCheck.getLong(RECORDING_TIMESTAMP, System.currentTimeMillis()),
                        duration / 1000
                    )
                }
            }
        }
    }

    private fun getRecordingDuration(id : String) : Long {
        var duration = 0L

        try {

            val filePath = service.filesDir.absolutePath + "/" + id
            val extractor = MediaExtractor()
            extractor.setDataSource(filePath)
            val format = extractor.getTrackFormat(0)
            val dur = format.getLong(MediaFormat.KEY_DURATION)
            duration = dur

        } catch (e : Exception){
//            Log.d("CHECKTAGS", e.stackTraceToString())
        }

        return duration
    }


}