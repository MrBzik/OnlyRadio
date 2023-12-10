package com.ogg.vorbis

import com.ogg.vorbis.models.VorbisInfo

class VorbisWrapper private constructor(
   filePath: String,
   vorbisInfo: VorbisInfo
) {

    private val vorbisFileOutputStream : VorbisFileOutputStream = VorbisFileOutputStream(filePath, vorbisInfo)

    fun close(){
        vorbisFileOutputStream.close()
    }

    fun write(shortArray: ShortArray, offset : Int = 0, size : Int = shortArray.size){
        vorbisFileOutputStream.write(shortArray, offset, size)
    }

    class Builder {

        private var path = ""
        private var channels = 1
        private var sampleRate = 44_100
        private var quality = 0.4f

        fun setPath(path: String) : Builder {
            this.path = path
            return this
        }

        fun setChannels(channelCount : Int) : Builder {
            channels = channelCount
            return this
        }

        fun setSampleRate(sampleRate : Int) : Builder {
            this.sampleRate = sampleRate
            return this
        }

        fun setQuality(quality: Float) : Builder {
            this.quality = quality
            return this
        }

        fun build() : VorbisWrapper {
            return VorbisWrapper(
                filePath = path,
                vorbisInfo = VorbisInfo(channels, sampleRate, quality)
            )
        }
    }


}