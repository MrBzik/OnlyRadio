package com.example.radioplayer.dagger

import android.app.Application
import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.radioplayer.R
import com.example.radioplayer.data.remote.RadioApi
import com.example.radioplayer.exoPlayer.RadioServiceConnection
import com.example.radioplayer.exoPlayer.RadioSource
import com.example.radioplayer.utils.Constants
import com.example.radioplayer.utils.Constants.BASE_RADIO_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun providesGlide (
        @ApplicationContext app : Context
    ) : RequestManager {

        return Glide.with(app).setDefaultRequestOptions(
            RequestOptions()
                .placeholder(R.drawable.ic_radio_default)
                .error(R.drawable.ic_radio_default)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
        )
    }

    @Provides
    @Singleton
    fun providesRadioServiceConnection(
        @ApplicationContext app : Context
    ) = RadioServiceConnection(app)

    @Provides
    @Singleton
    fun providesRadioApi () : RadioApi {

        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(Constants.BASE_RADIO_URL)
            .build()
            .create(RadioApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRadioSource (radioApi: RadioApi) : RadioSource = RadioSource(radioApi)
}