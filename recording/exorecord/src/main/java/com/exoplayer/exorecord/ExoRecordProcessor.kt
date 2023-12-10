package com.exoplayer.exorecord

import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.audio.AudioProcessor
import com.ogg.vorbis.VorbisWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Suppress("unused")
class ExoRecordProcessor constructor(
    private val applicationContext: Context
) : AudioProcessor,
    IExoRecord {

    private var sampleRateHz: Int = 0
    private var channelCount: Int = 0
    private var bytePerFrame: Int = 0

    private var encoding: Int = 0

    private var isActive = false

    private var processBuffer: ByteBuffer = AudioProcessor.EMPTY_BUFFER
    private var outputBuffer: ByteBuffer? = null

    private var inputEnded: Boolean = false
    private var fileName: String = ""

    private var vorbis : VorbisWrapper? = null

    init {
        outputBuffer = AudioProcessor.EMPTY_BUFFER
        channelCount = Format.NO_VALUE
        sampleRateHz = Format.NO_VALUE
    }


    override fun configure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
            throw AudioProcessor.UnhandledAudioFormatException(AudioProcessor.AudioFormat(inputAudioFormat.sampleRate, inputAudioFormat.channelCount, inputAudioFormat.encoding))
        }

        this.sampleRateHz = inputAudioFormat.sampleRate
        this.channelCount = inputAudioFormat.channelCount
        this.encoding = inputAudioFormat.encoding
        this.bytePerFrame = inputAudioFormat.bytesPerFrame

        isActive = true

        return inputAudioFormat
    }

    override fun isActive() = isActive

    private fun recordBuffer(inputBuffer: ByteBuffer) {

        if (isActive) {
            val buffer = ByteArray(1024)

            while (inputBuffer.hasRemaining()) {
                val bytesToRead = minOf(inputBuffer.remaining(), buffer.size)

                inputBuffer.get(buffer, 0, bytesToRead)

                val shortArray = ShortArray(bytesToRead / 2) { index ->
                    (buffer[index * 2].toUByte().toInt() +
                            (buffer[(index * 2) + 1].toInt() shl 8)).toShort()
                }

                vorbis?.write(shortArray, 0, shortArray.size)
            }
        }
    }


//    private fun recordBuffer(inputBuffer: ByteBuffer) {
//
//        val byteArray = ByteArray(inputBuffer.remaining())
//        inputBuffer.get(byteArray)
//        if (isActive) {
//
//            Logger.log("chunk: ${byteArray.size}")
//
//            val inputStream = ByteArrayInputStream(byteArray)
//
//            val buffer = ByteArray(1024)
//
//            var length: Int
//            var bytesRead : Int
//            while (inputStream.read(buffer).also { bytesRead = it } > 0) {
//
//
//                val shortArray = ShortArray(bytesRead / 2) {
//                    (buffer[it * 2].toUByte().toInt() +
//                            (buffer[(it * 2) + 1].toInt() shl 8)).toShort()
//                }
//                length = shortArray.size
//                vorbis?.write(shortArray, 0, length)
//            }
//
//            inputStream.close()
//
//        }
////            wavFile?.appendBytes(byteArray)
//    }

    override fun queueInput(inputBuffer: ByteBuffer) {

        var position = inputBuffer.position()
        val limit = inputBuffer.limit()
        val frameCount = (limit - position) / (2 * channelCount)
        val outputSize = frameCount * channelCount * 2

        recordBuffer(inputBuffer)

        if (processBuffer.capacity() < outputSize) {
            processBuffer = ByteBuffer.allocateDirect(outputSize).order(ByteOrder.nativeOrder())
        } else {
            processBuffer.clear()
        }

        while (position < limit) {
            for (channelIndex in 0 until channelCount) {
                processBuffer.putShort(
                    inputBuffer.getShort(position + 2 * channelIndex)
                )
            }
            position += channelCount * 2
        }

        inputBuffer.position(limit)

        processBuffer.flip()
        outputBuffer = this.processBuffer


    }

    override fun queueEndOfStream() {
        inputEnded = true
    }

    override fun getOutput(): ByteBuffer {
        val outputBuffer = this.outputBuffer
        this.outputBuffer = AudioProcessor.EMPTY_BUFFER
        return outputBuffer ?: ByteBuffer.allocate(0)
    }

    override fun isEnded(): Boolean = inputEnded && processBuffer === AudioProcessor.EMPTY_BUFFER

    override fun flush() {
        outputBuffer = AudioProcessor.EMPTY_BUFFER
        inputEnded = false
    }

    override fun reset() {
        CoroutineScope(Dispatchers.IO).launch {
            stopRecording()
            withContext(Dispatchers.Main) {
                flush()
                processBuffer = AudioProcessor.EMPTY_BUFFER
                sampleRateHz = Format.NO_VALUE
                channelCount = Format.NO_VALUE
                encoding = Format.NO_VALUE
            }
        }
    }

    override suspend fun startRecording(quality : Float) : String {
        stopRecording()

        fileName = "radio-${System.nanoTime()}.ogg"

        val filePath = applicationContext.filesDir.absolutePath.toString() + "/" + fileName

        vorbis = VorbisWrapper.Builder()
            .setPath(filePath)
            .setChannels(channelCount)
            .setSampleRate(sampleRateHz)
            .setQuality(quality)
            .build()

        isActive = true

        return fileName
    }

    override suspend fun stopRecording(): IExoRecord.Record {
        isActive = false
        vorbis?.close()
        vorbis = null
        return IExoRecord.Record(fileName, sampleRateHz, bytePerFrame, channelCount)
    }

}