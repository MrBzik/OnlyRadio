package com.example.radioplayer.exoPlayer

// FOR REC NOTIFICATION

//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//
//        intent?.let {
//
//            if(it.action == COMMAND_STOP_RECORDING) {
//
//                stopRecording()

//                NotificationManagerCompat.from(this@RadioService).cancel(RECORDING_NOTIFICATION_ID)
//            }
//        }
//
//        return super.onStartCommand(intent, flags, startId)
//    }



//    private fun createRecordingNotificationChannel(){
//
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//
//            val channel = NotificationChannel(
//                RECORDING_CHANNEL_ID, "Recording",
//                NotificationManager.IMPORTANCE_DEFAULT
//            )
//            channel.description = "Shows ongoing recording"
//
//            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(channel)
//        }
//    }


// FOR VISUALIZER

//    private lateinit var visualizer: Visualizer

//        visualizer.measurementMode = MEASUREMENT_MODE_PEAK_RMS
//
//        visualizer.enabled = true

//      val peaks = visualizer.getMeasurementPeakRms(android.media.audiofx.Visualizer.MeasurementPeakRms())
//

//        val rate = Visualizer.getMaxCaptureRate()
//
//        visualizer.setDataCaptureListener(object : Visualizer.OnDataCaptureListener{
//            override fun onWaveFormDataCapture(
//                visualizer: Visualizer?,
//                waveform: ByteArray?,
//                samplingRate: Int
//            ) {
//                waveform?.let{
//                    val intensity = (it[0].toFloat() + 128f) / 256
//                    Log.d("CHECKTAGS", "intensity is: $intensity")
//                }
//            }
//
//            override fun onFftDataCapture(
//                visualizer: Visualizer?,
//                fft: ByteArray?,
//                samplingRate: Int
//            ) {
//
//            }
//        }, rate,  true, false)





// FOR SKIPPING EXCLUSIVE AD

//        serviceScope.launch {
//
//            exoPlayer.playbackParameters = PlaybackParameters(6f)
//            exoPlayer.volume = 0f
//
//            while (true) {
//
//               delay(200)
//
//                if(exoPlayer.currentPosition > 5000)
//                    break
//
//            }
//
//            exoPlayer.playbackParameters = PlaybackParameters(1f)
//            exoPlayer.volume = 1f
//
//        }


// CLEAR MEDIA ITEMS IMPL

//        if(exoPlayer.mediaItemCount != 1){
//
//            if(exoPlayer.currentMediaItemIndex != 0)
//                exoPlayer.moveMediaItem(exoPlayer.currentMediaItemIndex, 0)
//
//            exoPlayer.removeMediaItems(1, exoPlayer.mediaItemCount)
//        }
//


// ON LOAD CHILDREN
//       when(parentId){
//           MEDIA_ROOT_ID -> {
//               val resultSent = radioSource.whenReady { isInitialized ->
//                   if(isInitialized){
//                        try{

//                            result.sendResult(radioSource.asMediaItems())

//                            if(!isPlayerInitialized && radioSource.stations.isNotEmpty()) {
//                                preparePlayer(radioSource.stations, radioSource.stations[0], false)
//                                isPlayerInitialized = true
//                            }
//                        } catch (e : java.lang.IllegalStateException){
//                         notifyChildrenChanged(MEDIA_ROOT_ID)
//                        }
//                   } else {
//                       mediaSession.sendSessionEvent(NETWORK_ERROR, null)
//                       result.sendResult(null)
//                   }
//               }
//               if(!resultSent) {
//                   result.detach()
//               }
//           }
//       }


// REVERB EFFECT

//    private var wasReverbSet = false
//    private fun changeReverbMode() {
//
//        if(!wasReverbSet){
//            environmentalReverb = EnvironmentalReverb(0, 0)
//        }
//
//        setPreset(environmentalReverb, reverbMode)
//
//        if(reverbMode == 0){
//            environmentalReverb.enabled = false
//            environmentalReverb.release()
//            wasReverbSet = false
//            exoPlayer.clearAuxEffectInfo()
//        } else {
//            environmentalReverb.enabled = true
//            if(!wasReverbSet){
//                exoPlayer.setAuxEffectInfo(AuxEffectInfo(environmentalReverb.id, 1f))
//                wasReverbSet = true
//            }
//        }
//    }

//    private fun changeReverbMode() {
//
//        effectReverb.preset = reverbMode.toShort()
//
//        effectReverb.enabled = reverbMode != 0
//
//      if(!wasReverbSet){
//          exoPlayer.setAuxEffectInfo(AuxEffectInfo(effectReverb.id, 1f))
//          wasReverbSet = true
//      }
//
//    }


// FAV LIVE DATA

//    private val observerForDatabase by lazy {
//        Observer<List<RadioStation>>{
//
//            if(currentMediaItems == SEARCH_FROM_FAVOURITES){
//                if(isInStationDetails)
//                isFavPlaylistPendingUpdate = true
//            } else {
//                radioSource.createMediaItemsFromDB(it, exoPlayer, currentRadioStation)
//            }
//
//        }
//    }


// BANDWIDTH METER


//        bandwidthMeter = DefaultBandwidthMeter.Builder(this@RadioService).build()
//        bandwidthMeter.addEventListener(handler, listener)


//    lateinit var bandwidthMeter: DefaultBandwidthMeter

//    val handler = android.os.Handler(Looper.getMainLooper())
//    val listener = BandwidthMeter.EventListener { elapsedMs, bytesTransferred, bitrateEstimate ->
//
//        Log.d("CHECKTAGS", "elapse : $elapsedMs, bytes transfered : $bytesTransferred, bitrate : $bitrateEstimate")
//
//    }