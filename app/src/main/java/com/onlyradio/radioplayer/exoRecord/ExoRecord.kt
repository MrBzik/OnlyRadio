package com.onlyradio.radioplayer.exoRecord

import android.app.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExoRecord(private val application: Application) : IExoRecord {

    val exoRecordProcessor: ExoRecordProcessor by lazy {
        ExoRecordProcessor(applicationContext = application)
    }
    private val _listeners: HashMap<String, ExoRecordListener> = hashMapOf()

    interface ExoRecordListener {
        fun onStartRecording(recordFileName: String)
        fun onStopRecording(record: IExoRecord.Record)
    }

    override suspend fun startRecording(quality : Float): String = withContext(Dispatchers.IO) {
        val fileName = exoRecordProcessor.startRecording(quality)
        withContext(Dispatchers.Main) {
            for (listener in _listeners.values)
                listener.onStartRecording(fileName)
        }
        return@withContext fileName
    }

    override suspend fun stopRecording(): IExoRecord.Record = withContext(Dispatchers.IO){
        val record = exoRecordProcessor.stopRecording()
        withContext(Dispatchers.Main) {
            for (listener in _listeners.values)
                listener.onStopRecording(record)
        }
        return@withContext record
    }

    fun addExoRecordListener(tag: String, listener: ExoRecordListener) : Boolean {
        if (_listeners.containsKey(tag)) return false
        _listeners[tag] = listener
        return true
    }

    fun clearListeners() = _listeners.clear()

    fun removeExoRecordListener(tag: String) : Boolean {
        return if (_listeners.containsKey(tag)) {
            _listeners.remove(tag)
            true
        } else false
    }

    fun removeExoRecordListener(listener: ExoRecordListener) : Boolean {
        return if (_listeners.containsValue(listener)) {
            val tags = _listeners.filterValues { it == listener }.keys
            for (tag in tags) _listeners.remove(tag)
            true
        } else false
    }
}