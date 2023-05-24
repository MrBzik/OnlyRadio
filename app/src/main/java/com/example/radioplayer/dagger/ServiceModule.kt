package com.example.radioplayer.dagger

import android.app.Application
import android.content.Context
import android.media.AudioFormat
import android.os.Handler
import com.example.radioplayer.data.remote.RadioApi
import com.example.radioplayer.utils.Constants
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.LoadControl
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.audio.AudioCapabilities
import com.google.android.exoplayer2.audio.AudioCapabilities.DEFAULT_AUDIO_CAPABILITIES
import com.google.android.exoplayer2.audio.AudioCapabilities.getCapabilities
import com.google.android.exoplayer2.audio.AudioSink
import com.google.android.exoplayer2.audio.DefaultAudioSink
import com.google.android.exoplayer2.source.hls.HlsDataSourceFactory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import dev.brookmg.exorecord.lib.ExoRecord
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


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
                .setEnableFloatOutput(true)
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
