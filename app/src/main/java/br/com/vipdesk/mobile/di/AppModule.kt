package br.com.vipdesk.mobile.di

import android.content.Context
import br.com.vipdesk.mobile.BuildConfig
import br.com.vipdesk.mobile.data.api.ApiService
import br.com.vipdesk.mobile.data.api.AuthInterceptor
import br.com.vipdesk.mobile.data.api.FlexibleClientDeserializer
import br.com.vipdesk.mobile.data.api.FlexibleUserDeserializer
import br.com.vipdesk.mobile.data.api.SafeIntAdapter
import br.com.vipdesk.mobile.data.api.SafeLongAdapter
import br.com.vipdesk.mobile.data.local.TokenManager
import br.com.vipdesk.mobile.data.model.Client
import br.com.vipdesk.mobile.data.model.User
import br.com.vipdesk.mobile.data.repository.AuthRepository
import br.com.vipdesk.mobile.data.repository.ConversationRepository
import br.com.vipdesk.mobile.data.repository.MobileRepository
import br.com.vipdesk.mobile.data.socket.SocketService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object AppContainer {

    lateinit var appContext: Context
        private set

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    val tokenManager: TokenManager by lazy {
        TokenManager(appContext)
    }

    private val authInterceptor: AuthInterceptor by lazy {
        AuthInterceptor(tokenManager)
    }

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(User::class.java, FlexibleUserDeserializer())
            .registerTypeAdapter(Client::class.java, FlexibleClientDeserializer())
            .registerTypeAdapter(Int::class.java, SafeIntAdapter())
            .registerTypeAdapter(Int::class.javaObjectType, SafeIntAdapter())
            .registerTypeAdapter(Long::class.java, SafeLongAdapter())
            .registerTypeAdapter(Long::class.javaObjectType, SafeLongAdapter())
            .setLenient()
            .create()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(apiService, tokenManager)
    }

    val conversationRepository: ConversationRepository by lazy {
        ConversationRepository(apiService)
    }

    val mobileRepository: MobileRepository by lazy {
        MobileRepository(apiService)
    }

    val socketService: SocketService by lazy {
        SocketService(tokenManager, gson)
    }
}
