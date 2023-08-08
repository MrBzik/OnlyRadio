package com.onlyradio.radioplayer.dagger

import android.app.Application
import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.audio.AudioCapabilities.getCapabilities
import com.google.android.exoplayer2.audio.AudioSink
import com.google.android.exoplayer2.audio.DefaultAudioSink
import com.google.android.exoplayer2.upstream.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import dev.brookmg.exorecord.lib.ExoRecord


@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {



    @Provides
    @ServiceScoped
    fun providesAudioAttributes() = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    @Provides
    @ServiceScoped
    fun providesExoPlayer (
        @ApplicationContext app : Context,
        audioAttributes: AudioAttributes,
        renderersFactory: DefaultRenderersFactory
    ) = ExoPlayer.Builder(app, renderersFactory)
        .setAudioAttributes(audioAttributes, true)
        .setHandleAudioBecomingNoisy(true)
        .setLoadControl(DefaultLoadControl.Builder()
            .setBufferDurationsMs(20000, 20000, 50, 50)
//            .setTargetBufferBytes(1024*500)
            .build())
        .build()




    @Provides
    @ServiceScoped
    fun providesDataSourceFactory (
        @ApplicationContext app : Context
    ) = DefaultDataSource.Factory(app)

//    @Provides
//    @ServiceScoped
//    fun providesHlsDataSourceFactory(
//        @ApplicationContext app : Context
//    ) = HlsDataSourceFactory()

    @Provides
    @ServiceScoped
    fun providesRendersFactory(
        @ApplicationContext app : Context,
        exoRecord: ExoRecord
    ) = object : DefaultRenderersFactory(app){

//        override fun setEnableAudioFloatOutput(enableFloatOutput: Boolean): DefaultRenderersFactory {
//            return super.setEnableAudioFloatOutput(true)
//        }

        override fun buildAudioSink(
            context: Context,
            enableFloatOutput: Boolean,
            enableAudioTrackPlaybackParams: Boolean,
            enableOffload: Boolean
        ): AudioSink {

            return DefaultAudioSink.Builder()
                .setAudioCapabilities(getCapabilities(app))
                .setAudioProcessorChain(DefaultAudioSink
                    .DefaultAudioProcessorChain(exoRecord.exoRecordProcessor))
                .setEnableFloatOutput(false)
                .setEnableAudioTrackPlaybackParams(true)
                .build()

        }
    }

    @Provides
    @ServiceScoped
    fun providesExoRecord(@ApplicationContext app : Context) =
        ExoRecord(app.applicationContext as Application)


//            .setAudioCapabilities(AudioCapabilities(intArrayOf(22), 8))

}
