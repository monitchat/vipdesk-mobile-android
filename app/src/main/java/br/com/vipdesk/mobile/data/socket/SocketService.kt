package br.com.vipdesk.mobile.data.socket

import android.util.Log
import br.com.vipdesk.mobile.data.local.TokenManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import com.pusher.client.util.HttpChannelAuthorizer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.runBlocking

sealed class SocketEvent {
    data class MessageReceived(val data: JsonObject) : SocketEvent()
    data class MessageAnswered(val data: JsonObject) : SocketEvent()
    data class MessageUpdated(val data: JsonObject) : SocketEvent()
    data class NewTicketCreated(val data: JsonObject) : SocketEvent()
    data class TicketChangedOwner(val data: JsonObject) : SocketEvent()
    data class TicketChangedStatus(val data: JsonObject) : SocketEvent()
    data class TicketDeleted(val data: JsonObject) : SocketEvent()
    data class ConversationStateChanged(val data: JsonObject) : SocketEvent()
    data object ConversationCountChanged : SocketEvent()
    data object UserLoggedOut : SocketEvent()
}

class SocketService(
    private val tokenManager: TokenManager,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "VipDeskSocket"
        private const val PUSHER_KEY = "83bfc2b3c9ff978c217c"
        private const val WS_HOST = "wss.monitchat.com"
        private const val AUTH_ENDPOINT = "https://alb.monitchat.com/broadcasting/auth"
    }

    private var pusher: Pusher? = null
    private var companyChannel: String? = null
    private var userChannel: String? = null

    private val _events = MutableSharedFlow<SocketEvent>(extraBufferCapacity = 50)
    val events: SharedFlow<SocketEvent> = _events.asSharedFlow()

    var isConnected: Boolean = false
        private set

    fun connect(companyId: Int, userId: Int) {
        if (pusher != null) disconnect()

        val token = runBlocking { tokenManager.getToken() } ?: return

        val authorizer = HttpChannelAuthorizer(AUTH_ENDPOINT).apply {
            setHeaders(mapOf("Authorization" to "Bearer $token"))
        }

        val options = PusherOptions().apply {
            setCluster("mt1")
            setChannelAuthorizer(authorizer)
            setHost(WS_HOST)
            isUseTLS = true
        }

        pusher = Pusher(PUSHER_KEY, options).apply {
            connect(object : ConnectionEventListener {
                override fun onConnectionStateChange(change: ConnectionStateChange) {
                    Log.d(TAG, "State: ${change.previousState} -> ${change.currentState}")
                    isConnected = change.currentState == ConnectionState.CONNECTED
                }

                override fun onError(message: String?, code: String?, e: Exception?) {
                    Log.e(TAG, "Error: $message (code: $code)", e)
                    isConnected = false
                }
            }, ConnectionState.ALL)
        }

        companyChannel = "private-channel-$companyId"
        subscribeToCompanyChannel(companyChannel!!)

        userChannel = "private-user-logged-out-$userId"
        subscribeToUserChannel(userChannel!!)

        Log.d(TAG, "Connecting: $companyChannel, $userChannel via $WS_HOST")
    }

    private fun subscribeToCompanyChannel(channelName: String) {
        val listener = object : PrivateChannelEventListener {
            override fun onEvent(event: com.pusher.client.channel.PusherEvent) {
                handleEvent(event.eventName, event.data)
            }

            override fun onSubscriptionSucceeded(channelName: String) {
                Log.d(TAG, "Subscribed: $channelName")
            }

            override fun onAuthenticationFailure(message: String, e: Exception?) {
                Log.e(TAG, "Auth failed $channelName: $message", e)
            }
        }

        pusher?.subscribePrivate(channelName, listener,
            "App\\Events\\MessageReceived",
            "App\\Events\\MessageAnswered",
            "App\\Events\\MessageUpdated",
            "App\\Events\\NewTicketCreated",
            "App\\Events\\TicketChangedOwner",
            "App\\Events\\TicketChangedStatus",
            "App\\Events\\TicketDeleted",
            "App\\Events\\ConversationCount",
            "App\\Events\\ConversationArchived",
            "App\\Events\\ConversationRead",
            "App\\Events\\NewSystemLog"
        )
    }

    private fun subscribeToUserChannel(channelName: String) {
        val listener = object : PrivateChannelEventListener {
            override fun onEvent(event: com.pusher.client.channel.PusherEvent) {
                if (event.eventName.contains("UserLoggedOut")) {
                    _events.tryEmit(SocketEvent.UserLoggedOut)
                }
            }

            override fun onSubscriptionSucceeded(channelName: String) {
                Log.d(TAG, "Subscribed: $channelName")
            }

            override fun onAuthenticationFailure(message: String, e: Exception?) {
                Log.e(TAG, "Auth failed $channelName: $message", e)
            }
        }

        pusher?.subscribePrivate(channelName, listener, "App\\Events\\UserLoggedOut")
    }

    private fun handleEvent(eventName: String, data: String) {
        Log.d(TAG, "Event: $eventName")
        try {
            val json = gson.fromJson(data, JsonObject::class.java)
            val event = when {
                eventName.contains("MessageReceived") -> SocketEvent.MessageReceived(json)
                eventName.contains("MessageAnswered") -> SocketEvent.MessageAnswered(json)
                eventName.contains("MessageUpdated") -> SocketEvent.MessageUpdated(json)
                eventName.contains("NewTicketCreated") -> SocketEvent.NewTicketCreated(json)
                eventName.contains("TicketChangedOwner") -> SocketEvent.TicketChangedOwner(json)
                eventName.contains("TicketChangedStatus") -> SocketEvent.TicketChangedStatus(json)
                eventName.contains("TicketDeleted") -> SocketEvent.TicketDeleted(json)
                eventName.contains("ConversationCount") -> SocketEvent.ConversationCountChanged
                eventName.contains("ConversationArchived") -> SocketEvent.ConversationCountChanged
                else -> null
            }
            event?.let { _events.tryEmit(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Parse error: $eventName", e)
        }
    }

    fun disconnect() {
        companyChannel?.let { pusher?.unsubscribe(it) }
        userChannel?.let { pusher?.unsubscribe(it) }
        pusher?.disconnect()
        pusher = null
        isConnected = false
        companyChannel = null
        userChannel = null
        Log.d(TAG, "Disconnected")
    }
}
