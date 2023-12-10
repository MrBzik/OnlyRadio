package com.ogg.vorbis.models

internal data class VorbisInfo(
    /**
     * The number of channels to be encoded. For your sake, here are the official channel positions for the first five according to Xiph.org.
     * one channel - the stream is monophonic
     * two channels - the stream is stereo. channel order: left, right
     * three channels - the stream is a 1d-surround encoding. channel order: left, center, right
     * four channels - the stream is quadraphonic surround. channel order: front left, front right, rear left, rear right
     * five channels - the stream is five-channel surround. channel order: front left, center, front right, rear left, rear right
     */
    val channels: Int = 1,

    /**
     * The number of samples per second of pcm data.
     */
    val sampleRate: Int = 44100,

    /** The recording quality of the encoding. The range goes from -.1 (worst) to 1 (best)  */
    val quality: Float = 0.4f,

    /**
     * the total number of samples from the recording. This field means nothing to the encoder.
     */
    val length: Long = 0
)
