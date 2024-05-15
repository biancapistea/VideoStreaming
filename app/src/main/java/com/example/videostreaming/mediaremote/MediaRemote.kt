package com.example.videostreaming.mediaremote

import android.content.Context
import android.widget.FrameLayout
import com.example.videostreaming.media.AecContext
import fm.liveswitch.AudioConfig
import fm.liveswitch.AudioDecoder
import fm.liveswitch.AudioFormat
import fm.liveswitch.RtcRemoteMedia
import fm.liveswitch.VideoDecoder
import fm.liveswitch.VideoFormat
import fm.liveswitch.VideoPipe
import fm.liveswitch.ViewSink
import fm.liveswitch.android.AudioTrackSink
import fm.liveswitch.android.OpenGLSink
import fm.liveswitch.AudioSink
import fm.liveswitch.matroska.VideoSink
import fm.liveswitch.opus.Decoder
import fm.liveswitch.yuv.ImageConverter
import java.util.Locale


class RemoteMedia(
    context: Context,
    disableAudio: Boolean,
    disableVideo: Boolean,
    aecContext: AecContext?
) :
    RtcRemoteMedia<FrameLayout>(disableAudio, disableVideo, aecContext) {
    private val context: Context

    // Enable AEC
    init {
        this.context = context
        super.initialize()
    }

    // Remote Audio
    override fun createAudioRecorder(audioFormat: AudioFormat): AudioSink {
        return fm.liveswitch.matroska.AudioSink(
            (id + "-remote-audio-" + audioFormat.name.lowercase(Locale.ROOT)) + ".mkv"
        )
    }

    override fun createAudioSink(audioConfig: AudioConfig): AudioSink {
        return AudioTrackSink(audioConfig)
    }

    override fun createOpusDecoder(audioConfig: AudioConfig): AudioDecoder {
        return Decoder()
    }

    // Remote Video
    override fun createVideoRecorder(videoFormat: VideoFormat): VideoSink {
        return VideoSink(id + "-remote-video-" + videoFormat.name.lowercase(Locale.getDefault()) + ".mkv")
    }

    override fun createViewSink(): ViewSink<FrameLayout> {
        return OpenGLSink(context)
    }

    override fun createVp8Decoder(): VideoDecoder {
        return fm.liveswitch.vp8.Decoder()
    }

    override fun createVp9Decoder(): VideoDecoder {
        return fm.liveswitch.vp9.Decoder()
    }

    override fun createH264Decoder(): VideoDecoder? {
        return null
    }

    override fun createImageConverter(videoFormat: VideoFormat): VideoPipe {
        return ImageConverter(videoFormat)
    }
}