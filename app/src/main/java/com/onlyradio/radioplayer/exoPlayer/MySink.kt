package com.onlyradio.radioplayer.exoPlayer

import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.audio.AudioSink
import com.google.android.exoplayer2.audio.ForwardingAudioSink

class MySink(sink : AudioSink) : ForwardingAudioSink(sink) {


    override fun configure(
        inputFormat: Format,
        specifiedBufferSize: Int,
        outputChannels: IntArray?
    ) {
        super.configure(inputFormat, specifiedBufferSize, outputChannels)
        
    }



}