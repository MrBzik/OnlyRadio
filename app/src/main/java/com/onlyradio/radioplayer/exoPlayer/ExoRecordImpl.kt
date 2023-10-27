package com.onlyradio.radioplayer.exoPlayer

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaExtractor
import android.media.MediaFormat
import android.widget.Toast
import com.onlyradio.radioplayer.data.local.entities.Recording
import com.onlyradio.radioplayer.utils.Constants.TITLE_UNKNOWN
import com.onlyradio.radioplayer.utils.Utils
import com.onlyradio.radioplayer.R
import com.onlyradio.radioplayer.exoRecord.ExoRecord
import com.onlyradio.radioplayer.exoRecord.IExoRecord
import com.onlyradio.radioplayer.extensions.makeToast
import com.onlyradio.radioplayer.utils.Constants.RECORDING_QUALITY_PREF
import com.onlyradio.radioplayer.utils.Constants.REC_QUALITY_DEF

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

private const val RECORDING_HANDLER = "keeps info about last started recording"
private const val IS_RECORDING_HANDLED = "is recording handled"
private const val RECORDING_FILE_NAME = "file and path of the recording"
private const val RECORDING_NAME = "name of recording"
private const val RECORDING_TIMESTAMP = "rec. time stamp"
private const val RECORDING_ICON_URL = "rec. url path"

class ExoRecordImpl (private val service: RadioService) {



    private val recordingCheck : SharedPreferences by lazy {
        service.getSharedPreferences(RECORDING_HANDLER, Context.MODE_PRIVATE)
    }

    private val recQualityPref by lazy {
        service.getSharedPreferences(RECORDING_QUALITY_PREF, Context.MODE_PRIVATE)
    }

    private val exoRecord by lazy {
        service.exoRecord
    }

    private var isExoRecordListenerToSet = true

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

            service.radioSource.exoRecordState.postValue(true)

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
                putLong(RECORDING_TIMESTAMP, System.currentTimeMillis())
            }.apply()


            service.radioNotificationManager.updateForStartRecording()

        }


        override fun onStopRecording(record: IExoRecord.Record) {

            service.radioNotificationManager.updateForStopRecording()

            timer.cancel()

            service.radioSource.exoRecordState.postValue(false)

            try {
                insertNewRecording(
                    record.filePath,
                    System.currentTimeMillis(),
                    duration
                )

                recordingCheck.edit().putBoolean(IS_RECORDING_HANDLED, true).apply()
            } catch (e : Exception){
                service.makeToast(R.string.exorecord_error)
            }
        }
    }


    private  fun insertNewRecording(
        recId : String, timeStamp : Long, duration : Long
    ) = service.serviceScope.launch(Dispatchers.IO) {

        val iconUri = recordingCheck.getString(RECORDING_ICON_URL, "") ?: ""
        val name = "Rec. ${ recordingCheck.getString(RECORDING_NAME, "") ?: ""}"

        var dur = duration

        if(duration == 0L){
            dur = getRecordingDuration(recId)
        }

        service.radioSource.insertRecording(
            Recording(
                recId, iconUri, timeStamp, name, dur
            )
        )
    }


    private fun setExoRecordListener(){
        exoRecord.addExoRecordListener("MainListener", exoRecordListener)
        isExoRecordListenerToSet = false
    }


     private fun startRecording () = service.serviceScope.launch {

            val quality = recQualityPref.getFloat(
                RECORDING_QUALITY_PREF,
                REC_QUALITY_DEF
            )
         exoRecord.startRecording(quality)
    }

    fun stopRecording () = service.serviceScope.launch(Dispatchers.IO) {
        exoRecord.stopRecording()
    }


    fun checkRecordingAndRecoverIfNeeded(){

        val check = recordingCheck.getBoolean(IS_RECORDING_HANDLED, true)

        if(!check){

            service.serviceScope.launch(Dispatchers.IO){
                val recId = recordingCheck.getString(RECORDING_FILE_NAME, "")
                recId?.let { id ->
                    val duration = getRecordingDuration(id)

                    insertNewRecording(
                        recId = recId,
                        timeStamp = recordingCheck.getLong(RECORDING_TIMESTAMP, System.currentTimeMillis()),
                        duration = duration
                    )
                }

                recordingCheck.edit().putBoolean(IS_RECORDING_HANDLED, true).apply()
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