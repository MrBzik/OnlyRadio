package com.onlyradio.radioplayer.exoPlayer

import android.content.Context
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.drm.DrmSessionManagerProvider
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.LoadErrorHandlingPolicy

class MyMediaSourceFactory(context: Context, datasourceFactory: DataSource.Factory, checkIntervals: Int) : MediaSource.Factory {
    private val defaultMediaSourceFactory: DefaultMediaSourceFactory
    private val progressiveFactory : MediaSource.Factory

    init {
        defaultMediaSourceFactory = DefaultMediaSourceFactory(context)
        progressiveFactory = ProgressiveMediaSource.Factory(datasourceFactory)
            .setContinueLoadingCheckIntervalBytes(1024 * checkIntervals)
    }


    override fun setDrmSessionManagerProvider(drmSessionManagerProvider: DrmSessionManagerProvider): MediaSource.Factory {
        defaultMediaSourceFactory.setDrmSessionManagerProvider(drmSessionManagerProvider)
        progressiveFactory.setDrmSessionManagerProvider(drmSessionManagerProvider)
        return this;
    }

    override fun setLoadErrorHandlingPolicy(loadErrorHandlingPolicy: LoadErrorHandlingPolicy): MediaSource.Factory {
        defaultMediaSourceFactory.setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
        progressiveFactory.setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
        return this
    }

    override fun getSupportedTypes(): IntArray {
        return defaultMediaSourceFactory.supportedTypes
    }

    override fun createMediaSource(mediaItem: MediaItem): MediaSource {
        if (mediaItem.localConfiguration?.uri?.path?.contains("m3u8") == true) {
            return defaultMediaSourceFactory.createMediaSource(mediaItem);

        }
        return progressiveFactory.createMediaSource(mediaItem)
    }
}