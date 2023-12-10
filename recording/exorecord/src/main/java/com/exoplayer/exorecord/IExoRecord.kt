package com.exoplayer.exorecord

interface IExoRecord {

    data class Record(val filePath: String, val sampleBitRate: Int, val bitRate: Int,
                      val channelCount: Int)

    suspend fun startRecording(quality : Float) : String
    suspend fun stopRecording() : Record

}