package com.example.radioplayer.dagger

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.example.radioplayer.bluetooth.AndroidBluetoothController
import com.example.radioplayer.bluetooth.BluetoothController
import com.example.radioplayer.data.local.RadioDAO
import com.example.radioplayer.data.local.RadioDB
import com.example.radioplayer.data.remote.PixabayApi
import com.example.radioplayer.data.remote.RadioApi
import com.example.radioplayer.exoPlayer.RadioServiceConnection
import com.example.radioplayer.exoPlayer.RadioSource
import com.example.radioplayer.repositories.DatabaseRepository
import com.example.radioplayer.utils.Constants.BASE_RADIO_URL3
import com.example.radioplayer.utils.Constants.DATABASE_NAME
import com.example.radioplayer.utils.Constants.PIXABAY_BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providesBluetoothController(@ApplicationContext app : Context) : BluetoothController{
        return AndroidBluetoothController(app)

    }


    @Provides
    @Singleton
    fun providesRadioDB(
        @ApplicationContext app : Context
    ) : RadioDB = Room.databaseBuilder(
        app, RadioDB::class.java, DATABASE_NAME)
        .addMigrations(RadioDB.migration10To11, RadioDB.migration15To16, RadioDB.migration17To18,
        RadioDB.migration22To23, RadioDB.migration23To24)
        .build()

    @Provides
    @Singleton
    fun providesRadioDAO(
        radioDB: RadioDB
    ) : RadioDAO = radioDB.getRadioDAO()

    @Provides
    @Singleton
    fun providesDatabaseRepository(
        radioDAO: RadioDAO
    ) = DatabaseRepository(radioDAO)

    @Provides
    @Singleton
    fun providesGlide (
        @ApplicationContext app : Context
    ) : RequestManager {

        return Glide.with(app).setDefaultRequestOptions(
            RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.DATA)
        )
    }

    @Provides
    @Singleton
    fun providesDrawableCrossFadeFactory()
                = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()



    @Provides
    @Singleton
    fun providesRadioServiceConnection(
        @ApplicationContext app : Context
    ) = RadioServiceConnection(app)


    @Provides
    @Singleton
    fun providesValidUrlPrefs(
        @ApplicationContext app : Context
    ) : SharedPreferences {
      return app.getSharedPreferences("valid url preferences", Context.MODE_PRIVATE)
    }


    @Provides
    @Singleton
    fun providesRadioApi () : RadioApi {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_RADIO_URL3)
            .build()
            .create(RadioApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRadioSource (
        radioApi: RadioApi,
        radioDAO: RadioDAO,
        sharedPref : SharedPreferences
    ) : RadioSource
            = RadioSource(radioApi, radioDAO, sharedPref)



    @Provides
    @Singleton
    fun providesPixabayApi() : PixabayApi {

        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(PIXABAY_BASE_URL)
            .build()
            .create(PixabayApi::class.java)
    }
}