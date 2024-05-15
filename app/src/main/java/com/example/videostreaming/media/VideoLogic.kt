package com.example.videostreaming.media

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import com.example.videostreaming.mediaremote.RemoteMedia
import fm.liveswitch.AudioStream
import fm.liveswitch.Channel
import fm.liveswitch.ChannelClaim
import fm.liveswitch.Client
import fm.liveswitch.ClientState
import fm.liveswitch.ConnectionState
import fm.liveswitch.Future
import fm.liveswitch.IAction1
import fm.liveswitch.Layout
import fm.liveswitch.LayoutUtility
import fm.liveswitch.Log
import fm.liveswitch.ManagedConnection
import fm.liveswitch.ManagedThread
import fm.liveswitch.McuConnection
import fm.liveswitch.Promise
import fm.liveswitch.Token
import fm.liveswitch.VideoLayout
import fm.liveswitch.VideoStream
import fm.liveswitch.android.LayoutManager
import java.util.Locale
import kotlin.concurrent.Volatile

class VideoLogic private constructor(
    private var context: Context,
    private var handler: Handler,
    private var localMedia: LocalMedia<View>? = null,
    private var layoutManager: LayoutManager? = null,
) {
    private val aecContext = AecContext()
    private lateinit var channel: Channel
    private lateinit var videoLayout: VideoLayout
    private var mcuViewId: String? = null
    private var mcuConnection: McuConnection? = null
    private lateinit var client: Client
    private val applicationId = "d134d488-970a-42be-b873-ad38df7c2469"
    private val channelId = "199809194564565465465465"

    companion object {

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: VideoLogic? = null

        fun getInstance(appContext: Context) =
            instance ?: synchronized(this) { // synchronized to avoid concurrency problem
                instance ?: VideoLogic(appContext, Handler(appContext.mainLooper)).also {
                    instance = it
                }
            }
    }

    private var reRegisterBackoff = 200
    private var maxRegisterBackoff = 60000
    private var unregistering = false

    fun joinAsync(): Future<Array<Channel>> {
        client = Client("https://cloud.liveswitch.io/", applicationId)

        // Create a token
        val token = Token.generateClientRegisterToken(
            applicationId,
            client.userId,
            client.deviceId,
            client.id,
            null,
            arrayOf(ChannelClaim(channelId)),
            "3f2bb43b26a849dcb73edd6eee4924606fcb80e83b034b758fb3d6c0b3b61afa"
        )

        // Allow re-register.
        unregistering = false
        client.addOnStateChange { client: Client ->
            if (client.state == ClientState.Unregistered) {
                Log.debug("Client has been unregistered.")
                if (!unregistering) {
                    Log.debug(
                        java.lang.String.format(
                            Locale.US,
                            "Registering with backoff = %d.",
                            reRegisterBackoff
                        )
                    )

                    ManagedThread.sleep(reRegisterBackoff)
                    if (reRegisterBackoff < maxRegisterBackoff) {
                        reRegisterBackoff += reRegisterBackoff
                    }
                    client.register(token)
                        .then({ channels: Array<Channel> ->
                            reRegisterBackoff = 200
                            onClientRegistered(channels)
                        }
                        ) { ex: java.lang.Exception? ->
                            Log.error(
                                "ERROR: Client unable to register with the gateway.",
                                ex
                            )
                        }
                }
            }
        }

        // Register client with token.
        return client.register(token).then(::onClientRegistered) { ex ->
            Log.error(
                "ERROR: Client unable to register with the gateway.",
                ex
            )
        }
    }

    fun leaveAsync(): Future<Any>? {
        if (client != null) {
            // Disable re-register.
            unregistering = true
            return client.unregister().fail(IAction1 { ex: java.lang.Exception? ->
                Log.error(
                    "ERROR: Unable to unregister client.",
                    ex
                )
            })
        }
        return null
    }


    fun getChannel(): Channel {
        return channel
    }

    fun getClient(): Client {
        return client
    }

    private fun openMcuConnection(): McuConnection {
        // Create remote media.
        val remoteMedia = RemoteMedia(context,
            disableAudio = false,
            disableVideo = false,
            aecContext = aecContext
        )
        mcuViewId = remoteMedia.id
        handler.post {
            layoutManager?.addRemoteView(mcuViewId, remoteMedia.view)
        }

        val audioStream: AudioStream? =
            if (localMedia?.audioTrack != null) AudioStream(localMedia, remoteMedia) else null
        val videoStream =
            if (localMedia?.videoTrack != null) VideoStream(localMedia, remoteMedia) else null

        val connection = channel.createMcuConnection(audioStream, videoStream)
        connection.addOnStateChange { conn: ManagedConnection ->
            Log.info(
                String.format(
                    "MCU connection %s is currently in a %s state.",
                    conn.id,
                    conn.state.toString()
                )
            )
            if (conn.state == ConnectionState.Closing || conn.state == ConnectionState.Failing) {
                if (conn.remoteClosed) {
                    Log.info(
                        String.format(
                            "Media server has closed the MCU connection %s.",
                            conn.id
                        )
                    )
                }
                handler.post {

                    // Removing remote view from UI.
                    layoutManager?.removeRemoteView(remoteMedia.id)
                    remoteMedia.destroy()
                }
            } else if (conn.state == ConnectionState.Failed) {
                openMcuConnection()
            }
        }

        layoutManager?.addOnLayout { layout: Layout? ->
            if (mcuConnection != null) {
                LayoutUtility.floatLocalPreview(
                    layout,
                    videoLayout,
                    mcuConnection!!.id,
                    mcuViewId,
                    localMedia!!.viewSink
                )
            }
        }
        connection.open()
        return connection
    }

    private fun onClientRegistered(channels: Array<Channel>) {
        channel = channels[0]

        channel.addOnMcuVideoLayout { vidLayout ->
            videoLayout = vidLayout
            if (layoutManager != null) {
                handler.post { layoutManager!!.layout() }
            }
        }

        mcuConnection = openMcuConnection()
    }

    fun startLocalMedia(activity: Activity, container: ViewGroup?): Promise<Any?> {
        val promise = Promise<Any?>()
        activity.runOnUiThread {

            // Create a new local media with audio and video enabled.
            localMedia = CameraLocalMedia(context,
                disableAudio = false,
                disableVideo = false,
                aecContext = aecContext
            )

            // Set local media in the layout.
            layoutManager = LayoutManager(container)
            layoutManager?.setLocalView((localMedia as CameraLocalMedia).view)

            // Start capturing local media.
            (localMedia as CameraLocalMedia).start().then(
                {
                    promise.resolve(
                        null
                    )
                }
            ) { exception: Exception? ->
                promise.reject(
                    exception
                )
            }
        }
        return promise
    }

    fun stopLocalMedia(): Promise<Any?> {
        val promise = Promise<Any?>()
        if (localMedia == null) {
            promise.resolve(null)
        } else {
            localMedia?.stop()?.then({
                if (layoutManager != null) {
                    // Remove views from the layout.
                    layoutManager?.removeRemoteViews()
                    layoutManager?.unsetLocalView()
                    layoutManager = null
                }
                if (localMedia != null) {
                    localMedia?.destroy()
                    localMedia = null
                }
                promise.resolve(null)
            }) { exception: Exception? -> promise.reject(exception) }
        }
        return promise
    }
}