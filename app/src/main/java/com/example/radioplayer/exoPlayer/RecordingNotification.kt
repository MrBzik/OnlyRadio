package com.example.radioplayer.exoPlayer


//class RecordingNotification (
//    private val context : Context,
//    private val currentStation : () -> String
//        ) {
//
//    fun showNotification(){
//
//        val notificationManager =  NotificationManagerCompat.from(context)
//
//        val stopRecordingIntent = PendingIntent.getService(
//            context, 7, Intent(context, RadioService::class.java).also {
//                   it.action = COMMAND_STOP_RECORDING
//            },
//           PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val notification = NotificationCompat.Builder(context, RECORDING_CHANNEL_ID)
//            .setSmallIcon(R.drawable.ic_start_recording)
//            .setContentTitle("Recording")
//            .setContentText(currentStation())
//            .setUsesChronometer(true)
//            .setColor(ContextCompat.getColor(context, R.color.main_background))
//            .addAction(0, "Stop",
//                stopRecordingIntent
//            ).build()
//
//         notificationManager.notify(RECORDING_NOTIFICATION_ID, notification)
//
//
//    }
//
//
//}