package com.onlyradio.radioplayer.vorbis

data class Record(val filePath: String, val sampleBitRate: Int, val quality: Float,
                  val channelCount: Int, val oggFilePath: String? = null)