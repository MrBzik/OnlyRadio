package com.onlyradio.radioplayer.vorbis

import android.util.Log
import com.onlyradio.radioplayer.vorbis.models.VorbisInfo
import java.io.IOException

internal class VorbisFileOutputStream : AudioOutputStream {
    // The index into native memory where the ogg stream info is stored.
    private val oggStreamIdx: Int
    private lateinit var info: VorbisInfo

    companion object {
        const val VORBIS_BLOCK_SIZE = 1024

        init {
            System.loadLibrary("ogg")
            System.loadLibrary("vorbis")
            System.loadLibrary("vorbis-stream")
        }
    }

    constructor(fileName: String, s: VorbisInfo) {
        info = s
        oggStreamIdx = create(fileName, s)
        Log.e("OGG", "OggStreamIDx = $oggStreamIdx")
    }

    constructor(fileName: String) {
        oggStreamIdx = create(fileName, VorbisInfo())
    }

    @Throws(IOException::class)
    override fun close() {
        closeStreamIdx(oggStreamIdx)
        Log.e("OGG", "Close = $oggStreamIdx")
    }

    /**
     * Write PCM data to ogg. This assumes that you pass your streams in interleaved.
     * @param buffer the pcm buffer short array
     * @param offset the start offset in the data.
     * @param length the number of bytes to write.
     * @throws IOException if an I/O error occurs. In particular,
     * an `IOException` is thrown if the output
     * stream is closed.
     */

    override fun write(b: ByteArray?, off: Int, len: Int) {
        writeStreamIdx(oggStreamIdx, b, off, len)
    }

    @Throws(IOException::class)
    override fun write(buffer: ShortArray, offset: Int, length: Int) {
        writeStreamIdx(oggStreamIdx, buffer, offset, length)
        Log.e(
            "OGG",
            "Writing = { " + oggStreamIdx + ", " + buffer.size + ", " + offset + ", " + length + " }"
        )
    }


    @Throws(IOException::class)
    private external fun writeStreamIdx(idx: Int, pcmdata: ShortArray, offset: Int, size: Int): Int

    private external fun writeStreamIdx(idx: Int, pcmdata: ByteArray?, offset: Int, size: Int) : Int

    @Throws(IOException::class)
    private external fun closeStreamIdx(idx: Int)

    @Throws(IOException::class)
    private external fun create(path: String, s: VorbisInfo?): Int

    override fun getSampleRate(): Int = info.sampleRate
}